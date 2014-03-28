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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Bond Amortization table model
 */
public final class AmortizationTableModel extends AbstractTableModel {

    /** Column names */
    private String[] columnNames;

    /** Column classes */
    private Class<?>[] columnClasses;

    /** List data */
    private List<AmortizationElement> listData;

    /**
     * Create the bond amortization table model
     *
     * @param       columnNames         The table column names
     * @param       columnClasses       The table column classes
     * @param       faceValue           The bond face value
     * @param       purchaseCost        The purchase cost
     * @param       couponYield         The coupon yield (%)
     * @param       purchaseDate        The purchase date
     * @param       maturityDate        The maturity date
     * @param       accruedInterest     The accrued interest
     * @param       yieldToMaturity     The yield to maturity (%)
     * @param       paymentInterval     The number of months in a payment interval
     */    
    public AmortizationTableModel(String[] columnNames, Class<?>[] columnClasses,
                    double faceValue, double purchaseCost, double couponYield, 
                    Date purchaseDate, Date maturityDate,
                    double accruedInterest, double yieldToMaturity, int paymentInterval) {
        if (columnNames == null)
            throw new NullPointerException("No column names provided");
        if (columnClasses == null)
            throw new NullPointerException("No column classes provided");
        if (columnNames.length != columnClasses.length)
            throw new IllegalArgumentException("Number of names not same as number of classes");

        this.columnNames = columnNames;
        this.columnClasses = columnClasses;

        //
        // Get the purchase date
        //
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(purchaseDate);
        int purchaseYear = calendar.get(Calendar.YEAR);
        int purchaseMonth = calendar.get(Calendar.MONTH);
        int purchaseDay = calendar.get(Calendar.DATE);
                        
        //
        // Get the maturity date
        //
        calendar.setTime(maturityDate);
        int maturityYear = calendar.get(Calendar.YEAR);
        int maturityMonth = calendar.get(Calendar.MONTH);
        int maturityDay = calendar.get(Calendar.DATE);
                
        //
        // Calculate the interest payment for each interval
        //
        int intervals = 12/paymentInterval;
        double interestPayment = (double)Math.round(faceValue*couponYield)/100.0;
        double intervalPayment = (double)Math.round((interestPayment/intervals)*100.0)/100.0;
        double intervalYield = yieldToMaturity/intervals;

        //
        // Create the amortization element list
        //
        listData = new ArrayList<>((maturityYear-purchaseYear+1)*intervals);
        
        //
        // Get the first interest payment date in the year
        //
        int firstMonth = maturityMonth;
        while (true) {
            int testMonth = firstMonth - paymentInterval;
            if (testMonth < 0)
                break;
            
            firstMonth = testMonth;
        }
        
        //
        // Calculate the first interest payment date following the purchase date
        //
        int paymentMonth = firstMonth;
        int paymentYear = purchaseYear;
        double totalPayment = interestPayment;
        int paymentCount = 1;
        
        while (paymentMonth < purchaseMonth) {
            paymentCount++;
            paymentMonth += paymentInterval;
            totalPayment -= intervalPayment;
        }
        
        if (paymentMonth == purchaseMonth && maturityDay <= purchaseDay) {
            paymentCount++;
            paymentMonth += paymentInterval;
            totalPayment -= intervalPayment;
        }
        
        if (paymentMonth >= 12) {
            paymentCount = 1;
            totalPayment = interestPayment;
            paymentYear++;
            paymentMonth = firstMonth;
        }
        
        //
        // Build the amortization elements
        //
        double currentBasis = purchaseCost;
        boolean done = false;
        while (!done) {
            Date date;
            double interest;
            double adjustment;
            double amortizedPayment;

            //
            // Stop after processing the final interest payment
            //
            if (paymentYear >= maturityYear && paymentMonth >= maturityMonth)
                done = true;
            
            //
            // Compute the interest payment for this interval.  If the
            // yearly payment is not evenly divisible, we will make up
            // the difference in the final interval each year.
            //
            calendar.setTime(maturityDate);
            calendar.set(Calendar.YEAR, paymentYear);
            calendar.set(Calendar.MONTH, paymentMonth);
            date = calendar.getTime();
                
            if (paymentCount == intervals) {
                interest = totalPayment;
                paymentCount = 1;
                totalPayment = interestPayment;
                paymentYear++;
                paymentMonth = firstMonth;
            } else {
                interest = intervalPayment;
                paymentCount++;
                totalPayment -= intervalPayment;
                paymentMonth += paymentInterval;
            }
            
            //
            // Calculate the amortized payment based on the yield-to-maturity.
            // The interest adjustment is the difference between the amortized
            // yield and the coupon yield for the current cost basis.  For
            // the first interest payment, we will use the accrued interest to 
            // prorate the adjustment amount.
            //
            amortizedPayment = (double)Math.round(currentBasis*intervalYield)/100.0;
            adjustment = amortizedPayment-interest;
            
            if (listData.isEmpty() && accruedInterest != 0.0) {
                double ratio = 1.00-(accruedInterest/interest);
                adjustment *= ratio;
            }
            
            //
            // Adjust the current basis based on the amortization amount.
            // We will stop amortizing if we reach the face value of the bond
            // or this is the final interest payment.
            //
            currentBasis += adjustment;
            if ((adjustment > 0.0 && currentBasis > faceValue) ||
                        (adjustment < 0.0 && currentBasis < faceValue)) {
                
                adjustment -= currentBasis-faceValue;
                currentBasis = faceValue;
            } else if (done) {
                adjustment += faceValue-currentBasis;
                currentBasis = faceValue;
            }
                
            AmortizationElement element = new AmortizationElement(date, 
                            Double.valueOf(interest), 
                            Double.valueOf(adjustment),
                            Double.valueOf(currentBasis));
            listData.add(element);
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
     */
    public Object getValueAt(int row, int column) {
        if (row >= listData.size())
            throw new IndexOutOfBoundsException("Table row "+row+" is not valid");

        AmortizationElement element = listData.get(row);
        Object value;
        switch (column) {
            case 0:                             // Payment date
                value = element.getDate();
                break;

            case 1:                             // Interest payment
                value = element.getInterest();
                break;

            case 2:                             // Interest adjustment
                value = element.getAdjustment();
                break;

            case 3:                             // Updated cost basis
                value = element.getCostBasis();
                break;
                
            default:
                throw new IndexOutOfBoundsException("Table column "+column+" is not valid");
        }

        return value;
    }
    
    /**
     * Amortization element
     */
    private final class AmortizationElement {
    
        /** Payment date */
        private Date date;
        
        /** Interest payment */
        private Double interest;
        
        /** Interest adjustment */
        private Double adjustment;
        
        /** Updated cost basis */
        private Double costBasis;
        
        /**
         * Create the amortization element
         * 
         * @param   date              Payment date
         * @param   interest          Interest payment
         * @param   adjustment        Interest adjustment
         * @param   costBasis         Updated cost basis
         */
        private AmortizationElement(Date date, Double interest, Double adjustment,
                            Double costBasis) {
            this.date = date;
            this.interest = interest;
            this.adjustment = adjustment;
            this.costBasis = costBasis;
        }
        
        /**
         * Get the payment date
         */
        private Date getDate() {
            return date;
        }
        
        /**
         *  Get the interest payment
         */
        private Double getInterest() {
            return interest;
        }
        
        /**
         *  Get the interest adjustment
         */         
        private Double getAdjustment() {
            return adjustment;
        }
        
        /**
         *  Get the updated cost basis
         */
        private Double getCostBasis() {
            return costBasis;
        }
    }
}
