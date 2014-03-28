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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import java.awt.*;
import java.awt.event.*;

/**
 * Dialog to reconcile transactions.  The column names can be changed but the
 * column usages are fixed (with or without the check number field depending
 * on the number of column names)
 */
public final class ReconcileDialog extends JDialog implements ActionListener, ListSelectionListener {

    /** Bank reconcile table column classes */
    private static final Class<?>[] reconcileClasses_Bank = {
        Date.class, Integer.class, String.class, String.class, Double.class, Double.class};

    /** Bank reconcile table column types */
    private static final int[] reconcileTypes_Bank = {
        SizedTable.DATE_COLUMN, SizedTable.CHECK_COLUMN, SizedTable.NAME_COLUMN, SizedTable.RECONCILED_COLUMN, 
        SizedTable.AMOUNT_COLUMN, SizedTable.AMOUNT_COLUMN};
    
    /** Credit reconcile table column classes */
    private static final Class<?>[] reconcileClasses_Credit = {
        Date.class, String.class, String.class, Double.class, Double.class};
    
    /** Credit reconcile table column types */
    private static final int[] reconcileTypes_Credit = {
        SizedTable.DATE_COLUMN, SizedTable.NAME_COLUMN, SizedTable.RECONCILED_COLUMN, 
        SizedTable.AMOUNT_COLUMN, SizedTable.AMOUNT_COLUMN};    

    /** Account being reconciled */
    private AccountRecord account;

    /** Reconcile table column classes */
    private Class<?>[] reconcileClasses;

    /** Reconcile table column names */
    private String[] reconcileNames;
    
    /** Reconcile table column types */
    private int[] reconcileTypes;

    /** Statement balance */
    private double statementBalance;

    /** Reconciled balance */
    private double reconciledBalance;

    /** Transaction table model */
    private AccountTableModel tableModel;

    /** Reconcile table model */
    private ReconcileModel reconcileModel;

    /** Reconcile table */
    private JTable reconcileTable;

    /** Reconciled balance field */
    private JLabel reconciledField;

    /** Difference field */
    private JLabel differenceField;

    /**
     * Create the reconcile dialog
     *
     * @param       parent              Parent window
     * @param       account             Account being reconciled
     * @param       tableModel          Transaction table model
     * @param       statementBalance    The statement balance
     * @param       columnNames         The column names
     */
    public ReconcileDialog(JFrame parent, AccountRecord account, AccountTableModel tableModel,
                           double statementBalance, String[] columnNames) {
        super(parent, "Reconcile Transactions", true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.account = account;
        this.tableModel = tableModel;
        this.statementBalance = statementBalance;
        reconcileNames = columnNames;
        if (reconcileNames.length == reconcileClasses_Bank.length) {
            reconcileClasses = reconcileClasses_Bank;
            reconcileTypes = reconcileTypes_Bank;
        } else if (reconcileNames.length == reconcileClasses_Credit.length) {
            reconcileClasses = reconcileClasses_Credit;
            reconcileTypes = reconcileTypes_Credit;
        } else {
            throw new IllegalArgumentException("Unsupported column arrangement");
        }

        //
        // Create the reconcile table
        //
        reconcileModel = new ReconcileModel();
        reconcileTable = new SizedTable(reconcileModel, reconcileTypes);
        reconcileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Dimension tableSize = reconcileTable.getPreferredSize();
        reconcileTable.setPreferredScrollableViewportSize(new Dimension(tableSize.width, 
                                                          25*reconcileTable.getRowHeight()));
        ListSelectionModel lsm = reconcileTable.getSelectionModel();
        lsm.addListSelectionListener(this);

        //
        // Create the table scroll pane
        //
        JScrollPane scrollPane = new JScrollPane(reconcileTable);

        //
        // Display the reconciled and target balances
        //
        JPanel summaryPane = new JPanel(new GridLayout(0, 2, 5, 5));
        summaryPane.setBorder(BorderFactory.createEmptyBorder(15, 100, 15, 100));

        summaryPane.add(new JLabel("Reconciled balance:", JLabel.LEADING));
        reconciledField = new JLabel(String.format("%.2f", reconciledBalance), JLabel.TRAILING);
        summaryPane.add(reconciledField);

        summaryPane.add(new JLabel("Target balance:", JLabel.LEADING));
        summaryPane.add(new JLabel(String.format("%.2f", statementBalance), JLabel.TRAILING));

        double difference = statementBalance-reconciledBalance;
        if (Math.abs(difference) < 0.005)
            difference = 0.00;
        summaryPane.add(new JLabel("Difference:", JLabel.LEADING));
        differenceField = new JLabel(String.format("%.2f", difference), JLabel.TRAILING);
        summaryPane.add(differenceField);

        //
        // Display the buttons (Done, Finish Later, Cancel)
        //
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));

        JButton button = new JButton("Done");
        button.setActionCommand("done");
        button.addActionListener(this);
        buttonPane.add(button);
        getRootPane().setDefaultButton(button);
        
        buttonPane.add(Box.createHorizontalStrut(10));
        
        button = new JButton("Finish Later");
        button.setActionCommand("finish later");
        button.addActionListener(this);
        buttonPane.add(button);
        
        buttonPane.add(Box.createHorizontalStrut(10));

        button = new JButton("Cancel");
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
        contentPane.add(scrollPane);
        contentPane.add(Box.createVerticalStrut(15));
        contentPane.add(summaryPane);
        contentPane.add(Box.createVerticalStrut(15));
        contentPane.add(buttonPane);
        setContentPane(contentPane);
    }

    /**
     * Action performed (ActionListener interface)
     *
     * @param   ae                  Action event
     */
    public void actionPerformed(ActionEvent ae) {

        //
        // Process the action command
        //
        // "done" - Finished reconciling transactions.  Transactions that are
        //          "reconcile pending" will be changed to "reconciled" and the
        //          table model will be notified that unreconciled transactions
        //          were modified.
        // "finish later" - Suspend reconciling transactions.  The table model
        //          will be notified that unreconciled transactions were modified.
        // "cancel" Cancel reconciling.  Transactions that are "reconcile pending"
        //          will be changed to "not reconciled" and the table model will
        //          be notified that unreconciled transactions were modified.
        //
        try {
            String action = ae.getActionCommand();
            boolean dialogComplete = false;
            int reconciled;
            if (action.equals("done")) {
                for (TransactionRecord t : TransactionRecord.transactions) {
                    if (t.getAccount() == account) {
                        reconciled = t.getReconciled();
                        if ((reconciled&TransactionRecord.SOURCE_PENDING) != 0) {
                            reconciled &= (255-TransactionRecord.SOURCE_PENDING);
                            reconciled |= TransactionRecord.SOURCE_RECONCILED;
                            t.setReconciled(reconciled);
                            Main.dataModified = true;
                            tableModel.transactionModified(t);
                        } else if ((reconciled&TransactionRecord.SOURCE_RECONCILED) == 0) {
                            tableModel.transactionModified(t);
                        }
                    } else if (t.getTransferAccount() == account) {
                        reconciled = t.getReconciled();
                        if ((reconciled&TransactionRecord.TARGET_PENDING) != 0) {
                            reconciled &= (255-TransactionRecord.TARGET_PENDING);
                            reconciled |= TransactionRecord.TARGET_RECONCILED;
                            t.setReconciled(reconciled);
                            Main.dataModified = true;
                            tableModel.transactionModified(t);
                        } else if ((reconciled&TransactionRecord.TARGET_RECONCILED) == 0) {
                            tableModel.transactionModified(t);
                        }
                    } else {
                        List<TransactionSplit> splits = t.getSplits();
                        if (splits != null) {
                            for (TransactionSplit split : splits) {
                                if (split.getAccount() == account) {
                                    reconciled = split.getReconciled();
                                    if ((reconciled&TransactionRecord.TARGET_PENDING) != 0) {
                                        reconciled &= (255-TransactionRecord.TARGET_PENDING);
                                        reconciled |= TransactionRecord.TARGET_RECONCILED;
                                        split.setReconciled(reconciled);
                                        Main.dataModified = true;
                                        tableModel.transactionModified(t);
                                    } else if ((reconciled&TransactionRecord.TARGET_RECONCILED) == 0) {
                                        tableModel.transactionModified(t);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }

                dialogComplete = true;
            } else if (action.equals("finish later")) {
                for (TransactionRecord t : TransactionRecord.transactions) {
                    if (t.getAccount() == account) {
                        reconciled = t.getReconciled();
                        if ((reconciled&TransactionRecord.SOURCE_RECONCILED) == 0)
                            tableModel.transactionModified(t);
                    } else if (t.getTransferAccount() == account) {
                        reconciled = t.getReconciled();
                        if ((reconciled&TransactionRecord.TARGET_RECONCILED) == 0)
                            tableModel.transactionModified(t);
                    } else {
                        List<TransactionSplit> splits = t.getSplits();
                        if (splits != null) {
                            for (TransactionSplit split : splits) {
                                if (split.getAccount() == account) {
                                    reconciled = split.getReconciled();
                                    if ((reconciled&TransactionRecord.TARGET_RECONCILED) == 0)
                                        tableModel.transactionModified(t);
                                    break;
                                }
                            }
                        }
                    }
                }

                dialogComplete = true;
            } else if (action.equals("cancel")) {
                for (TransactionRecord t : TransactionRecord.transactions) {
                    if (t.getAccount() == account) {
                        reconciled = t.getReconciled();
                        if ((reconciled&TransactionRecord.SOURCE_PENDING) != 0) {
                            reconciled &= (255-TransactionRecord.SOURCE_PENDING);
                            t.setReconciled(reconciled);
                            Main.dataModified = true;
                            tableModel.transactionModified(t);
                        } else if ((reconciled&TransactionRecord.SOURCE_RECONCILED) == 0) {
                            tableModel.transactionModified(t);
                        }
                    } else if (t.getTransferAccount() == account) {
                        reconciled = t.getReconciled();
                        if ((reconciled&TransactionRecord.TARGET_PENDING) != 0) {
                            reconciled &= (255-TransactionRecord.TARGET_PENDING);
                            t.setReconciled(reconciled);
                            Main.dataModified = true;
                            tableModel.transactionModified(t);
                        } else if ((reconciled&TransactionRecord.TARGET_RECONCILED) == 0) {
                            tableModel.transactionModified(t);
                        }
                    } else {
                        List<TransactionSplit> splits = t.getSplits();
                        if (splits != null) {
                            for (TransactionSplit split : splits) {
                                if (split.getAccount() == account) {
                                    reconciled = split.getReconciled();
                                    if ((reconciled&TransactionRecord.TARGET_PENDING) != 0) {
                                        reconciled &= (255-TransactionRecord.TARGET_PENDING);
                                        split.setReconciled(reconciled);
                                        Main.dataModified = true;
                                        tableModel.transactionModified(t);
                                    } else if ((reconciled&TransactionRecord.TARGET_RECONCILED) == 0) {
                                        tableModel.transactionModified(t);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }

                dialogComplete = true;
            }

            //
            // Close the dialog if we are done
            //
            if (dialogComplete) {
                setVisible(false);
                dispose();
            }
        } catch (Exception exc) {
            Main.logException("Exception while processing action event", exc);
        }
    }

    /**
     * Value changed (ListSelectionListener interface)
     *
     * @param   se                  List selection event
     */
    public void valueChanged(ListSelectionEvent se) {
        ListSelectionModel lsm = (ListSelectionModel)se.getSource();
        if (lsm.getValueIsAdjusting())
            return;

        if (lsm.isSelectionEmpty())
            return;

        //
        // Flip the pending state for the selected transaction
        //
        int index = lsm.getMinSelectionIndex();
        if (lsm.isSelectedIndex(index)) {
            TransactionRecord t = reconcileModel.getTransactionAt(index);
            int reconciled;
            double amount;
            if (t.getAccount() == account) {
                amount = t.getAmount();
                reconciled = t.getReconciled();
                reconciled ^= TransactionRecord.SOURCE_PENDING;
                if ((reconciled&TransactionRecord.SOURCE_PENDING) != 0)
                    reconciledBalance += amount;
                else
                    reconciledBalance -= amount;
                t.setReconciled(reconciled);
                Main.dataModified = true;
            } else if (t.getTransferAccount() == account) {
                amount = t.getAmount();
                reconciled = t.getReconciled();
                reconciled ^= TransactionRecord.TARGET_PENDING;
                if ((reconciled&TransactionRecord.TARGET_PENDING) != 0)
                    reconciledBalance -= amount;
                else
                    reconciledBalance += amount;
                t.setReconciled(reconciled);
                Main.dataModified = true;
            } else {
                List<TransactionSplit> splits = t.getSplits();
                if (splits != null) {
                    for (TransactionSplit split : splits) {
                        if (split.getAccount() == account) {
                            reconciled = split.getReconciled();
                            amount = split.getAmount();
                            reconciled ^= TransactionRecord.TARGET_PENDING;
                            if ((reconciled&TransactionRecord.TARGET_PENDING) != 0)
                                reconciledBalance -= amount;
                            else
                                reconciledBalance += amount;
                            split.setReconciled(reconciled);
                            Main.dataModified = true;
                            break;
                        }
                    }
                }
            }

            lsm.clearSelection();
            reconcileModel.fireTableRowsUpdated(index, index);
            reconciledField.setText(String.format("%.2f", reconciledBalance));
            double difference = statementBalance-reconciledBalance;
            if (Math.abs(difference) < 0.005)
                difference = 0.00;
            differenceField.setText(String.format("%.2f", difference));
        }
    }

    /**
     * Show the reconcile dialog
     *
     * @param       parent              Parent window
     * @param       account             Account being reconciled
     * @param       tableModel          Transaction table model
     * @param       columnNames         The column names
     */
    public static void showDialog(JFrame parent, AccountRecord account, AccountTableModel tableModel, 
                                  String[] columnNames) {
        boolean doReconcile = false;
        double statementBalance = 0.0;

        //
        // Get the statement balance
        //
        while (!doReconcile) {
            String balance = JOptionPane.showInputDialog(parent, "Enter statement balance");
            if (balance == null)
                return;

            if (balance.length() == 0) {
                JOptionPane.showMessageDialog(parent, "You must enter the statement balance",
                                              "Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            try {
                statementBalance = Double.valueOf(balance).doubleValue();
                statementBalance = ((double)Math.round(statementBalance*100.0))/100.0;
                doReconcile = true;
            } catch (NumberFormatException exc) {
                JOptionPane.showMessageDialog(parent, "The statement balance is not a valid number",
                                              "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        //
        // Show the reconcile dialog
        //
        JDialog dialog = new ReconcileDialog(parent, account, tableModel, statementBalance, columnNames);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    /**
     * Reconcile transaction table model
     */
    private class ReconcileModel extends AbstractTableModel {

        /** List data */
        private List<TransactionRecord> listData;

        /**
         * Create the reconcile transaction model
         */
        public ReconcileModel() {
            int listSize = TransactionRecord.transactions.size();
            listData = new ArrayList<>(listSize);
            reconciledBalance = 0.00;
            int reconciled;

            for (TransactionRecord t : TransactionRecord.transactions) {
                if (t.getAccount() == account) {
                    reconciled = t.getReconciled();
                    if ((reconciled&(TransactionRecord.SOURCE_PENDING|TransactionRecord.SOURCE_RECONCILED)) != 0)
                        reconciledBalance += t.getAmount();
                    if ((reconciled&TransactionRecord.SOURCE_RECONCILED) == 0)
                        listData.add(t);
                } else if (t.getTransferAccount() == account) {
                    reconciled = t.getReconciled();
                    if ((reconciled&(TransactionRecord.TARGET_PENDING|TransactionRecord.TARGET_RECONCILED)) != 0)
                        reconciledBalance -= t.getAmount();
                    if ((reconciled&TransactionRecord.TARGET_RECONCILED) == 0)
                        listData.add(t);
                } else {
                    List<TransactionSplit> splits = t.getSplits();
                    if (splits != null) {
                        for (TransactionSplit split : splits) {
                            if (split.getAccount() == account) {
                                reconciled = split.getReconciled();
                                if ((reconciled&(TransactionRecord.TARGET_PENDING|TransactionRecord.TARGET_RECONCILED)) != 0)
                                    reconciledBalance -= split.getAmount();
                                if ((reconciled&TransactionRecord.TARGET_RECONCILED) == 0)
                                    listData.add(t);
                                break;
                            }
                        }
                    }
                }
            }
        }

        /**
         * Get the number of columns in the table
         *
         * @return                  The number of columns
         */
        public int getColumnCount() {
            return reconcileNames.length;
        }

        /**
         * Get the column class
         *
         * @param       column      Column number
         * @return                  The column class
         */
        public Class<?> getColumnClass(int column) {
            return reconcileClasses[column];
        }

        /**
         * Get the column name
         *
         * @param       column      Column number
         * @return                  Column name
         */
        public String getColumnName(int column) {
            return reconcileNames[column];
        }

        /**
         * Get the number of rows in the table
         *
         * @return                  The number of rows
         */
        public int getRowCount() {
            return listData.size();
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

            TransactionRecord t = listData.get(row);
            Object value;
            double amount;
            int columnType;
            if (column == 0)
                columnType = 0;
            else if (reconcileClasses == reconcileClasses_Bank)
                columnType = column;
            else
                columnType = column+1;

            switch (columnType) {
                case 0:                             // Date
                    value = t.getDate();
                    break;

                case 1:                             // Check (only for bank transactions)
                    int number = 0;
                    if (t.getAccount() == account)
                        number = t.getCheckNumber();
                    if (number != 0)
                        value = new Integer(number);
                    else
                        value = null;
                    break;

                case 2:                             // Name
                    SecurityRecord s = t.getSecurity();
                    if (s != null)
                        value = s.getName();
                    else
                        value = t.getName();
                    break;

                case 3:                             // Reconciled
                    value = null;
                    int reconciled;
                    if (t.getAccount() == account) {
                        reconciled = t.getReconciled();
                        if ((reconciled&TransactionRecord.SOURCE_PENDING) != 0)
                            value = "c";
                    } else if (t.getTransferAccount() == account) {
                        reconciled = t.getReconciled();
                        if ((reconciled&TransactionRecord.TARGET_PENDING) != 0)
                            value = "c";
                    } else {
                        List<TransactionSplit> splits = t.getSplits();
                        if (splits != null) {
                            for (TransactionSplit split : splits) {
                                if (split.getAccount() == account) {
                                    reconciled = split.getReconciled();
                                    if ((reconciled&TransactionRecord.TARGET_PENDING) != 0)
                                        value = "c";
                                    break;
                                }
                            }
                        }
                    }

                    if (value == null)
                        value = " ";
                    break;

                case 4:                             // Payment
                    value = null;
                    if (t.getAccount() == account) {
                        amount = t.getAmount();
                        if (amount < 0.0)
                            value = new Double(-amount);
                    } else if (t.getTransferAccount() == account) {
                        amount = t.getAmount();
                        if (amount > 0.0)
                            value = new Double(amount);
                    } else {
                        List<TransactionSplit> splits = t.getSplits();
                        if (splits != null) {
                            for (TransactionSplit split : splits) {
                                if (split.getAccount() == account) {
                                    amount = split.getAmount();
                                    if (amount > 0.0)
                                        value = new Double(amount);
                                    break;
                                }
                            }
                        }
                    }
                    break;

                case 5:                             // Deposit
                    value = null;
                    if (t.getAccount() == account) {
                        amount = t.getAmount();
                        if (amount >= 0.0)
                            value = new Double(amount);
                    } else if (t.getTransferAccount() == account) {
                        amount = t.getAmount();
                        if (amount <= 0.0)
                            value = new Double(-amount);
                    } else {
                        List<TransactionSplit> splits = t.getSplits();
                        if (splits != null) {
                            for (TransactionSplit split : splits) {
                                if (split.getAccount() == account) {
                                    amount = split.getAmount();
                                    if (amount <= 0.0)
                                        value = new Double(-amount);
                                    break;
                                }
                            }
                        }
                    }
                    break;

                default:
                    throw new IndexOutOfBoundsException("Table column "+column+" is not valid");
            }

            return value;
        }

        /**
         * Get the transaction for a table row
         *
         * @param       row         Row index
         * @return                  The TransactionRecord reference
         */
        public TransactionRecord getTransactionAt(int row) {
            if (row >= listData.size())
                throw new IndexOutOfBoundsException("Table row "+row+" is not valid");

            return listData.get(row);
        }
    }
}
