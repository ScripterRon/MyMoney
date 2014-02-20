package MyMoney;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.table.AbstractTableModel;

/**
 * AccountTableModel provides an abstract table model where the table data
 * consists of transactions and associated account balance.  To create a
 * concrete TableModel as a subclass of AccountTableModel, the subclass needs
 * to provide just the getValueAt() method.
 */
public abstract class AccountTableModel extends AbstractTableModel {

    /** Account */
    protected AccountRecord account;

    /** Column names */
    protected String[] columnNames;

    /** Column classes */
    protected Class<?>[] columnClasses;

    /** List data */
    protected List<AccountTransaction> listData;
    
    /** Transaction names */
    protected SortedSet<String> transactionNames;

    /**
     * Create the account table model
     *
     * @param       account         The account
     * @param       columnNames     The table column names
     * @param       columnClasses   The table column classes
     */
    public AccountTableModel(AccountRecord account, String[] columnNames, Class<?>[] columnClasses) {
        if (account == null)
            throw new NullPointerException("No account provided");
        if (columnNames == null)
            throw new NullPointerException("No column names provided");
        if (columnClasses == null)
            throw new NullPointerException("No column classes provided");
        if (columnNames.length != columnClasses.length)
            throw new IllegalArgumentException("Number of names not same as number of classes");

        this.account = account;
        this.columnNames = columnNames;
        this.columnClasses = columnClasses;

        //
        // Create the transaction lists
        //
        int listSize = TransactionRecord.transactions.size();
        listData = new ArrayList<AccountTransaction>(listSize);
        transactionNames = new TreeSet<>();
        double balance = 0.00;

        //
        // Add the transactions for the current account to the list and
        // update the running balance.  We will also update the transaction
        // name list.
        //
        for (TransactionRecord t : TransactionRecord.transactions) {
            boolean addTransaction = false;
            String name = t.getName();
            if (t.getAccount() == account) {
                balance += t.getAmount();
                addTransaction = true;
            } else if (t.getTransferAccount() == account) {
                balance -= t.getAmount();
                addTransaction = true;
            } else {
                List<TransactionSplit> splits = t.getSplits();
                if (splits != null) {
                    for (TransactionSplit split : splits) {
                        if (split.getAccount() == account) {
                            balance -= split.getAmount();
                            name = split.getDescription();
                            addTransaction = true;
                            break;
                        }
                    }
                }
            }
            
            if (addTransaction) {
                listData.add(new AccountTransaction(t, balance));
                if (name != null && name.length() > 0)
                    transactionNames.add(name);
            }
        }
    }

    /**
     * Get the number of columns in the table
     *
     * @return                  The number of columns
     */
    public int getColumnCount() {
        return columnNames.length;
    }

    /**
     * Get the column class
     *
     * @param       column      Column number
     * @return                  The column class
     */
    public Class<?> getColumnClass(int column) {
        return columnClasses[column];
    }

    /**
     * Get the column name
     *
     * @param       column      Column number
     * @return                  Column name
     */
    public String getColumnName(int column) {
        return columnNames[column];
    }

    /**
     * Get the number of rows in the table
     *
     * @return                  The number of rows
     */
    public int getRowCount() {
        return listData.size();
    }

    /**
     * Get the transaction associated with a table row
     *
     * @param       row         Row index
     * @return                  The TransactionRecord reference
     */
    public TransactionRecord getTransactionAt(int row) {
        if (row >= listData.size())
            throw new IndexOutOfBoundsException("Table row "+row+" is not valid");

        return listData.get(row).transaction;
    }

    /**
     * Get the transaction names
     * 
     * @return                      Transaction name list or null
     */
    public SortedSet<String> getTransactionNames() {
        return transactionNames;
    }

    /**
     * A transaction has been added to the table
     *
     * @param       position        Position to add the transaction or -1
     * @param       transaction     The new transaction
     * @return                      The index of the new transaction
     */
    public int transactionAdded(int position, TransactionRecord transaction) {
        Date date = transaction.getDate();
        int listSize = listData.size();
        double balance = 0.00;
        int index = 0, lowIndex, highIndex;
        boolean transactionInserted = false;

        //
        // Insert the new transaction based on the transaction date (position == -1)
        // or at the specified position (position >= 0).  Since the transactions are
        // sorted by date, we can use a binary search to find the insertion point.
        //
        if (position >= 0) {
            index = position;

            if (position < listSize)
                transactionInserted = true;

            if (position > 0)
                balance = listData.get(index-1).balance;
        } else if (listSize != 0) {
            AccountTransaction r = listData.get(listSize-1);
            if (date.compareTo(r.transaction.getDate()) >= 0) {
                index = listSize;
                balance = r.balance;
            } else {
                lowIndex = -1;
                highIndex = listSize-1;
                while (highIndex-lowIndex > 1) {
                    index = (highIndex-lowIndex)/2+lowIndex;
                    r = listData.get(index);
                    if (date.compareTo(r.transaction.getDate()) < 0)
                        highIndex = index;
                    else
                        lowIndex = index;
                }

                transactionInserted = true;
                index = highIndex;
                if (lowIndex != -1)
                    balance = listData.get(lowIndex).balance;
            }
        }

        //
        // Update the account balance for the new transaction
        //
        if (transaction.getAccount() == account) {
            balance += transaction.getAmount();
        } else if (transaction.getTransferAccount() == account) {
            balance -= transaction.getAmount();
        } else {
            List<TransactionSplit> splits = transaction.getSplits();
            if (splits != null) {
                for (TransactionSplit split : splits) {
                    if (split.getAccount() == account) {
                        balance -= split.getAmount();
                        break;
                    }
                }
            }
        }

        //
        // Add the new account transaction
        //
        AccountTransaction record = new AccountTransaction(transaction, balance);
        if (transactionInserted)
            listData.add(index, record);
        else
            listData.add(record);

        //
        // Update the running balance for the transactions after the new
        // transaction
        //
        if (transactionInserted) {
            ListIterator<AccountTransaction> i = listData.listIterator(index+1);
            while (i.hasNext()) {
                AccountTransaction r = i.next();
                if (r.transaction.getAccount() == account) {
                    balance += r.transaction.getAmount();
                } else if (r.transaction.getTransferAccount() == account) {
                    balance -= r.transaction.getAmount();
                } else {
                    List<TransactionSplit> splits = r.transaction.getSplits();
                    if (splits != null) {
                        for (TransactionSplit split : splits) {
                            if (split.getAccount() == account) {
                                balance -= split.getAmount();
                                break;
                            }
                        }
                    }
                }

                r.balance = balance;
            }
        }

        //
        // Notify table listeners
        //
        fireTableRowsInserted(index, index);
        if (transactionInserted)
            fireTableRowsUpdated(index+1, listData.size()-1);

        return index;
    }

    /**
     * A transaction has been removed from the table
     *
     * @param       transaction     The transaction to remove
     * @return                      The index of the removed transaction
     */
    public int transactionRemoved(TransactionRecord transaction) {
        double balance = 0.00;
        int index = 0;

        //
        // Remove the transaction from our list
        //
        ListIterator<AccountTransaction> i = listData.listIterator();
        while (i.hasNext()) {
            AccountTransaction r = i.next();
            if (r.transaction == transaction) {
                i.remove();
                break;
            }

            balance = r.balance;
            index++;
        }

        //
        // Update the running balance for the transactions after the
        // deleted transaction
        //
        if (index < listData.size()) {
            i = listData.listIterator(index);
            while (i.hasNext()) {
                AccountTransaction r = i.next();
                if (r.transaction.getAccount() == account) {
                    balance += r.transaction.getAmount();
                } else if (r.transaction.getTransferAccount() == account) {
                    balance -= r.transaction.getAmount();
                } else {
                    List<TransactionSplit> splits = r.transaction.getSplits();
                    if (splits != null) {
                        for (TransactionSplit split : splits) {
                            if (split.getAccount() == account) {
                                balance -= split.getAmount();
                                break;
                            }
                        }
                    }
                }

                r.balance = balance;
            }
        }

        //
        // Notify table listeners
        //
        fireTableRowsDeleted(index, index);
        if (index < listData.size())
            fireTableRowsUpdated(index, listData.size()-1);

        return index;
    }

    /**
     * An existing transaction has been modified
     *
     * @param       transaction     The modified transaction
     */
    public void transactionModified(TransactionRecord transaction) {
        int index = 0;
        for (AccountTransaction r : listData) {
            if (r.transaction == transaction) {
                fireTableRowsUpdated(index, index);
                break;
            }

            index++;
        }
    }
}
