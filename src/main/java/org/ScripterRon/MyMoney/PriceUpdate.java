/**
 * Copyright 2005-2017 Ronald W Hoffman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ScripterRon.MyMoney;

import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.*;
import java.util.Scanner;
import java.util.SortedSet;

import java.io.*;
import java.net.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Update the current price for all securities that are not hidden.  The
 * current stock prices will be obtained from Yahoo! using an existing
 * internet connection.
 *
 * The current price history will be cleaned by keeping just one price per quarter
 * except for the current month.
 */
public final class PriceUpdate extends JDialog implements ActionListener {

    /** Parent frame */
    private final JFrame parent;

    /** Worker thread */
    private final Thread worker;

    /**
     * Create a price update instance
     *
     * @param   parent              Parent frame
     */
    public PriceUpdate(JFrame parent) {
        super(parent, "Update Prices", true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.parent = parent;
        //
        // Create the Cancel button
        //
        JPanel buttonPane = new JPanel();
        buttonPane.setOpaque(false);

        JButton button = new JButton("Cancel");
        button.setActionCommand("cancel");
        button.addActionListener(this);
        buttonPane.add(button);
        //
        // Set up the content pane
        //
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.setOpaque(true);
        contentPane.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        contentPane.add(new JLabel("Getting security prices from http://download.finance.yahoo.com"));
        contentPane.add(Box.createHorizontalStrut(45));
        contentPane.add(buttonPane);
        setContentPane(contentPane);
        //
        // Create a worker thread to do the actual work (we don't want to tie up
        // the AWT event dispatching thread)
        //
        worker = new WorkerThread(this);
    }

    /**
     * Update prices using an existing internet connection
     *
     * @param   parent              Parent frame
     */
    public static void onlineUpdate(JFrame parent) {
        //
        // Create the price update dialog
        //
        PriceUpdate update = new PriceUpdate(parent);
        //
        // Start the worker thread.  The worker thread will dispatch our
        // ActionListener method when it is done.
        //
        update.worker.start();
        //
        // Display the status dialog
        //
        update.pack();
        update.setLocationRelativeTo(parent);
        update.setVisible(true);
    }

    /**
     * Action performed (ActionListener interface)
     *
     * @param       ae              Action event
     */
    @Override
    public void actionPerformed(ActionEvent ae) {
        //
        // Process the action command
        //
        // "update complete" - Stock price update completed
        // "update failed" - Stock price update failed (the event source is the
        //                   Exception describing the failure)
        // "cancel" - Update canceled by the user
        //
        try {
            switch (ae.getActionCommand()) {
                case "update complete":
                    setVisible(false);
                    dispose();
                    JOptionPane.showMessageDialog(parent, "Price update complete",
                                                  "Update Prices", JOptionPane.INFORMATION_MESSAGE);
                    break;

                case "update failed":
                    setVisible(false);
                    dispose();
                    Main.logException("Price update failed", (Exception)ae.getSource());
                    break;

                case "cancel":
                    worker.interrupt();
                    break;
            }
        } catch (Exception exc) {
            Main.logException("Exception while processing action event", exc);
        }
    }

    /**
     * Worker thread to update the security prices
     */
    private final class WorkerThread extends Thread {

        /** Action listener */
        private final ActionListener listener;

        /** Current exception */
        private Exception exception;

        /** Map ticker symbol to security record */
        private final Map<String,SecurityRecord> symbols;

        /**
         * Create a new worker thread
         *
         * @param       listener    ActionListener to be notified when done
         */
        public WorkerThread(ActionListener listener) {
            super();
            this.listener = listener;
            symbols = new HashMap<>(SecurityRecord.securities.size()*2);
        }

        /**
         * Run the executable code for the thread
         */
        @Override
        public void run() {
            //
            // Get the current year, month and quarter
            //
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(Main.getCurrentDate());
            int month = cal.get(Calendar.MONTH);
            int year = cal.get(Calendar.YEAR);
            //
            // Get the ticker symbols for all securities that are not hidden
            // and clean up the price history elements
            //
            StringBuilder urlString = new StringBuilder(256);
            urlString.append("http://download.finance.yahoo.com/d/quotes.csv?s=");
            boolean addPlus = false;
            for (SecurityRecord s : SecurityRecord.securities) {
                //
                // Skip securities without a ticker symbol
                //
                String symbol = s.getSymbol();
                if (symbol.length() == 0)
                    continue;
                //
                // Clean up the price history entries keeping one entry for
                // each quarter.  However, do not delete stock split entries.
                //
                SortedSet<PriceHistory> phs = s.getPriceHistory();
                Iterator<PriceHistory> it = phs.iterator();
                int lastYear = 0;
                int lastQuarter = 0;
                while (it.hasNext()) {
                    PriceHistory ph = it.next();
                    cal.setTime(ph.getDate());
                    int currentYear = cal.get(Calendar.YEAR);
                    int currentMonth = cal.get(Calendar.MONTH);
                    int currentQuarter = (currentMonth/3)+1;
                    if (currentYear == year && currentMonth == month)
                        break;

                    if (currentYear == lastYear && currentQuarter == lastQuarter) {
                        if (ph.getSplitRatio() == 0.0) {
                            it.remove();
                            Main.dataModified = true;
                        }
                    } else {
                        lastQuarter = currentQuarter;
                        lastYear = currentYear;
                    }
                }
                //
                // Don't update the price for a hidden security
                //
                if (s.isHidden())
                    continue;
                //
                // Add the security ticker symbol to the URL
                //
                if (addPlus)
                    urlString.append("+");
                urlString.append(s.getSymbol());
                symbols.put(symbol, s);
                addPlus = true;
            }
            urlString.append("&f=sod1");
            //
            // Get the price quotes from Yahoo!
            //
            InputStream in = null;
            try {
                URL url = new URL(urlString.toString());
                in = url.openStream();
                fileUpdate(in);
            } catch (Exception exc) {
                exception = exc;
            } finally {
                try {
                    if (in != null)
                        in.close();
                } catch (Exception rexc) {
                    Main.logException("Unable to close URL stream", rexc);
                }
            }
            //
            // Notify the price update dialog that we are done.  This must be done
            // on the event dispatch thread.
            //
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ActionEvent ae;
                    if (exception == null)
                        ae = new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
                                             "update complete");
                    else
                        ae = new ActionEvent(exception, ActionEvent.ACTION_PERFORMED,
                                             "update failed");

                    listener.actionPerformed(ae);
                }
            });
        }

        /**
         * Update the security prices using a CSV file
         *
         * The first field is the ticker symbol enclosed in quotes, the second
         * field is the current stock price and the third field is the date enclosed
         * in quotes.
         */
        private void fileUpdate(InputStream in) throws IOException, ParseException {
            Scanner scanner = new Scanner(in);
            Pattern pattern = Pattern.compile("\"([^\"]*)\",(\\d+\\.?\\d*)");
            while (scanner.hasNextLine()) {
                if (scanner.findInLine(pattern) != null) {
                    MatchResult result = scanner.match();
                    String symbol = result.group(1);
                    double price = Double.valueOf(result.group(2));
                    SecurityRecord s = symbols.get(symbol);
                    if (s != null) {
                        PriceHistory ph = new PriceHistory(price);
                        SortedSet<PriceHistory> priceHistory = s.getPriceHistory();
                        priceHistory.remove(ph);
                        priceHistory.add(ph);
                        Main.dataModified = true;
                    }
                }
                scanner.nextLine();
            }
        }
    }
}

