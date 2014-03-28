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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.TreeSet;

import java.io.*;

import javax.swing.*;

/**
 * This is the MyMoney application class.  The main() method is invoked by
 * the Java VM to start the application.
 */
public final class Main {

    /** Application data directory */
    public static String dataPath;
    
    /** Application identifier */
    public static String applicationID;
    
    /** Application name */
    public static String applicationName;
    
    /** Application version */
    public static String applicationVersion;

    /** Main application window */
    public static MainWindow mainWindow;
    
    /** Help window */
    public static HelpWindow helpWindow;

    /** Application properties filename */
    public static File propFile;

    /** Application properties */
    public static Properties properties;

    /** Database modified */
    public static boolean dataModified=false;
    
    /** Transaction database */
    public static Database database;

    /** Deferred exception text */
    private static String deferredText;

    /** Deferred exception */
    private static Throwable deferredException;

    /**
     * Main program for the MyMoney application
     *
     * @param       args        Command line arguments
     */
    public static void main(String[] args) {

        try {
            //
            // Initialize the application variables
            //
            dataPath = System.getProperty("user.home")+"\\My Documents\\My Money";
            propFile = new File(dataPath+"\\MyMoney.properties");
            properties = new Properties();
            AccountRecord.accounts = new TreeSet<>();
            CategoryRecord.categories = new TreeSet<>();
            SecurityRecord.securities = new TreeSet<>();
            TransactionRecord.transactions = new LinkedList<>();
            ScheduleRecord.transactions = new LinkedList<>();
            //
            // Get the application build properties
            //
            Class<?> mainClass = Class.forName("org.ScripterRon.MyMoney.Main");
            try (InputStream classStream = mainClass.getClassLoader().getResourceAsStream("META-INF/application.properties")) {
                if (classStream == null)
                    throw new IOException("Application build properties not found");
                Properties applicationProperties = new Properties();
                applicationProperties.load(classStream);
                applicationID = applicationProperties.getProperty("application.id");
                applicationName = applicationProperties.getProperty("application.name");
                applicationVersion = applicationProperties.getProperty("application.version");
            }
            //
            // Create the data directory if it doesn't exist
            //
            File dirFile = new File(Main.dataPath);
            if (!dirFile.exists())
                dirFile.mkdir();
            //
            // Load the saved application properties
            //
            if (Main.propFile.exists()) {
                try (FileInputStream in = new FileInputStream(Main.propFile)) {
                    Main.properties.load(in);
                }
            }
            //
            // Save the system properties for debug purposes
            //
            Main.properties.setProperty("java.version", System.getProperty("java.version"));
            Main.properties.setProperty("java.home", System.getProperty("java.home"));
            Main.properties.setProperty("os.name", System.getProperty("os.name"));
            Main.properties.setProperty("sun.os.patch.level", System.getProperty("sun.os.patch.level"));
            Main.properties.setProperty("user.name", System.getProperty("user.name"));
            Main.properties.setProperty("user.home", System.getProperty("user.home"));
            //
            // Load the current transaction database
            //
            database = new Database(dataPath+"\\MyMoney.database");
            database.load();
            //
            // Process scheduled transactions
            //
            Main.processScheduledTransactions();
            //
            // Start the Swing GUI using the system look and feel
            //
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    createAndShowGUI();
                }
            });
        } catch (Exception exc) {
            Main.logException("Exception during program initialization", exc);
        }
    }

    /**
     * Create and show our application GUI
     *
     * This method is invoked on the AWT event thread to avoid timing
     * problems with other window events
     */
    private static void createAndShowGUI() {

        try {

            //
            // Use the normal window decorations as defined by the look-and-feel
            // schema
            //
            JFrame.setDefaultLookAndFeelDecorated(true);

            //
            // Create the main application window
            //
            Main.mainWindow = new MainWindow();

            //
            // Show the application window
            //
            Main.mainWindow.pack();
            Main.mainWindow.setVisible(true);
        } catch (Exception exc) {
            Main.logException("Exception while initializing application window", exc);
        }
    }

    /**
     * Get the current date (the time is always set to 12:00:00)
     *
     * @return                      Date object
     */
    public static Date getCurrentDate() {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * Convert a date to a string in the format "mm/dd/yyyy"
     *
     * @param       date            Date to be converted
     * @return                      Date string
     */
    public static String getDateString(Date date) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        return String.format("%02d/%02d/%04d", cal.get(Calendar.MONTH)+1,
                                               cal.get(Calendar.DAY_OF_MONTH),
                                               cal.get(Calendar.YEAR));
    }

    /**
     * Process scheduled transactions
     *
     * @return                      TRUE if any transactions were processed
     */
    public static boolean processScheduledTransactions() {
        if (ScheduleRecord.transactions.isEmpty())
            return false;

        boolean transactionsProcessed = false;
        GregorianCalendar cal = new GregorianCalendar();
        Date currentDate = Main.getCurrentDate();
        ListIterator<ScheduleRecord> li = ScheduleRecord.transactions.listIterator();

        //
        // Process scheduled transactions until we reach the current date
        //
        while (li.hasNext()) {
            boolean rescheduleTransaction = true;
            ScheduleRecord r = li.next();
            Date scheduledDate = r.getDate();
            if (scheduledDate.compareTo(currentDate) > 0)
                break;

            //
            // Remove the scheduled transaction from the list
            //
            li.remove();

            //
            // Build the new transaction
            //
            TransactionRecord t = new TransactionRecord(scheduledDate, r.getAccount());
            t.setName(r.getDescription());
            t.setAmount(r.getAmount());
            t.setCategory(r.getCategory());
            t.setTransferAccount(r.getTransferAccount());
            t.setMemo("Scheduled transaction");

            //
            // Copy any splits and update a loan payment based on the current
            // loan balance
            //
            List<TransactionSplit> splits = r.getSplits();
            if (splits != null) {
                splits = TransactionSplit.copySplits(splits);
                t.setSplits(splits);

                boolean loanPayment = false;
                AccountRecord loanAccount = null;
                double amount = r.getAmount();

                //
                // Check the splits for a payment to a loan account.  We always have
                // two splits for a scheduled loan payment: one for the interest
                // payment and the other for the principal payment.
                //
                if (splits.size() == 2 && amount < 0.0) {
                    for (TransactionSplit split : splits) {
                        loanAccount = split.getAccount();
                        if (loanAccount != null && loanAccount.getType() == AccountRecord.LOAN) {
                            loanPayment = true;
                            break;
                        }
                    }
                }

                if (loanPayment) {
                    double rate = loanAccount.getLoanRate()/12.0;
                    double balance = 0.0;

                    //
                    // Get the current loan balance
                    //
                    for (TransactionRecord x : TransactionRecord.transactions) {
                        if (x.getAccount() == loanAccount) {
                            balance += x.getAmount();
                        } else if (x.getTransferAccount() == loanAccount) {
                            balance -= x.getAmount();
                        } else {
                            List<TransactionSplit> xSplits = x.getSplits();
                            if (xSplits != null) {
                                for (TransactionSplit xSplit : xSplits) {
                                    if (xSplit.getAccount() == loanAccount) {
                                        balance -= xSplit.getAmount();
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    //
                    // The transaction amount will be allocated between the two
                    // splits based on the loan rate.  The total payment will then
                    // be set to the sum of the interest and principal payments.
                    //
                    double interest = (double)Math.round(balance*rate*100.0)/100.0;
                    double principal = (double)Math.round((amount-interest)*100.0)/100.0;
                    if (principal < balance)
                        principal = balance;

                    balance -= principal;
                    if (Math.abs(balance) < 0.005)
                        rescheduleTransaction = false;

                    for (TransactionSplit split : splits) {
                        if (split.getAccount() == loanAccount)
                            split.setAmount(principal);
                        else
                            split.setAmount(interest);
                    }

                    t.setAmount(principal+interest);
                }
            }

            //
            // Add the new transaction to the transaction list
            //
            TransactionRecord.insertTransaction(t);
            Main.dataModified = true;
            transactionsProcessed = true;

            //
            // Reschedule a recurring transaction
            //
            int scheduleType = r.getType();
            if (scheduleType != ScheduleRecord.SINGLE && rescheduleTransaction) {

                //
                // Reset the scheduled date for a recurring transaction
                //
                cal.setTime(scheduledDate);
                switch (scheduleType) {
                    case ScheduleRecord.WEEKLY:
                        cal.add(Calendar.DAY_OF_YEAR, 7);
                        break;

                    case ScheduleRecord.BIWEEKLY:
                        int day = cal.get(Calendar.DAY_OF_MONTH);
                        if (day == 1) {
                            cal.set(Calendar.DAY_OF_MONTH, 15);
                        } else {
                            cal.set(Calendar.DAY_OF_MONTH, 1);
                            cal.add(Calendar.MONTH, 1);
                        }
                        break;

                    case ScheduleRecord.MONTHLY:
                        cal.add(Calendar.MONTH, 1);
                        break;

                    default:
                        throw new IllegalArgumentException("Schedule type "+scheduleType+" is not recognized");
                }

                //
                // Add the transaction to the scheduled transaction list
                //
                r.setDate(cal.getTime());
                ScheduleRecord.insertTransaction(r);

                //
                // Get a new list iterator since the list has changed
                //
                li = ScheduleRecord.transactions.listIterator();
            }
        }

        return transactionsProcessed;
    }

    /**
     * Save the current application properties
     */
    public static void saveProperties() {
        try {
            try (FileOutputStream out = new FileOutputStream(Main.propFile)) {
                Main.properties.store(out, "MyMoney Properties");
            }
        } catch (Exception exc) {
            Main.logException("Exception while saving application properties", exc);
        }
    }

    /**
     * Log an exception.  A dialog window will be displayed if the exception occurred
     * on an AWT event dispatching thread.  Otherwise, the stack trace will be written
     * to stderr.
     *
     * @param       text        Text message describing the cause of the exception
     * @param       exc         The Java exception object
     */
    public static void logException(String text, Throwable exc) {
        if (SwingUtilities.isEventDispatchThread()) {
            StringBuilder string = new StringBuilder(512);

            //
            // Display our error message
            //
            string.append("<html><b>");
            string.append(text);
            string.append("</b><br><br>");

            //
            // Display the exception object
            //
            string.append(exc.toString());
            string.append("<br>");

            //
            // Display the stack trace
            //
            StackTraceElement[] trace = exc.getStackTrace();
            int count = 0;
            for (StackTraceElement elem : trace) {
                string.append(elem.toString());
                string.append("<br>");
                if (++count == 25)
                    break;
            }

            string.append("</html>");
            JOptionPane.showMessageDialog(Main.mainWindow, string, "Error",
                                          JOptionPane.ERROR_MESSAGE);
        } else if (deferredException == null) {
            deferredText = text;
            deferredException = exc;
            try {
                javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        Main.logException(deferredText, deferredException);
                        deferredException = null;
                        deferredText = null;
                    }
                });
            } catch (Exception logexc) {
                System.err.println("Unable to log exception during program initialization");
            }
        }
    }

    /**
     * Dump a byte array to stdout
     *
     * @param       text        Text message
     * @param       data        Byte array
     * @param       length      Data length
     */
    public static void dumpData(String text, byte[] data, int length) {
        System.out.println(text);

        for (int i=0; i<length; i++) {
            if (i%32 == 0)
                System.out.print(String.format(" %14X  ", i));
            else if (i%4 == 0)
                System.out.print(" ");

            System.out.print(String.format("%02X", data[i]));

            if (i%32 == 31)
                System.out.println();
        }

        if (length%32 != 0)
            System.out.println();
    }
}
