package MyMoney;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import java.io.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Archive transactions dialog
 */
public class ArchiveDialog extends JDialog implements ActionListener {

    /** Archive date */
    private JFormattedTextField dateField;
    
    /**
     * Create the dialog
     *
     * @param       parent          Parent frame
     */
    public ArchiveDialog(JFrame parent) {
        super(parent, "Archive Transactions", Dialog.ModalityType.DOCUMENT_MODAL);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        //
        // Set the archive date to the end of the previous year
        //
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(Main.getCurrentDate());
        cal.set(Calendar.MONTH, 11);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        cal.add(Calendar.YEAR, -1);
        dateField = new JFormattedTextField(new EditDate());
        dateField.setColumns(8);
        dateField.setInputVerifier(new EditInputVerifier(false));
        dateField.addActionListener(new FormattedTextFieldListener(this));
        dateField.setValue(cal.getTime());

        //
        // Create the edit pane
        //
        //    Archive Date: <date-field>
        //
        JPanel editPane = new JPanel(new GridLayout(0, 2, 5, 5));

        editPane.add(new JLabel("Archive Date:", JLabel.RIGHT));
        editPane.add(dateField);

        //
        // Create the button pane (Archive, Cancel, Help)
        //
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));

        JButton button = new JButton("Archive");
        button.setActionCommand("archive");
        button.addActionListener(this);
        buttonPane.add(button);
        getRootPane().setDefaultButton(button);
        
        buttonPane.add(Box.createHorizontalStrut(10));

        button = new JButton("Cancel");
        button.setActionCommand("cancel");
        button.addActionListener(this);
        buttonPane.add(button);
        
        buttonPane.add(Box.createHorizontalStrut(10));

        button = new JButton("Help");
        button.setActionCommand("help");
        button.addActionListener(this);
        buttonPane.add(button);

        //
        // Set up the content pane
        //
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.setOpaque(true);
        contentPane.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        contentPane.add(editPane);
        contentPane.add(Box.createVerticalStrut(15));
        contentPane.add(buttonPane);
        setContentPane(contentPane);        
    }

    /**
     * Show the dialog
     *
     * @param       parent          Parent window for the dialog
     */
    public static void showDialog(JFrame parent) {
        try {
            //
            // Create and display the dialog
            //
            JDialog dialog = new ArchiveDialog(parent);
            dialog.pack();
            dialog.setLocationRelativeTo(parent);
            dialog.setVisible(true);
            
            //
            // Make sure the main window is at the front when we return
            //
            Main.mainWindow.toFront();
        } catch (Exception exc) {
            Main.logException("Exception while displaying dialog", exc);
        }
    }

    /**
     * TransactionReportDialog action performed (ActionListener interface)
     *
     * @param       ae              Action event
     */
    public void actionPerformed(ActionEvent ae) {

        //
        // Process the action command
        //
        // "archive" - Archive the transactions
        // "cancel" - Cancel the request
        // "help" - Display help for the archive function
        //
        try {
            switch (ae.getActionCommand()) {
                case "archive":
                    if (archiveTransactions()) {
                       setVisible(false);
                        dispose();
                    }
                    break;
                    
                case "cancel":
                    setVisible(false);
                    dispose();
                    break;
                    
                case "help":
                    Main.mainWindow.displayHelp(HelpWindow.ARCHIVE_FILE);
                    break;
            }
        } catch (Exception exc) {
            Main.logException("Exception while processing action event", exc);
        }
    }
    
    /**
     * Archive transactions
     *
     * @exception   IOException     An I/O error occurred
     */
    private boolean archiveTransactions() throws IOException {
        
        //
        // Validate the archive date
        //
        if (!dateField.isEditValid()) {
            JOptionPane.showMessageDialog(this, "You must specify the archive date",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        Date archiveDate = (Date)dateField.getValue();
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(archiveDate);
        String archivePath = String.format("%s\\My Money Archive %04d.database", 
                                           Main.dataPath, cal.get(Calendar.YEAR));
        File archiveFile = new File(archivePath);
        if (archiveFile.exists()) {
            JOptionPane.showMessageDialog(this, "The archive file already exists",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        //
        // Initialize the account balances
        //
        for (AccountRecord a : AccountRecord.accounts)
            a.balance = 0.0;
 
        //
        // Gather the archived transactions
        //
        // For non-investment accounts, we will remove all transactions up to 
        // and including the archive date.  For an investment account, we will 
        // remove non-income transactions only if all of the shares have been 
        // sold by the archive date.
        //
        List<TransactionRecord> archiveTransactions = new LinkedList<>();
        List<TransactionRecord> securityTransactions = new ArrayList<>();
        List<SecurityHolding> holdings = new ArrayList<>();
        ListIterator<TransactionRecord> li = TransactionRecord.transactions.listIterator();
        
        while (li.hasNext()) {
            TransactionRecord t = li.next();
            if (t.getDate().compareTo(archiveDate) > 0)
                break;

            archiveTransactions.add(t);
            AccountRecord account = t.getAccount();
            if (account.getType() == AccountRecord.INVESTMENT) {
                int action = t.getAction();
                if (action == TransactionRecord.INCOME || action == TransactionRecord.EXPENSE) {
                    t.getTransferAccount().balance -= t.getAmount();
                    li.remove();
                    t.clearReferences();
                    Main.dataModified = true;
                } else {
                    securityTransactions.add(t);
                    SecurityHolding.updateSecurityHolding(holdings, t);
                }
            } else {
                double amount = t.getAmount();
                account.balance += amount;
                AccountRecord transferAccount = t.getTransferAccount();
                if (transferAccount != null)
                    transferAccount.balance -= amount;
                
                List<TransactionSplit> splits = t.getSplits();
                if (splits != null) {
                    for (TransactionSplit split : splits) {
                        account = split.getAccount();
                        if (account != null)
                            account.balance -= split.getAmount();
                    }
                }
                
                li.remove();
                t.clearReferences();
                Main.dataModified = true;
            }
        }
        
        //
        // Remove transactions for securities that have been sold
        //
        for (TransactionRecord t : securityTransactions) {
            AccountRecord account = t.getAccount();
            AccountRecord transferAccount = t.getTransferAccount();
            SecurityRecord security = t.getSecurity();
            for (SecurityHolding h : holdings) {
                if (h.getSecurity() == security && h.getAccount() == account) {
                    if (h.getTotalShares() == 0.0) {
                        if (transferAccount != null)
                            transferAccount.balance -= t.getAmount();
                        
                        TransactionRecord.transactions.remove(t);
                        t.clearReferences();
                        Main.dataModified = true;
                    }
                    
                    break;
                }
            }
        }
        
        //
        // Create an adjustment transaction for each archived account
        //
        for (AccountRecord account : AccountRecord.accounts) {
            if (account.balance != 0.0) {
                TransactionRecord t = new TransactionRecord(archiveDate, account);
                t.setAmount(account.balance);
                t.setName("Archived transactions");
                t.setReconciled(TransactionRecord.SOURCE_RECONCILED);
                TransactionRecord.insertTransaction(t);
                Main.dataModified = true;
            }
        }
        
        //
        // Save the archived transactions
        //
        Database archiveDatabase = new Database(archiveFile);
        archiveDatabase.save(archiveTransactions);
        JOptionPane.showMessageDialog(this, "Archive '"+archiveDatabase.getName()+"' created",
                                      "File created", JOptionPane.INFORMATION_MESSAGE);        
        return true;
    }
}
