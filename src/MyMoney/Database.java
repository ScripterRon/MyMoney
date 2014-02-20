package MyMoney;

import java.io.*;
import java.util.*;

import RecordIO.*;

/**
 * The MyMoney database consists of ASN1-encoded records describing the accounts, categories,
 * securities and transactions.
 */
public final class Database {
    
    /** The database file */
    private File file;
    
    /**
     * Create a new database
     *
     * @param       path            The database file path
     */
    public Database(String path) {
        file = new File(path);
    }

    /**
     * Create a new database
     *
     * @param       file            The database file
     */
    public Database(File file) {
        this.file = file;
    }
    
    /**
     * Get the database file name
     *
     * @return                      The database file name
     */
    public String getName() {
        return file.getName();
    }

    /**
     * Load the database
     *
     * @exception   DBException     Unable to load application data
     * @exception   IOException     An I/O error occurred
     */
    public void load() throws DBException, IOException {
        RecordInputStream in = null;
            
        //
        // Reset the database structures
        //
        AccountRecord.accounts.clear();
        CategoryRecord.categories.clear();
        SecurityRecord.securities.clear();
        TransactionRecord.transactions.clear();
        ScheduleRecord.transactions.clear();

        //
        // Read the database records
        //
        try {
            byte[] stream;
            if (file.exists()) {
                in = new RecordInputStream(file);
                while ((stream=in.readRecord()) != null) {
                    try {
                        if (AccountRecord.isEncodedStream(stream)) {
                            AccountRecord a = new AccountRecord(stream);
                            while (!AccountRecord.accounts.add(a))
                                a.setName(a.getName()+" NEW");
                        } else if (CategoryRecord.isEncodedStream(stream)) {
                            CategoryRecord c = new CategoryRecord(stream);
                            while (!CategoryRecord.categories.add(c))
                                c.setName(c.getName()+" NEW");
                        } else if (SecurityRecord.isEncodedStream(stream)) {
                            SecurityRecord s = new SecurityRecord(stream);
                            while (!SecurityRecord.securities.add(s))
                                s.setName(s.getName()+" NEW");
                        } else if (TransactionRecord.isEncodedStream(stream)) {
                            TransactionRecord t = new TransactionRecord(stream);
                            TransactionRecord.transactions.add(t);
                        } else if (ScheduleRecord.isEncodedStream(stream)) {
                            ScheduleRecord r = new ScheduleRecord(stream);
                            ScheduleRecord.transactions.add(r);
                        } else {
                            throw new DBException("Unrecognized encoded record type");
                        }
                    } catch (DBException exc) {
                        Main.logException("Invalid transaction discarded", exc);
                        Main.dataModified = true;
                    }
                }

                in.close();
                in = null;
            }
        } finally {
            if (in != null) {
                AccountRecord.accounts.clear();
                CategoryRecord.categories.clear();
                SecurityRecord.securities.clear();
                TransactionRecord.transactions.clear();
                ScheduleRecord.transactions.clear();
                in.close();
            }
        }
    }

    /**
     * Save the current application database.  Accounts, categories, securities, transactions
     * and scheduled transactions will be saved.
     *
     * @exception   IOException     An I/O error occurred
     */
    public void save() throws IOException {
        RecordOutputStream out = null;
        boolean cleanup = false;
        File saveFile = new File(file.getPath()+".save");

        try {

            //
            // Create the temporary save file
            //
            out = new RecordOutputStream(saveFile);
            cleanup = true;

            //
            // Write out linked accounts first since we need to have
            // them defined before we can define the linking accounts
            // when the database is loaded again
            //
            for (AccountRecord a : AccountRecord.accounts)
                if (a.getLinkCount() != 0)
                    out.writeRecord(a.encode());

            //
            // Write out non-linked accounts next
            //
            for (AccountRecord a : AccountRecord.accounts)
                if (a.getLinkCount() == 0)
                    out.writeRecord(a.encode());

            //
            // Write out the categories
            //
            for (CategoryRecord c : CategoryRecord.categories)
                out.writeRecord(c.encode());

            //
            // Write out the securities
            //
            for (SecurityRecord s : SecurityRecord.securities)
                out.writeRecord(s.encode());

            //
            // Write out the transactions
            //
            for (TransactionRecord t : TransactionRecord.transactions)
                out.writeRecord(t.encode());

            //
            // Write out the scheduled transactions
            //
            for (ScheduleRecord r : ScheduleRecord.transactions)
                out.writeRecord(r.encode());

            //
            // Close and rename the save file
            //
            out.close();
            out = null;
            
            if (file.exists())
                if (!file.delete())
                    throw new IOException("Unable to delete "+file.getName());

            cleanup = false;
            if (!saveFile.renameTo(file))
                throw new IOException("Unable to rename "+saveFile.getName());
        } finally {
            if (cleanup) {
                if (out != null)
                    out.close();

                if (saveFile.exists())
                    saveFile.delete();
            }
        }
    }
    
    /**
     * Save archived transactions.  Accounts, categories and securities will also be
     * saved.  Scheduled transactions will not be saved.
     *
     * @param       transactions    The archived transactions
     * @exception   IOException     An I/O error occurred
     */
    public void save(List<TransactionRecord> transactions) throws IOException {
        RecordOutputStream out = null;
        boolean cleanup = false;
        File saveFile = new File(file.getPath()+".save");

        try {

            //
            // Create the temporary save file
            //
            out = new RecordOutputStream(saveFile);
            cleanup = true;

            //
            // Write out linked accounts first since we need to have
            // them defined before we can define the linking accounts
            // when the database is loaded again
            //
            for (AccountRecord a : AccountRecord.accounts)
                if (a.getLinkCount() != 0)
                    out.writeRecord(a.encode());

            //
            // Write out non-linked accounts next
            //
            for (AccountRecord a : AccountRecord.accounts)
                if (a.getLinkCount() == 0)
                    out.writeRecord(a.encode());

            //
            // Write out the categories
            //
            for (CategoryRecord c : CategoryRecord.categories)
                out.writeRecord(c.encode());

            //
            // Write out the securities
            //
            for (SecurityRecord s : SecurityRecord.securities)
                out.writeRecord(s.encode());

            //
            // Write out the transactions
            //
            for (TransactionRecord t : transactions)
                out.writeRecord(t.encode());

            //
            // Close and rename the save file
            //
            out.close();
            out = null;
            
            if (file.exists())
                if (!file.delete())
                    throw new IOException("Unable to delete "+file.getName());

            cleanup = false;
            if (!saveFile.renameTo(file))
                throw new IOException("Unable to delete "+saveFile.getName());
        } finally {
            if (cleanup) {
                if (out != null)
                    out.close();

                if (saveFile.exists())
                    saveFile.delete();
            }
        }
    }
}
