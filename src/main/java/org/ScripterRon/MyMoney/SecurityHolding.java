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

/**
 *  A security holding represents a position in an investment account
 *  and includes one or more security purchases.
 */
public class SecurityHolding {

    /** Account containing the security */
    private AccountRecord account;
    
    /** Security */
    private SecurityRecord security;

    /** Total number of shares */
    private double totalShares;

    /** Annual yield */
    private double annualYield;

    /** Total cost */
    private double totalCost;

    /** Security lots */
    private List<SecurityLot> lots;
    
    /** Capital gains */
    private List<CapitalGainRecord> gains;

    /**
     * Create a new security holding
     *
     * @param       account         Account containing the security
     * @param       security        Security record
     */
    public SecurityHolding(AccountRecord account, SecurityRecord security) {
        this.account = account;
        this.security = security;
        lots = new ArrayList<>(5);
        gains = new ArrayList<>(5);
    }
    
    /**
     * Get the account containing the security
     * 
     * @return                      Account record
     */
    public AccountRecord getAccount() {
        return account;
    }
    
    /**
     * Get the security
     * 
     * @return                      Security record
     */
    public SecurityRecord getSecurity() {
        return security;
    }
    
    /**
     * Get the earliest purchase date
     * 
     * @return                      First purchase date or null
     */
    public Date getPurchaseDate() {
        return (lots.isEmpty() ? null : lots.get(0).date);
    }
    
    /**
     * Get the total number of shares with 4 decimal places
     * 
     * @return                      Total number of shares
     */
    public double getTotalShares() {
        return (double)Math.round(totalShares*10000.0)/10000.0;
    }
    
    /**
     * Get the total cost with 2 decimal places
     * 
     * @return                      Total cost
     */
    public double getTotalCost() {
        return (double)Math.round(totalCost*100.0)/100.0;
    }
    
    /**
     * Get the annual yield based on the most recent income payment and
     * the payment frequency associated with the security.  The yield for
     * an INCOME transaction is the recent income multiplied by the number
     * of payments in a year.  The yield for a REINVEST transaction is
     * the compounded yield for the year.
     * 
     * @return                      Annual yield
     */
    public double getAnnualYield() {
        return annualYield;
    }
    
    /**
     * Get the list of capital gains
     * 
     * @return                      List of capital gains
     */
    public List<CapitalGainRecord> getCapitalGains() {
        return gains;
    }
    
    /**
     * Locate and update the security holding for the supplied transaction.
     * A new security holding will be created if necessary.  Entries in the
     * security holdings list are sorted by the security name.  Multiple list
     * entries for the same security will occur if the security is held in
     * multiple accounts.
     * 
     * @param       holdings            Sorted list of security holdings
     * @param       t                   Transaction to be processed
     */
    public static void updateSecurityHolding(List<SecurityHolding> holdings,
                                             TransactionRecord t) {
        AccountRecord account = t.getAccount();
        SecurityRecord security = t.getSecurity();
        String name = security.getName();
        int action = t.getAction();
        boolean addSecurity = true;
        int index = 0;

        //
        // Scan the existing security holdings
        //
        for (SecurityHolding h : holdings) {
            
            //
            // Process the transaction if we found a matching security holding
            //
            if (h.getSecurity() == security && h.getAccount() == account) {
                h.processTransaction(t);
                addSecurity = false;
                break;
            }

            //
            // Create a new security holding if the name of the current
            // security name is greater than the transaction security name
            //
            if (name.compareTo(h.getSecurity().getName()) < 0) {
                if (action == TransactionRecord.BUY) {
                    SecurityHolding n = new SecurityHolding(account, security);
                    n.processTransaction(t);
                    holdings.add(index, n);
                } else {
                    JOptionPane.showMessageDialog(Main.mainWindow, 
                                "Missing BUY transaction for security "+name,
                                "Warning", JOptionPane.WARNING_MESSAGE);
                }

                addSecurity = false;
                break;
            }

            index++;
        }

        //
        // Create a new security holding if we didn't find a match
        //
        if (addSecurity) {
            if (action == TransactionRecord.BUY) {
                SecurityHolding n = new SecurityHolding(account, security);
                n.processTransaction(t);
                holdings.add(n);
            } else {
                JOptionPane.showMessageDialog(Main.mainWindow, 
                                "Missing BUY transaction for security "+name,
                                "Warning", JOptionPane.WARNING_MESSAGE);
            }
        }

        //
        // Update the target security position for EXCHANGE and SPIN_OFF transactions
        //
        if (action == TransactionRecord.EXCHANGE || action == TransactionRecord.SPIN_OFF) {
            SecurityRecord newSecurity = t.getNewSecurity();
            addSecurity = true;
            index = 0;

            //
            // Scan the existing security holdings
            //
            for (SecurityHolding h : holdings) {
                
                //
                // Process the transaction if we found a match
                //
                if (h.getSecurity() == newSecurity && h.getAccount() == account) {
                    h.processTransaction(t);
                    addSecurity = false;
                    break;
                }

                //
                // Create a new security holding if the current security name
                // is greater than the transaction security name
                //
                if (newSecurity.getName().compareTo(h.getSecurity().getName()) < 0) {
                    SecurityHolding n = new SecurityHolding(account, newSecurity);
                    n.processTransaction(t);
                    holdings.add(index, n);
                    addSecurity = false;
                    break;
                }

                index++;
            }

            //
            // Create a new security holding if we didn't find a match
            //
            if (addSecurity) {
                SecurityHolding n = new SecurityHolding(account, newSecurity);
                n.processTransaction(t);
                holdings.add(n);
            }
        }
    }
    
    /**
     * Process an investment transaction for this security
     * 
     * @param                       Investment transaction
     */
    private void processTransaction(TransactionRecord t) {
        Date date = t.getDate();
        int action = t.getAction();
        SecurityRecord baseSecurity = t.getSecurity();
        double shares = t.getShares();
        double newShares = t.getNewShares();
        double amount = t.getAmount();
        double payments;

        //
        // Update the security holding
        //
        // Note that the transaction amount is negative for SELL, INCOME,
        // RETURN_OF_CAPITAL and ACCRETION transactions
        //
        switch (action) {
            case TransactionRecord.BUY:
                addPosition(date, shares, amount);
                break;
                
            case TransactionRecord.REINVEST:
                switch (security.getPaymentType()) {
                    case SecurityRecord.MONTHLY_PAYMENTS:
                        payments = 12.0;
                        break;

                    case SecurityRecord.QUARTERLY_PAYMENTS:
                        payments = 4.0;
                        break;

                    case SecurityRecord.SEMI_ANNUAL_PAYMENTS:
                        payments = 2.0;
                        break;

                    case SecurityRecord.ANNUAL_PAYMENTS:
                        payments = 1.0;
                        break;

                    default:
                        payments = 0.0;
                }

                if (amount != 0.0 && payments != 0.0) {
                    double value = totalShares*security.getPrice();
                    if (value > 0.0)
                        annualYield = Math.pow(1.0+amount/value, payments)-1.0;
                    else
                        annualYield = 0.0;
                } else {
                    annualYield = 0.0;
                }
                
                addPosition(date, shares, amount);
                break;

            case TransactionRecord.SELL:
                removePosition(date, shares, -amount, t.getAccountingMethod());
                break;
            
            case TransactionRecord.EXCHANGE:
                if (baseSecurity == security) {
                    removePosition(date, shares, amount, TransactionRecord.FIFO);
                } else {
                    addPosition(date, newShares, amount);
                }
                break;

            case TransactionRecord.SPIN_OFF:
                if (baseSecurity == security) {
                    returnOfCapital(amount);
                } else {
                    addPosition(date, newShares, amount);
                }
                break;
                
            case TransactionRecord.ACCRETION:
                double shareAmount = -amount/totalShares;
                totalCost = 0.0;
                for (SecurityLot lot : lots) {
                    lot.cost += shareAmount*lot.shares;
                    totalCost += lot.cost;
                }
                break;
                
            case TransactionRecord.AMORTIZATION:
                returnOfCapital(amount);
                break;
                            
            case TransactionRecord.INCOME:
                amount = -amount;
                switch (security.getPaymentType()) {
                    case SecurityRecord.MONTHLY_PAYMENTS:
                        payments = 12.0;
                        break;

                    case SecurityRecord.QUARTERLY_PAYMENTS:
                        payments = 4.0;
                        break;

                    case SecurityRecord.SEMI_ANNUAL_PAYMENTS:
                        payments = 2.0;
                        break;

                    case SecurityRecord.ANNUAL_PAYMENTS:
                        payments = 1.0;
                        break;

                    default:
                        payments = 0.0;
                }

                if (amount != 0.0 && payments != 0.0) {
                    double value = totalShares*security.getPrice();
                    if (value > 0.0)
                        annualYield = (amount*payments)/value;
                    else
                        annualYield = 0.0;
                } else {
                    annualYield = 0.0;
                }
                break;
                
            case TransactionRecord.RETURN_OF_CAPITAL:
                returnOfCapital(-amount);
                break;
                
            case TransactionRecord.SPLIT:
                double ratio = (totalShares+shares)/totalShares;
                totalShares = 0.0;
                for (SecurityLot lot : lots) {
                    lot.shares = (double)Math.round(lot.shares*ratio*10000.0)/10000.0;
                    totalShares += lot.shares;
                }
                break;
        }
    }

    /**
     * Add a security position
     * 
     * @param       date            Date purchased
     * @param       shares          Number of shares
     * @param       cost            Cost of shares
     */
    private void addPosition(Date date, double shares, double cost) {
        lots.add(new SecurityLot(date, shares, cost));
        totalShares += shares;
        totalCost += cost;
    }

    /**
     * Remove a security position
     * 
     * @param       date            Sale date
     * @param       shares          Number of shares to remove
     * @param       amount          Sale amount
     * @param       method          Accounting method
     */
    private void removePosition(Date date, double shares, double amount, int method) {
        SecurityLot lot;
        CapitalGainRecord gain;
        double residualCost, residualShares;
        int index;
        
        //
        // Nothing to do if there are no security lots
        //
        if (lots.isEmpty())
            return;
        
        //
        // Get the number of shares to remove
        //
        residualShares = Math.min(shares, totalShares);
        double price = amount/residualShares;

        //
        // Remove a partial position
        //
        // For the FIFO and LIFO accounting methods, we will remove shares from
        // successive lots until the requested number of shares have been removed.
        //
        // For the average cost accounting method, we will combine all of the lots
        // into a single lot and then remove the requested number of shares.
        //
        if (method == TransactionRecord.FIFO || method == TransactionRecord.LIFO) {
            if (method == TransactionRecord.FIFO)
                index = 0;
            else
                index = lots.size()-1;

            //
            // Process successive lots until all shares have been removed
            //
            while (residualShares > 0.0 && !lots.isEmpty()) {
                lot = lots.get(index);
                if (lot.shares > residualShares) {
                    residualCost = (double)Math.round((lot.cost/lot.shares)*residualShares*100.0)/100.0;
                    lot.shares -= residualShares;
                    lot.cost -= residualCost;
                    totalShares -= residualShares;
                    totalCost -= residualCost;
                    gain = new CapitalGainRecord(security,
                                                 lot.date, residualShares, residualCost,
                                                 date, price*residualShares);
                    gains.add(gain);
                    residualShares = 0.0;
                } else {
                    residualShares -= lot.shares;
                    totalShares -= lot.shares;
                    totalCost -= lot.cost;
                    gain = new CapitalGainRecord(security,
                                                 lot.date, lot.shares, lot.cost,
                                                 date, price*lot.shares);
                    gains.add(gain);
                    lots.remove(index);
                    if (method == TransactionRecord.LIFO)
                        index--;
                }
            }
            
        } else {
            
            //
            // Combine all security lots into a single lot
            //
            lot = lots.get(0);
            if (lots.size() > 1) {
                lots.clear();
                lots.add(lot);
            }

            //
            // Remove the requested number of shares
            //
            residualCost = (double)Math.round((totalCost/totalShares)*residualShares*100.0)/100.0;
            totalShares -= residualShares;
            totalCost -= residualCost;
            if (totalShares < 0.00005) {
                totalShares = 0.0;
                totalCost = 0.0;
                lots.clear();
            } else {
                lot.shares = totalShares;
                lot.cost = totalCost;
            }
            
            //
            // Create a capital gains record
            //
            gain = new CapitalGainRecord(security,
                                         lot.date, residualShares, residualCost,
                                         date, amount);
            gains.add(gain);
        }

        //
        // Consider everything as sold if we have less than 0.0001 shares.
        // Otherwise, truncate total shares to 4 decimal places and total
        // cost to 2 decimal places.
        //
        if (totalShares < 0.0001) {
            lots.clear();
            totalShares = 0.0;
            totalCost = 0.0;
        } else {
            totalShares = (double)Math.round(totalShares*10000.0)/10000.0;
            totalCost = (double)Math.round(totalCost*100.0)/100.0;
        }
    }
    
    /**
     * Process a return of capital
     * 
     * @param   amount              Amount returned
     */
    private void returnOfCapital(double amount) {
        double ratio = (totalCost-amount)/totalCost;
        totalCost = 0.0;
        for (SecurityLot lot : lots) {
            lot.cost = (double)Math.round(lot.cost*ratio*100.0)/100.0;
            totalCost += lot.cost;
        }
    }
        
    /**
     * Security lot
     */
    private class SecurityLot {
        
        /** Date acquired */
        public Date date;
        
        /** Number of shares */
        public double shares;
        
        /** Cost of shares */
        public double cost;
        
        /**
         * Create a new security lot
         * 
         * @param       date            Date security acquired
         * @param       shares          Number of shares
         * @param       cost            Cost of shares
         */
        public SecurityLot(Date date, double shares, double cost) {
            this.date = date;
            this.shares = shares;
            this.cost = cost;
        }
    }
}
