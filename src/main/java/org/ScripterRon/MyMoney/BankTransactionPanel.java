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

import java.util.Date;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Bank account transaction panel
 */
public final class BankTransactionPanel extends TransactionPanel implements ActionListener {

    /** Transaction table column classes */
    private static final Class<?>[] columnClasses = {
        Date.class, Integer.class, String.class, String.class, String.class, String.class,
        Double.class, Double.class, Double.class};

    /** Transaction table column names */
    private static final String[] columnNames = {
        "Date", "Check", "Name", "Category/Account", "Memo", "C", "Payment", "Deposit", "Balance"};

    /** Transaction table column types */
    private static final int[] columnTypes = {
        SizedTable.DATE_COLUMN, SizedTable.CHECK_COLUMN, SizedTable.NAME_COLUMN,
        SizedTable.CATEGORY_COLUMN, SizedTable.MEMO_COLUMN, SizedTable.RECONCILED_COLUMN,
        SizedTable.AMOUNT_COLUMN, SizedTable.AMOUNT_COLUMN, SizedTable.AMOUNT_COLUMN};
    
    /** Reconcile table column names */
    private static final String[] reconcileNames = {"Date", "Check", "Name", "C", "Payment", "Deposit"};

    /** Transaction table model */
    private AccountTableModel tableModel;

    /**
     * Create the bank account transaction panel
     *
     * @param       account         The bank account
     */
    public BankTransactionPanel(AccountRecord account) {

        //
        // Create the transaction pane
        //
        super(new BorderLayout());
        setOpaque(true);
        setBackground(Color.white);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        //
        // Remember the account
        //
        this.account = account;

        //
        // Create the title pane containing the account name
        //
        JPanel titlePane = new JPanel();
        titlePane.setBackground(Color.white);
        nameLabel = new JLabel("<HTML><h1>"+account.getName()+"</h1></HTML>");
        titlePane.add(nameLabel);

        //
        // Create the buttons (New Transaction, Edit Transaction, 
        // Delete Transaction, Reconcile Transactions and Help)
        //
        JPanel buttonPane = new JPanel();
        buttonPane.setBackground(Color.white);

        JButton button = new JButton("New Transaction");
        button.setActionCommand("new");
        button.addActionListener(this);
        buttonPane.add(button);
        
        buttonPane.add(Box.createHorizontalStrut(10));

        button = new JButton("Edit Transaction");
        button.setActionCommand("edit");
        button.addActionListener(this);
        buttonPane.add(button);
        
        buttonPane.add(Box.createHorizontalStrut(10));

        button = new JButton("Delete Transaction");
        button.setActionCommand("delete");
        button.addActionListener(this);
        buttonPane.add(button);
        
        buttonPane.add(Box.createHorizontalStrut(10));

        button = new JButton("Reconcile Transactions");
        button.setActionCommand("reconcile");
        button.addActionListener(this);
        buttonPane.add(button);
        
        buttonPane.add(Box.createHorizontalStrut(10));

        button = new JButton("Help");
        button.setActionCommand("help");
        button.addActionListener(this);
        buttonPane.add(button);

        //
        // Create the transaction table
        //
        tableModel = new ListModel(account, columnNames, columnClasses);
        table = new SizedTable(tableModel, columnTypes);
        table.setRowSorter(new TableRowSorter<TableModel>(tableModel));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Dimension tableSize = table.getPreferredSize();
        int rowHeight = table.getRowHeight();
        int tableRows = Math.max(5, (Main.mainWindow.getSize().height-230)/rowHeight);
        table.setPreferredScrollableViewportSize(new Dimension(tableSize.width, tableRows*rowHeight));

        //
        // Create the table scroll pane
        //
        scrollPane = new JScrollPane(table);
        
        //
        // Create the table pane
        //
        JPanel tablePane = new JPanel();
        tablePane.setBackground(Color.WHITE);
        tablePane.add(Box.createGlue());
        tablePane.add(scrollPane);
        tablePane.add(Box.createGlue());

        //
        // Set up the content pane
        //
        add(titlePane, BorderLayout.NORTH);
        add(tablePane, BorderLayout.CENTER);
        add(buttonPane, BorderLayout.SOUTH);
    }

    /**
     * Refresh the account display
     *
     * @return                      The refreshed content pane
     */
    public TransactionPanel refreshTransactions() {
        return new BankTransactionPanel(account);
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
        // "new" - Create a new transaction
        // "edit" - Edit a transaction
        // "delete" - Delete a transaction
        // "reconcile" - Reconcile transactions
        // "help" - Display help for bank accounts
        //
        try {
            int row, modelRow, option;
            switch (ae.getActionCommand()) {
                case "new":
                    BankTransactionEditDialog.showDialog(Main.mainWindow, this, null);
                    break;
                    
                case "edit":
                    row = table.getSelectedRow();
                    if (row < 0) {
                        JOptionPane.showMessageDialog(this, "You must select a transaction to edit",
                                                      "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        modelRow = table.convertRowIndexToModel(row);
                        BankTransactionEditDialog.showDialog(Main.mainWindow, this, 
                                                             tableModel.getTransactionAt(modelRow));
                    }
                    break;
                    
                case "delete":
                    row = table.getSelectedRow();
                    if (row < 0) {
                        JOptionPane.showMessageDialog(this, "You must select a transaction to delete",
                                                      "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        option = JOptionPane.showConfirmDialog(this,
                                        "Do you want to delete the selected transaction?",
                                        "Confirm Delete", JOptionPane.YES_NO_OPTION);
                        if (option == JOptionPane.YES_OPTION) {
                            modelRow = table.convertRowIndexToModel(row);
                            TransactionRecord transaction = tableModel.getTransactionAt(modelRow);
                            TransactionRecord.transactions.remove(transaction);
                            transaction.clearReferences();
                            tableModel.transactionRemoved(transaction);
                            Main.dataModified = true;
                            row = Math.min(row, table.getRowCount()-1);
                            if (row >= 0)
                                table.setRowSelectionInterval(row, row);
                        }
                    }
                    break;
                    
                case "reconcile":
                    ReconcileDialog.showDialog(Main.mainWindow, account, tableModel, reconcileNames);
                    break;
                    
                case "help":
                    Main.mainWindow.displayHelp(HelpWindow.BANK_ACCOUNT);
                    break;
            }
        } catch (Exception exc) {
            Main.logException("Exception while processing action event", exc);
        }
    }

    /**
     * Bank account transaction table model
     */
    private class ListModel extends AccountTableModel {

        /**
         * Create the account transaction table model
         *
         * @param       account         The account
         * @param       columnNames     The table column names
         * @param       columnClasses   The table column classes
         */
        public ListModel(AccountRecord account, String[] columnNames, Class<?>[] columnClasses) {
            super(account, columnNames, columnClasses);
        }

        /**
         * Get the value for a cell
         *
         * @param       row         Row number
         * @param       column      Column number
         * @return                  Returns the object associated with the cell
         */
        public Object getValueAt(int row, int column) {
            if (row >= listData.size())
                throw new IndexOutOfBoundsException("Table row "+row+" is not valid");

            AccountTransaction r = listData.get(row);
            AccountRecord a;
            CategoryRecord c;
            Object value;
            double amount;

            switch (column) {
                case 0:                             // Date
                    value = r.transaction.getDate();
                    break;

                case 1:                             // Check number
                    int number = 0;
                    if (r.transaction.getAccount() == account)
                        number = r.transaction.getCheckNumber();

                    if (number != 0)
                        value = new Integer(number);
                    else
                        value = null;
                    break;

                case 2:                             // Name
                    SecurityRecord s = r.transaction.getSecurity();
                    if (s != null) {
                        value = s.getName();
                    } else {
                        value = r.transaction.getName();
                        if (r.transaction.getAccount() != account) {
                            List<TransactionSplit> splits = r.transaction.getSplits();
                            if (splits != null) {
                                for (TransactionSplit split : splits) {
                                    if (split.getAccount() == account) {
                                        value = split.getDescription();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    break;

                case 3:                             // Category/Account
                    a = r.transaction.getAccount();
                    if (a != account) {
                        value = "["+a.getName()+"]";
                    } else if (r.transaction.getSplits() != null) {
                        value = "--Split--";
                    } else {
                        c = r.transaction.getCategory();
                        if (c != null) {
                            value = c.getName();
                        } else {
                            a = r.transaction.getTransferAccount();
                            if (a != null)
                                value = "["+a.getName()+"]";
                            else
                                value = new String();
                        }
                    }
                    break;

                case 4:                             // Memo
                    value = r.transaction.getMemo();
                    break;

                case 5:                             // Reconciled
                    value = null;
                    if (r.transaction.getAccount() == account) {
                        int reconciled = r.transaction.getReconciled();
                        if ((reconciled&TransactionRecord.SOURCE_RECONCILED) != 0)
                            value = "C";
                        else if ((reconciled&TransactionRecord.SOURCE_PENDING) != 0)
                            value = "c";
                    } else if (r.transaction.getTransferAccount() == account) {
                        int reconciled = r.transaction.getReconciled();
                        if ((reconciled&TransactionRecord.TARGET_RECONCILED) != 0)
                            value = "C";
                        else if ((reconciled&TransactionRecord.TARGET_PENDING) != 0)
                            value = "c";
                    } else {
                        List<TransactionSplit> splits = r.transaction.getSplits();
                        if (splits != null) {
                            for (TransactionSplit split : splits) {
                                if (split.getAccount() == account) {
                                    int reconciled = split.getReconciled();
                                    if ((reconciled&TransactionRecord.TARGET_RECONCILED) != 0)
                                        value = "C";
                                    else if ((reconciled&TransactionRecord.TARGET_PENDING) != 0)
                                        value = "c";

                                    break;
                                }
                            }
                        }
                    }

                    if (value == null)
                        value = " ";
                    break;

                case 6:                             // Payment
                    value = null;
                    if (r.transaction.getAccount() == account) {
                        amount = r.transaction.getAmount();
                        if (amount <= 0.0)
                            value = new Double(-amount);
                    } else if (r.transaction.getTransferAccount() == account) {
                        amount = r.transaction.getAmount();
                        if (amount >= 0.0)
                            value = new Double(amount);
                    } else {
                        List<TransactionSplit> splits = r.transaction.getSplits();
                        if (splits != null) {
                            for (TransactionSplit split : splits) {
                                if (split.getAccount() == account) {
                                    amount = split.getAmount();
                                    if (amount >= 0.0)
                                        value = new Double(amount);

                                    break;
                                }
                            }
                        }
                    }
                    break;

                case 7:                             // Deposit
                    value = null;
                    if (r.transaction.getAccount() == account) {
                        amount = r.transaction.getAmount();
                        if (amount > 0.0)
                            value = new Double(amount);
                    } else if (r.transaction.getTransferAccount() == account) {
                        amount = r.transaction.getAmount();
                        if (amount < 0.0)
                            value = new Double(-amount);
                    } else {
                        List<TransactionSplit> splits = r.transaction.getSplits();
                        if (splits != null) {
                            for (TransactionSplit split : splits) {
                                if (split.getAccount() == account) {
                                    amount = split.getAmount();
                                    if (amount < 0.0)
                                        value = new Double(-amount);

                                    break;
                                }
                            }
                        }
                    }
                    break;

                case 8:                             // Balance
                    value = new Double(r.balance);
                    break;

                default:
                    throw new IndexOutOfBoundsException("Table column "+column+" is not valid");
            }

            return value;
        }
    }
}
