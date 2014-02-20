package MyMoney;

import java.util.*;
import Asn1.*;

/**
 * Database transaction record.
 * <p>
 * Transaction actions are used with investment transactions.  The following
 * transaction actions are supported:
 * <ul>
 * <li>ACCRETION            Bond discount accretion
 * <li>AMORTIZATION         Bond premium amortization
 * <li>BUY                  Buy shares
 * <li>EXCHANGE             Exchange shares for shares in another company
 * <li>EXPENSE              Investment expense
 * <li>INCOME               Investment income
 * <li>REINVEST             Reinvest income
 * <li>RETURN_OF_CAPITAL    Return of capital
 * <li>SELL                 Sell shares
 * <li>SPIN_OFF             Receive shares for a new company
 * <li>SPLIT                Split shares
 * </ul>
 * <p>
 * All transaction records are contained in the <code>transactions</code>
 * linked list.  The list entries are sorted by date and new entries are
 * added after all transactions with the same date.
 * <p>
 * The transaction record is encoded as follows:
 * <pre>
 *   TransactionRecord ::= [APPLICATION 5] SEQUENCE {
 *     date                        GENERALTIME,
 *     account                     INTEGER,
 *     name                        BMPSTRING,
 *     memo                        BMPSTRING,
 *     amount                      DOUBLE,
 *     reconciled                  INTEGER,
 *     category                [0] INTEGER OPTIONAL,
 *     transferAccount         [1] INTEGER OPTIONAL,
 *     checkNumber             [2] INTEGER OPTIONAL,
 *     security                [3] INTEGER OPTIONAL,
 *     shares                  [4] DOUBLE OPTIONAL,
 *     sharePrice              [5] DOUBLE OPTIONAL,
 *     commission              [6] DOUBLE OPTIONAL,
 *     splits                  [7] SEQUENCE OF TransactionSplit OPTIONAL,
 *     action                  [8] INTEGER OPTIONAL,
 *     newSecurity             [9] INTEGER OPTIONAL,
 *     newShares              [10] DOUBLE OPTIONAL,
 *     accountingMethod       [11] INTEGER OPTIONAL }
 * </pre>
 */
public final class TransactionRecord implements Cloneable {

    /** Buy a security */
    public static final int BUY=1;

    /** Sell a security */
    public static final int SELL=2;

    /** Security income */
    public static final int INCOME=3;

    /** Security expense */
    public static final int EXPENSE=4;

    /** Stock split */
    public static final int SPLIT=5;

    /** Reinvest dividend or interest */
    public static final int REINVEST=6;

    /** Return of capital */
    public static final int RETURN_OF_CAPITAL=7;
    
    /** Bond amortization (bond bought at a premium) */
    public static final int AMORTIZATION=8;
    
    /** Bond accretion (bond bought at a discount) */
    public static final int ACCRETION=9;
    
    /** Exchange shares */
    public static final int EXCHANGE=10;
    
    /** Receive shares in a new company */
    public static final int SPIN_OFF=11;

    /** Supported transaction actions */
    private static final int[] actions =
        {BUY, SELL, INCOME, EXPENSE, SPLIT, REINVEST, RETURN_OF_CAPITAL, AMORTIZATION, 
            ACCRETION, EXCHANGE, SPIN_OFF};
    
    /** Transaction action strings */
    private static final String[] actionStrings = {
        "Buy", "Sell", "Income", "Expense", "Split", "Reinvest", "RtnCapital", 
        "Amortization", "Accretion", "Exchange", "SpinOff"};
    
    /** Accounting method - FIFO */
    public static final int FIFO=0;
    
    /** Accounting method - LIFO */
    public static final int LIFO=1;
    
    /** Accounting method - Average cost */
    public static final int AVG_COST=2;
    
    /** Source account reconcile is pending */
    public static final int SOURCE_PENDING=1;

    /** Source account reconciled */
    public static final int SOURCE_RECONCILED=2;

    /** Target account reconcile is pending */
    public static final int TARGET_PENDING=4;

    /** Target account reconciled */
    public static final int TARGET_RECONCILED=8;

    /** All database transactions */
    public static List<TransactionRecord> transactions;

    /** The encoded TransactionRecord ASN.1 tag identifier */
    private static final byte tagID=(byte)(Asn1Stream.ASN1_APPLICATION+5);

    /** Transaction date */
    private Date date;

    /** Transaction account */
    private AccountRecord account;

    /** Transfer account */
    private AccountRecord transferAccount;

    /** Transaction category */
    private CategoryRecord category;

    /** Investment security */
    private SecurityRecord security;
    
    /** New investment security */
    private SecurityRecord newSecurity;

    /** Transaction amount as it pertains to the transaction account */
    private double amount;

    /** Check number */
    private int checkNumber;

    /** Name */
    private String name;

    /** Memo */
    private String memo;

    /** Number of shares */
    private double shares;
    
    /** Number of new shares */
    private double newShares;

    /** Share price */
    private double sharePrice;

    /** Commission */
    private double commission;

    /** Transaction action */
    private int action;
    
    /** Accounting method */
    private int accountingMethod;

    /** Transaction reconciled flags */
    private int reconciled;

    /** Transaction splits */
    private List<TransactionSplit> splits;

    /** References have been cleared */
    private boolean referencesCleared=false;

    /** This is an expanded transaction */
    private boolean expandedTransaction=false;

    /**
     * Create a new transaction
     */
    public TransactionRecord(Date date, AccountRecord account) {
        if (date == null)
            throw new NullPointerException("No transaction date supplied");

        if (account == null)
            throw new NullPointerException("No transaction account supplied");

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        this.date = cal.getTime();
        this.account = account;
        account.addReference();
        this.name = new String();
        this.memo = new String();
    }

    /**
     * Create a transaction record from an encoded byte stream
     *
     * @param       data            Encoded byte stream for the record
     * @exception   DBException     Unable to decode object stream
     */
    public TransactionRecord(byte[] data) throws DBException {
        if (data == null)
            throw new NullPointerException("No encoded data supplied");

        DecodeStream stream = new DecodeStream(data);

        try {

            //
            //  Validate the application identifier tag
            //
            if (stream.getTag() != tagID)
                throw new DBException("Not an encoded TransactionRecord object");

            //
            //  Get the TransactionRecord sequence
            //
            DecodeStream seq = stream.getSequence(true);

            //
            //  Decode the transaction date, account, name, memo, amount
            //  and reconciled flags
            //
            date = seq.decodeTime(false);
            int accountID = seq.decodeInteger(false);
            name = seq.decodeString(false);
            memo = seq.decodeString(false);
            amount = seq.decodeDouble(false);
            reconciled = seq.decodeInteger(false);

            account = AccountRecord.getAccount(accountID);
            if (account == null)
                throw new DBException("Account "+accountID+" is not defined");
            
            account.addReference();

            //
            //  The transaction category is encoded as an optional context-specific
            //  field with identifier 0
            //
            if (seq.getLength() != 0 &&
                        seq.getTag() == (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+0)) {
                int categoryID = seq.decodeInteger(true);
                category = CategoryRecord.getCategory(categoryID);
                if (category == null)
                    throw new DBException("Category "+categoryID+" is not defined");
                
                category.addReference();
            }

            //
            //  The transfer account is encoded as an optional context-specific
            //  field with identifier 1
            //
            if (seq.getLength() != 0 &&
                        seq.getTag() == (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+1)) {
                int transferID = seq.decodeInteger(true);
                transferAccount = AccountRecord.getAccount(transferID);
                if (transferAccount == null)
                    throw new DBException("Transfer account "+transferID+" is not defined");
                
                transferAccount.addReference();
            }

            //
            //  The check number is encoded as an optional context-specific
            //  field with identifier 2
            //
            if (seq.getLength() != 0 &&
                        seq.getTag() == (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+2))
                checkNumber = seq.decodeInteger(true);

            //
            //  The security is encoded as an optional context-specific
            //  field with identifier 3
            //
            if (seq.getLength() != 0 &&
                        seq.getTag() == (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+3)) {
                int securityID = seq.decodeInteger(true);
                security = SecurityRecord.getSecurity(securityID);
                if (security == null)
                    throw new DBException("Security "+securityID+" is not defined");
                
                security.addReference();
            }

            //
            //  The number of shares is encoded as an optional context-specific
            //  field with identifier 4
            //
            if (seq.getLength() != 0 &&
                        seq.getTag() == (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+4))
                shares = seq.decodeDouble(true);

            //
            //  The share price is encoded as an optional context-specific
            //  field with identifier 5
            //
            if (seq.getLength() != 0 &&
                        seq.getTag() == (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+5))
                sharePrice = seq.decodeDouble(true);

            //
            //  The commission is encoded as an optional context-specific
            //  field with identifier 6
            //
            if (seq.getLength() != 0 &&
                        seq.getTag() == (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+6))
                commission = seq.decodeDouble(true);

            //
            //  The splits are encoded as an optional context-specific
            //  field with identifier 7
            //
            if (seq.getLength() != 0 &&
                        seq.getTag() == (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+7)) {
                DecodeStream splitsSeq = seq.getSequence(true);
                splits = new ArrayList<>(5);

                while (splitsSeq.getLength() != 0) {
                    TransactionSplit s = new TransactionSplit(splitsSeq);
                    splits.add(s);
                }
            }

            //
            //  The action is encoded as an optional context-specific field
            //  with identifier 8
            //
            if (seq.getLength() != 0 &&
                        seq.getTag() == (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+8))
                action = seq.decodeInteger(true);
            
            //
            // The new security is encoded as an optional context-specific field
            // with identifier 9
            //
            if (seq.getLength() != 0 &&
                        seq.getTag() == (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+9)) {
                int securityID = seq.decodeInteger(true);
                newSecurity = SecurityRecord.getSecurity(securityID);
                if (newSecurity == null)
                    throw new DBException("Security "+securityID+" is not defined");
                
                newSecurity.addReference();
            }
            
            //
            // The number of new security shares is encoded as an optional context-specific
            // field with identifier 10
            //
            if (seq.getLength() != 0 &&
                        seq.getTag() == (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+10))
                newShares = seq.decodeDouble(true);
            
            //
            // The accounting method is encoded as an optional context-specific
            // field with identifier 11
            //
            if (seq.getLength() != 0 &&
                        seq.getTag() == (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+11))
                accountingMethod = seq.decodeInteger(true);

            //
            //  Check for unconsummed data
            //
            if (seq.getLength() != 0)
                throw new DBException("Unconsummed data in TransactionRecord sequence");
            
        } catch (Asn1Exception exc) {
            
            throw new DBException("ASN.1 decode error", exc);
        }
    }

    /**
     * Test if the supplied byte stream represents an encoded TransactionRecord object
     *
     * @param       data            The encoded byte stream
     * @return                      TRUE if TransactionRecord object
     */
    public static boolean isEncodedStream(byte[] data) {
        if (data == null)
            throw new NullPointerException("Null byte stream reference");

        if (data.length == 0)
            return false;

        return (data[0]==(byte)(tagID|Asn1Stream.ASN1_CONSTRUCTED) ? true : false);
    }

    /**
     * Encode the TransactionRecord object
     *
     * @return                      The encoded byte stream for the object
     */
    public byte[] encode() {
        int seqLength = 0;
        EncodeStream stream = new EncodeStream(256);

        //
        //  The accounting method is encoded as an optional context-specific
        //  field with identifier 11
        //
        if (accountingMethod != 0)
            seqLength += stream.encodeInteger(accountingMethod,
                                             (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+11));
        
        //
        //  The number of new security shares is encoded as an optional context-specific
        //  field with identifier 10
        //
        //  The new security is encoded as an optional context-specific field
        //  with identifier 9
        //
        if (newShares != 0.0) {
            seqLength += stream.encodeDouble(newShares,
                                             (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+10));
            seqLength += stream.encodeInteger(newSecurity.getID(),
                                              (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+9));
        }
        
        //
        //  The action is encoded as an optional context-specific field
        //  with identifier 8
        //
        if (action != 0)
            seqLength += stream.encodeInteger(action,
                                              (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+8));

        //
        //  The transaction splits are encoded as an optional context-specific
        //  field with identifier 7
        //
        if (splits != null && !splits.isEmpty()) {
            int splitsLength = 0;

            for (int i=splits.size()-1; i>=0; i--)
                splitsLength += splits.get(i).encode(stream);

            seqLength += stream.makeSequence(splitsLength,
                                             (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+7));
        }

        //
        //  The commission is encoded as an optional context-specific field
        //  with identifier 6
        //
        if (commission != 0.0)
            seqLength += stream.encodeDouble(commission,
                                             (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+6));

        //
        //  The share price is encoded as an optional context-specific field
        //  with identifier 5
        //
        if (sharePrice != 0.0)
            seqLength += stream.encodeDouble(sharePrice,
                                             (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+5));

        //
        //  The number of shares is encoded as an optional context-specific field
        //  with identifier 4
        //
        if (shares != 0.0)
            seqLength += stream.encodeDouble(shares,
                                             (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+4));

        //
        //  The security is encoded as an optional context-specific field
        //  with identifier 3
        //
        if (security != null)
            seqLength += stream.encodeInteger(security.getID(),
                                              (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+3));

        //
        //  The check number is encoded as an optional context-specific field
        //  with identifier 2
        //
        if (checkNumber != 0)
            seqLength += stream.encodeInteger(checkNumber,
                                              (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+2));

        //
        //  The transfer account is encoded as an optional context-specific field
        //  with identifier 1
        //
        if (transferAccount != null)
            seqLength += stream.encodeInteger(transferAccount.getID(),
                                              (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+1));

        //
        //  The category is encoded as an optional context-specific field
        //  with identifier 0
        //
        if (category != null)
            seqLength += stream.encodeInteger(category.getID(),
                                              (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+0));

        //
        //  Encode the reconciled flags, amount, memo, name, account and date
        //
        seqLength += stream.encodeInteger(reconciled);
        seqLength += stream.encodeDouble(amount);
        seqLength += stream.encodeString(memo);
        seqLength += stream.encodeString(name);
        seqLength += stream.encodeInteger(account.getID());
        seqLength += stream.encodeTime(date);

        //
        //  Make the TransactionRecord sequence
        //
        stream.makeSequence(seqLength, tagID);
        return stream.getData();
    }

    /**
     * Get the transaction date
     *
     * @return                      Date reference
     */
    public Date getDate() {
        return date;
    }

    /**
     * Get the transaction account
     *
     * @return                      Account reference
     */
    public AccountRecord getAccount() {
        return account;
    }

    /**
     * Get the transaction amount
     *
     * @return                      Amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Set the transaction amount.  The amount is maintained with 2 decimal digits.
     *
     * @param       amount          Amount
     */
    public void setAmount(double amount) {
        this.amount = (double)Math.round(amount*100.0)/100.0;
    }

    /**
     * Get the number of shares
     *
     * @return                      Shares
     */
    public double getShares() {
        return shares;
    }

    /**
     * Set the number of shares.  The number of shares is maintained with
     * 4 decimal digits.
     *
     * @param       shares          Shares
     */
    public void setShares(double shares) {
        this.shares = (double)Math.round(shares*10000.0)/10000.0;
    }
    
    /**
     * Get the number of new shares
     * 
     * @return                      Shares
     */
    public double getNewShares() {
        return newShares;
    }
    
    /**
     * Set the number of new shares.  The number of shares is maintained with
     * 4 decimal digits.
     * 
     * @param       shares          Shares
     */
    public void setNewShares(double shares) {
        newShares = (double)Math.round(shares*10000.0)/10000.0;
    }

    /**
     * Get the share price
     *
     * @return                      Share price
     */
    public double getSharePrice() {
        return sharePrice;
    }

    /**
     * Set the share price.  The share price is maintained with 4 decimal digits.
     *
     * @param       price           Share price
     */
    public void setSharePrice(double price) {
        sharePrice = (double)Math.round(price*10000.0)/10000.0;
    }

    /**
     * Get the commission
     *
     * @return                      Commission
     */
    public double getCommission() {
        return commission;
    }

    /**
     * Set the commission.  The commission is maintained with 2 decimal digits.
     *
     * @param       amount          Commission
     */
    public void setCommission(double amount) {
        commission = (double)Math.round(amount*100.0)/100.0;
    }

    /**
     * Get the transaction category
     *
     * @return                      CategoryRecord reference or null
     */
    public CategoryRecord getCategory() {
        return category;
    }

    /**
     * Set the transaction category
     *
     * @param       category        Category
     */
    public void setCategory(CategoryRecord category) {
        if (this.category != null)
            this.category.removeReference();

        this.category = category;

        if (this.category != null)
            this.category.addReference();
    }

    /**
     * Get the transfer account
     *
     * @return                      AccountRecord reference or null
     */
    public AccountRecord getTransferAccount() {
        return transferAccount;
    }

    /**
     * Set the transfer account
     *
     * @param       account         Transfer account
     */
    public void setTransferAccount(AccountRecord account) {
        if (transferAccount != null)
            transferAccount.removeReference();

        transferAccount = account;

        if (transferAccount != null)
            transferAccount.addReference();
    }

    /**
     * Get the security
     *
     * @return                      SecurityRecord reference or null
     */
    public SecurityRecord getSecurity() {
        return security;
    }

    /**
     * Set the security
     *
     * @param       security        Security
     */
    public void setSecurity(SecurityRecord security) {
        if (this.security != null)
            this.security.removeReference();

        this.security = security;

        if (this.security != null)
            this.security.addReference();
    }
    
    /**
     * Get the new security
     * 
     * @return                      SecurityRecord reference or null
     */
    public SecurityRecord getNewSecurity() {
        return newSecurity;
    }
    
    /**
     * Set the new security
     *
     * @param       security        Security
     */
    public void setNewSecurity(SecurityRecord security) {
        if (newSecurity != null)
            newSecurity.removeReference();

        newSecurity = security;

        if (newSecurity != null)
            newSecurity.addReference();
    }

    /**
     * Get the name
     *
     * @return                      String reference
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name
     *
     * @param       name            Name
     */
    public void setName(String name) {
        if (name == null)
            throw new NullPointerException("No name supplied");

        this.name = name;
    }

    /**
     * Get the memo
     *
     * @return                      String reference
     */
    public String getMemo() {
        return memo;
    }

    /**
     * Set the memo
     *
     * @param       memo            Memo
     */
    public void setMemo(String memo) {
        if (memo == null)
            throw new NullPointerException("No memo supplied");

        this.memo = memo;
    }

    /**
     * Get the check number
     *
     * @return                      Check number or zero
     */
    public int getCheckNumber() {
        return checkNumber;
    }

    /**
     * Set the check number
     *
     * @param       checkNumber     Check number
     */
    public void setCheckNumber(int checkNumber) {
        this.checkNumber = checkNumber;
    }
    
    /**
     * Get the accounting method for a SELL transaction
     * 
     * @return                      Accounting method
     */
    public int getAccountingMethod() {
        return accountingMethod;
    }
    
    /**
     * Set the accounting method for a SELL transaction
     * 
     * @param       accountingMethod    Accounting method
     */
    public void setAccountingMethod(int accountingMethod) {
        if (accountingMethod != FIFO && accountingMethod != LIFO &&
                                        accountingMethod != AVG_COST)
            throw new IllegalArgumentException("Transaction accounting method "+accountingMethod+" is invalid");
        
        this.accountingMethod = accountingMethod;
    }

    /**
     * Get the action
     *
     * @return                      Action or zero
     */
    public int getAction() {
        return action;
    }

    /**
     * Set the action
     *
     * @param       action          Action
     */
    public void setAction(int action) {
        if (action != 0) {
            boolean validAction = false;

            for (int i=0; i<actions.length; i++) {
                if (actions[i] == action) {
                    validAction = true;
                    break;
                }
            }

            if (!validAction)
                throw new IllegalArgumentException("Transaction action "+action+" is invalid");
        }

        this.action = action;
    }

    /**
     * Get the displayable string for an action
     *
     * @param       action          The action
     * @return                      Displayable string
     */
    public static String getActionString(int action) {
        String retValue = null;
        
        for (int i=0; i<actions.length; i++) {
            if (actions[i] == action) {
                retValue = actionStrings[i];
                break;
            }
        }
        
        if (retValue == null)
            retValue = new String();

        return retValue;
    }
    
    /**
     * Get the supported actions
     *
     * @return                      Array of supported actions
     */
    public static int[] getActions() {
        return actions;
    }
    
    /**
     * Get the actions strings
     *
     * @return                      Array of action strings
     */
    public static String[] getActionStrings() {
        return actionStrings;
    }

    /**
     * Get the transaction splits
     *
     * @return                  Splits list or null
     */
    public List<TransactionSplit> getSplits() {
        return splits;
    }

    /**
     * Set the transaction splits.  Existing transaction splits will be removed
     * and any references will be cleared.
     *
     * @param       splits          Splits list
     */
    public void setSplits(List<TransactionSplit> splits) {
        //
        // Nothing to do if the new splits list is the same as the existing
        // splits list
        //
        if (this.splits == splits)
            return;

        //
        // Release the existing splits
        //
        if (this.splits != null) {
            ListIterator<TransactionSplit> li = splits.listIterator();
            while (li.hasNext()) {
                TransactionSplit split = li.next();
                split.clearReferences();
            }

            splits.clear();
        }

        //
        // Set the new transaction splits
        //
        this.splits = splits;
    }

    /**
     * Set the reconciled flags
     *
     * @param       reconciled      Reconciled state
     */
    public void setReconciled(int reconciled) {
        this.reconciled = reconciled;
    }

    /**
     * Get the reconciled flags
     *
     * @return                      Reconciled state
     */
    public int getReconciled() {
        return reconciled;
    }

    /**
     * Set the expanded transaction flag (an expanded transaction is a
     * temporary transaction created from another transaction)
     *
     * @param       type            TRUE if this is an expanded transaction
     */
    public void setExpandedTransaction(boolean type) {
        expandedTransaction = type;
    }

    /**
     * Get the expanded transaction state
     *
     * @return                      TRUE if this is an expanded transaction
     */
    public boolean isExpandedTransaction() {
        return expandedTransaction;
    }

    /**
     * Clear account, category and security references.  This method
     * should be called when the transaction is no longer active
     * instead of waiting for the transaction object to be finalized by the
     * Java garbage collector
     */
    public void clearReferences() {
        if (!referencesCleared) {
            if (account != null)
                account.removeReference();

            if (transferAccount != null)
                transferAccount.removeReference();

            if (category != null)
                category.removeReference();

            if (security != null)
                security.removeReference();
            
            if (newSecurity != null)
                newSecurity.removeReference();

            if (splits != null) {
                for (TransactionSplit split : splits)
                    split.clearReferences();
            }

            referencesCleared = true;
        }
    }

    /**
     * Update reference counts when this object is finalized
     */
    protected void finalize() {
        clearReferences();
    }

    /**
     * Insert a transaction into the current transaction list
     *
     * @param       transaction     Transaction to be inserted
     * @return                      The index of the inserted transaction
     */
    public static int insertTransaction(TransactionRecord transaction) {
        return insertTransaction(transactions, transaction);
    }
    
    /**
     * Insert a transaction into a transaction list
     *
     * @param       transactionList     The transaction list
     * @param       transfer            Transaction to be inserted
     * @return                          The index of the inserted transaction
     */
    public static int insertTransaction(List<TransactionRecord> transactions, TransactionRecord transaction) {
        int index, lowIndex, highIndex;
        Date date = transaction.getDate();
        int lastElem = transactions.size()-1;
        if (lastElem < 0) {
            transactions.add(transaction);
            highIndex = 0;
        } else if (date.compareTo(transactions.get(lastElem).getDate()) >= 0) {
            transactions.add(transaction);
            highIndex = lastElem+1;
        } else {
            lowIndex = -1;
            highIndex = lastElem;
            while (highIndex-lowIndex > 1) {
                index = (highIndex-lowIndex)/2+lowIndex;
                if (date.compareTo(transactions.get(index).getDate()) < 0)
                    highIndex = index;
                else
                    lowIndex = index;
            }

            transactions.add(highIndex, transaction);
        }

        return highIndex;
    }

    /**
     * Clone the transaction.  The reference counts will be updated to reflect
     * the new transaction.
     *
     * @return                      Cloned transaction
     */
    public Object clone() {
        Object clonedObject;
        try {
            clonedObject = super.clone();
            TransactionRecord t = (TransactionRecord)clonedObject;
            if (account != null)
                account.addReference();
            if (transferAccount != null)
                transferAccount.addReference();
            if (category != null)
                category.addReference();
            if (security != null)
                security.addReference();
            if (splits != null)
                t.splits = TransactionSplit.copySplits(splits);
        } catch (CloneNotSupportedException exc) {
            throw new UnsupportedOperationException("Unable to clone transaction", exc);
        }

        return clonedObject;
    }
}
