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
 * The ScheduleTableModel class provides a table model consisting of scheduled transactions.
 * The table rows correspond to the list elements in ScheduleRecord.transactions.  The
 * application should call the fireTableRowsInserted() and fireTableRowsDeleted() methods
 * when the scheduled transaction list is modified.
 */
public final class ScheduleTableModel extends AbstractTableModel {
    
    /** The column names */
    private String[] columnNames;
    
    /** The column classes */
    private Class<?>[] columnClasses;
    
    /** 
     * Create the scheduled transaction table model
     *
     * @param       columnNames     The column names
     * @param       columnClasses   The column classes
     */
    public ScheduleTableModel(String[] columnNames, Class<?>[] columnClasses) {
        this.columnNames = columnNames;
        this.columnClasses = columnClasses;
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
        return ScheduleRecord.transactions.size();
    }

    /**
     * Get the value for a cell
     *
     * @param       row         Row number
     * @param       column      Column number
     * @return                  Returns the object associated with the cell
     */
    public Object getValueAt(int row, int column) {
        if (row >= ScheduleRecord.transactions.size())
            throw new IndexOutOfBoundsException("Table row "+row+" is not valid");

        ScheduleRecord r = ScheduleRecord.transactions.get(row);
        AccountRecord a;
        CategoryRecord c;
        Object value;
        double amount;

        switch (column) {
            case 0:                             // Type
                value = ScheduleRecord.getTypeString(r.getType());
                break;

            case 1:                             // Date
                value = r.getDate();
                break;

            case 2:                             // Description
                value = r.getDescription();
                break;

            case 3:                             // Account
                value = r.getAccount().getName();
                break;

            case 4:                             // Category/Account
                if (r.getSplits() != null) {
                    value = "--Split--";
                } else {
                    c = r.getCategory();
                    if (c != null) {
                        value = c.getName();
                    } else {
                        a = r.getTransferAccount();
                        if (a != null)
                            value = "["+a.getName()+"]";
                        else
                            value = "--None--";
                    }
                }
                break;

            case 5:                             // Payment
                amount = r.getAmount();
                if (amount < 0.0)
                    value = new Double(-amount);
                else
                    value = null;
                break;

            case 6:                             // Deposit
                amount = r.getAmount();
                if (amount >= 0.0)
                    value = new Double(amount);
                else
                    value = null;
                break;

            default:
                throw new IndexOutOfBoundsException("Table column "+column+" is not valid");
        }

        return value;
    }
}
