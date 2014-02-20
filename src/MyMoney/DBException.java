package MyMoney;

/**
 * The DBException class defines the exceptions thrown by the application
 * database support.
 */
public final class DBException extends Exception {

    /**
     * Construct an exception with no message text or causing exception
     */
    public DBException() {
        super();
    }

    /**
     * Construct an exception with message text but no causing exception
     *
     * @param       exceptionMsg    The message text for the exception
     */
    public DBException(String exceptionMsg) {
        super(exceptionMsg);
    }

    /**
     * Construct an exception with message text and a causing exception
     *
     * @param       exceptionMsg    The message text for the exception
     * @param       cause           The causing exception
     */
    public DBException(String exceptionMsg, Throwable cause) {
        super(exceptionMsg, cause);
    }
}

