package MyMoney;

import java.util.*;
import Asn1.*;

/**
 * Database transaction split record
 * <p>
 * The transaction split record is encoded as follows:
 * <pre>
 *   TransactionSplit ::= [APPLICATION 6] SEQUENCE {
 *     description                 BMPSTRING,
 *     category                    INTEGER,
 *     account                     INTEGER,
 *     amount                      DOUBLE,
 *     reconciled                  INTEGER }
 * </pre>
 */
public final class TransactionSplit implements Cloneable {

    /** The encoded TransactionSplit ASN.1 tag identifier */
    private static final byte tagID=(byte)(Asn1Stream.ASN1_APPLICATION+6);

    /** Split description */
    private String description;

    /** Account */
    private AccountRecord account;

    /** Category */
    private CategoryRecord category;

    /** Split amount as pertains to the source account */
    private double amount;

    /** Split reconciled flags */
    private int reconciled;

    /** References have been cleared */
    private boolean referencesCleared=false;

    /**
     * Create a new transaction split
     */
    public TransactionSplit() {
        description = new String();
    }

    /**
     * Create a transaction split from an encoded stream
     *
     * @param       data            Encoded byte stream
     * @exception   DBException     Unable to decode object stream
     */
    public TransactionSplit(byte[] data) throws DBException {
        if (data == null)
            throw new NullPointerException("Null byte stream reference");

        decode(new DecodeStream(data));
    }

    /**
     * Create a transaction split from an existing stream
     *
     * @param       stream          Decode stream
     * @exception   DBException     Unable to decode object stream
     */
    public TransactionSplit(DecodeStream stream) throws DBException {
        if (stream == null)
            throw new NullPointerException("Null decode stream reference");

        decode(stream);
    }

    /**
     * Decode the TransactionSplit object stream
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
                throw new DBException("Not an encoded TransactionSplit object");

            //
            //  Get the TransactionSplit sequence
            //
            DecodeStream seq = stream.getSequence(true);

            //
            //  Decode the description, category, account, amount and
            //  reconciled state
            //
            description = seq.decodeString(false);
            int categoryID = seq.decodeInteger(false);
            int accountID = seq.decodeInteger(false);
            amount = seq.decodeDouble(false);
            reconciled = seq.decodeInteger(false);

            if (categoryID != 0) {
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

            if (accountID != 0) {
                for (AccountRecord a : AccountRecord.accounts) {
                    if (a.getID() == accountID) {
                        account = a;
                        account.addReference();
                        break;
                    }
                }

                if (account == null)
                    throw new DBException("Account "+accountID+" is not defined");
            }

            //
            //  Check for unconsummed data
            //
            if (seq.getLength() != 0)
                throw new DBException("Unconsummed data in TransactionSplit sequence");

        } catch (Asn1Exception exc) {

            throw new DBException("ASN.1 decode error", exc);
        }
    }

    /**
     * Test if the supplied byte stream represents an encoded TransactionSplit
     *
     * @param       data            The encoded byte stream
     * @return                      TRUE if TransactionSplit object
     */
    public static boolean isEncodedStream(byte[] data) {
        if (data == null)
            throw new NullPointerException("Null byte stream reference");

        if (data.length == 0)
            return false;

        return (data[0]==(byte)(tagID|Asn1Stream.ASN1_CONSTRUCTED) ? true : false);
    }

    /**
     * Encode the TransactionSplit object
     *
     * @return                      The encoded byte stream for the object
     */
    public byte[] encode() {
        EncodeStream stream = new EncodeStream(64);
        encode(stream);
        return stream.getData();
    }

    /**
     * Encode the TransactionSplit object using an existing encode stream
     *
     * @param       stream          The encode stream
     * @return                      The length of the encoded data
     */
    public int encode(EncodeStream stream) {

        //
        //  Encode the reconciled state, amount, account, category and description
        //  (the fields are encoded in reverse order because the stream
        //  is constructed from the end to the beginning)
        //
        int length = stream.encodeInteger(reconciled);
        length += stream.encodeDouble(amount);
        length += stream.encodeInteger((account!=null ? account.getID() : 0));
        length += stream.encodeInteger((category!=null ? category.getID() : 0));
        length += stream.encodeString(description);

        //
        //  Make the TransactionSplit sequence
        //
        return stream.makeSequence(length, tagID);
    }

    /**
     * Get the split description
     *
     * @return                      Description string
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the split description
     *
     * @param       description     Description string
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the split account
     *
     * @return                      Account record or null
     */
    public AccountRecord getAccount() {
        return account;
    }

    /**
     * Set the split account
     *
     * @param       account         Account record
     */
    public void setAccount(AccountRecord account) {
        if (this.account != null)
            this.account.removeReference();

        this.account = account;

        if (this.account != null)
            this.account.addReference();
    }

    /**
     * Get the split category
     *
     * @return                      Category record or null
     */
    public CategoryRecord getCategory() {
        return category;
    }

    /**
     * Set the split category
     *
     * @param       category        Category record
     */
    public void setCategory(CategoryRecord category) {
        if (this.category != null)
            this.category.removeReference();

        this.category = category;

        if (this.category != null)
            this.category.addReference();
    }

    /**
     * Get the split amount
     *
     * @return                      Amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Set the split amount.  The split amount is maintained with
     * 2 decimal digits.
     *
     * @param       amount          Amount
     */
    public void setAmount(double amount) {
        this.amount = (double)Math.round(amount*100.0)/100.0;
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
     * Set the reconciled flags
     *
     * @param       reconciled      Reconciled state
     */
    public void setReconciled(int reconciled) {
        this.reconciled = reconciled;
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

            if (category != null)
                category.removeReference();

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
     * Clone the transaction split.  The reference counts will be updated to reflect
     * the new transaction split.
     *
     * @return                      Cloned transaction split
     */
    public Object clone() {
        Object clonedObject;
        try {
            clonedObject = super.clone();
            TransactionSplit split = (TransactionSplit)clonedObject;
            if (account != null)
                account.addReference();
            if (category != null)
                category.addReference();
        } catch (CloneNotSupportedException exc) {
            throw new UnsupportedOperationException("Unable to clone transaction split", exc);
        }

        return clonedObject;
    }

    /**
     * Make a copy of a splits list
     *
     * @param       splits          The splits to be copied
     * @return                      New splits list
     */
    public static List<TransactionSplit> copySplits(List<TransactionSplit> splits) {
        if (splits == null)
            throw new NullPointerException("No splits provided");

        List<TransactionSplit> newSplits = new ArrayList<TransactionSplit>(splits.size());
        for (TransactionSplit split : splits)
            newSplits.add((TransactionSplit)split.clone());

        return newSplits;
    }
}

