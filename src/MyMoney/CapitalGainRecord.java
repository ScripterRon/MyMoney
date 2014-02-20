package MyMoney;

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
