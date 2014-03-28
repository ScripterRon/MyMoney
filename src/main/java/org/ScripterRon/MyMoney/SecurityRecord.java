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
import org.ScripterRon.Asn1.*;

import java.util.*;

/**
 * Database security record.
 * <p>
 * The following security types are supported:
 * <ul>
 * <li>STOCK
 * <li>MUNICIPAL_BOND
 * <li>CORPORATE_BOND
 * <li>TREASURY_NOTE
 * <li>CD
 * <li>STOCK_MUTUAL_FUND
 * <li>BOND_MUTUAL_FUND
 * </ul>
 * <p>
 * The following payment types are supported:
 * <ul>
 * <li>NONE
 * <li>MONTHLY
 * <li>QUARTERLY
 * <li>SEMI_ANNUAL
 * <li>ANNUAL
 * </ul>
 * <p>
 * All security records are contained in the <code>securities</code> sorted
 * set.  The entries in the set are sorted by the security name. Two security
 * records are equal if they have the same security name.
 * <p>
 * The security record is encoded as follows:
 * <pre>
 *   SecurityRecord ::= [APPLICATION 1] SEQUENCE {
 *     recordID                    INTEGER,
 *     securityName                BMPSTRING,
 *     securityType                INTEGER,
 *     securityHidden              BOOLEAN,
 *     tickerSymbol                BMPSTRING,
 *     priceHistory            [0] SEQUENCE OF PriceHistory OPTIONAL,
 *     paymentType             [1] INTEGER OPTIONAL }
 * </pre>
 */
public final class SecurityRecord extends DBElement {

    /** Stock */
    public static final int STOCK = 1;

    /** Municipal bond */
    public static final int MUNICIPAL_BOND = 2;

    /** Corporate bond */
    public static final int CORPORATE_BOND = 3;

    /** Treasury note */
    public static final int TREASURY_NOTE = 4;

    /** Certificate of deposit */
    public static final int CD = 5;

    /** Stock mutual fund */
    public static final int STOCK_MUTUAL_FUND = 6;
    
    /** Bond mutual fund */
    public static final int BOND_MUTUAL_FUND = 7;

    /** The security types */
    private static final int[] securityTypes = {
            STOCK, MUNICIPAL_BOND, CORPORATE_BOND, TREASURY_NOTE, CD, STOCK_MUTUAL_FUND, BOND_MUTUAL_FUND};
    
    /** The security type strings */
    private static final String[] securityTypeStrings = {
            "Stock", "Municipal bond", "Corporate bond", "Treasury note", "CD", "Stock mutual fund", "Bond mutual fund"};

    /** No income payments */
    public static final int NO_PAYMENTS = 0;
    
    /** Monthly payments */
    public static final int MONTHLY_PAYMENTS = 1;
    
    /** Quarterly payments */
    public static final int QUARTERLY_PAYMENTS = 2;
    
    /** Semi-annual payments */
    public static final int SEMI_ANNUAL_PAYMENTS = 3;
    
    /** Annual payments */
    public static final int ANNUAL_PAYMENTS = 4;
    
    /** The payment types */
    private static final int[] paymentTypes = {
        NO_PAYMENTS, MONTHLY_PAYMENTS, QUARTERLY_PAYMENTS, SEMI_ANNUAL_PAYMENTS, ANNUAL_PAYMENTS};
    
    /** The payment type strings */
    private static final String[] paymentTypeStrings = {
        "None", "Monthly", "Quarterly", "Semi-Annual", "Annual"};
    
    /** The set of defined securities */
    public static SortedSet<SecurityRecord> securities;
    
    /** The security map */
    private static Map<Integer, SecurityRecord> map;

    /** The next SecurityRecord identifier */
    private static int nextRecordID = 1;

    /** The encoded SecurityRecord ASN.1 tag identifier */
    private static final byte tagID = (byte)(Asn1Stream.ASN1_APPLICATION+1);

    /** Payment type */
    private int paymentType = NO_PAYMENTS;

    /** Ticker symbol */
    private String tickerSymbol;

    /** Price history */
    SortedSet<PriceHistory> priceHistory;

    /**
     * Create a new security record
     *
     * @param       name            Security name
     * @param       type            Security type
     */
    public SecurityRecord(String name, int type) {
        if (name == null)
            throw new NullPointerException("No security name supplied");

        //
        // Create the new security
        //
        recordID = nextRecordID++;
        elementName = name;
        setType(type);
        tickerSymbol = new String();
        priceHistory = new TreeSet<>();
        
        //
        // Add the security to the map
        //
        if (map == null)
            map = new HashMap<>();
        
        map.put(new Integer(recordID), this);
    }

    /**
     * Create a security record from an encoded byte stream
     *
     * @param       data            Encoded byte stream for the record
     * @exception   DBException     Unable to decode object stream
     */
    public SecurityRecord(byte[] data) throws DBException {
        if (data == null)
            throw new NullPointerException("No encoded data supplied");

        priceHistory = new TreeSet<>();
        DecodeStream stream = new DecodeStream(data);

        try {

            //
            //  Validate the application identifier tag
            //
            if (stream.getTag() != tagID)
                throw new DBException("Not an encoded SecurityRecord object");

            //
            //  Get the SecurityRecord sequence
            //
            DecodeStream seq = stream.getSequence(true);

            //
            //  Decode the record identifier, security name, security type,
            //  security hidden state and ticker symbol
            //
            recordID = seq.decodeInteger(false);
            elementName = seq.decodeString(false);
            elementType = seq.decodeInteger(false);
            elementHidden = seq.decodeBoolean(false);
            tickerSymbol = seq.decodeString(false);

            //
            //  The price history is encoded as an optional context-specific
            //  field with identifier 0
            //
            if (seq.getLength() != 0 && seq.getTag() == (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+0)) {
                DecodeStream priceSeq = seq.getSequence(true);
                while (priceSeq.getLength() != 0)
                    priceHistory.add(new PriceHistory(priceSeq));
            }
            
            //
            // The payment type is encoded as an optional context-specific
            // field with identifier 1
            //
            if (seq.getLength() != 0 && seq.getTag() == (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+1))
                paymentType = seq.decodeInteger(true);

            //
            //  Check for unconsummed data
            //
            if (seq.getLength() != 0)
                throw new DBException("Unconsummed data in ServiceRecord sequence");

            //
            //  Update the next record identifier
            //
            if (recordID >= nextRecordID)
                nextRecordID = recordID+1;
            
            //
            // Add the security to the map
            //
            if (map == null)
                map = new HashMap<>();
        
            map.put(new Integer(recordID), this);
        } catch (Asn1Exception exc) {
            throw new DBException("ASN.1 decode error", exc);
        }
    }

    /**
     * Test if the supplied byte stream represents an encoded SecurityRecord object
     *
     * @param       data            The encoded byte stream
     * @return                      TRUE if SecurityRecord object
     */
    public static boolean isEncodedStream(byte[] data) {
        if (data == null)
            throw new NullPointerException("Null byte stream reference");

        if (data.length == 0)
            return false;

        return (data[0]==(byte)(tagID|Asn1Stream.ASN1_CONSTRUCTED) ? true : false);
    }

    /**
     * Encode the SecurityRecord object
     *
     * @return                      The encoded byte stream for the object
     */
    public byte[] encode() {
        int seqLength = 0;
        int priceCount = priceHistory.size();
        EncodeStream stream = new EncodeStream(128+priceCount*32);
        
        //
        // Encode the payment type as an optional context-specific sequence
        // with identifier 1.
        //
        if (paymentType != NO_PAYMENTS)
            seqLength += stream.encodeInteger(paymentType, (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+1));

        //
        //  Encode the price history set as an optional context-specific sequence
        //  with identifier 0.  We don't need to preserve the set order in the
        //  encoded stream because the elements will be sorted correctly when
        //  the stream is decoded and the elements are added to the set.
        //
        if (priceCount != 0) {
            int length = 0;

            for (PriceHistory ph : priceHistory)
                length += ph.encode(stream);

            seqLength += stream.makeSequence(length, (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+0));
        }

        //
        //  Encode the ticker symbol, security hidden state, security type,
        //  security name and record identifier
        //
        seqLength += stream.encodeString(tickerSymbol);
        seqLength += stream.encodeBoolean(elementHidden);
        seqLength += stream.encodeInteger(elementType);
        seqLength += stream.encodeString(elementName);
        seqLength += stream.encodeInteger(recordID);

        //
        //  Make the SecurityRecord sequence
        //
        stream.makeSequence(seqLength, tagID);
        return stream.getData();
    }
    
    /**
     * Get the security associated with the supplied record identifier.
     *
     * @param       recordID        The record identifier
     * @return                      The security or NULL
     */
    public static SecurityRecord getSecurity(int recordID) {
        SecurityRecord security = null;
        if (map != null)
            security = map.get(new Integer(recordID));
        
        return security;
    }

    /**
     * Get the ticker symbol for the security
     *
     * @return                      The ticker symbol
     */
    public String getSymbol() {
        return tickerSymbol;
    }

    /**
     * Set the ticker symbol for the security
     *
     * @param       symbol          The ticker symbol
     */
    public void setSymbol(String symbol) {
        if (symbol == null)
            throw new NullPointerException("No ticker symbol supplied");

        tickerSymbol = symbol;
    }

    /**
     * Get the current price for the security
     *
     * @return                      The current price
     */
    public double getPrice() {
        return (priceHistory.size()!=0 ? priceHistory.last().getPrice() : 0.00);
    }

    /**
     * Set the current price for the security
     *
     * The price will be added to the price history using the current date.  An
     * existing price history for the date will be updated with the new price.
     *
     * @param       price           The security price
     */
    public void setPrice(double price) {
        PriceHistory ph = new PriceHistory(price);
        priceHistory.remove(ph);
        priceHistory.add(ph);
    }

    /**
     * Get the price history for this security
     *
     * @return                                  Security price history set
     */
    public SortedSet<PriceHistory> getPriceHistory() {
        return priceHistory;
    }

    /**
     * Add a price to the price history for this security.  An existing
     * price history entry for the date will be updated with the new price.
     *
     * @param       date            Date
     * @param       price           Price
     */
    public void addPrice(Date date, double price) {
        PriceHistory ph = new PriceHistory(date, price);
        priceHistory.remove(ph);
        priceHistory.add(ph);
    }

    /**
     * Remove a price from the price history for this security
     *
     * @param       date            Date
     */
    public void removePrice(Date date) {
        PriceHistory ph = new PriceHistory(date, 0.0);
        priceHistory.remove(ph);
    }

    /**
     * Get the security type
     *
     * @return                      The type for this security
     */
    public int getType() {
        return elementType;
    }

    /**
     * Set the security type
     *
     * @param       type            The security type
     */
    public void setType(int type) {
        boolean validType = false;

        for (int i=0; i<securityTypes.length; i++) {
            if (type == securityTypes[i]) {
                validType = true;
                break;
            }
        }

        if (!validType)
            throw new IllegalArgumentException("Security type "+type+" is invalid");

        elementType = type;
    }

    /**
     * Get the known security types
     *
     * @return                      Array of security types
     */
    public static int[] getTypes() {
        return securityTypes;
    }
    
    /**
     * Get the security type strings
     *
     * @return                      Array of security type strings
     */
    public static String[] getTypeStrings() {
        return securityTypeStrings;
    }

    /**
     * Get the displayable string for a security type
     *
     * @param       type            The security type
     * @return                      Displayable string
     */
    public static String getTypeString(int type) {
        String retValue = null;
        for (int index=0; index<securityTypes.length; index++) {
            if (securityTypes[index] == type) {
                retValue = securityTypeStrings[index];
                break;
            }
        }
        
        if (retValue == null)
            retValue = new String();

        return retValue;
    }

    /**
     * Get the payment type
     *
     * @return                      The payment type for this security
     */
    public int getPaymentType() {
        return paymentType;
    }

    /**
     * Set the payment type
     *
     * @param       type            The payment type for this security
     */
    public void setPaymentType(int type) {
        boolean validType = false;

        for (int i=0; i<paymentTypes.length; i++) {
            if (type == paymentTypes[i]) {
                validType = true;
                break;
            }
        }

        if (!validType)
            throw new IllegalArgumentException("Payment type "+type+" is invalid");

        paymentType = type;
    }

    /**
     * Get the known payment types
     *
     * @return                      Array of payment types
     */
    public static int[] getPaymentTypes() {
        return paymentTypes;
    }
    
    /**
     * Get the payment type strings
     *
     * @return                      Array of payment type strings
     */
    public static String[] getPaymentTypeStrings() {
        return paymentTypeStrings;
    }

    /**
     * Get the displayable string for a payment type
     *
     * @param       type            The payment type
     * @return                      Displayable string
     */
    public static String getPaymentTypeString(int type) {
        String retValue = null;
        for (int index=0; index<paymentTypes.length; index++) {
            if (paymentTypes[index] == type) {
                retValue = paymentTypeStrings[index];
                break;
            }
        }
        
        if (retValue == null)
            retValue = new String();

        return retValue;
    }    
}
