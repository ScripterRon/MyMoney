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
    
/**
 * Capital gain record
 * 
 * There will be a separate record for each security lot that is sold as part
 * of an investment sale
 */
public class CapitalGainRecord {

    /** Security */
    private SecurityRecord security;
    
    /** Date acquired */
    private Date purchaseDate;

    /** Cost basis */
    private double costBasis;

    /** Number of shares */
    private double shares;

    /** Date sold */
    private Date sellDate;

    /** Sell amount */
    private double sellAmount;

    /**
     * Create a new capital gain
     * 
     * @param       security        Security
     * @param       purchaseDate    Date purchased
     * @param       shares          Number of shares
     * @param       costBasis       Cost basis
     * @param       sellDate        Date sold
     * @param       sellAmount      Net proceeds from sale
     */
    public CapitalGainRecord(SecurityRecord security,
                             Date purchaseDate, double shares, double costBasis,
                             Date sellDate, double sellAmount) {
        this.security = security;
        this.purchaseDate = purchaseDate;
        this.shares = shares;
        this.costBasis = (double)Math.round(costBasis*100.0)/100.0;
        this.sellDate = sellDate;
        this.sellAmount = (double)Math.round(sellAmount*100.0)/100.0;
        
        if (Math.abs(this.costBasis) < 0.005)
            this.costBasis = 0.0;
        
        if (Math.abs(this.sellAmount) < 0.005)
            this.sellAmount = 0.0;
    }
    
    /**
     * Get the security
     * 
     * @return                      Security
     */
    public SecurityRecord getSecurity() {
        return security;
    }
    
    /**
     * Get the date purchased
     * 
     * @return                      Date purchased
     */
    public Date getPurchaseDate() {
        return purchaseDate;
    }
    
    /**
     * Get the date sold
     * 
     * @return                      Date sold
     */
    public Date getSellDate() {
        return sellDate;
    }
    
    /**
     * Get the number of shares
     * 
     * @return                      Number of shares
     */
    public double getShares() {
        return shares;
    }
    
    /**
     * Get the cost basis
     * 
     * @return                      Cost basis
     */
    public double getCostBasis() {
        return costBasis;
    }
    
    /**
     * Get the sale amount
     * 
     * @return                      Sale amount
     */
    public double getSellAmount() {
        return sellAmount;
    }
}
