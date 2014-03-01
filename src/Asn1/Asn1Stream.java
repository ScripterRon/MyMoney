package Asn1;

/**
 * The Asn1Stream class defines fields used by the EncodeStream and DecodeStream
 * classes
 */
public abstract class Asn1Stream {

    /** ASN.1 UNIVERSAL class */
    public static final byte ASN1_UNIVERSAL = (byte)0x00;

    /** ASN.1 APPLICATION class */
    public static final byte ASN1_APPLICATION = (byte)0x40;

    /** ASN.1 CONTEXT-SPECIFIC class */
    public static final byte ASN1_CONTEXT_SPECIFIC = (byte)0x80;

    /** ASN.1 PRIVATE class */
    public static final byte ASN1_PRIVATE = (byte)0xc0;

    /** ASN.1 constructed field */
    public static final byte ASN1_CONSTRUCTED = (byte)0x20;

    /** ASN.1 boolean primitive type */
    public static final byte ASN1_BOOLEAN = (byte)0x01;

    /** ASN.1 integer primitive type */
    public static final byte ASN1_INTEGER = (byte)0x02;

    /** ASN.1 bit string primitive type */
    public static final byte ASN1_BITSTRING = (byte)0x03;

    /** ASN.1 octet string primitive type */
    public static final byte ASN1_OCTETSTRING = (byte)0x04;

    /** ASN.1 null primitive type */
    public static final byte ASN1_NULL = (byte)0x05;

    /** ASN.1 object identifier primitive type */
    public static final byte ASN1_OID = (byte)0x06;

    /** ASN.1 descriptor primitive type */
    public static final byte ASN1_DESCRIPTOR = (byte)0x07;

    /** ASN.1 external primitive type */
    public static final byte ASN1_EXTERNAL = (byte)0x08;

    /** ASN.1 real primitive type */
    public static final byte ASN1_REAL = (byte)0x09;

    /** ASN.1 enumerated primitive type */
    public static final byte ASN1_ENUMERATED = (byte)0x0a;

    /** ASN.1 embedded primitive type */
    public static final byte ASN1_EMBEDDED = (byte)0x0b;

    /** ASN.1 UTF-8 string primitive type */
    public static final byte ASN1_UTF8STRING = (byte)0x0c;

    /** ASN.1 sequence */
    public static final byte ASN1_SEQUENCE = (byte)0x10;

    /** ASN.1 set */
    public static final byte ASN1_SET = (byte)0x11;

    /** ASN.1 printable string primitive type */
    public static final byte ASN1_PRINTABLESTRING = (byte)0x13;

    /** ASN.1 Telex string primitive type */
    public static final byte ASN1_TELETEXSTRING = (byte)0x14;

    /** ASN.1 IA5 string primitive type */
    public static final byte ASN1_IA5STRING = (byte)0x16;

    /** ASN.1 UTC time primitive type */
    public static final byte ASN1_UTCTIME = (byte)0x17;

    /** ASN.1 general time primitive type */
    public static final byte ASN1_GENERALTIME = (byte)0x18;

    /** ASN.1 visible string primitive type */
    public static final byte ASN1_VISIBLESTRING = (byte)0x1a;

    /** ASN.1 general string primitive type */
    public static final byte ASN1_GENERALSTRING = (byte)0x1b;

    /** ASN.1 universal string primitive type */
    public static final byte ASN1_UNIVERSALSTRING = (byte)0x1c;

    /** ASN.1 BMP string primitive type */
    public static final byte ASN1_BMPSTRING = (byte)0x1e;

    /** Private type for a single-precision floating-point number */
    public static final byte ASN1_FLOAT = (byte)0xc1;

    /** Private type for a double-precision floating-point number */
    public static final byte ASN1_DOUBLE = (byte)0xc2;
}


