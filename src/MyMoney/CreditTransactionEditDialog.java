package MyMoney;

import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Credit transaction edit dialog
 */
public final class CreditTransactionEditDialog extends JDialog implements ActionListener {

    /** Transaction panel */
    private TransactionPanel transactionPanel;
    
    /** Transaction account */
    private AccountRecord account;
    
    /** Transaction */
    private TransactionRecord transaction;

    /** Transaction splits */
    private List<TransactionSplit> splits;

    /** Transaction date field */
    private JFormattedTextField dateField;

    /** Transaction name field */
    private JTextField nameField;

    /** Category/Account field */
    private JComboBox categoryAccountField;
    
    /** Category/Account model */
    private TransferComboBoxModel categoryAccountModel;

    /** Memo field */
    private JTextField memoField;

    /** Charge field */
    private JFormattedTextField chargeField;

    /** Payment field */
    private JFormattedTextField paymentField;

    /** "Not reconciled" radio button */
    private JRadioButton notReconciledField;

    /** "Reconcile pending" radio button */
    private JRadioButton reconcilePendingField;

    /** "Reconciled" radio button */
    private JRadioButton reconciledField;

    /**
     * Create the dialog
     *
     * @param       parent              Parent window
     * @param       title               Dialog title
     * @param       transactionPanel    The transaction panel
     * @param       transaction         Transaction to edit or null
     */
    public CreditTransactionEditDialog(JFrame parent, String title, TransactionPanel transactionPanel,
                                       TransactionRecord transaction) {
        super(parent, title, Dialog.ModalityType.DOCUMENT_MODAL);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.transactionPanel = transactionPanel;
        this.account = transactionPanel.getTransactionAccount();
        this.transaction = transaction;

        //
        // Get the transaction specifics
        //
        AccountRecord transferAccount = null;
        CategoryRecord category = null;
        String name = null;
        double charge = 0.00;
        double payment = 0.00;
        int reconciledState = 0;
        double amount;
        splits = null;
        if (transaction != null) {
            splits = transaction.getSplits();
            if (transaction.getAccount() == account) {
                transferAccount = transaction.getTransferAccount();
                category = transaction.getCategory();
                name = transaction.getName();
                amount = transaction.getAmount();
                if (amount < 0.0)
                    charge = -amount;
                else if (amount > 0.0)
                    payment = amount;
                int reconciled = transaction.getReconciled();
                if ((reconciled&TransactionRecord.SOURCE_PENDING) != 0)
                    reconciledState = 1;
                else if ((reconciled&TransactionRecord.SOURCE_RECONCILED) != 0)
                    reconciledState = 2;
            } else if (transaction.getTransferAccount() == account) {
                transferAccount = transaction.getAccount();
                category = transaction.getCategory();
                name = transaction.getName();
                amount = transaction.getAmount();
                if (amount > 0.0)
                    charge = amount;
                else if (amount < 0.0)
                    payment = -amount;
                int reconciled = transaction.getReconciled();
                if ((reconciled&TransactionRecord.TARGET_PENDING) != 0)
                    reconciledState = 1;
                else if ((reconciled&TransactionRecord.TARGET_RECONCILED) != 0)
                    reconciledState = 2;
            } else if (splits != null) {
                for (TransactionSplit split : splits) {
                    if (split.getAccount() == account) {
                        transferAccount = transaction.getAccount();
                        category = split.getCategory();
                        name = split.getDescription();
                        amount = split.getAmount();
                        if (amount > 0.0)
                            charge = amount;
                        else if (amount < 0.0)
                            payment = -amount;
                        int reconciled = split.getReconciled();
                        if ((reconciled&TransactionRecord.TARGET_PENDING) != 0)
                            reconciledState = 1;
                        else if ((reconciled&TransactionRecord.TARGET_RECONCILED) != 0)
                            reconciledState = 2;
                        break;
                    }
                }
            }
        }

        //
        // Get the transaction date
        //
        dateField = new JFormattedTextField(new EditDate());
        dateField.setColumns(8);
        dateField.setInputVerifier(new EditInputVerifier(false));
        dateField.addActionListener(new FormattedTextFieldListener(this));
        if (transaction != null)
            dateField.setValue(transaction.getDate());
        else
            dateField.setValue(Main.getCurrentDate());

        //
        // Get the transaction name
        //
        if (name != null)
            nameField = new JTextField(name, 20);
        else
            nameField = new JTextField(20);
        
        JTable table = transactionPanel.getTransactionTable();
        AccountTableModel model = (AccountTableModel)table.getModel();
        NameDocumentListener.addInstance(nameField, model.getTransactionNames());

        //
        // Get the category or transfer account
        //
        // The source account cannot be changed from a transfer account
        //
        if (transaction != null && transaction.getAccount() != account) {
            categoryAccountField = new JComboBox();
            categoryAccountField.addItem("["+transaction.getAccount().getName()+"]");
            categoryAccountField.setSelectedIndex(0);
        } else {
            categoryAccountModel = new TransferComboBoxModel(account, transferAccount, category);
            categoryAccountField = new JComboBox(categoryAccountModel);
            if (transferAccount != null)
                categoryAccountModel.setSelectedItem("["+transferAccount.getName()+"]");
            else if (category != null)
                categoryAccountModel.setSelectedItem(category.getName());
            else
                categoryAccountField.setSelectedIndex(0);
        }

        //
        // Get the memo
        //
        if (transaction != null)
            memoField = new JTextField(transaction.getMemo(), 25);
        else
            memoField = new JTextField(25);

        //
        // Get the charge amount
        //
        // Split amounts cannot be changed from the transfer account
        //
        chargeField = new JFormattedTextField(new EditNumber(2, true));
        chargeField.setColumns(10);
        chargeField.setInputVerifier(new EditInputVerifier(true));
        chargeField.addActionListener(new FormattedTextFieldListener(this));
        if (charge != 0.00)
            chargeField.setValue(new Double(charge));
        if (splits != null)
            chargeField.setEditable(false);

        //
        // Get the payment amount
        //
        // Split amounts cannot be changed from the transfer account
        //
        paymentField = new JFormattedTextField(new EditNumber(2, true));
        paymentField.setColumns(10);
        paymentField.setInputVerifier(new EditInputVerifier(true));
        paymentField.addActionListener(new FormattedTextFieldListener(this));
        if (payment != 0.00)
            paymentField.setValue(new Double(payment));
        if (splits != null)
            paymentField.setEditable(false);

        //
        // Create the edit pane
        //
        //    Date:              <text-field>
        //    Name:              <text-field>
        //    Category/Account:  <combo-box>
        //    Memo:              <text-field>
        //    Payment:           <text-field>
        //    Deposit:           <text-field>
        //
        JPanel editPane = new JPanel(new GridLayout(0, 2, 5, 5));
        
        editPane.add(new JLabel("Date:", JLabel.RIGHT));
        editPane.add(dateField);

        editPane.add(new JLabel("Name:", JLabel.RIGHT));
        editPane.add(nameField);

        editPane.add(new JLabel("Category/Account:", JLabel.RIGHT));
        editPane.add(categoryAccountField);

        editPane.add(new JLabel("Memo:", JLabel.RIGHT));
        editPane.add(memoField);

        editPane.add(new JLabel("Charge:", JLabel.RIGHT));
        editPane.add(chargeField);

        editPane.add(new JLabel("Payment:", JLabel.RIGHT));
        editPane.add(paymentField);

        //
        // Create the reconcile radio buttons
        //
        notReconciledField = new JRadioButton("Not Reconciled");
        reconcilePendingField = new JRadioButton("Reconcile Pending");
        reconciledField = new JRadioButton("Reconciled");

        switch (reconciledState) {
            case 0:                             // Not reconciled
                notReconciledField.setSelected(true);
                break;

            case 1:                             // Reconcile pending
                reconcilePendingField.setSelected(true);
                break;

            case 2:                             // Reconciled
                reconciledField.setSelected(true);
                break;
        }

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(notReconciledField);
        buttonGroup.add(reconcilePendingField);
        buttonGroup.add(reconciledField);

        JPanel groupPane = new JPanel();
        groupPane.setLayout(new BoxLayout(groupPane, BoxLayout.X_AXIS));
        groupPane.add(notReconciledField);
        groupPane.add(Box.createHorizontalStrut(15));
        groupPane.add(reconcilePendingField);
        groupPane.add(Box.createHorizontalStrut(15));
        groupPane.add(reconciledField);

        //
        // Create the button pane (OK, Cancel, Help)
        //
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));

        JButton button = new JButton("OK");
        button.setActionCommand("ok");
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
        contentPane.add(groupPane);
        contentPane.add(Box.createVerticalStrut(15));
        contentPane.add(buttonPane);
        setContentPane(contentPane);
    }

    /**
     * Show the credit transaction edit dialog
     *
     * @param       parent              Parent window for the dialog
     * @param       transactionPanel    Transaction panel
     * @param       transaction         Transaction or null for a new transaction
     */
    public static void showDialog(JFrame parent, TransactionPanel transactionPanel, 
                                  TransactionRecord transaction) {
        try {
            String title = (transaction==null ? "New Transaction" : "Edit Transaction");
            JDialog dialog = new CreditTransactionEditDialog(parent, title, transactionPanel, transaction);
            dialog.pack();
            dialog.setLocationRelativeTo(parent);
            dialog.setVisible(true);
        } catch (Exception exc) {
            Main.logException("Exception while displaying dialog", exc);
        }            
    }

    /**
     * Action performed (ActionListener interface)
     *
     * @param   ae              Action event
     */
    public void actionPerformed(ActionEvent ae) {

        //
        // Process the action command
        //
        // "ok" - Data entry is complete
        // "cancel" - Cancel the request
        // "help" - Display help for credit accounts
        //
        try {
            switch (ae.getActionCommand()) {
                case "ok":
                    if (processFields()) {
                       setVisible(false);
                        dispose();
                    }
                    break;
                    
                case "cancel":
                    setVisible(false);
                    dispose();
                    break;
                    
                case "help":
                    Main.mainWindow.displayHelp(HelpWindow.CREDIT_ACCOUNT);
                    break;
            }
        } catch (Exception exc) {
            Main.logException("Exception while processing action event", exc);
        }
    }

    /**
     * Process the transaction fields
     *
     * @return                  TRUE if the entered data is valid
     */
    private boolean processFields() {
        AccountRecord a, t;
        int index, reconciled;
        int transactionPosition = -1, modelPosition = -1;
        double amount;
        String name;

        //
        // Validate the transaction information
        //
        if (!dateField.isEditValid()) {
            JOptionPane.showMessageDialog(this, "You must specify a transaction date",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!chargeField.isEditValid() && !paymentField.isEditValid()) {
            JOptionPane.showMessageDialog(this, "You must specify a charge or payment amount",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Date date = (Date)dateField.getValue();
        JTable table = transactionPanel.getTransactionTable();
        AccountTableModel tableModel = (AccountTableModel)table.getModel();

        //
        // Remove an existing transaction
        //
        if (transaction != null) {
            a = transaction.getAccount();
            t = transaction.getTransferAccount();
            name = transaction.getName();
            amount = transaction.getAmount();
            reconciled = transaction.getReconciled();
            ListIterator<TransactionRecord> i = TransactionRecord.transactions.listIterator();
            transactionPosition = 0;
            while (i.hasNext()) {
                if (i.next() == transaction) {
                    i.remove();
                    break;
                }

                transactionPosition++;
            }

            transaction.clearReferences();
            modelPosition = tableModel.transactionRemoved(transaction);
            if (date.compareTo(transaction.getDate()) != 0) {
                transactionPosition = -1;
                modelPosition = -1;
            }

            transaction = new TransactionRecord(date, a);
            if (t != null && t == account)
                transaction.setTransferAccount(t);

            if (a != account && splits != null) {
                transaction.setName(name);
                transaction.setAmount(amount);
            }

            if (a == account) {
                reconciled &= (255-TransactionRecord.SOURCE_PENDING-TransactionRecord.SOURCE_RECONCILED);
                transaction.setReconciled(reconciled);
            } else if (t == account) {
                reconciled &= (255-TransactionRecord.TARGET_PENDING-TransactionRecord.TARGET_RECONCILED);
                transaction.setReconciled(reconciled);
            } else if (splits != null) {
                for (TransactionSplit split : splits) {
                    if (split.getAccount() == account) {
                        reconciled = split.getReconciled();
                        reconciled &= (255-TransactionRecord.TARGET_PENDING-TransactionRecord.TARGET_RECONCILED);
                        split.setReconciled(reconciled);
                        break;
                    }
                }
            }
        } else {
            transaction = new TransactionRecord(date, account);
        }
        
        //
        // Get the transaction name and update the name list
        //
        name = nameField.getText();
        if (name.length() > 0)
            tableModel.getTransactionNames().add(name);

        //
        // Build the new transaction
        //
        transaction.setMemo(memoField.getText());
        
        index = categoryAccountField.getSelectedIndex();
        if (index > 0) {
            DBElement element = categoryAccountModel.getDBElementAt(index);
            if (element instanceof AccountRecord)
                transaction.setTransferAccount((AccountRecord)element);
            else
                transaction.setCategory((CategoryRecord)element);
        }

        a = transaction.getAccount();
        t = transaction.getTransferAccount();
        amount = 0.00;
        if (chargeField.isEditValid())
            amount -= ((Number)chargeField.getValue()).doubleValue();
        if (paymentField.isEditValid())
            amount += ((Number)paymentField.getValue()).doubleValue();

        if (a == account) {
            transaction.setName(name);
            transaction.setAmount(amount);
            reconciled = transaction.getReconciled();
            if (reconciledField.isSelected())
                reconciled |= TransactionRecord.SOURCE_RECONCILED;
            else if (reconcilePendingField.isSelected())
                reconciled |= TransactionRecord.SOURCE_PENDING;
            transaction.setReconciled(reconciled);
        } else if (t == account) {
            transaction.setName(name);
            transaction.setAmount(-amount);
            reconciled = transaction.getReconciled();
            if (reconciledField.isSelected())
                reconciled |= TransactionRecord.TARGET_RECONCILED;
            else if (reconcilePendingField.isSelected())
                reconciled |= TransactionRecord.TARGET_PENDING;
            transaction.setReconciled(reconciled);
        } else if (splits != null) {
            for (TransactionSplit split : splits) {
                if (split.getAccount() == account) {
                    split.setDescription(name);
                    reconciled = split.getReconciled();
                    if (reconciledField.isSelected())
                        reconciled |= TransactionRecord.TARGET_RECONCILED;
                    else if (reconcilePendingField.isSelected())
                        reconciled |= TransactionRecord.TARGET_PENDING;
                    split.setReconciled(reconciled);
                    break;
                }
            }
        }

        transaction.setSplits(splits);
        splits = null;

        //
        // Add the new transaction and scroll the table to display the transaction
        //
        if (transactionPosition >= 0)
            TransactionRecord.transactions.add(transactionPosition, transaction);
         else
            TransactionRecord.insertTransaction(transaction);

        int modelRow = tableModel.transactionAdded(modelPosition, transaction);
        transactionPanel.showSelectedRow(table.convertRowIndexToView(modelRow));
        Main.dataModified = true;
        return true;
    }
}
