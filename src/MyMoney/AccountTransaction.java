package MyMoney;

/**
 * Associate a running balance with an account transaction.  This is a
 * transient class and the class instance variables are accessed directly
 * by the application.
 */
public final class AccountTransaction {

    /** Transaction record */
    public TransactionRecord transaction;

    /** Current account balance */
    public double balance;

    /**
     * Create a new account transaction
     *
     * @param       transaction     TransactionRecord
     * @param       balance         Current balance
     */
    public AccountTransaction(TransactionRecord transaction, double balance) {
        this.transaction = transaction;
        this.balance = balance;
    }
}
