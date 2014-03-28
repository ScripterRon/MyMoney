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
 * Dialog to manage scheduled transactions
 */
public final class ScheduleDialog extends JDialog implements ActionListener {

    /** Transaction table column classes */
    private static final Class<?>[] columnClasses = {
        String.class, Date.class, String.class, String.class, String.class, Double.class, Double.class};

    /** Transaction table column names */
    private static final String[] columnNames = {
        "Type", "Date", "Description", "Account", "Category/Account", "Payment", "Deposit"};

    /** Transaction table column types */
    private static final int[] columnTypes = {
        SizedTable.TYPE_COLUMN, SizedTable.DATE_COLUMN, SizedTable.MEMO_COLUMN, SizedTable.CATEGORY_COLUMN,
        SizedTable.CATEGORY_COLUMN, SizedTable.AMOUNT_COLUMN, SizedTable.AMOUNT_COLUMN};
    
    /** Transaction table */
    private JTable table;

    /** Transaction table model */
    private ScheduleTableModel tableModel;

    /**
     * Create the scheduled transactions dialog
     *
     * @param       parent          Parent frame
     */
    public ScheduleDialog(JFrame parent) {
        super(parent, "Scheduled Transactions", Dialog.ModalityType.DOCUMENT_MODAL);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        //
        // Create the scheduled transactions table
        //
        tableModel = new ScheduleTableModel(columnNames, columnClasses);
        table = new SizedTable(tableModel, columnTypes);
        table.setRowSorter(new TableRowSorter<TableModel>(tableModel));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Dimension tableSize = table.getPreferredSize();
        table.setPreferredScrollableViewportSize(new Dimension(tableSize.width, 15*table.getRowHeight()));
        
        //
        // Create the table scroll pane
        //
        JScrollPane scrollPane = new JScrollPane(table);

        //
        // Create the buttons (New Transaction, Edit Transaction, Delete Transaction, Done, Help)
        //
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));

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

        button = new JButton("Done");
        button.setActionCommand("done");
        button.addActionListener(this);
        buttonPane.add(button);
        
        buttonPane.add(Box.createHorizontalStrut(10));

        button = new JButton("Help");
        button.setActionCommand("help");
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
     * Show the scheduled transactions dialog
     *
     * @param       parent          Parent window for the dialog
     */
    public static void showDialog(JFrame parent) {
        try {
            JDialog dialog = new ScheduleDialog(parent);
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
     * @param       ae              Action event
     */
    public void actionPerformed(ActionEvent ae) {

        //
        // Process the action command
        //
        // "new" - Create a new scheduled transaction
        // "edit" - Edit a scheduled transaction
        // "delete" - Delete a scheduled transaction
        // "done" - All done
        // "help" - Display help for schedules
        //
        try {
            String action = ae.getActionCommand();
            if (action.equals("new")) {
                ScheduleEditDialog.showDialog(this, tableModel, -1);
            } else if (action.equals("edit")) {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(this, "You must select a transaction to edit",
                                                  "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    int modelRow = table.convertRowIndexToModel(row);
                    ScheduleEditDialog.showDialog(this, tableModel, modelRow);
                }
            } else if (action.equals("delete")) {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(this, "You must select a transaction to delete",
                                                  "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    int option = JOptionPane.showConfirmDialog(this,
                                            "Do you want to delete the selected transaction?",
                                            "Confirm Delete", JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.YES_OPTION) {
                        int modelRow = table.convertRowIndexToModel(row);
                        ScheduleRecord transaction = ScheduleRecord.transactions.get(modelRow);
                        ScheduleRecord.transactions.remove(modelRow);
                        transaction.clearReferences();
                        tableModel.fireTableRowsDeleted(modelRow, modelRow);
                        Main.dataModified = true;
                        row = Math.min(row, table.getRowCount()-1);
                        if (row >= 0)
                            table.setRowSelectionInterval(row, row);
                    }
                }
            } else if (action.equals("done")) {
                setVisible(false);
                dispose();
            } else if (action.equals("help")) {
                Main.mainWindow.displayHelp(HelpWindow.SCHEDULES);
            }
        } catch (Exception exc) {
            Main.logException("Exception while processing action event", exc);
        }
    }
}
