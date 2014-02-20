package MyMoney;

import java.util.*;

import Asn1.*;

/**
 * Database scheduled transaction record
 * <p>
 * The following schedule types are provided:
 * <ul>
 * <li>SINGLE
 * <li>WEEKLY
 * <li>BIWEEKLY
 * <li>MONTHLY
 * </ul>
 * <p>
 * All scheduled transaction records are contained in the <code>transactions</code>
 * list.  The entries are added to the list in ascending date sequence.
 * <p>
 * The scheduled transaction record is encoded as follows:
 * <pre>
 *   ScheduleRecord ::= [APPLICATION 7] SEQUENCE {
 *     type                        INTEGER,
 *     date                        GENERALTIME,
 *     description                 BMPSTRING,
 *     account                     INTEGER,
 *     amount                      DOUBLE,
 *     category                [0] INTEGER OPTIONAL,
 *     transferAccount         [1] INTEGER OPTIONAL,
 *     splits                  [2] SEQUENCE OF TransactionSplit OPTIONAL }
 * </pre>
 */
public final class ScheduleRecord {

    /** Single transaction */
    public static final int SINGLE=1;

    /** Weekly transaction */
    public static final int WEEKLY=2;

    /** Biweekly transaction */
    public static final int BIWEEKLY=3;

    /** Monthly transaction */
    public static final int MONTHLY=4;

    /** Supported schedule types */
    private static final int[] scheduleTypes = {SINGLE, WEEKLY, BIWEEKLY, MONTHLY};
    
    /** Schedule type strings */
    private static final String[] scheduleTypeStrings = {"Single", "Weekly", "Biweekly", "Monthly"};

    /** Scheduled transactions */
    public static List<ScheduleRecord> transactions;

    /** The encoded ScheduleRecord ASN.1 tag identifier */
    private static final byte tagID=(byte)(Asn1Stream.ASN1_APPLICATION+7);

    /** Schedule type */
    private int type;

    /** Scheduled date */
    private Date date;

    /** Transaction account */
    private AccountRecord account;

    /** Transfer account */
    private AccountRecord transferAccount;

    /** Transaction category */
    private CategoryRecord category;

    /** Transaction amount */
    private double amount;

    /** Transaction description */
    private String description;

    /** Transaction splits */
    private List<TransactionSplit> splits;

    /** References have been cleared */
    private boolean referencesCleared=false;

    /**
     * Create a new scheduled transaction
     *
     * @param       type            Schedule type
     * @param       date            Scheduled date
     * @param       account         Account
     */
    public ScheduleRecord(int type, Date date, AccountRecord account) {
        if (date == null)
            throw new NullPointerException("No transaction date supplied");

        if (account == null)
            throw new NullPointerException("No transaction account supplied");

        this.type = type;
        this.account = account;
        account.addReference();
        this.description = new String();
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        this.date = cal.getTime();
    }

    /**
     * Create a schedule record from an encoded byte stream
     *
     * @param       data            Encoded byte stream for the record
     * @exception   DBException     Unable to decode object stream
     */
    public ScheduleRecord(byte[] data) throws DBException {
        if (data == null)
            throw new NullPointerException("No encoded data supplied");

        DecodeStream stream = new DecodeStream(data);

        try {

            //
            //  Validate the application identifier tag
            //
            if (stream.getTag() != tagID)
                throw new DBException("Not an encoded ScheduleRecord object");

            //
            //  Get the ScheduleRecord sequence
            //
            DecodeStream seq = stream.getSequence(true);

            //
            //  Decode the schedule type, transaction date, description, account
            //  and amount
            //
            type = seq.decodeInteger(false);
            date = seq.decodeTime(false);
            description = seq.decodeString(false);
            int accountID = seq.decodeInteger(false);
            amount = seq.decodeDouble(false);

            for (AccountRecord a : AccountRecord.accounts) {
                if (a.getID() == accountID) {
                    account = a;
                    account.addReference();
                    break;
                }
            }

            if (account == null)
                throw new DBException("Account "+accountID+" is not defined");

            //
            //  The category is encoded as an optional context-specific
            //  field with identifier 0
            //
            if (seq.getLength() != 0 &&
                        seq.getTag() == (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+0)) {
                int categoryID = seq.decodeInteger(true);

                for (CategoryRecord c : CategoryRecord.categories) {
                    if (c.getID() == categoryID) {
                        category = c;
                        category.addReference();
                        break;
                    }
                }

                if (category == null)
                    throw new DBException("Category "+categoryID+" is not defined");
            }

            //
            //  The transfer account is encoded as an optional context-specific
            //  field with identifier 1
            //
            if (seq.getLength() != 0 &&
                        seq.getTag() == (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+1)) {
                int transferID = seq.decodeInteger(true);

                for (AccountRecord a : AccountRecord.accounts) {
                    if (a.getID() == transferID) {
                        transferAccount = a;
                        transferAccount.addReference();
                        break;
                    }
                }

                if (transferAccount == null)
                    throw new DBException("Transfer account "+transferID+" is not defined");
            }

            //
            //  The splits are encoded as an optional context-specific
            //  field with identifier 2
            //
            if (seq.getLength() != 0 &&
                        seq.getTag() == (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+2)) {
                DecodeStream splitsSeq = seq.getSequence(true);
                splits = new ArrayList<>(5);

                while (splitsSeq.getLength() != 0) {
                    TransactionSplit s = new TransactionSplit(splitsSeq);
                    splits.add(s);
                }
            }

            //
            //  Check for unconsummed data
            //
            if (seq.getLength() != 0)
                throw new DBException("Unconsummed data in ScheduleRecord sequence");
        } catch (Asn1Exception exc) {
            throw new DBException("ASN.1 decode error", exc);
        }
    }

    /**
     * Test if the supplied byte stream represents an encoded ScheduleRecord object
     *
     * @param       data            The encoded byte stream
     * @return                      TRUE if ScheduleRecord object
     */
    public static boolean isEncodedStream(byte[] data) {
        if (data == null)
            throw new NullPointerException("Null byte stream reference");

        if (data.length == 0)
            return false;

        return (data[0]==(byte)(tagID|Asn1Stream.ASN1_CONSTRUCTED) ? true : false);
    }

    /**
     * Encode the ScheduleRecord object
     *
     * @return                      The encoded byte stream for the object
     */
    public byte[] encode() {
        int seqLength = 0;
        EncodeStream stream = new EncodeStream(256);

        //
        //  The transaction splits are encoded as an optional context-specific
        //  field with identifier 2
        //
        if (splits != null && !splits.isEmpty()) {
            int splitsLength = 0;

            for (int i=splits.size()-1; i>=0; i--)
                splitsLength += splits.get(i).encode(stream);

            seqLength += stream.makeSequence(splitsLength,
                                             (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+2));
        }

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
        //  Encode the amount, account, date and schedule type
        //
        seqLength += stream.encodeDouble(amount);
        seqLength += stream.encodeInteger(account.getID());
        seqLength += stream.encodeString(description);
        seqLength += stream.encodeTime(date);
        seqLength += stream.encodeInteger(type);

        //
        //  Make the ScheduleRecord sequence
        //
        stream.makeSequence(seqLength, tagID);
        return stream.getData();
    }

    /**
     * Get the schedule type
     *
     * @return                      Schedule type
     */
    public int getType() {
        return type;
    }

    /**
     * Set the schedule type
     *
     * @param       type            Schedule type
     */
    public void setType(int type) {
        boolean validType = false;

        for (int i=0; i<scheduleTypes.length; i++) {
            if (scheduleTypes[i] == type) {
                validType = true;
                break;
            }
        }

        if (!validType)
            throw new IllegalArgumentException("Schedule type "+type+" is invalid");

        this.type = type;
    }

    /**
     * Get the known schedule types
     *
     * @return                      Array of schedule types
     */
    public static int[] getTypes() {
        return scheduleTypes;
    }
    
    /**
     * Get the schedule type strings
     *
     * @return                      Array of schedule type strings
     */
    public static String[] getTypeStrings() {
        return scheduleTypeStrings;
    }

    /**
     * Get the displayable string for a schedule type
     *
     * @param       type            The schedule type
     * @return                      Displayable string
     */
    public static String getTypeString(int type) {
        String retValue = null;
        for (int i=0; i<scheduleTypes.length; i++) {
            if (scheduleTypes[i] == type) {
                retValue = scheduleTypeStrings[i];
                break;
            }
        }
        
        if (retValue == null)
            retValue = new String();

        return retValue;
    }

    /**
     * Get the scheduled date
     *
     * @return                      Date reference
     */
    public Date getDate() {
        return date;
    }

    /**
     * Set the scheduled date
     *
     * @param       date            Scheduled date
     */
    public void setDate(Date date) {
        if (date == null)
            throw new NullPointerException("No date provided");

        this.date = date;
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
     * Set the transaction account
     *
     * @param       account         Account
     */
    public void setAccount(AccountRecord account) {
        if (account == null)
            throw new NullPointerException("No account provided");

        this.account.removeReference();
        this.account = account;
        this.account.addReference();
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
     * Set the transaction amount.  The amount is maintained with
     * 2 decimal digits.
     *
     * @param       amount          Amount
     */
    public void setAmount(double amount) {
        this.amount = (double)Math.round(amount*100.0)/100.0;
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
     * Get the description
     *
     * @return                      String reference
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description
     *
     * @param       description     Transaction description
     */
    public void setDescription(String description) {
        if (description == null)
            throw new NullPointerException("No description supplied");

        this.description = description;
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
     * Clear account and category references
     *
     * This method should be called when the transaction is no longer active
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

            if (splits != null) {
                for (TransactionSplit split : splits)
                    split.clearReferences();
            }

            referencesCleared = true;
        }
    }

    /**
     * Insert a scheduled transaction into the transaction list
     *
     * @param       transaction     Transaction to be inserted
     * @return                      The index of the inserted transaction
     */
    public static int insertTransaction(ScheduleRecord transaction) {
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
     * Update reference counts when this object is finalized
     */
    protected void finalize() {
        clearReferences();
    }
}
