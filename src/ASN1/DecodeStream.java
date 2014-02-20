package Asn1;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * The DecodeStream class provides ASN.1 stream decode support
 *
 * <p>In order to support the Distinguished Encoding Rules (DER), we do not
 * allow indefinite-length encodings and all fields are encoded using the
 * minimum number of bytes.
 */
public final class DecodeStream extends Asn1Stream {

    /**
     * Class instance variables
     */
    private byte[] stream;                  // ASN.1 byte stream
    private int size;                       // Byte stream length
    private int next;                       // Index of next stream byte
    private int residualLength;             // Residual length
    private int baseOffset;                 // Base offset into byte stream
    private GregorianCalendar calendar;     // Calendar object for the GMT timezone

    /**
     * Constructor for a decode stream.  The byte array must not be
     * modified until the decode stream is no longer needed.
     *
     * @param       octetData       The encoded octet stream
     */
    public DecodeStream(byte[] octetData) {
        this(octetData, octetData.length, 0);
    }

    /**
     * Constructor for an embedded decode stream.  The byte array must not be
     * modified until the decode stream is no longer needed.
     *
     * @param       octetData       The encoded octet stream
     * @param       dataLength      The length of the encoded octet stream
     * @param       dataOffset      The base offset of the start of the octet stram
     */
    public DecodeStream(byte[] octetData, int dataLength, int dataOffset) {
        if (octetData == null)
            throw new NullPointerException("Null byte stream reference");

        stream = octetData;
        size = dataLength;
        baseOffset = dataOffset;
        residualLength = size;
        next = baseOffset;
        calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    }

    /**
     * Reset the encoded data so that the stream is full
     */
    public void clear() {
        next = baseOffset;
        residualLength = size;
    }

    /**
     * Get the size of the encoded data
     *
     * @return                      The size of the encoded data
     */
    public int getSize() {
        return size;
    }

    /**
     * Get the number of unprocessed bytes
     *
     * @return                      The number of unprocessed bytes
     */
    public int getLength() {
        return residualLength;
    }

    /**
     * Get the tag for the next field.  This operation does not consume the
     * tag and it remains available for the next operation.
     *
     * @return                      Tag with the CONSTRUCTED bit set to 0
     * @exception   Asn1Exception   Attempt to read past end of stream
     */
    public byte getTag() throws Asn1Exception {

        //
        // There must be a tag available
        //
        if (residualLength == 0)
            throw new Asn1Exception("Attempt to read past end of stream");

        //
        // Return the tag with the CONSTRUCTED bit set to 0
        //
        return (byte)(stream[next]&(~ASN1_CONSTRUCTED));
    }

    /**
     * Decode the length (L) field
     *
     * @return                      The length of the value (V) field
     * @exception   Asn1Exception   Attempt to read past end of stream
     * @exception   Asn1Exception   Length field is too long
     */
    private int decodeLength() throws Asn1Exception {
        int length = 0;

        //
        // There must be at least one byte available
        //
        if (residualLength < 1)
            throw new Asn1Exception("Attempt to read past end of stream");

        //
        // The high-order bit is set in the first byte of the L field
        // if the length is encoded in multiple bytes
        //
        if (stream[next] >= 0) {
            length = (int)stream[next++];
            residualLength--;
        } else {
            int count = (int)stream[next]&0x7f;

            if (count > 4)
                throw new Asn1Exception("Length field at offset "+next+" is too long");

            next++;
            residualLength--;

            if (residualLength < count)
                throw new Asn1Exception("Attempt to read past end of stream");

            for (int i=0; i<count; i++)
                length = (length<<8)|((int)stream[next++]&0xff);

            residualLength -= count;
        }

        return length;
    }

    /**
     * Get a sequence.  The caller is responsible for validating the tag for an implicit
     * sequence.
     *
     * @param       implicit        TRUE if this is an implicit sequence
     * @return                      New DecodeStream for the sequence
     * @exception   Asn1Exception   Attempt to read past end of stream
     * @exception   Asn1Exception   Field is not a SEQUENCE
     */
    public DecodeStream getSequence(boolean implicit) throws Asn1Exception {
        DecodeStream seqStream;

        //
        // There must be at least T and L fields available
        //
        if (residualLength < 2)
            throw new Asn1Exception("Attempt to read past end of stream");

        //
        // Validate the tag for an explicit sequence
        //
        if (!implicit && stream[next] != ASN1_SEQUENCE+ASN1_CONSTRUCTED)
            throw new Asn1Exception("Field at offset "+next+" is not a SEQUENCE");

        if ((stream[next]&ASN1_CONSTRUCTED) == 0)
            throw new Asn1Exception("SEQUENCE must be a constructed field");

        next++;
        residualLength--;

        //
        // Get the sequence length
        //
        int length = decodeLength();

        if (length > residualLength)
            throw new Asn1Exception("Attempt to read past end of stream");

        //
        // Create a DecodeStream for the body of the sequence
        //
        seqStream = new DecodeStream(stream, length, next);
        next += length;
        residualLength -= length;
        return seqStream;
    }

    /**
     * Decode a boolean value.  The caller is responsible for validating the
     * tag for an implicit value.
     *
     * @param       implicit        TRUE if this is an implicit value
     * @return                      The decoded value
     * @exception   Asn1Exception   Attempt to read past end of stream
     * @exception   Asn1Exception   Field is not a BOOLEAN
     * @exception   Asn1Exception   Value length is not valid
     */
    public boolean decodeBoolean(boolean implicit) throws Asn1Exception {
        boolean retValue;

        //
        // There must be at least 2 bytes available
        //
        if (residualLength < 2)
            throw new Asn1Exception("Attempt to read past end of stream");

        //
        // Validate the tag for an explicit value
        //
        if (!implicit && stream[next] != ASN1_BOOLEAN)
            throw new Asn1Exception("Field at offset "+next+" is not a BOOLEAN");

        if ((stream[next]&ASN1_CONSTRUCTED) != 0)
            throw new Asn1Exception("BOOLEAN must not be a constructed field");

        next++;
        residualLength--;

        //
        // Decode the field length
        //
        int length = decodeLength();

        if (length != 1)
            throw new Asn1Exception("Value length "+length+" is not valid");

        if (length > residualLength)
            throw new Asn1Exception("Attempt to read past end of stream");

        //
        // Any non-zero value is TRUE while a zero value is FALSE
        //
        retValue = (stream[next]!=0 ? true : false);
        next++;
        residualLength--;
        return retValue;
    }

    /**
     * Decode an integer value.  The caller is responsible for validating
     * the tag for an implicit value.
     *
     * @param       implicit        TRUE if this is an implicit value
     * @return                      The decoded value
     * @exception   Asn1Exception   Attempt to read past end of stream
     * @exception   Asn1Exception   Field is not an INTEGER
     * @exception   Asn1Exception   Value length is not valid
     */
    public int decodeInteger(boolean implicit) throws Asn1Exception {
        int retValue;

        //
        // There must be at least 2 bytes available
        //
        if (residualLength < 2)
            throw new Asn1Exception("Attempt to read past end of stream");

        //
        // Validate the tag for an explicit value
        //
        if (!implicit && stream[next] != ASN1_INTEGER)
            throw new Asn1Exception("Field at offset "+next+" is not an INTEGER");

        if ((stream[next]&ASN1_CONSTRUCTED) != 0)
            throw new Asn1Exception("INTEGER must not be a constructed field");

        next++;
        residualLength--;

        //
        // Decode the field length
        //
        int length = decodeLength();

        if (length > 4)
            throw new Asn1Exception("Value length "+length+" is not valid");

        if (length > residualLength)
            throw new Asn1Exception("Attempt to read past end of stream");

        //
        // Decode the integer value
        //
        if (stream[next] >= 0)
            retValue = 0;
        else
            retValue = -1;

        for (int i=0; i<length; i++)
            retValue = (retValue<<8)|((int)stream[next++]&0xff);

        residualLength -= length;
        return retValue;
    }

    /**
     * Decode a floating-point value.  The caller is responsible for
     * validating the tag for an implicit value.
     *
     * @param       implicit        TRUE if this is an implicit value
     * @return                      The decoded value
     * @exception   Asn1Exception   Attempt to read past end of stream
     * @exception   Asn1Exception   Field is not a DOUBLE
     * @exception   Asn1Exception   Value length is not valid
     */
    public double decodeDouble(boolean implicit) throws Asn1Exception {
        long intValue;

        //
        // There must be at least 2 bytes available
        //
        if (residualLength < 2)
            throw new Asn1Exception("Attempt to read past end of stream");

        //
        // Validate the tag for an explicit value
        //
        if (!implicit && stream[next] != ASN1_DOUBLE)
            throw new Asn1Exception("Field at offset "+next+" is not a DOUBLE");

        if ((stream[next]&ASN1_CONSTRUCTED) != 0)
            throw new Asn1Exception("DOUBLE must not be a constructed field");

        next++;
        residualLength--;

        //
        // Decode the field length
        //
        int length = decodeLength();

        if (length > 8)
            throw new Asn1Exception("Value length "+length+" is not valid");

        if (length > residualLength)
            throw new Asn1Exception("Attempt to read past end of stream");

        //
        // Decode the double value
        //
        if (stream[next] >= 0)
            intValue = 0;
        else
            intValue = -1;

        for (int i=0; i<length; i++)
            intValue = (intValue<<8)|((int)stream[next++]&0xff);

        residualLength -= length;

        return Double.longBitsToDouble(intValue);
    }

    /**
     * Decode an octet string.  The caller is responsible for validating
     * the tag for an implicit value.
     *
     * @param       implicit        TRUE if this is an implicit value
     * @return                      New byte array containing the decoded value
     * @exception   Asn1Exception   Attempt to read past end of stream
     * @exception   Asn1Exception   Field is not an OCTETSTRING
     */
    public byte[] decodeBytes(boolean implicit) throws Asn1Exception {
        byte[] retValue = null;

        //
        // There must be at least 2 bytes available
        //
        if (residualLength < 2)
            throw new Asn1Exception("Attempt to read past end of stream");

        //
        // Validate the tag for an explicit value
        //
        if (!implicit && stream[next] != ASN1_OCTETSTRING)
            throw new Asn1Exception("Field at offset "+next+" is not an OCTETSTRING");

        if ((stream[next]&ASN1_CONSTRUCTED) != 0)
            throw new Asn1Exception("OCTETSTRING must not be a constructed field");

        next++;
        residualLength--;

        //
        // Decode the field length
        //
        int length = decodeLength();

        if (length > residualLength)
            throw new Asn1Exception("Attempt to read past end of stream");

        //
        // Extract the octet string
        //
        retValue = new byte[length];
        if (length != 0) {
            System.arraycopy(stream, next, retValue, 0, length);
            next += length;
            residualLength -= length;
        }

        return retValue;
    }

    /**
     * Decode a character string.  This method supports just UTF-16
     * which is encoded as 2 bytes per character.  The caller is responsible
     * for validating the tag for an implicit value.
     *
     * @param       implicit        TRUE if this is an implicit value
     * @return                      New String object containing the decoded value
     * @exception   Asn1Exception   Attempt to read past end of stream
     * @exception   Asn1Exception   Field is not a BMPSTRING
     * @exception   Asn1Exception   Value length is not valid
     */
    public String decodeString(boolean implicit) throws Asn1Exception {
        String retValue = null;

        //
        // There must be at least 2 bytes available
        //
        if (residualLength < 2)
            throw new Asn1Exception("Attempt to read past end of stream");

        //
        // Validate the tag for an explicit value
        //
        if (!implicit && stream[next] != ASN1_BMPSTRING)
            throw new Asn1Exception("Field at offset "+next+" is not a BMPSTRING");

        if ((stream[next]&ASN1_CONSTRUCTED) != 0)
            throw new Asn1Exception("BMPSTRING must not be a constructed field");

        next++;
        residualLength--;

        //
        // Decode the field length
        //
        int length = decodeLength();
        int stringLength = length/2;

        if ((length&1) != 0)
            throw new Asn1Exception("Value length "+length+" is not valid");

        if (length > residualLength)
            throw new Asn1Exception("Attempt to read past end of stream");

        //
        // Extract the character string
        //
        if (length != 0) {
            char[] charData = new char[stringLength];

            for (int i=0; i<stringLength; i++) {
                charData[i] = (char)
                        ((((int)stream[next]&0xff)<<8)|((int)stream[next+1]&0xff));
                next += 2;
                residualLength -= 2;
            }

            retValue = new String(charData);
        } else {
            retValue = new String();
        }

        return retValue;
    }

    /**
     * Decode a general time value.  The caller is responsible for
     * validating the tag for an implicit value.
     *
     * @param       implicit        TRUE if this is an implicit value
     * @return                      New Date object containing the decoded value
     * @exception   Asn1Exception   Attempt to read past end of stream
     * @exception   Asn1Exception   Field is not a BMPSTRING
     * @exception   Asn1Exception   Value length is not valid
     */
    public Date decodeTime(boolean implicit) throws Asn1Exception {

        //
        // There must be at least 2 bytes available
        //
        if (residualLength < 2)
            throw new Asn1Exception("Attempt to read past end of stream");

        //
        // Validate the tag for an explicit value
        //
        if (!implicit && stream[next] != ASN1_GENERALTIME)
            throw new Asn1Exception("Field at offset "+next+" is not a GENERALTIME");

        if ((stream[next]&ASN1_CONSTRUCTED) != 0)
            throw new Asn1Exception("GENERALTIME must not be a constructed field");

        next++;
        residualLength--;

        //
        // Decode the field length
        //
        int length = decodeLength();

        if (length > residualLength)
            throw new Asn1Exception("Attempt to read past end of stream");

        if (length != 15)
            throw new Asn1Exception("GENERALTIME length "+length+" is invalid");

        if (((int)stream[next+14]&0xff) != 0x5a)
            throw new Asn1Exception("GENERALTIME timezone is not GMT");

        //
        // Decode the time fields.  We just look at the lower nibble of each
        // byte since the ISO-8859-1 character set assigns the digits 0-9 to
        // code points 0x30-0x39.
        //
        int year = ((int)stream[next]&15)*1000+
                            ((int)stream[next+1]&15)*100+
                            ((int)stream[next+2]&15)*10+
                            ((int)stream[next+3]&15);
        int month = ((int)stream[next+4]&15)*10+
                            ((int)stream[next+5]&15)-1;
        int day = ((int)stream[next+6]&15)*10+
                            ((int)stream[next+7]&15);
        int hour = ((int)stream[next+8]&15)*10+
                            ((int)stream[next+9]&15);
        int minute = ((int)stream[next+10]&15)*10+
                            ((int)stream[next+11]&15);
        int second = ((int)stream[next+12]&15)*10+
                            ((int)stream[next+13]&15);
        next += 15;
        residualLength -= 15;

        //
        // Set the time
        //
        calendar.set(year, month, day, hour, minute, second);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}

