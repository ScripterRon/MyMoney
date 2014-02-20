package Asn1;

/**
 * The Asn1Exception class defines the exceptions thrown by the ASN.1
 * encode/decode support when an error is detected while processing
 * an ASN.1 stream
 */
public final class Asn1Exception extends Exception {

    /**
     * Construct an exception with no message text or causing exception
     */
    public Asn1Exception() {
        super();
    }

    /**
     * Construct an exception with message text but no causing exception
     *
     * @param       exceptionMsg    The message text for the exception
     */
    public Asn1Exception(String exceptionMsg) {
        super(exceptionMsg);
    }
}

