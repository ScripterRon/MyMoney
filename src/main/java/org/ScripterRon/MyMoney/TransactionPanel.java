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

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Abstract class for the transaction panels
 */
public abstract class TransactionPanel extends JPanel {

    /** Position transaction table to the first row */
    public static final int TOP = 0;

    /** Position transaction table to the last row */
    public static final int BOTTOM = 1;

    /** Account */
    protected AccountRecord account;

    /** Transaction table */
    protected JTable table;

    /** Table scroll pane */
    protected JScrollPane scrollPane;

    /** Panel name label */
    protected JLabel nameLabel;
    
    /**
     * Create the transaction panel
     *
     * @param       lm              Layout manager
     */
    public TransactionPanel(LayoutManager lm) {
        super(lm);
    }
    
    /**
     * Get the account for the transaction panel
     *
     * @return                      The transaction account
     */
    public AccountRecord getTransactionAccount() {
        return account;
    }

    /**
     * Get the table for the transaction panel
     *
     * @return                      The transaction table
     */
    public JTable getTransactionTable() {
        return table;
    }
    
    /**
     * Get the scroll pane for the transaction panel
     *
     * @return                      The transaction scroll pane
     */
    public JScrollPane getTransactionScrollPane() {
        return scrollPane;
    }
    
    /**
     * Position the table to show the top or the bottom row
     *
     * @param       pos             Desired table position (TOP, BOTTOM)
     */
    public void positionTable(int pos) {
        if (pos != TransactionPanel.TOP && pos != TransactionPanel.BOTTOM)
            throw new IllegalArgumentException("Table position "+pos+" is not valid");

        if (table == null || scrollPane == null || table.getRowCount() == 0)
            return;

        JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        if (scrollBar == null)
            return;

        if (pos == TransactionPanel.TOP) {
            scrollBar.setValue(0);
        } else {
            scrollBar.setValue(scrollBar.getMaximum());
        }
    }
    
    /**
     * Select and show the specified row
     *
     * @param       row             The table row
     */
    public void showSelectedRow(int row) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != row) {
            table.setRowSelectionInterval(row, row);
            if (scrollPane.getVerticalScrollBar() != null) {
                int rowHeight = table.getRowHeight();
                JViewport viewPort = scrollPane.getViewport();
                Dimension viewSize = viewPort.getExtentSize();
                Rectangle rect = new Rectangle(0, row*rowHeight, viewSize.width, rowHeight);
                if (!viewPort.getViewRect().contains(rect))
                    table.scrollRectToVisible(rect);
            }
        }
    }

    /**
     * Validate the transaction panel when account, category or security names have
     * changed
     */
    public void validate() {

        //
        // Make sure the account name is correct
        //
        if (nameLabel != null)
            nameLabel.setText("<HTML><h1>"+account.getName()+"</h1></HTML>");

        //
        // Make sure the transaction table is correct
        //
        if (table != null) {
            TableModel model = table.getModel();
            if (model instanceof AbstractTableModel) {
                AbstractTableModel tableModel = (AbstractTableModel)model;
                if (tableModel.getRowCount() > 0) {
                    int row = table.getSelectedRow();
                    tableModel.fireTableDataChanged();
                    if (row >= 0)
                        table.setRowSelectionInterval(row, row);
                }
            }
        }

        //
        // Pass the validate request up the component hierarchy
        //
        super.validate();
    }

    /**
     * Refresh the transaction panel when account transactions have changed.
     * Subclasses should override this method to provide account-specific
     * processing.
     *
     * @return                      The refreshed content pane
     */
    public TransactionPanel refreshTransactions() {
        return this;
    }
}
