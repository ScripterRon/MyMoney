package Report;

/**
 * The ReportException class defines the exceptions thrown while generating
 * a report.
 */
public final class ReportException extends Exception {

    /**
     * Construct an exception with no message text or causing exception
     */
    public ReportException() {
        super();
    }

    /**
     * Construct an exception with message text but no causing exception
     *
     * @param       exceptionMsg    The message text for the exception
     */
    public ReportException(String exceptionMsg) {
        super(exceptionMsg);
    }

    /**
     * Construct an exception with message text and a causing exception
     *
     * @param       exceptionMsg    The message text for the exception
     * @param       cause           The causing exception
     */
    public ReportException(String exceptionMsg, Throwable cause) {
        super(exceptionMsg, cause);
    }
}

