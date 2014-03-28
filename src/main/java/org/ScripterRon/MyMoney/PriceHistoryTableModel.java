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

import java.util.SortedSet;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Price history table model
 */
public class PriceHistoryTableModel extends AbstractTableModel {

    /** Security */
    protected SecurityRecord security;

    /** Column names */
    protected String[] columnNames;

    /** Column classes */
    protected Class<?>[] columnClasses;

    /** List data */
    private PriceHistory[] listData;

    /**
     * Create the price history table model
     *
     * @param       security        The security record
     * @param       columnNames     The table column names
     * @param       columnClasses   The table column classes
     */
    public PriceHistoryTableModel(SecurityRecord security, String[] columnNames, Class<?>[] columnClasses) {
        this.security = security;
        this.columnNames = columnNames;
        this.columnClasses = columnClasses;
        
        SortedSet<PriceHistory> ph = security.getPriceHistory();
        listData = new PriceHistory[ph.size()];
        listData = ph.toArray(listData);
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
        return listData.length;
    }

    /**
     * Get the value for a cell
     *
     * @param       row         Row number
     * @param       column      Column number
     * @return                  Returns the object associated with the cell
     */
    public Object getValueAt(int row, int column) {
        if (row >= listData.length)
            throw new IndexOutOfBoundsException("Table row "+row+" is not valid");

        PriceHistory h = listData[row];
        Object value;

        switch (column) {
            case 0:                                 // Date
                value = h.getDate();
                break;

            case 1:                                 // Price
                value = Double.valueOf(h.getPrice());
                break;
                
            case 2:                                 // Split
                double ratio = h.getSplitRatio();
                if (ratio == 0.0) {
                    value = "";
                } else if (ratio < 1.0) {
                    value = String.format("  1:%d", Math.round(1.0/ratio));
                } else {
                    value = String.format("  %d:1", Math.round(ratio));
                }
                break;

            default:
                throw new IndexOutOfBoundsException("Table column "+column+" is not valid");
        }

        return value;
    }

    /**
     * Get the price history entry for a row
     *
     * @param       row         Row number
     * @return      entry       Price history entry
     */
    public PriceHistory getEntryAt(int row) {
        if (row >= listData.length)
            throw new IndexOutOfBoundsException("Table row "+row+" is not valid");

        return listData[row];
    }

    /**
     * The price history set has changed and the table should be recreated
     */
    public void priceHistoryChanged() {
        SortedSet<PriceHistory> ph = security.getPriceHistory();
        listData = new PriceHistory[ph.size()];
        listData = ph.toArray(listData);
        fireTableDataChanged();
    }
}
