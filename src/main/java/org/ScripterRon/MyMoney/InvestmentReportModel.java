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
import org.ScripterRon.Report.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Transaction data model for the investment report
 */
public class InvestmentReportModel implements ReportModel {

    /** The report column names */
    private String[] columnNames;
    
    /** The investment transactions data */
    private List<TransactionRecord> listData;
    
    /** The security holdings */
    private List<SecurityHolding> holdings;

    /**
     * Create the report model
     *
     * @param       columnNames     Report column names
     * @param       account         Investment account
     * @param       security        Security or null
     */
    public InvestmentReportModel(String[] columnNames, AccountRecord account, SecurityRecord security) {
        this.columnNames = columnNames;

        //
        // Create the report lists
        //
        listData = new ArrayList<>(TransactionRecord.transactions.size());
        holdings = new ArrayList<>(SecurityRecord.securities.size());

        //
        // Build the report data using transactions for the specified investment account
        // and optionally for the specified security.  Hidden securities will not be
        // included.
        //
        for (TransactionRecord t : TransactionRecord.transactions) {

            //
            // Skip the transaction if it is not for the requested account
            //
            if (t.getAccount() != account)
                continue;
            
            //
            // Update the security holdings for this account
            //
            SecurityHolding.updateSecurityHolding(holdings, t);

            //
            // Insert the transaction if it is not hidden and matches the
            // security filter
            //
            SecurityRecord s = t.getSecurity();
            if (s != null) {
                if (!s.isHidden() && (security == null || s == security))
                    insertTransaction(t);
            }

            //
            // Create an expanded transaction for an exchange or spin-off transaction
            //
            int action = t.getAction();
            if (action == TransactionRecord.EXCHANGE || action == TransactionRecord.SPIN_OFF) {
                s = t.getNewSecurity();
                if (!s.isHidden() && (security == null || s == security)) {
                    TransactionRecord e = new TransactionRecord(t.getDate(), account);
                    e.setExpandedTransaction(true);
                    e.setAction(TransactionRecord.BUY);
                    e.setSecurity(s);
                    e.setShares(t.getNewShares());
                    e.setAmount(t.getAmount());
                    e.setSharePrice((double)Math.round((e.getAmount()/e.getShares())*10000.0)/10000.0);
                    insertTransaction(e);
                }
            }
        }
    }

    /**
     * Insert a transaction into the report list based on the security name
     * 
     * @param       t               Transaction
     */
    private void insertTransaction(TransactionRecord t) {
        SecurityRecord s = t.getSecurity();

        //
        // Add the security to our list based on the security name
        //
        String name = s.getName();
        int lastElem = listData.size()-1;
        if (lastElem < 0) {
            listData.add(t);
        } else if (name.compareTo(listData.get(lastElem).getSecurity().getName()) >= 0) {
            listData.add(t);
        } else {
            int lowIndex = -1;
            int highIndex = lastElem;
            while (highIndex-lowIndex > 1) {
                int index = (highIndex-lowIndex)/2+lowIndex;
                if (name.compareTo(listData.get(index).getSecurity().getName()) < 0)
                    highIndex = index;
                else
                    lowIndex = index;
            }

            listData.add(highIndex, t);
        }
    }
    
    /**
     * Get the security holdings for this account
     * 
     * @return                  The security holdings
     */
    public List<SecurityHolding> getSecurityHoldings() {
        return holdings;
    }

    /**
     * Get the number of columns
     *
     * @return                  The number of columns
     */
    public int getColumnCount() {
        return columnNames.length;
    }

    /**
     * Get the number of rows
     *
     * @return                  The number of rows
     */
    public int getRowCount() {
        return listData.size();
    }

    /**
     * Get the column name
     *
     * @param       column      Column index
     * @return                  Column name
     */
    public String getColumnName(int column) {
        return columnNames[column];
    }

    /**
     * Get the object class for a column
     *
     * @param       column      Column index
     * @return                  Object class
     */
    public Class<?> getColumnClass(int column) {
        return String.class;
    }

    /**
     * Get the value for a cell
     *
     * @param       row         Row index
     * @param       column      Column index
     * @return                  Cell value
     */
    public Object getValueAt(int row, int column) {
        if (row >= listData.size())
            throw new IndexOutOfBoundsException("Report row "+row+" is not valid");

        Object value;
        SecurityRecord s;
        TransactionRecord t = listData.get(row);
        int action = t.getAction();

        switch (column) {
            case 0:                             // Date (mm/dd/yyyy)
                value = Main.getDateString(t.getDate());
                break;

            case 1:                             // Action
                value = TransactionRecord.getActionString(action);
                break;

            case 2:                             // Security
                value = t.getSecurity().getName();
                break;

            case 3:                             // Category
                if (action == TransactionRecord.EXCHANGE || action == TransactionRecord.SPIN_OFF) {
                    value = t.getNewSecurity().getName();
                } else {
                    CategoryRecord c = t.getCategory();
                    if (c != null)
                        value = c.getName();
                    else
                        value = new String();
                }
                break;

            case 4:                             // Shares
                double shares = t.getShares();
                if (Math.abs(shares) > 0.00005)
                    value = String.format("%,.4f", shares);
                else
                    value = new String();
                break;

            case 5:                             // Price
                if (t.getShares() != 0.0)
                    value = String.format("%,.4f", t.getSharePrice());
                else
                    value = new String();
                break;

            case 6:                             // Commission
                double commission = t.getCommission();
                if (commission != 0.0)
                    value = String.format("%,.2f", commission);
                else
                    value = new String();
                break;

            case 7:                             // Amount
                value = String.format("%,.2f", Math.abs(t.getAmount()));
                break;

            default:
                throw new IndexOutOfBoundsException("Report column "+column+" is not valid");
        }

        return value;
    }
}