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
import java.util.ListIterator;
import java.util.SortedSet;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Display the transaction splits dialog
 */
public final class SplitsDialog extends JDialog implements ActionListener {

    /** Splits table column classes */
    private static final Class<?>[] columnClasses = {
        String.class, String.class, Double.class, Double.class, Double.class};

    /** Splits table column names */
    private static final String[] columnNames = {
        "Description", "Category/Account", "Payment", "Deposit", "Balance"};

    /** Splits table column types */
    private static final int[] columnTypes = {
        SizedTable.MEMO_COLUMN, SizedTable.CATEGORY_COLUMN, SizedTable.AMOUNT_COLUMN,
        SizedTable.AMOUNT_COLUMN, SizedTable.AMOUNT_COLUMN};
    
    /** Table model */
    private SplitModel tableModel;

    /** Table */
    private JTable table;
    
    /** Category/Account model */
    private TransferComboBoxModel categoryAccountModel;

    /** Transaction splits */
    private List<TransactionSplit> splits;

    /**
     * Create the splits dialog
     *
     * @param       parent              Parent window
     * @param       splits              Splits list
     * @param       account             Current account or null
     */
    public SplitsDialog(JDialog parent, List<TransactionSplit> splits, AccountRecord account) {
        super(parent, "Edit Splits", true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.splits = splits;

        //
        // Create the splits table
        //
        tableModel = new SplitModel();
        table = new SizedTable(tableModel, columnTypes);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setCellSelectionEnabled(true);
        table.setSurrendersFocusOnKeystroke(true);
        Dimension tableSize = table.getPreferredSize();
        table.setPreferredScrollableViewportSize(new Dimension(tableSize.width, 10*table.getRowHeight()));

        //
        // Set up the table cell editors
        //
        categoryAccountModel = new TransferComboBoxModel(account, null, null);
        TableColumnModel columnModel = table.getColumnModel();
        for (int i=0; i<columnNames.length; i++) {
            Object value = null;
            TableColumn column = columnModel.getColumn(i);

            switch (i) {
                case 0:                         // Description
                    break;

                case 1:                         // Category/Account
                    column.setCellEditor(new DefaultCellEditor(new JComboBox(categoryAccountModel)));
                    break;

                case 2:                         // Payment
                case 3:                         // Deposit
                    column.setCellEditor(new AmountEditor(2, true));
                    break;

                case 4:                         // Balance (not editable)
                    DefaultTableCellRenderer renderer = (DefaultTableCellRenderer)column.getCellRenderer();
                    renderer.setBackground(new Color(240, 240, 240));
                    break;

                default:
                    throw new IndexOutOfBoundsException("Table column "+i+" is not valid");
            }
        }
            
        //
        // Pressing ENTER while not editing a cell will activate the
        // default button.  Pressing ENTER while editing a cell will
        // stop editing the cell.
        //
        table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "check edit");
        table.getActionMap().put("check edit", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                JTable table = (JTable)event.getSource();
                JButton defaultButton = table.getRootPane().getDefaultButton();
                if (defaultButton != null)
                    defaultButton.doClick();
            }
        });

        //
        // Create the table scroll pane
        //
        JScrollPane scrollPane = new JScrollPane(table);

        //
        // Create the buttons (New Split, Delete Split, Done)
        //
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));

        JButton button = new JButton("New Split");
        button.setActionCommand("add");
        button.addActionListener(this);
        buttonPane.add(button);
        getRootPane().setDefaultButton(button);
        
        buttonPane.add(Box.createHorizontalStrut(10));

        button = new JButton("Delete Split");
        button.setActionCommand("delete");
        button.addActionListener(this);
        buttonPane.add(button);
        
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
        contentPane.add(scrollPane);
        contentPane.add(Box.createVerticalStrut(15));
        contentPane.add(buttonPane);
        setContentPane(contentPane);
    }

    /**
     * Show the splits dialog
     *
     * @param       parent              Parent dialog
     * @param       splits              Splits list
     * @param       account             Current account or null
     */
    public static void showDialog(JDialog parent, List<TransactionSplit> splits, AccountRecord account) {
        try {
            JDialog dialog = new SplitsDialog(parent, splits, account);
            dialog.pack();
            dialog.setLocationRelativeTo(parent);
            dialog.setVisible(true);
        } catch (Exception exc) {
            Main.logException("Exception while displaying dialog", exc);
        }
    }

    /**
     * Action performed (ActionListener interface)
     *
     * @param       ae          Action event
     */
    public void actionPerformed(ActionEvent ae) {

        //
        // Process the action command
        //
        // "add" - Add a new split
        // "delete" - Delete a split
        // "done" - Done processing splits
        //
        try {
            String action = ae.getActionCommand();
            if (action.equals("add")) {
                int row = tableModel.addSplit();
                table.setRowSelectionInterval(row, row);
                table.changeSelection(row, 0, false, false);
            } else if (action.equals("delete")) {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(this, "You must select a split to delete",
                                                  "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    tableModel.removeSplit(row);
                    row = Math.min(row, table.getRowCount()-1);
                    if (row >= 0)
                        table.setRowSelectionInterval(row, row);
                }
            } else if (action.equals("done")) {
                if (splits.size() != 0) {
                    for (TransactionSplit s1 : splits) {
                        AccountRecord a1 = s1.getAccount();
                        if (a1 == null)
                            continue;

                        for (TransactionSplit s2 : splits) {
                            AccountRecord a2 = s2.getAccount();
                            if (a1 == a2 && s1 != s2) {
                                JOptionPane.showMessageDialog(this,
                                        "Account '"+a1.getName()+"' is used more than once",
                                        "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                    }
                }

                setVisible(false);
                dispose();
            }
        } catch (Exception exc) {
            Main.logException("Exception while processing action event", exc);
        }
    }

    /**
     * Transaction splits table model
     */
    private class SplitModel extends AbstractTableModel {

        /**
         * Create the transaction splits model
         */
        public SplitModel() {
        }

        /**
         * Get the number of columns in the table
         *
         * @return                  The number of columns
         */
        public int getColumnCount() {
            return columnNames.length;
        }

        /**
         * Get the column class
         *
         * @param       column      Column number
         * @return                  The column class
         */
        public Class<?> getColumnClass(int column) {
            return columnClasses[column];
        }

        /**
         * Get the column name
         *
         * @param       column      Column number
         * @return                  Column name
         */
        public String getColumnName(int column) {
            return columnNames[column];
        }

        /**
         * Get the number of rows in the table
         *
         * @return                  The number of rows
         */
        public int getRowCount() {
            return splits.size();
        }

        /**
         * Check if the specified cell is editable
         *
         * @param       row         Row number
         * @param       column      Column number
         * @return                  TRUE if the cell is editable
         */
        public boolean isCellEditable(int row, int column) {
            boolean editable;
            if (column < 4)
                editable = true;
            else
                editable = false;

            return editable;
        }

        /**
         * Get the value for a cell
         *
         * @param       row         Row number
         * @param       column      Column number
         * @return                  Returns the object associated with the cell
         */
        public Object getValueAt(int row, int column) {
            if (row >= splits.size())
                throw new IndexOutOfBoundsException("Table row "+row+" is not valid");

            TransactionSplit split = splits.get(row);
            Object value ;
            double amount;

            switch (column) {
                case 0:                             // Description
                    value = split.getDescription();
                    break;

                case 1:                             // Category/Account
                    CategoryRecord c = split.getCategory();
                    if (c != null) {
                        value = c.getName();
                    } else {
                        AccountRecord a = split.getAccount();
                        if (a != null)
                            value = "["+a.getName()+"]";
                        else
                            value = "--None--";
                    }

                    break;

                case 2:                             // Payment
                    amount = split.getAmount();
                    if (amount < 0.0)
                        value = new Double(-amount);
                    else
                        value = null;
                    break;

                case 3:                             // Deposit
                    amount = split.getAmount();
                    if (amount > 0.0)
                        value = new Double(amount);
                    else
                        value = null;
                    break;

                case 4:                             // Balance
                    amount = 0.00;
                    for (int i=0; i<=row; i++) {
                        TransactionSplit s = splits.get(i);
                        amount += s.getAmount();
                    }

                    value = new Double(amount);
                    break;

                default:
                    throw new IndexOutOfBoundsException("Table column "+column+" is not valid");
            }

            return value;
        }

        /**
         * Set the value for a cell
         *
         * @param       value       Cell value
         * @param       row         Row number
         * @param       column      Column number
         */
        public void setValueAt(Object value, int row, int column) {
            if (row >= splits.size())
                throw new IndexOutOfBoundsException("Table row "+row+" is not valid");

            TransactionSplit split = splits.get(row);
            double amount;

            switch (column) {
                case 0:                             // Description
                    split.setDescription(value!=null ? (String)value : new String());
                    break;

                case 1:                             // Category/Account
                    split.setAccount(null);
                    split.setCategory(null);
                    int index = categoryAccountModel.getElementIndex(value);
                    if (index > 0) {
                        DBElement element = categoryAccountModel.getDBElementAt(index);
                        if (element instanceof AccountRecord)
                            split.setAccount((AccountRecord)element);
                        else
                            split.setCategory((CategoryRecord)element);
                    }
                    break;

                case 2:                             // Payment
                    if (value != null)
                        amount = -((Number)value).doubleValue();
                    else
                        amount = 0.00;

                    split.setAmount(amount);
                    break;

                case 3:                             // Deposit
                    if (value != null)
                        amount = ((Number)value).doubleValue();
                    else
                        amount = 0.00;

                    split.setAmount(amount);
                    break;

                default:
                    throw new IndexOutOfBoundsException("Table column "+column+" is not valid");
            }

            fireTableRowsUpdated(row, splits.size()-1);
        }

        /**
         * Add a new split
         *
         * @return                      Row index for the new split
         */
        public int addSplit() {
            int row = splits.size();
            splits.add(new TransactionSplit());
            fireTableRowsInserted(row, row);
            return row;
        }

        /**
         * Delete a split
         *
         * @param       row             Row index
         */
        public void removeSplit(int row) {
            splits.remove(row);
            fireTableRowsDeleted(row, row);
        }
    }
}
