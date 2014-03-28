/**
 * Copyright 2005-2014 Ronald W Hoffman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ScripterRon.MyMoney;
import org.ScripterRon.Asn1.*;

import java.util.*;

/**
 * Database account record.
 * <p>
 * The following account types are provided:
 * <ul>
 * <li>ASSET
 * <li>BANK
 * <li>CREDIT
 * <li>INVESTMENT
 * <li>LOAN
 * </ul>
 * <p>
 * All account records are contained in the <code>accounts</code> sorted set.  The
 * entries in the set are sorted by the account name.  Two account records are
 * equal if they have the same account name.
 * <p>
 * The account record is encoded as follows:
 * <pre>
 *   AccountRecord ::= [APPLICATION 4] SEQUENCE {
 *     recordID                    INTEGER,
 *     accountType                 INTEGER,
 *     accountName                 BMPSTRING,
 *     accountNumber               BMPSTRING,
 *     accountHidden               BOOLEAN,
 *     linkedAccount           [0] INTEGER OPTIONAL,
 *     loanRate                [1] DOUBLE OPTIONAL,
 *     taxDeferred             [2] BOOLEAN OPTIONAL }
 * </pre>
 */
public final class AccountRecord extends DBElement {

    /** Bank account */
    public static final int BANK = 1;

    /** Investment account */
    public static final int INVESTMENT = 2;

    /** Asset account */
    public static final int ASSET = 3;

    /** Loan account */
    public static final int LOAN = 4;

    /** Credit account */
    public static final int CREDIT = 5;

    /** Account types */
    private static final int[] accountTypes = {BANK, INVESTMENT, ASSET, LOAN, CREDIT};
    
    /** Account type strings */
    private static final String[] accountTypeStrings = {"Bank", "Investment", "Asset", "Loan", "Credit"};

    /** The set of defined accounts */
    public static SortedSet<AccountRecord> accounts;
    
    /** The account map */
    private static Map<Integer, AccountRecord> map;

    /** The next AccountRecord identifier */
    private static int nextRecordID = 1;

    /** The encoded AccountRecord ASN.1 tag identifier */
    private static final byte tagID = (byte)(Asn1Stream.ASN1_APPLICATION+4);

    /**
     * The current account balance.  This is an application work area and
     * is not preserved across application restarts.
     */
    public double balance;

    /** Account number (never null) */
    private String accountNumber;

    /** Linked account (only for an investment account) */
    private AccountRecord linkedAccount;

    /** Number of linking accounts (only for a bank account) */
    private int linkCount;

    /** Loan rate (only for a loan account) */
    private double loanRate;
    
    /** Account is tax-deferred */
    private boolean taxDeferred;

    /**
     * Create a new account record
     *
     * @param       name            Account name
     * @param       type            Account type
     */
    public AccountRecord(String name, int type) {
        if (name == null)
            throw new NullPointerException("No account name supplied");

        //
        // Create the new account
        //
        recordID = nextRecordID++;
        elementName = name;
        setType(type);
        accountNumber = new String();
        
        //
        // Add the account to the map
        //
        if (map == null)
            map = new HashMap<>();
        
        map.put(new Integer(recordID), this);
    }

    /**
     * Create an account record from an encoded byte stream
     *
     * @param       data            Encoded byte stream for the record
     * @exception   DBException     Unable to decode object stream
     */
    public AccountRecord(byte[] data) throws DBException {
        if (data == null)
            throw new NullPointerException("No encoded data supplied");

        DecodeStream stream = new DecodeStream(data);

        try {

            //
            //  Validate the application identifier tag
            //
            if (stream.getTag() != tagID)
                throw new DBException("Not an encoded AccountRecord object");

            //
            //  Get the AccountRecord sequence
            //
            DecodeStream seq = stream.getSequence(true);

            //
            //  Decode the record identifier, account type, account name,
            //  account number and account hidden state
            //
            recordID = seq.decodeInteger(false);
            elementType = seq.decodeInteger(false);
            elementName = seq.decodeString(false);
            accountNumber = seq.decodeString(false);
            elementHidden = seq.decodeBoolean(false);

            //
            //  The linked account is encoded as an optional context-specific
            //  field with identifier 0
            //
            if (seq.getLength() != 0 && seq.getTag() == (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+0)) {
                int linkID = seq.decodeInteger(true);
                linkedAccount = AccountRecord.getAccount(linkID);
                if (linkedAccount == null)
                    throw new DBException("Linked account "+linkID+" is not defined");
                
                linkedAccount.setLinkCount(linkedAccount.getLinkCount()+1);
            }

            //
            //  The loan rate is encoded as an optional context-specific
            //  field with identifier 1
            //
            if (seq.getLength() != 0 && seq.getTag() == (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+1))
                loanRate = seq.decodeDouble(true);
            
            //
            //  The tax deferred flag is encoded as an optional context-specific
            //  field with identifier 2
            //
            if (seq.getLength() != 0 && seq.getTag() == (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+2))
                taxDeferred = seq.decodeBoolean(true);

            //
            //  Check for unconsummed data
            //
            if (seq.getLength() != 0)
                throw new DBException("Unconsummed data in AccountRecord sequence");

            //
            //  Update the next record identifier
            //
            if (recordID >= nextRecordID)
                nextRecordID = recordID+1;
        
            //
            // Add the new account to the map
            //
            if (map == null)
                map = new HashMap<>();
        
            map.put(new Integer(recordID), this);
        } catch (Asn1Exception exc) {
            throw new DBException("ASN.1 decode error", exc);
        }
    }

    /**
     * Test if the supplied byte stream represents an encoded AccountRecord object
     *
     * @param       data            The encoded byte stream
     * @return                      TRUE if AccountRecord object
     */
    public static boolean isEncodedStream(byte[] data) {
        if (data == null)
            throw new NullPointerException("Null byte stream reference");

        if (data.length == 0)
            return false;

        return (data[0]==(byte)(tagID|Asn1Stream.ASN1_CONSTRUCTED) ? true : false);
    }

    /**
     * Encode the AccountRecord object
     *
     * @return                      The encoded byte stream for the object
     */
    public byte[] encode() {
        int seqLength = 0;
        EncodeStream stream = new EncodeStream(128);

        //
        //  The tax deferred flag is encoded as an optional context-specific
        //  field with identifier 2
        //
        if (taxDeferred)
            seqLength += stream.encodeBoolean(taxDeferred, (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+2));
            
        //
        //  The loan rate is encoded as an optional context-specific field
        //  with identifier 1
        //
        if (loanRate != 0.0)
            seqLength += stream.encodeDouble(loanRate, (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+1));

        //
        //  The linked account is encoded as an optional context-specific field
        //  with identifier 0
        //
        if (linkedAccount != null)
            seqLength += stream.encodeInteger(linkedAccount.getID(), (byte)(Asn1Stream.ASN1_CONTEXT_SPECIFIC+0));

        //
        //  Encode the account hidden state, account number, account name,
        //  account type and record identifier
        //
        seqLength += stream.encodeBoolean(elementHidden);
        seqLength += stream.encodeString(accountNumber);
        seqLength += stream.encodeString(elementName);
        seqLength += stream.encodeInteger(elementType);
        seqLength += stream.encodeInteger(recordID);

        //
        //  Make the AccountRecord sequence
        //
        stream.makeSequence(seqLength, tagID);
        return stream.getData();
    }
    
    /**
     * Get the account associated with the supplied record identifier.
     *
     * @param       recordID        The record identifier
     * @return                      The account or NULL
     */
    public static AccountRecord getAccount(int recordID) {
        AccountRecord account = null;
        if (map != null)
            account = map.get(new Integer(recordID));
        
        return account;
    }

    /**
     * Get the account number.  The return value will never be null.
     *
     * @return                      The account number
     */
    public String getNumber() {
        return accountNumber;
    }

    /**
     * Set the account number
     *
     * @param       number          The account number
     */
    public void setNumber(String number) {
        if (number == null)
            throw new NullPointerException("No account number supplied");

        accountNumber = number;
    }

    /**
     * Validate and set the account type
     *
     * @param       type            The account type
     */
    public void setType(int type) {
        boolean validType = false;
        for (int i=0; i<accountTypes.length; i++) {
            if (type == accountTypes[i]) {
                validType = true;
                break;
            }
        }

        if (!validType)
            throw new IllegalArgumentException("Account type "+type+" is invalid");

        elementType = type;
    }
    
    /**
     * Set tax-deferred status
     * 
     * @param       deferred        TRUE if account is tax deferred
     */
    public void setTaxDeferred(boolean deferred) {
        taxDeferred = deferred;
    }
    
    /**
     * Check if the account is tax deferred
     * 
     * @return                      TRUE if account is tax deferred
     */
    public boolean isTaxDeferred() {
        return taxDeferred;
    }

    /**
     * Get the known account types
     *
     * @return                      Array of account types
     */
    public static int[] getTypes() {
        return accountTypes;
    }
    
    /**
     * Get the account type strings
     *
     * @return                      Array of account type strings
     */
    public static String[] getTypeStrings() {
        return accountTypeStrings;
    }

    /**
     * Get the displayable string for an account type
     *
     * @param       type            The account type
     * @return                      Displayable string
     */
    public static String getTypeString(int type) {
        String retValue = null;
        for (int index=0; index<accountTypes.length; index++) {
            if (accountTypes[index] == type) {
                retValue = accountTypeStrings[index];
                break;
            }
        }

        return (retValue!=null ? retValue : new String());        
    }

    /**
     * Get the linked account
     *
     * @return                      AccountRecord reference or null
     */
    public AccountRecord getLinkedAccount() {
        return linkedAccount;
    }

    /**
     * Set the linked account
     *
     * @param       account         Linked account
     */
    public void setLinkedAccount(AccountRecord account) {
        if (linkedAccount != null)
            linkedAccount.setLinkCount(linkedAccount.getLinkCount()-1);

        linkedAccount = account;

        if (linkedAccount != null)
            linkedAccount.setLinkCount(linkedAccount.getLinkCount()+1);
    }

    /**
     * Get the loan rate
     *
     * @return                      The loan rate
     */
    public double getLoanRate() {
        return loanRate;
    }

    /**
     * Set the loan rate
     *
     * @param       rate            The loan rate
     */
    public void setLoanRate(double rate) {
        loanRate = rate;
    }

    /**
     * Get the link count
     *
     * @return                      Link count
     */
    public int getLinkCount() {
        return linkCount;
    }

    /**
     * Set the link count
     *
     * @param       count           Link count
     */
    public void setLinkCount(int count) {
        if (count < 0)
            throw new IllegalArgumentException("Link count is negative");

        linkCount = count;
    }
}
