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
 * Database category record
 * <p>
 * The following category types are provided:
 * <ul>
 * <li>ACCRUED_INTEREST
 * <li>LONG_TERM_CAPITAL_GAIN
 * <li>SHORT_TERM_CAPITAL_GAIN
 * <li>CHARITY
 * <li>DIVIDEND
 * <li>ESTIMATED_FEDERAL_TAX
 * <li>ESTIMATED_STATE_TAX
 * <li>EXPENSE
 * <li>FEDERAL_TAX
 * <li>FEDERAL_TAX_EXEMPT_INTEREST
 * <li>INCOME
 * <li>INTEREST_EXPENSE
 * <li>MEDICAL
 * <li>MORTGAGE_INTEREST
 * <li>PENSION
 * <li>PROPERTY_TAX
 * <li>STATE_TAX
 * <li>STATE_TAX_EXEMPT_INTEREST
 * <li>TAX_EXEMPT_INTEREST
 * <li>TAXABLE_INTEREST
 * <li>WAGES
 * </ul>
 * <p>
 * All category records are contained in the <code>categories</code> sorted
 * set.  The entries in the set are sorted by the category name. Two category
 * records are equal if they have the same category name.
 * <p>
 * The category record is encoded as follows:
 * <pre>
 *   CategoryRecord ::= [APPLICATION 3] SEQUENCE {
 *     recordID                    INTEGER,
 *     categoryName                BMPSTRING,
 *     categoryType                INTEGER,
 *     categoryHidden          [0] BOOLEAN OPTIONAL }
 * </pre>
 */
public final class CategoryRecord extends DBElement {

    /** General income */
    public static final int INCOME = 1;

    /** General expense */
    public static final int EXPENSE = 2;

    /** Wages/Salary/Tips/Pension */
    public static final int WAGES = 3;

    /** Long-term capital gain/loss */
    public static final int LONG_TERM_CAPITAL_GAIN = 4;

    /** Dividend */
    public static final int DIVIDEND = 5;

    /** Accrued interest */
    public static final int ACCRUED_INTEREST = 6;

    /** Charitable contribution */
    public static final int CHARITY = 7;

    /** Mortgage interest */
    public static final int MORTGAGE_INTEREST = 8;

    /** Property tax payment */
    public static final int PROPERTY_TAX = 9;

    /** Federal tax payment */
    public static final int FEDERAL_TAX = 10;

    /** State tax payment */
    public static final int STATE_TAX = 11;

    /** Taxable interest */
    public static final int TAXABLE_INTEREST = 12;

    /** Federal tax-exempt interest */
    public static final int FEDERAL_TAX_EXEMPT_INTEREST = 13;

    /** State tax-exempt interest */
    public static final int STATE_TAX_EXEMPT_INTEREST = 14;

    /** Tax-exempt interest */
    public static final int TAX_EXEMPT_INTEREST = 15;

    /** Interest expense */
    public static final int INTEREST_EXPENSE = 16;

    /** Medical expense */
    public static final int MEDICAL = 17;

    /** Short-term capital gain/loss */
    public static final int SHORT_TERM_CAPITAL_GAIN = 18;

    /** Estimated federal tax */
    public static final int ESTIMATED_FEDERAL_TAX = 19;

    /** Estimated state tax */
    public static final int ESTIMATED_STATE_TAX = 20;

    /** Pension */
    public static final int PENSION = 21;

    /** Category types */
    private static final int[] categoryTypes = {
        INCOME, EXPENSE, WAGES, PENSION, LONG_TERM_CAPITAL_GAIN, SHORT_TERM_CAPITAL_GAIN, 
        DIVIDEND, ACCRUED_INTEREST, CHARITY, MORTGAGE_INTEREST, PROPERTY_TAX, 
        FEDERAL_TAX, STATE_TAX, ESTIMATED_FEDERAL_TAX, ESTIMATED_STATE_TAX,
        TAXABLE_INTEREST, FEDERAL_TAX_EXEMPT_INTEREST, STATE_TAX_EXEMPT_INTEREST,
        TAX_EXEMPT_INTEREST, INTEREST_EXPENSE, MEDICAL};
    
    /** Category type strings */
    private static final String[] categoryTypeStrings = {
        "Income", "Expense", "Wages", "Pension", "Long-term Capital Gain", "Short-term Capital Gain",
        "Dividend", "Accrued Interest", "Charity", "Mortgage Interest", "Property Tax",
        "Federal Tax", "State Tax", "Estimated Federal Tax", "Estimated State Tax",
        "Taxable Interest", "Federal Tax-exempt Interest", "State Tax-exempt Interest",
        "Tax-exempt Interest", "Interest Expense", "Medical"};

    /** The set of defined categories */
    public static SortedSet<CategoryRecord> categories;
    
    /** The category map */
    private static Map<Integer, CategoryRecord> map;

    /** The next CategoryRecord identifier */
    private static int nextRecordID = 1;

    /** The encoded CategoryRecord ASN.1 tag identifier */
    private static final byte tagID = (byte)(Asn1Stream.ASN1_APPLICATION+3);

    /**
     * Current category balance.  This is an application work area and is
     * not preserved across application restarts.
     */
    public double balance;

    /**
     * Create a new category record
     *
     * @param       name            Category name
     * @param       type            Category type
     */
    public CategoryRecord(String name, int type) {
        if (name == null)
            throw new NullPointerException("No category name supplied");

        //
        // Create the new category
        //
        recordID = nextRecordID++;
        elementName = name;
        setType(type);
        
        //
        // Add the category to the map
        //
        if (map == null)
            map = new HashMap<>();
        
        map.put(new Integer(recordID), this);
    }

    /**
     * Create a category record from an encoded byte stream
     *
     * @param       data            Encoded byte stream for the record
     * @exception   DBException     Unable to decode object stream
     */
    public CategoryRecord(byte[] data) throws DBException {
        if (data == null)
            throw new NullPointerException("No encoded data supplied");

        DecodeStream stream = new DecodeStream(data);

        try {

            //
            //  Validate the application identifier tag
            //
            if (stream.getTag() != tagID)
                throw new DBException("Not an encoded CategoryRecord object");

            //
            //  Get the CategoryRecord sequence
            //
            DecodeStream seq = stream.getSequence(true);

            //
            //  Decode the record identifier, category name and category type
            //
            recordID = seq.decodeInteger(false);
            elementName = seq.decodeString(false);
            elementType = seq.decodeInteger(false);
            
            //
            //  The category hidden state is encoded as an optional context-specific
            //  field with identifier 0
            //
            if (seq.getLength() != 0 && seq.getTag() == (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+0))
                elementHidden = seq.decodeBoolean(true);
            
            //
            //  Check for unconsummed data
            //
            if (seq.getLength() != 0)
                throw new DBException("Unconsummed data in CategoryRecord sequence");

            //
            //  Update the next record identifier
            //
            if (recordID >= nextRecordID)
                nextRecordID = recordID+1;
        
            //
            // Add the category to the map
            //
            if (map == null)
                map = new HashMap<>();
        
            map.put(new Integer(recordID), this);
        } catch (Asn1Exception exc) {
            throw new DBException("ASN.1 decode error", exc);
        }
    }

    /**
     * Test if the supplied byte stream represents an encoded CategoryRecord object
     *
     * @param       data            The encoded byte stream
     * @return                      TRUE if CategoryRecord object
     */
    public static boolean isEncodedStream(byte[] data) {
        if (data == null)
            throw new NullPointerException("Null byte stream reference");

        if (data.length == 0)
            return false;

        return (data[0]==(byte)(tagID|Asn1Stream.ASN1_CONSTRUCTED) ? true : false);
    }

    /**
     * Encode the CategoryRecord object
     *
     * @return                      The encoded byte stream for the object
     */
    public byte[] encode() {
        int seqLength = 0;
        EncodeStream stream = new EncodeStream(128);
        
        //
        //  The category hidden state is encoded as an optional context-specific
        //  field with identifier 0
        //
        if (elementHidden)
            seqLength += stream.encodeBoolean(elementHidden, (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+0));
        
        //
        //  Encode the category type, category name and record identifier
        //
        seqLength += stream.encodeInteger(elementType);
        seqLength += stream.encodeString(elementName);
        seqLength += stream.encodeInteger(recordID);

        //
        //  Make the CategoryRecord sequence
        //
        stream.makeSequence(seqLength, tagID);
        return stream.getData();
    }
    
    /**
     * Get the category associated with the supplied record identifier.
     *
     * @param       recordID        The record identifier
     * @return                      The category or NULL
     */
    public static CategoryRecord getCategory(int recordID) {
        CategoryRecord category = null;
        if (map != null)
            category = map.get(new Integer(recordID));
        
        return category;
    }

    /**
     * Validate and set the category type
     *
     * @param       type            The category type
     */
    public void setType(int type) {
        boolean validType = false;
        for (int i=0; i<categoryTypes.length; i++) {
            if (type == categoryTypes[i]) {
                validType = true;
                break;
            }
        }

        if (!validType)
            throw new IllegalArgumentException("Category type "+type+" is invalid");

        elementType = type;
    }

    /**
     * Get the known category types
     *
     * @return                      Array of category types
     */
    public static int[] getTypes() {
        return categoryTypes;
    }
    
    /**
     * Get the category type strings
     *
     * @return                      Array of category type strings
     */
    public static String[] getTypeStrings() {
        return categoryTypeStrings;
    }

    /**
     * Get the displayable string for a category type
     *
     * @param       type            The category type
     * @return                      Displayable string
     *
     * The return value will be null if the category type is unknown
     */
    public static String getTypeString(int type) {
        String retValue = null;
        for (int index=0; index<categoryTypes.length; index++) {
            if (categoryTypes[index] == type) {
                retValue = categoryTypeStrings[index];
                break;
            }
        }
        
        if (retValue == null)
            retValue = new String();

        return retValue;
    }
}
