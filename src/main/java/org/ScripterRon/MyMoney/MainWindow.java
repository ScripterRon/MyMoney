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

import java.io.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Main application window
 */
public final class MainWindow extends JFrame implements ActionListener {

    /** Main window is minimized */
    private boolean windowMinimized = false;

    /** Title shows modified status */
    private boolean titleModified = false;

    /** Toolbar view menu */
    private final JMenu viewMenu;

    /** Currently active account panel */
    private AccountRecord activeAccount;

    /**
     * Create the application window
     */
    public MainWindow() {

        //
        // Create the frame
        //
        super("MyMoney - "+Main.database.getName());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        //
        // Position the window using the saved position from the last time
        // the program was run.  Default position is the upper left corner
        // of the screen.
        //
        int frameX = 0;
        int frameY = 0;
        String propValue = Main.properties.getProperty("window.main.position");
        if (propValue != null) {
            int sep = propValue.indexOf(',');
            frameX = Integer.parseInt(propValue.substring(0, sep));
            frameY = Integer.parseInt(propValue.substring(sep+1));
        }

        setLocation(frameX, frameY);

        //
        // Size the window using the saved size from the last time
        // the program was run.  The minimum size is 950x650.
        //
        int frameWidth = 950;
        int frameHeight = 650;
        propValue = Main.properties.getProperty("window.main.size");
        if (propValue != null) {
            int sep = propValue.indexOf(',');
            frameWidth = Math.max(frameWidth, Integer.parseInt(propValue.substring(0, sep)));
            frameHeight = Math.max(frameHeight, Integer.parseInt(propValue.substring(sep+1)));
        }

        setPreferredSize(new Dimension(frameWidth, frameHeight));

        //
        // Create the application menu bar
        //
        JMenuBar menuBar = new JMenuBar();
        menuBar.setOpaque(true);
        menuBar.setBackground(new Color(230,230,230));

        //
        // Add the "File" menu to the menu bar
        //
        // The "File" menu contains the "Open", "Archive", "Save" and "Exit" items.
        //
        JMenu menu;
        JMenuItem menuItem;

        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        
        menuItem = new JMenuItem("Archive");
        menuItem.setActionCommand("archive");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Open");
        menuItem.setActionCommand("open");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Save");
        menuItem.setActionCommand("save");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Exit");
        menuItem.setActionCommand("exit");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuBar.add(menu);

        //
        // Add the "View" menu to the menu bar
        //
        // The "View" menu contains the "Overview" item as well as items for
        // each account that is not hidden.  The action command for the
        // account items consists of '#' followed by the account identifier.
        //
        viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);
        buildViewMenu();
        menuBar.add(viewMenu);

        //
        // Add the "Objects" menu to the menu bar
        //
        // The "Objects" menu contains the "Accounts", "Categories", 
        // "Schedules" and "Securities" items
        //
        menu = new JMenu("Objects");
        menu.setMnemonic(KeyEvent.VK_O);

        menuItem = new JMenuItem("Accounts");
        menuItem.setActionCommand("edit accounts");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Categories");
        menuItem.setActionCommand("edit categories");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Schedules");
        menuItem.setActionCommand("edit schedules");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Securities");
        menuItem.setActionCommand("edit securities");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuBar.add(menu);
        //
        // Add the "Calculators" menu to the menu bar
        //
        // The "Calculators" menu contains the "Amortization", 
        // "Compound Interest" and "Yield to Maturity" items
        //
        menu = new JMenu("Calculators");
        menu.setMnemonic(KeyEvent.VK_C);

        menuItem = new JMenuItem("Amortization");
        menuItem.setActionCommand("calculate amortization");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Compound Interest");
        menuItem.setActionCommand("calculate compound interest");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Yield to Maturity");
        menuItem.setActionCommand("calculate yield to maturity");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuBar.add(menu);

        //
        // Add the "Graphs" menu to the menu bar
        //
        // The "Graphs" menu contains the "Net Worth" item
        //
        menu = new JMenu("Graphs");
        menu.setMnemonic(KeyEvent.VK_G);

        menuItem = new JMenuItem("Net Worth");
        menuItem.setActionCommand("net worth graph");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuBar.add(menu);
        //
        // Add the "Reports" menu to the menu bar
        //
        // The "Reports" menu contains the "Amortization", "Capital Gains", 
        // "Investments" and "Transactions" items
        //
        menu = new JMenu("Reports");
        menu.setMnemonic(KeyEvent.VK_R);

        menuItem = new JMenuItem("Amortization");
        menuItem.setActionCommand("amortization report");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Capital Gains");
        menuItem.setActionCommand("capital gains report");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Investments");
        menuItem.setActionCommand("investment report");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Transactions");
        menuItem.setActionCommand("transaction report");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuBar.add(menu);
        //
        // Add the "Online" menu to the menu bar
        //
        // The "Online" menu contains the "Update Prices" item
        //
        menu = new JMenu("Online");
        menu.setMnemonic(KeyEvent.VK_O);

        menuItem = new JMenuItem("Update Prices");
        menuItem.setActionCommand("update prices");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuBar.add(menu);
        //
        // Add the "Help" menu to the menu bar
        //
        // The "Help" menu contains the "Contents" and "About" items
        //
        menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);

        menuItem = new JMenuItem("Contents");
        menuItem.setActionCommand("help");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("About");
        menuItem.setActionCommand("about");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuBar.add(menu);
        //
        // Add the menu bar to the window frame
        //
        setJMenuBar(menuBar);
        //
        // Display the Overview panel
        //
        setContentPane(new OverviewPanel());
        //
        // Receive WindowListener events
        //
        addWindowListener(new ApplicationWindowListener(this));
    }

    /**
     * Set the main window title.  The frame title is formed from the
     * database file name if no title is supplied and will refresh the
     * 'database modified' indicator in the displayed title.
     *
     * @param       title           Title string (ignored)
     */
    @Override
    public void setTitle(String title) {
        if (title != null) {
            super.setTitle(title);
            titleModified = false;
        } else if (Main.dataModified && !titleModified) {
            super.setTitle("MyMoney - "+Main.database.getName()+"*");
            titleModified = true;
        } else if (!Main.dataModified && titleModified) {
            super.setTitle("MyMoney - "+Main.database.getName());
            titleModified = false;
        }
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
        // "about" - Display information about this application
        // "amortization report" - Generate the bond accretion/amortization report
        // "archive" - Archive transactions
        // "calculate amortization" - Calculate bond accretion/amortization
        // "calculate compound interest" - Calculate compound interest
        // "capital gains report" - Generate the capital gains report
        // "edit accounts" - Edit accounts
        // "edit categories" - Edit categories
        // "edit securities" - Edit securities
        // "edit schedules" - Edit scheduled transactions
        // "exit" - End the program
        // "help" - Display program help
        // "investment report" - Generate the investment report
        // "net worth graph" - Display the net worth graph
        // "open" - Open a new database file
        // "overview" - Display the account overview
        // "save" - Save the modified database
        // "transaction report" - Generate the transaction report
        // "update prices" - Update security prices
        // "#nn" - Display an account (nn is the account identifier)
        //
        try {
            String action = ae.getActionCommand();
            boolean contentPaneChanged = false;
            boolean validateContentPane = false;
            if (action.equals("exit")) {
                exitProgram();
            } else if (action.equals("save")) {
                saveDatabase();
            } else if (action.equals("open")) {
                openDatabase();
                activeAccount = null;
                setContentPane(new OverviewPanel());
                contentPaneChanged = true;
            } else if (action.equals("archive")) {
                ArchiveDialog.showDialog(this);
            } else if (action.equals("overview")) {
                activeAccount = null;
                setContentPane(new OverviewPanel());
                contentPaneChanged = true;
            } else if (action.charAt(0) == '#') {
                contentPaneChanged = viewAccount(Integer.valueOf(action.substring(1)));
            } else if (action.equals("calculate amortization")) {
                AmortizationDialog.showDialog(this);
            } else if (action.equals("calculate compound interest")) {
                CompoundDialog.showDialog(this);
            } else if (action.equals("calculate yield to maturity")) {
                YieldToMaturityDialog.showDialog(this);
            } else if (action.equals("edit accounts")) {
                contentPaneChanged = editAccounts();
            } else if (action.equals("edit categories")) {
                CategoryDialog.showDialog(this);
                validateContentPane = true;
            } else if (action.equals("edit securities")) {
                SecurityDialog.showDialog(this);
                validateContentPane = true;
            } else if (action.equals("edit schedules")) {
                contentPaneChanged = editSchedules();
                if (!contentPaneChanged)
                    validateContentPane = true;
            } else if (action.equals("net worth graph")) {
                NetWorthGraphDialog.showDialog(this);
            } else if (action.endsWith("amortization report")) {
                AmortizationReportDialog.showDialog(this);
            } else if (action.equals("capital gains report")) {
                CapitalGainsReportDialog.showDialog(this);
            } else if (action.equals("investment report")) {
                InvestmentReportDialog.showDialog(this);
            } else if (action.equals("transaction report")) {
                TransactionReportDialog.showDialog(this);
            } else if (action.equals("update prices")) {
                PriceUpdate.onlineUpdate(this);
                validateContentPane = true;
            } else if (action.equals("help")) {
                displayHelp(HelpWindow.CONTENTS);
            } else if (action.equals("about")) {
                aboutMyMoney();
            }

            //
            // Validate and display the updated application window
            //
            // Note that the dialogs are modal, so all updates have been completed
            // when control returns
            //
            if (validateContentPane) {
                Container contentPane = getContentPane();
                if (contentPane instanceof OverviewPanel) {
                    setContentPane(new OverviewPanel());
                    contentPaneChanged = true;
                } else {
                    contentPane.validate();
                }
            }

            if (contentPaneChanged)
                validate();

            //
            // Position the transaction table at the bottom to display the most
            // recent transactions when we display the panel for the first time
            //
            if (contentPaneChanged) {
                Container contentPane = getContentPane();
                if (contentPane instanceof TransactionPanel)
                    ((TransactionPanel)contentPane).positionTable(TransactionPanel.BOTTOM);
            }
        } catch (Exception exc) {
            Main.logException("Exception while processing action event", exc);
        }
    }
    
    /**
     * Display help
     * 
     * @param       page            Help page
     */
    public void displayHelp(String page) {
        if (Main.helpWindow != null) {
            Main.helpWindow.toFront();
            Main.helpWindow.setPage(page);
        } else {
            Main.helpWindow = new HelpWindow(page);
            Main.helpWindow.pack();
            Main.helpWindow.setVisible(true);
        }
    }

    /**
     * Build the View menu from the AccountRecord list
     */
    private void buildViewMenu() {
        JMenuItem menuItem;

        //
        // Remove the existing menu items
        //
        viewMenu.removeAll();

        //
        // The Overview view is always the first menu item
        //
        menuItem = new JMenuItem("Overview");
        menuItem.setActionCommand("overview");
        menuItem.addActionListener(this);
        viewMenu.add(menuItem);

        //
        // Add the account views (hidden accounts will not be included)
        //
        if (AccountRecord.accounts.size() != 0) {
            viewMenu.addSeparator();

            for (AccountRecord a : AccountRecord.accounts) {
                if (!a.isHidden()) {
                    menuItem = new JMenuItem(a.getName());
                    menuItem.setActionCommand("#"+a.getID());
                    menuItem.addActionListener(this);
                    viewMenu.add(menuItem);
                }
            }
        }
    }

    /**
     * View an account
     *
     * @param       accountID       Account identifier
     * @return                      TRUE if the content pane has been changed
     */
    private boolean viewAccount(int accountID) {
        boolean contentPaneChanged = false;
        for (AccountRecord a : AccountRecord.accounts) {
            if (a.getID() == accountID) {
                switch (a.getType()) {
                    case AccountRecord.BANK:
                        activeAccount = a;
                        setContentPane(new BankTransactionPanel(a));
                        contentPaneChanged = true;
                        break;

                    case AccountRecord.CREDIT:
                        activeAccount = a;
                        setContentPane(new CreditTransactionPanel(a));
                        contentPaneChanged = true;
                        break;

                    case AccountRecord.INVESTMENT:
                        activeAccount = a;
                        setContentPane(new InvestmentTransactionPanel(a));
                        contentPaneChanged = true;
                        break;

                    case AccountRecord.ASSET:
                        activeAccount = a;
                        setContentPane(new AssetTransactionPanel(a));
                        contentPaneChanged = true;
                        break;

                    case AccountRecord.LOAN:
                        activeAccount = a;
                        setContentPane(new LoanTransactionPanel(a));
                        contentPaneChanged = true;
                        break;
                }

                break;
            }
        }

        return contentPaneChanged;
    }

    /**
     * Edit accounts
     *
     * @return                      TRUE if the content pane has changed
     */
    private boolean editAccounts() {
        boolean contentPaneChanged = false;

        //
        // Display the accounts dialog
        //
        AccountDialog.showDialog(this);

        //
        // Rebuild the View menu
        //
        buildViewMenu();

        //
        // Make sure the current account is still valid
        //
        Container contentPane = getContentPane();
        if (contentPane instanceof OverviewPanel) {
            setContentPane(new OverviewPanel());
            contentPaneChanged = true;
        } else {
            boolean accountValid = false;
            for (AccountRecord a : AccountRecord.accounts) {
                if (activeAccount == a) {
                    accountValid = true;
                    break;
                }
            }

            if (accountValid) {
                contentPane.validate();
            } else {
                activeAccount = null;
                setContentPane(new OverviewPanel());
                contentPaneChanged = true;
            }
        }

        return contentPaneChanged;
    }

    /**
     * Edit scheduled transactions
     *
     * @return                      TRUE if the content pane has changed
     */
    private boolean editSchedules() {
        boolean contentPaneChanged = false;

        //
        // Display the scheduled transactions dialog
        //
        ScheduleDialog.showDialog(this);

        //
        // Process pending scheduled transactions
        //
        boolean transactionsProcessed = Main.processScheduledTransactions();
        Container contentPane = getContentPane();
        if (transactionsProcessed && contentPane instanceof TransactionPanel) {
            setContentPane(((TransactionPanel)contentPane).refreshTransactions());
            contentPaneChanged = true;
        }

        return contentPaneChanged;
    }
    
    /**
     * Open another database
     *
     * @exception       DBException     Database structure error
     * @exception       IOException     Unable to read application data
     */
    private void openDatabase() throws DBException, IOException {
        
        //
        // Save the current database if it has been modified
        //
        if (Main.dataModified) {
            int option = JOptionPane.showConfirmDialog(this, "Do you want to save the database modifications?",
                                                       "Confirm Save", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION)
                Main.database.save();

            Main.dataModified = false;
        }

        //
        // Open the new database
        //
        JFileChooser chooser = new JFileChooser(Main.dataPath);
        chooser.setDialogTitle("Select Database File");
        chooser.setFileFilter(new DatabaseFileFilter());
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            Main.database = new Database(chooser.getSelectedFile());
            Main.database.load();
            Main.processScheduledTransactions();
            setTitle("MyMoney - "+Main.database.getName());
            buildViewMenu();
        }
    }
    
    /**
     * Save the database
     *
     * @exception       IOException     Unable to save application data
     */
    private void saveDatabase() throws IOException {
        if (Main.dataModified) {
            Main.database.save();
            Main.dataModified = false;
            setTitle(null);
        }        
    }

    /**
     * Exit the application
     *
     * @exception       IOException     Unable to save application data
     */
    private void exitProgram() throws IOException {

        //
        // Remember the current window position and size unless the window
        // is minimized
        //
        if (!windowMinimized) {
            Point p = Main.mainWindow.getLocation();
            Dimension d = Main.mainWindow.getSize();
            Main.properties.setProperty("window.main.position", p.x+","+p.y);
            Main.properties.setProperty("window.main.size", d.width+","+d.height);
        }

        //
        // Save the application data
        //
        if (Main.dataModified) {
            int option = JOptionPane.showConfirmDialog(this,
                                "Do you want to save the database modifications?",
                                "Confirm Save",
                                JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION)
                Main.database.save();
        }

        //
        // Save the application properties
        //
        Main.saveProperties();

        //
        // All done
        //
        System.exit(0);
    }

    /**
     * Display information about the MyMoney application
     */
    private void aboutMyMoney() {
        StringBuilder info = new StringBuilder(256);
        info.append(String.format("<html>%s Version %s<br>", Main.applicationName, Main.applicationVersion));
        
        info.append("<br>User name: ");
        info.append((String)System.getProperty("user.name"));

        info.append("<br>Home directory: ");
        info.append((String)System.getProperty("user.home"));

        info.append("<br><br>OS: ");
        info.append((String)System.getProperty("os.name"));

        info.append("<br>OS version: ");
        info.append((String)System.getProperty("os.version"));

        info.append("<br>OS patch level: ");
        info.append((String)System.getProperty("sun.os.patch.level"));

        info.append("<br><br>Java vendor: ");
        info.append((String)System.getProperty("java.vendor"));

        info.append("<br>Java version: ");
        info.append((String)System.getProperty("java.version"));

        info.append("<br>Java home directory: ");
        info.append((String)System.getProperty("java.home"));

        info.append("<br>Java class path: ");
        info.append((String)System.getProperty("java.class.path"));
        
        info.append("<br><br>Current Java memory usage: ");
        info.append(String.format("%,.3f MB", (double)Runtime.getRuntime().totalMemory()/(1024.0*1024.0)));

        info.append("<br>Maximum Java memory size: ");
        info.append(String.format("%,.3f MB", (double)Runtime.getRuntime().maxMemory()/(1024.0*1024.0)));
        
        info.append("</html>");
        JOptionPane.showMessageDialog(this, info.toString(), "About MyMoney",
                                      JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Listen for window events
     */
    private class ApplicationWindowListener extends WindowAdapter {
        
        /** Application window */
        private final JFrame window;
        
        /**
         * Create the window listener
         *
         * @param       window      The application window
         */
        public ApplicationWindowListener(JFrame window) {
            this.window = window;
        }

        /**
         * Window has been activated (WindowListener interface)
         *
         * @param       we              Window event
         */
        @Override
        public void windowActivated(WindowEvent we) {
            window.setTitle(null);
        }

        /**
         * Window has been minimized (WindowListener interface)
         *
         * @param       we              Window event
         */
        @Override
        public void windowIconified(WindowEvent we) {
            windowMinimized = true;
        }

        /**
         * Window has been restored (WindowListener interface)
         *
         * @param       we              Window event
         */
        @Override
        public void windowDeiconified(WindowEvent we) {
            windowMinimized = false;
        }

        /**
         * Window is closing (WindowListener interface)
         *
         * @param       we              Window event
         */
        @Override
        public void windowClosing(WindowEvent we) {
            try {
                exitProgram();
            } catch (Exception exc) {
                Main.logException("Exception while closing application window", exc);
            }
        }
    }
}
