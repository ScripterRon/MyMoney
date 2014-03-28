/**
 * Copyright 2005-2014 Ronald W Hoffman
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
import org.ScripterRon.Chart.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.SortedSet;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

/**
 * Display the net worth graph
 */
public final class NetWorthGraphDialog extends JDialog implements ActionListener {

    /** Start date field */
    private JFormattedTextField startField;

    /** End date field */
    private JFormattedTextField endField;

    /**
     * Construct the dialog
     *
     * @param       parent          Parent frame
     */
    public NetWorthGraphDialog(JFrame parent) {
        super(parent, "Net Worth", true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        //
        // Set the start date to the beginning of the year
        //
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(Main.getCurrentDate());
        cal.set(Calendar.DAY_OF_YEAR, 1);
        startField = new JFormattedTextField(new EditDate());
        startField.setColumns(8);
        startField.setInputVerifier(new EditInputVerifier(false));
        startField.addActionListener(new FormattedTextFieldListener(this));
        startField.setValue(cal.getTime());

        //
        // Set the end date to the current date
        //
        endField = new JFormattedTextField(new EditDate());
        endField.setColumns(8);
        endField.setInputVerifier(new EditInputVerifier(false));
        endField.addActionListener(new FormattedTextFieldListener(this));
        endField.setValue(Main.getCurrentDate());

        //
        // Create the edit pane
        //
        //    Start Date:        <text-field>
        //    End Date:          <text-field>
        //
        JPanel editPane = new JPanel(new GridLayout(0, 2, 5, 5));

        editPane.add(new JLabel("Start Date:", JLabel.RIGHT));
        editPane.add(startField);

        editPane.add(new JLabel("End Date:", JLabel.RIGHT));
        editPane.add(endField);

        //
        // Create the buttons (Create Graph, Done)
        //
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));

        JButton button = new JButton("Create Graph");
        button.setActionCommand("create graph");
        button.addActionListener(this);
        buttonPane.add(button);
        getRootPane().setDefaultButton(button);
        
        buttonPane.add(Box.createHorizontalStrut(10));

        button = new JButton("Done");
        button.setActionCommand("done");
        button.addActionListener(this);
        buttonPane.add(button);

        //
        // Set up the content pane
        //
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.setOpaque(true);
        contentPane.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        contentPane.add(editPane);
        contentPane.add(Box.createVerticalStrut(15));
        contentPane.add(buttonPane);
        setContentPane(contentPane);
    }

    /**
     * Action performed (ActionListener interface)
     *
     * @param       ae              Action event
     */
    public void actionPerformed(ActionEvent ae) {

        //
        // Process the action command
        //
        // "create graph" - Create the graph
        // "done" - All done
        //
        try {
            switch (ae.getActionCommand()) {
                case "create graph":
                    Date startDate, endDate;
                    if (!startField.isEditValid() || !endField.isEditValid()) {
                        JOptionPane.showMessageDialog(this, "You must specify start and end dates",
                                                      "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        startDate = (Date)startField.getValue();
                        endDate = (Date)endField.getValue();
                        if (endDate.compareTo(startDate) < 0) {
                            JOptionPane.showMessageDialog(this, "The end date is before the start date",
                                                          "Error", JOptionPane.ERROR_MESSAGE);
                        } else {
                            createGraph(startDate, endDate);
                        }
                    }
                    break;
                    
                case "done":
                    setVisible(false);
                    dispose();
                    break;
            }
        } catch (Exception exc) {
            Main.logException("Exception while processing action event", exc);
        }
    }

    /**
     * Show the dialog
     *
     * @param       parent          Parent window for the dialog
     */
    public static void showDialog(JFrame parent) {
        try {
            JDialog dialog = new NetWorthGraphDialog(parent);
            dialog.pack();
            dialog.setLocationRelativeTo(parent);
            dialog.setVisible(true);
        } catch (Exception exc) {
            Main.logException("Exception while displaying dialog", exc);
        }
    }

    /**
     * Create the graph
     *
     * @param       startDate       The start date
     * @param       endDate         The end date
     */
    private void createGraph(Date startDate, Date endDate) {

        //
        // We will have one data point for each month in the range.  As a rough
        // estimate on the list size, use 2592000 seconds/month.
        //
        int listSize = (int)((endDate.getTime()-startDate.getTime())/2592000L);
        List<TimeChartElement> dataPoints = new ArrayList<>(listSize+1);
        
        //
        // Maintain a list of securities so we can price them using the history price
        // for each month
        //
        List<SecurityHolding> holdings = new ArrayList<>(SecurityRecord.securities.size());

        //
        // Compute the net worth for each month between the start and end dates
        //
        GregorianCalendar cal = new GregorianCalendar();
        Date currentDate = (Date)startDate.clone();
        double accountBalance = 0.0;

        for (TransactionRecord t : TransactionRecord.transactions) {

            //
            // Get transaction date
            //
            Date date = t.getDate();

            //
            // Add a new data point to the list if we have passed the current date
            // and then advance to the next month
            //
            if (date.compareTo(currentDate) > 0) {
                double netWorth = accountBalance + getPortfolioValue(currentDate, holdings);
                dataPoints.add(new TimeChartElement(currentDate, netWorth/1000.0));
                cal.setTime(currentDate);
                cal.add(Calendar.MONTH, 1);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                currentDate = cal.getTime();
            }

            //
            // Stop if we have passed the end date
            //
            if (date.compareTo(endDate) > 0)
                break;

            //
            // Update the net worth
            //
            double amount = t.getAmount();
            AccountRecord a = t.getAccount();
            if (a.getType() == AccountRecord.INVESTMENT) {
                SecurityHolding.updateSecurityHolding(holdings, t);
                if (t.getTransferAccount() != null)
                    accountBalance -= amount;
            } else if (t.getTransferAccount() == null) {
                accountBalance += amount;
            }

            List<TransactionSplit> splits = t.getSplits();
            if (splits != null) {
                for (TransactionSplit split : splits) {
                    if (split.getAccount() != null)
                        accountBalance -= split.getAmount();
                }
            }
        }

        //
        // Add the final data point to the list
        //
        listSize = dataPoints.size();
        if (listSize == 0 || !dataPoints.get(listSize-1).getDate().equals(endDate)) {
            double netWorth = accountBalance + getPortfolioValue(endDate, holdings);
            dataPoints.add(new TimeChartElement(endDate, netWorth/1000.0));
        }

        //
        // Display the net worth graph
        //
        TimeChart chart = new TimeChart(dataPoints, "Date", "Net Worth ($K)");
        chart.setOpaque(true);
        chart.setBackground(Color.CYAN);
        chart.setPreferredSize(new Dimension(640, 640));
        chart.setMinimumGridIncrement(10.0);

        String dialogTitle = String.format("Net Worth for %s to %s",
                                           Main.getDateString(startDate),
                                           Main.getDateString(endDate));
        JDialog dialog = new JDialog(this, dialogTitle, true);
        dialog.setContentPane(chart);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    /**
     * Compute the current value of the security portfolio
     *
     * @param       date            The current date
     * @param       holdings        The current security holdings
     * @return                      The portfolio value
     */
    private double getPortfolioValue(Date date, List<SecurityHolding> holdings) {
        double portfolioValue = 0.0;
        
        for (SecurityHolding h : holdings) {
            SecurityRecord s = h.getSecurity();
            SortedSet<PriceHistory> prices = s.getPriceHistory();
            double price = 0.0;
            for (PriceHistory ph : prices) {
                if (ph.getDate().compareTo(date) > 0)
                    break;
                
                price = ph.getPrice();
            }

            if (price == 0.0)
                price = h.getTotalCost()/h.getTotalShares();
            
            portfolioValue += (double)Math.round(h.getTotalShares()*price*100.0)/100.0;
        }
        
        return portfolioValue;
    }
}
