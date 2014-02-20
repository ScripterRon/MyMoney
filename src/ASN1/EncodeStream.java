package Asn1;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * The EncodeStream class provides ASN.1 stream encode support
 *
 * <p>In order to support the Distinguished Encoding Rules (DER), we do not
 * allow indefinite-length encodings and all fields are encoded using the
 * minimum number of bytes.  We do this by encoding the stream in reverse
 * order.  This means that the encode methods must be called in the reverse
 * order.  That is, if the encoded data consists of integer A followed by
 * string B, then string B must be encoded before integer A.  The getData()
 * method will reverse the bytes so that the encoded stream is returned in
 * the correct order.
 */
public final class EncodeStream extends Asn1Stream {

    /**
     * Class instance variables
     */
    private byte[] stream;                  // ASN.1 byte stream
    private int increment;                  // Minimum allocation increment
    private int size;                       // Allocated size of byte stream
    private int next;                       // Index of next stream byte
    private GregorianCalendar calendar;     // Calendar object for the GMT timezone

    /**
     * Constructor for an encode stream using default values for the
     * initial size and the minimum increment
     */
    public EncodeStream() {
        this(1024, 256);
    }

    /**
     * Constructor for an encode stream with an explicit initial size
     * and the default minimum increment
     *
     * @param       initialSize     The initial size of the stream
     */
    public EncodeStream(int initialSize) {
        this(initialSize, 256);
    }

    /**
     * Constructor for an encode stream with explicit values for the initial size
     * and the minimum increment
     *
     * @param       initialSize       The initial size of the stream
     * @param       minimumIncrement  The minimum increment when expanding the stream
     */
    public EncodeStream(int initialSize, int minimumIncrement) {
        size = initialSize;
        increment = minimumIncrement;
        next = 0;
        stream = new byte[size];
        calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    }

    /**
     * Reset the encoded data so that the stream is empty
     */
    public void clear() {
        next = 0;
    }

    /**
     * Get the size of the byte stream array
     *
     * @return                      The size of the byte stream array
     */
    public int getSize() {
        return size;
    }

    /**
     * Get the length of the encoded data
     *
     * @return                      The length of the encoded data
     */
    public int getLength() {
        return next;
    }

    /**
     * Expand the encode stream
     *
     * @param       expandIncrement Number of bytes to expand the stream
     */
    private void expandStream(int expandIncrement) {
        int newSize = size + Math.max(expandIncrement, increment);
        byte[] newStream = new byte[newSize];

        if (next != 0)
            System.arraycopy(stream, 0, newStream, 0, next);

        stream = newStream;
        size = newSize;
    }

    /**
     * Get the encoded octet stream.  The return value will be null
     * if no data has been encoded.
     *
     * @return                      New byte array containing the encoded stream
     */
    public byte[] getData() {
        byte[] octetData = null;

        if (next != 0) {
            octetData = new byte[next];

            for (int i=0; i<next; i++)
                octetData[i] = stream[next-1-i];
        }

        return octetData;
    }

    /**
     * Make the length (L) field
     *
     * @param       fieldLength     Field data length
     * @return                      Length of the generated L field
     */
    private int makeLength(int fieldLength) {
        int retLength = 0;

        //
        // Expand the stream if necessary
        //
        if (size-next < 5)
            expandStream(5);

        //
        // The field length is used as-is if it is less than 128.  Otherwise,
        // it is encoded in byte-order with an initial byte containing the
        // number of bytes following the initial byte.  The high-order
        // bit in the initial byte is set to 1 in this case.
        //
        if (fieldLength < 128) {
            stream[next++] = (byte)fieldLength;
        } else {
            int residualLength = fieldLength;

            while (residualLength != 0) {
                stream[next++] = (byte)residualLength;
                residualLength >>>= 8;
                retLength++;
            }

            stream[next++] = (byte)(retLength|0x80);
        }

        return retLength+1;
    }

    /**
     * Make an explicit sequence
     *
     * @param       seqLength       Sequence data length
     * @return                      Length of encoded sequence
     */
    public int makeSequence(int seqLength) {
        return makeSequence(seqLength, ASN1_SEQUENCE);
    }

    /**
     * Make an implicit sequence
     *
     * @param       seqLength       Sequence data length
     * @param       seqTag          Tag
     * @return                      Length of encoded sequence
     */
    public int makeSequence(int seqLength, byte seqTag) {
        int retLength;

        //
        // The sequence length must be positive
        //
        if (seqLength < 0)
            throw new IllegalArgumentException("Sequence length "+seqLength+" is not valid");

        //
        // Expand the stream if necessary
        //
        if (size-next < 6)
            expandStream(6);

        //
        // Encode the length (L) field
        //
        retLength = makeLength(seqLength);

        //
        // Encode the tag (T) field (a SEQUENCE is a constructed field)
        //
        stream[next++] = (byte)(seqTag|ASN1_CONSTRUCTED);
        return seqLength+retLength+1;
    }

    /**
     * Encode an explicit boolean value
     *
     * @param       value           Value to be encoded
     * @return                      Length of encoded value
     */
    public int encodeBoolean(boolean value) {
        return encodeBoolean(value, ASN1_BOOLEAN);
    }

    /**
     * Encode an implicit boolean value
     *
     * @param       value           Value to be encoded
     * @param       tag             Tag
     * @return                      Length of encoded value
     */
    public int encodeBoolean(boolean value, byte tag) {

        //
        // Expand the stream if necessary
        //
        if (size-next < 3)
            expandStream(3);

        //
        // Encode the value (V) field
        //
        if (value)
            stream[next++] = (byte)0xff;
        else
            stream[next++] = (byte)0x00;

        //
        // Encode the length (L) field
        //
        makeLength(1);

        //
        // Encode the tag (T) field
        //
        stream[next++] = tag;
        return 3;
    }

    /**
     * Encode an explicit integer value
     *
     * @param       value           Value to be encoded
     * @return                      Length of encoded value
     */
    public int encodeInteger(int value) {
        return encodeInteger(value, ASN1_INTEGER);
    }

    /**
     * Encode an implicit integer value
     *
     * @param       value           Value to be encoded
     * @param       tag             Tag
     * @return                      Length of encoded value
     */
    public int encodeInteger(int value, byte tag) {

        //
        // Expand the stream if necessary
        //
        if (size-next < 6)
            expandStream(6);

        //
        // Encode the value (V) field
        //
        int retLength = 0;
        int residualValue = value;

        while (true) {
            byte c = (byte)residualValue;
            stream[next++] = c;
            residualValue >>= 8;
            retLength++;

            if ((residualValue == 0 && c >= 0) || (residualValue == -1 && c < 0))
                break;
        }

        //
        // Encode the length (L) field
        //
        retLength += makeLength(retLength);

        //
        // Encode the tag (T) field
        //
        stream[next++] = tag;
        return retLength+1;
    }

    /**
     * Encode an explicit floating-point value.  The floating-point value
     * is encoded using the internal 8-byte format provided by the
     * Double.doubleToLongBits() method.
     *
     * @param       value           Value to be encoded
     * @return                      Length of encoded value
     */
    public int encodeDouble(double value) {
        return encodeDouble(value, ASN1_DOUBLE);
    }

    /**
     * Encode an implicit floating-point value.  The floating-point value
     * is encoded using the internal 8-byte format provided by the
     * Double.doubleToLongBits() method.
     *
     * @param       value           Value to be encoded
     * @param       tag             Tag
     * @return                      Length of encoded value
     */
    public int encodeDouble(double value, byte tag) {
        int retLength = 0;
        long residualValue = Double.doubleToLongBits(value);

        //
        // Expand the stream if necessary
        //
        if (size-next < 10)
            expandStream(10);

        //
        // Encode the value (V) field
        //
        while (true) {
            byte c = (byte)residualValue;
            stream[next++] = c;
            residualValue >>= 8;
            retLength++;

            if ((residualValue == 0 && c >= 0) || (residualValue == -1 && c < 0))
                break;
        }

        //
        // Encode the length (L) field
        //
        retLength += makeLength(retLength);

        //
        // Encode the tag (T) field
        //
        stream[next++] = tag;
        return retLength+1;
    }

    /**
     * Encode an explicit octet string
     *
     * @param       value           Value to be encoded
     * @return                      Length of encoded value
     */
    public int encodeBytes(byte[] value) {
        return encodeBytes(value, ASN1_OCTETSTRING);
    }

    /**
     * Encode an implicit octet string
     *
     * @param       value           Value to be encoded
     * @param       tag             Tag
     * @return                      Length of encoded value
     */
    public int encodeBytes(byte[] value, byte tag) {
        int retLength = value.length;

        //
        // The value length must be positive
        //
        if (retLength < 0)
            throw new IllegalArgumentException("Octet string length "+retLength+" is invalid");

        //
        // Expand the stream if necessary
        //
        if (size-next < retLength+6)
            expandStream(retLength+6);

        //
        // Encode the value (V) field
        //
        for (int i=0; i<retLength; i++)
            stream[next++] = value[retLength-1-i];

        //
        // Encode the length (L) field
        //
        retLength += makeLength(retLength);

        //
        // Encode the tag (T) field
        //
        stream[next++] = tag;
        return retLength+1;
    }

    /**
     * Encode an explicit character string.  This method supports just UTF-16
     * which is encoded as 2 bytes per character.
     *
     * @param       value           Value to be encoded
     * @return                      Length of encoded value
     */
    public int encodeString(String value) {
        return encodeString(value, ASN1_BMPSTRING);
    }

    /**
     * Encode an implicit character string.  This method supports just UTF-16
     * which is encoded as 2 bytes per character.
     *
     * @param       value           Value to be encoded
     * @param       tag             Tag
     * @return                      Length of encoded value
     */
    public int encodeString(String value, byte tag) {
        int length = value.length();
        int retLength = 2*length;

        //
        // Expand the stream if necessary
        //
        if (size-next < retLength+6)
            expandStream(retLength+6);

        //
        // Encode the value (V) field
        //
        // Note that each UTF-16 character requires 2 bytes
        //
        for (int i=length-1; i>=0; i--) {
            int c = (int)value.charAt(i);
            stream[next++] = (byte)c;
            stream[next++] = (byte)(c>>8);
        }

        //
        // Encode the length (L) field
        //
        retLength += makeLength(retLength);

        //
        // Encode the tag (T) field
        //
        stream[next++] = tag;
        return retLength+1;
    }

    /**
     * Encode an explicit time value
     *
     * @param       value           Value to be encoded
     * @return                      Length of encoded value
     */
    public int encodeTime(Date value) {
        return encodeTime(value, ASN1_GENERALTIME);
    }

    /**
     * Encode an implicit time value
     *
     * @param       value           Value to be encoded
     * @param       tag             Tag
     * @return                      Length of encoded value
     */
    public int encodeTime(Date value, byte tag) {

        //
        // Encode the time as "YYYYMMDDHHMMSS"
        //
        calendar.setTime(value);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        String tv = String.format("%04d%02d%02d%02d%02d%02d",
                                  year, month, day, hour, minute, second);
        int retLength = tv.length();

        //
        // Expand the stream if necessary
        //
        if (size-next < retLength+3)
            expandStream(retLength+3);

        //
        // Convert the encoded time to the ISO-8859-1 character set
        // and store in the stream buffer
        //
        for (int i=0; i<retLength; i++) {
            int ch = (int)(tv.charAt(i));
            stream[next+retLength-i] = (byte)ch;
        }

        //
        // Append 'Z' to indicate GMT time
        //
        stream[next] = 0x5A;
        retLength++;
        next += retLength;

        //
        // Encode the length (L) field
        //
        retLength += makeLength(retLength);

        //
        // Encode the tag (T) field
        //
        stream[next++] = tag;
        return retLength+1;
    }
}
