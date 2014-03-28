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
 * The PriceHistory class defines a security price history element.  The price
 * is maintained with four decimal digits.
 * <p>
 * The price history is encoded as follows:
 * <pre>
 *   PriceHistory ::= [APPLICATION 2] SEQUENCE {
 *     date                        GENERALTIME,
 *     price                       DOUBLE,
 *     splitRatio              [0] DOUBLE OPTIONAL }
 * </pre>
 */
public final class PriceHistory implements Comparable<PriceHistory> {

    /** The encoded PriceHistory ASN.1 tag identifier */
    private static final byte tagID=(byte)(Asn1Stream.ASN1_APPLICATION+2);

    /** History date */
    private Date date;

    /** Security price */
    private double price;
    
    /** Split ratio */
    private double splitRatio;

    /**
     * Create a price history object for the current date
     *
     * @param       price           Security price
     */
    public PriceHistory(double price) {
        this(new Date(), price, 0.0);
    }

    /**
     * Create a price history object for the specified date
     *
     * @param       date            History date
     * @param       price           Security price
     */
    public PriceHistory(Date date, double price) {
        this(date, price, 0.0);
    }
    
    /**
     * Create a price history object for a stock split
     * 
     * @param       date            History date
     * @param       price           Security price after the split
     * @param       ratio           Stock split ratio
     */
    public PriceHistory(Date date, double price, double ratio) {
        if (date == null)
            throw new NullPointerException("No date supplied");

        //
        // Set the date (the time is always set to 12:00:00)
        //
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        this.date = cal.getTime();

        //
        // Set the price
        //
        this.price = (double)Math.round(price*10000.0)/10000.0;
        
        //
        // Set the stock split ratio
        //
        this.splitRatio = ratio;
    }

    /**
     * Create a price history object from an encoded byte stream
     *
     * @param       data            Encoded byte stream
     * @exception   DBException     Unable to decode object stream
     */
    public PriceHistory(byte[] data) throws DBException {
        if (data == null)
            throw new NullPointerException("Null byte stream reference");

        decode(new DecodeStream(data));
    }

    /**
     * Create a PriceHistory object using an existing stream
     *
     * @param       stream          Decode stream
     * @exception   DBException     Unable to decode object stream
     */
    public PriceHistory(DecodeStream stream) throws DBException {
        if (stream == null)
            throw new NullPointerException("Null decode stream reference");

        decode(stream);
    }

    /**
     * Decode the PriceHistory object stream
     *
     * @param       stream          Decode stream
     * @exception   DBException     Unable to decode object stream
     */
    private void decode(DecodeStream stream) throws DBException {
        try {

            //
            //  Validate the application identifier tag
            //
            if (stream.getTag() != tagID)
                throw new DBException("Not an encoded PriceHistory object");

            //
            //  Get the PriceHistory sequence
            //
            DecodeStream seq = stream.getSequence(true);

            //
            //  Decode the date
            //
            date = seq.decodeTime(false);
            
            //
            // Decode the price
            //
            price = seq.decodeDouble(false);
            
            //
            //  The stock split ratio is encoded as an optional context-specific
            //  field with identifier 0
            //
            if (seq.getLength() != 0 && seq.getTag() == (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+0))
                splitRatio = seq.decodeDouble(true);

            //
            //  Check for unconsummed data
            //
            if (seq.getLength() != 0)
                throw new DBException("Unconsummed data in PriceHistory sequence");

        } catch (Asn1Exception exc) {

            throw new DBException("ASN.1 decode error", exc);
        }
    }

    /**
     * Test if the supplied byte stream represents an encoded PriceHistory object
     *
     * @param       data            The encoded byte stream
     * @return                      TRUE if PriceHistory object
     */
    public static boolean isEncodedStream(byte[] data) {
        if (data == null)
            throw new NullPointerException("Null byte stream reference");

        if (data.length == 0)
            return false;

        return (data[0]==(byte)(tagID|Asn1Stream.ASN1_CONSTRUCTED) ? true : false);
    }

    /**
     * Encode the PriceHistory object
     *
     * @return                      The encoded byte stream for the object
     */
    public byte[] encode() {
        EncodeStream stream = new EncodeStream(64);
        encode(stream);
        return stream.getData();
    }

    /**
     * Encode the PriceHistory object using an existing encode stream
     *
     * @param       stream          The encode stream
     * @return                      The length of the encoded data
     */
    public int encode(EncodeStream stream) {
        int length = 0;
        
        //
        // Encode the stock split ratio as an optional context-specific sequence
        // with identifier 0.
        //
        if (splitRatio != 0.0)
            length += stream.encodeDouble(splitRatio, (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+0));

        //
        //  Encode the price (fields are encoded in reverse order because the 
        //  stream is constructed from the end to the beginning)
        //
        length += stream.encodeDouble(price);
        
        //
        // Encode the date
        //
        length += stream.encodeTime(date);

        //
        //  Make the PriceHistory sequence
        //
        return stream.makeSequence(length, tagID);
    }

    /**
     * Get the hash code.  The hash code for the PriceHistory object is the
     * hash code for the date.
     *
     * @return                      The hash code for the price history
     */
    public int hashCode() {
        return date.hashCode();
    }

    /**
     * Compare this object to the supplied object.  Two PriceHistory objects
     * are equal if they have the same date.
     *
     * @param       obj             Comparison object
     * @return                      TRUE if the objects are equal
     */
    public boolean equals(Object obj) {
        boolean retValue = false;

        if (this == obj) {
            retValue = true;
        } else if (obj instanceof PriceHistory) {
            PriceHistory comp = (PriceHistory)obj;
            if (date.equals(comp.date))
                retValue = true;
        }

        return retValue;
    }

    /**
     * Compare two price history objects for the Comparable interface.  The
     * comparison is based on the price history dates.
     *
     * @param       object          Comparison object
     * @return                      Negative, zero or positive based on comparison
     */
    public int compareTo(PriceHistory object) {
        return date.compareTo(object.date);
    }

    /**
     * Get the history date
     *
     * @return                      The date
     */
    public Date getDate() {
        return date;
    }

    /**
     * Get the history price
     *
     * @return                      The price
     */
    public double getPrice() {
        return price;
    }
    
    /**
     * Get the stock split ratio
     * 
     * @return                      The split ratio or 0
     */
    public double getSplitRatio() {
        return splitRatio;
    }

    /**
     * Set the history price
     *
     * @param       price           The new price
     */
    public void setPrice(double price) {
        this.price = (double)Math.round(price*10000.0)/10000.0;
    }
}
