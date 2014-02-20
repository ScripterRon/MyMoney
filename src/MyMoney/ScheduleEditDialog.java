package MyMoney;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Scheduled transaction edit dialog
 */
public final class ScheduleEditDialog extends JDialog implements ActionListener {

    /** Transaction */
    private ScheduleRecord transaction;
    
    /** Scheduled transactions table model */
    private ScheduleTableModel tableModel;

    /** Transaction has splits */
    private boolean showSplits;

    /** Transaction splits */
    private List<TransactionSplit> splits;

    /** Transaction date field */
    private JFormattedTextField dateField;

    /** Transaction description field */
    private JTextField descriptionField;

    /** Schedule type field */
    private JComboBox typeField;
    
    /** Schedule type model */
    private DBElementTypeComboBoxModel typeModel;

    /** Account field */
    private JComboBox accountField;
    
    /** Account model */
    private ScheduleAccountComboBoxModel accountModel;

    /** Category/Account field */
    private JComboBox categoryAccountField;
    
    /** Category/Account model */
    private TransferComboBoxModel categoryAccountModel;
    
    /** Splits model */
    private DefaultComboBoxModel splitsModel;

    /** Payment field */
    private JFormattedTextField paymentField;

    /** Deposit field */
    private JFormattedTextField depositField;

    /**
     * Create the dialog
     *
     * @param       parent          Parent window
     * @param       title           Dialog title
     * @param       tableModel      Scheduled transactions table model
     * @param       transaction     Transaction to edit or null for a new transaction
     */
    public ScheduleEditDialog(JDialog parent, String title, ScheduleTableModel tableModel, ScheduleRecord transaction) {
        super(parent, title, Dialog.ModalityType.DOCUMENT_MODAL);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.transaction = transaction;
        this.tableModel = tableModel;

        //
        // Get the transaction specifics for an existing transaction
        //
        int scheduleType = ScheduleRecord.SINGLE;
        AccountRecord account = null;
        AccountRecord transferAccount = null;
        CategoryRecord category = null;
        String description = null;
        double payment = 0.00;
        double deposit = 0.00;
        double amount;
        showSplits = false;
        splits = null;
        if (transaction != null) {
            scheduleType = transaction.getType();
            account = transaction.getAccount();
            transferAccount = transaction.getTransferAccount();
            category = transaction.getCategory();
            splits = transaction.getSplits();
            description = transaction.getDescription();
            amount = transaction.getAmount();
            if (amount < 0.0)
                payment = -amount;
            else if (amount > 0.0)
                deposit = amount;
        }

        //
        // Get the schedule type
        //
        typeModel = new DBElementTypeComboBoxModel(ScheduleRecord.getTypes(), ScheduleRecord.getTypeStrings());
        typeField = new JComboBox(typeModel);
        if (transaction != null)
            typeModel.setSelectedItem(ScheduleRecord.getTypeString(scheduleType));
        else
            typeModel.setSelectedItem(ScheduleRecord.getTypeString(ScheduleRecord.MONTHLY));
        
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
        // Get the transaction description
        //
        if (description != null)
            descriptionField = new JTextField(description);
        else
            descriptionField = new JTextField(20);

        //
        // Get the account
        //
        accountModel = new ScheduleAccountComboBoxModel(account);
        accountField = new JComboBox(accountModel);
        if (account != null)
            accountModel.setSelectedItem(account.getName());
        else
            accountModel.setSelectedItem(null);

        //
        // Get the category or transfer account
        //
        categoryAccountModel = new TransferComboBoxModel(null, transferAccount, category);
        splitsModel = new DefaultComboBoxModel();
        if (splits != null) {
            categoryAccountField = new JComboBox(splitsModel);
            categoryAccountField.addItem("--Split--");
            categoryAccountField.setSelectedIndex(0);
            showSplits = true;
        } else {
            categoryAccountField = new JComboBox(categoryAccountModel);
            if (transferAccount != null)
                categoryAccountModel.setSelectedItem("["+transferAccount.getName()+"]");
            else if (category != null)
                categoryAccountModel.setSelectedItem(category.getName());
            else
                categoryAccountField.setSelectedIndex(0);
        }

        //
        // Get the payment amount
        //
        paymentField = new JFormattedTextField(new EditNumber(2, true));
        paymentField.setColumns(8);
        paymentField.setInputVerifier(new EditInputVerifier(true));
        paymentField.addActionListener(new FormattedTextFieldListener(this));
        if (payment != 0.00)
            paymentField.setValue(new Double(payment));
        if (splits != null)
            paymentField.setEditable(false);

        //
        // Get the deposit amount
        //
        depositField = new JFormattedTextField(new EditNumber(2, true));
        depositField.setColumns(8);
        depositField.setInputVerifier(new EditInputVerifier(true));
        depositField.addActionListener(new FormattedTextFieldListener(this));
        if (deposit != 0.00)
            depositField.setValue(new Double(deposit));
        if (splits != null)
            depositField.setEditable(false);

        //
        // Create the edit pane
        //
        //    Type:              <combo-box>
        //    Date:              <text-field>
        //    Description:       <text-field>
        //    Account:           <combo-box>
        //    Category/Account:  <combo-box>
        //    Payment:           <text-field>
        //    Deposit:           <text-field>
        //
        JPanel editPane = new JPanel(new GridLayout(0, 2, 5, 5));

        editPane.add(new JLabel("Type:", JLabel.RIGHT));
        editPane.add(typeField);

        editPane.add(new JLabel("Date:", JLabel.RIGHT));
        editPane.add(dateField);

        editPane.add(new JLabel("Description:", JLabel.RIGHT));
        editPane.add(descriptionField);

        editPane.add(new JLabel("Account:", JLabel.RIGHT));
        editPane.add(accountField);

        editPane.add(new JLabel("Category/Account:", JLabel.RIGHT));
        editPane.add(categoryAccountField);

        editPane.add(new JLabel("Payment:", JLabel.RIGHT));
        editPane.add(paymentField);

        editPane.add(new JLabel("Deposit:", JLabel.RIGHT));
        editPane.add(depositField);

        //
        // Create the button pane (OK, Edit Splits, Cancel, Help)
        //
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));

        JButton button = new JButton("OK");
        button.setActionCommand("ok");
        button.addActionListener(this);
        buttonPane.add(button);
        getRootPane().setDefaultButton(button);
        
        buttonPane.add(Box.createHorizontalStrut(10));

        button = new JButton("Edit Splits");
        button.setActionCommand("edit splits");
        button.addActionListener(this);
        buttonPane.add(button);
        
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
     * Show the scheduled transaction edit dialog
     *
     * @param       parent          Parent window for the dialog
     * @param       tableModel      Scheduled transactions table model
     * @param       index           Selection index or -1 for a new transaction
     */
    public static void showDialog(JDialog parent, ScheduleTableModel tableModel, int index) {
        try {
            String title;
            ScheduleRecord transaction;
            if (index < 0) {
                transaction = null;
                title = "Add Scheduled Transaction";
            } else {
                transaction = ScheduleRecord.transactions.get(index);
                title = "Edit Scheduled Transaction";
            }
            
            JDialog dialog = new ScheduleEditDialog(parent, title, tableModel, transaction);
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
        // "ok" - Data entry complete
        // "edit splits" - Edit transaction splits
        // "cancel" - Cancel the request
        // "help" - Display help for schedules
        //
        try {
            String action = ae.getActionCommand();
            if (action.equals("ok")) {
                if (processFields()) {
                    setVisible(false);
                    dispose();
                }
            } else if (action.equals("edit splits")) {
                processSplits(this);
            } else if (action.equals("cancel")) {
                setVisible(false);
                dispose();
            } else if (action.equals("help")) {
                Main.mainWindow.displayHelp(HelpWindow.SCHEDULES);
            }
        } catch (Exception exc) {
            Main.logException("Exception while processing action event", exc);
        }
    }

    /**
     * Process transaction splits
     *
     * @param   parent          Parent dialog
     */
    private void processSplits(JDialog parent) {

        //
        // Display the splits dialog
        //
        if (splits == null)
            splits = new ArrayList<TransactionSplit>(5);

        AccountRecord account = null;
        int index = accountField.getSelectedIndex();
        if (index >= 0)
            account = accountModel.getAccountAt(index);

        SplitsDialog.showDialog(parent, splits, account);

        //
        // Update the category/account combo box if necessary
        //
        // The payment and deposit fields are set from the transaction splits
        // and are not editable when there are transaction splits.
        //
        if (splits.size() == 0) {
            splits = null;
            paymentField.setEditable(true);
            depositField.setEditable(true);
            if (showSplits) {
                categoryAccountField.setModel(categoryAccountModel);
                categoryAccountField.setSelectedIndex(0);
                showSplits = false;
            }
        } else {
            paymentField.setEditable(false);
            depositField.setEditable(false);
            double amount = 0.00;

            for (TransactionSplit split : splits)
                amount += split.getAmount();

            if (amount < 0.0) {
                paymentField.setValue(new Double(-amount));
                depositField.setValue(null);
            } else {
                paymentField.setValue(null);
                depositField.setValue(new Double(amount));
            }

            if (!showSplits) {
                categoryAccountField.setModel(splitsModel);
                categoryAccountField.removeAllItems();
                categoryAccountField.addItem("--Split--");
                categoryAccountField.setSelectedIndex(0);
                showSplits = true;
            }
        }
    }

    /**
     * Process the transaction fields
     *
     * @return                  TRUE if the entered data is valid
     */
    private boolean processFields() {
        AccountRecord account = null;
        AccountRecord transferAccount = null;
        CategoryRecord category = null;
        int scheduleType;
        int transactionPosition = -1;
        int index;

        //
        // Validate the transaction information
        //
        if (!dateField.isEditValid()) {
            JOptionPane.showMessageDialog(this, "You must specify a transaction date",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!paymentField.isEditValid() && !depositField.isEditValid()) {
            JOptionPane.showMessageDialog(this, "You must specify a payment or deposit amount",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        index = accountField.getSelectedIndex();
        if (index < 0) {
            JOptionPane.showMessageDialog(this, "You must specify a transaction account",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        account = accountModel.getAccountAt(index);
        index = categoryAccountField.getSelectedIndex();
        if (index > 0) {
            DBElement element = categoryAccountModel.getDBElementAt(index);
            if (element instanceof AccountRecord)
                transferAccount = (AccountRecord)element;
            else
                category = (CategoryRecord)element;
        }
        
        if (account == transferAccount) {
            JOptionPane.showMessageDialog(this, "Transaction account and transfer account cannot be the same",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        scheduleType = typeModel.getTypeAt(typeField.getSelectedIndex());
        Date date = (Date)dateField.getValue();

        //
        // A schedule loan payment requires 2 splits: one for the interest
        // payment (expense category) and the other for the principal
        // payment (loan account)
        //
        if (transferAccount != null && transferAccount.getType() == AccountRecord.LOAN) {
            JOptionPane.showMessageDialog(this, "A scheduled loan payment must have 2 splits",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (splits != null) {
            boolean loanAccount = false;
            boolean loanCategory = false;

            for (TransactionSplit split : splits) {
                AccountRecord a = split.getAccount();
                CategoryRecord c = split.getCategory();
                if (a != null) {
                    if (a == account) {
                        JOptionPane.showMessageDialog(this, "Transaction account and split account cannot be the same",
                                                      "Error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                    
                    if (a.getType() == AccountRecord.LOAN)
                        loanAccount = true;
                } else if (c != null) {
                    if (c.getType() == CategoryRecord.EXPENSE)
                        loanCategory = true;
                }
            }

            if (loanAccount) {
                if (splits.size() != 2) {
                    JOptionPane.showMessageDialog(this, "A scheduled loan payment must have 2 splits",
                                                  "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                if (!loanCategory) {
                    JOptionPane.showMessageDialog(this,
                                    "A scheduled loan payment must have an expense category",
                                    "ERROR", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }

        //
        // Remove an existing transaction
        //
        if (transaction != null) {
            ListIterator<ScheduleRecord> li = ScheduleRecord.transactions.listIterator();
            transactionPosition = 0;

            while (li.hasNext()) {
                if (li.next() == transaction) {
                    li.remove();
                    break;
                }

                transactionPosition++;
            }

            transaction.clearReferences();
            tableModel.fireTableRowsDeleted(transactionPosition, transactionPosition);
            if (date.compareTo(transaction.getDate()) != 0)
                transactionPosition = -1;
        }

        //
        // Build the new transaction
        //
        transaction = new ScheduleRecord(scheduleType, date, account);
        transaction.setDescription(descriptionField.getText());
        transaction.setTransferAccount(transferAccount);
        transaction.setCategory(category);
        double amount = 0.00;
        if (paymentField.isEditValid())
            amount -= ((Number)paymentField.getValue()).doubleValue();
        if (depositField.isEditValid())
            amount += ((Number)depositField.getValue()).doubleValue();
        transaction.setAmount(amount);
        transaction.setSplits(splits);
        splits = null;

        //
        // Add the new transaction
        //
        if (transactionPosition >= 0) {
            ScheduleRecord.transactions.add(transactionPosition, transaction);
            index = transactionPosition;
        } else {
            index = ScheduleRecord.insertTransaction(transaction);
        }

        tableModel.fireTableRowsInserted(index, index);
        Main.dataModified = true;
        return true;
    }
}
