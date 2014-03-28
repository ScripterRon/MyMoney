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

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Investment transaction table model
 */
public final class InvestmentTableModel extends AbstractTableModel {

    /** Account */
    private AccountRecord account;

    /** Column names */
    private String[] columnNames;

    /** Column classes */
    private Class<?>[] columnClasses;

    /** List data */
    private List<TransactionRecord> listData;

    /**
     * Create the investment account transaction table model
     *
     * @param       account         The account
     * @param       columnNames     The table column names
     * @param       columnClasses   The table column classes
     */
    public InvestmentTableModel(AccountRecord account, String[] columnNames, Class<?>[] columnClasses) {
        if (account == null)
            throw new NullPointerException("No account provided");
        if (columnNames == null)
            throw new NullPointerException("No column names provided");
        if (columnClasses == null)
            throw new NullPointerException("No column classes provided");
        if (columnNames.length != columnClasses.length)
            throw new IllegalArgumentException("Number of names not same as number of classes");

        this.account = account;
        this.columnNames = columnNames;
        this.columnClasses = columnClasses;
        
        int listSize = TransactionRecord.transactions.size();
        listData = new ArrayList<TransactionRecord>(listSize+10);

        //
        // Add transactions for the investment account
        //
        // An investment account cannot be a transfer account, so
        // we just need to check the source account for the transaction
        //
        for (TransactionRecord t : TransactionRecord.transactions) {
            if (t.getAccount() == account)
                listData.add(t);
        }
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
        return listData.size();
    }

    /**
     * Get the value for a cell
     *
     * @param       row         Row number
     * @param       column      Column number
     * @return                  Returns the object associated with the cell
     *
     * A negative amount indicates a Sell or Income action while a positive
     * amount indicates a Buy or Expense action
     */
    public Object getValueAt(int row, int column) {
        if (row >= listData.size())
            throw new IndexOutOfBoundsException("Table row "+row+" is not valid");

        TransactionRecord transaction = listData.get(row);
        Object value;
        double amount;
        double shares;
        int action;
        switch (column) {
            case 0:                             // Date
                value = transaction.getDate();
                break;

            case 1:                             // Action
                value = TransactionRecord.getActionString(transaction.getAction());
                break;

            case 2:                             // Security
                value = transaction.getSecurity().getName();
                break;

            case 3:                             // Category
                CategoryRecord c = transaction.getCategory();
                if (c != null)
                    value = c.getName();
                else
                    value = new String();
                break;

            case 4:                             // Shares
                shares = transaction.getShares();
                if (shares != 0.0)
                    value = new Double(shares);
                else
                    value = null;
                break;

            case 5:                             // Price
                amount = transaction.getSharePrice();
                action = transaction.getAction();
                if (amount != 0.0 || action == TransactionRecord.BUY || action == TransactionRecord.SELL)
                    value = new Double(amount);
                else
                    value = null;
                break;

            case 6:                             // Commission
                amount = transaction.getCommission();
                if (amount != 0.0)
                    value = new Double(amount);
                else
                    value = null;
                break;

            case 7:                             // Amount
                amount = transaction.getAmount();
                action = transaction.getAction();
                if (amount != 0.0 || action == TransactionRecord.BUY || action == TransactionRecord.SELL)
                    value = new Double(Math.abs(amount));
                else
                    value = null;
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

    /**
     * A transaction has been added to the table
     *
     * @param       position        Position to add the transaction or -1
     * @param       transaction     The new transaction
     * @return                      Index of the new transaction
     */
    public int transactionAdded(int position, TransactionRecord transaction) {
        Date date = transaction.getDate();
        int lastElem = listData.size()-1;
        int index, lowIndex, highIndex;

        //
        // Insert the new transaction based on the transaction date (position == -1)
        // or at the specified position (position >= 0)
        //
        if (position >= 0) {
            index = position;
            listData.add(index, transaction);
        } else if (lastElem < 0) {
            index = 0;
            listData.add(transaction);
        } else if (date.compareTo(listData.get(lastElem).getDate()) >= 0) {
            index = lastElem+1;
            listData.add(transaction);
        } else {
            lowIndex = -1;
            highIndex = lastElem;
            while (highIndex-lowIndex > 1) {
                index = (highIndex-lowIndex)/2+lowIndex;
                if (date.compareTo(listData.get(index).getDate()) < 0)
                    highIndex = index;
                else
                    lowIndex = index;
            }

            index = highIndex;
            listData.add(index, transaction);
        }

        //
        // Notify table listeners
        //
        fireTableRowsInserted(index, index);
        return index;
    }

    /**
     * A transaction has been removed from the table
     *
     * @param       transaction     The transaction to remove
     * @return                      The index of the removed transaction
     */
    public int transactionRemoved(TransactionRecord transaction) {
        int index = 0;

        //
        // Remove the transaction from our list
        //
        ListIterator<TransactionRecord> i = listData.listIterator();
        while (i.hasNext()) {
            TransactionRecord r = i.next();
            if (r == transaction) {
                i.remove();
                break;
            }

            index++;
        }

        //
        // Notify table listeners
        //
        fireTableRowsDeleted(index, index);
        return index;
    }
}
