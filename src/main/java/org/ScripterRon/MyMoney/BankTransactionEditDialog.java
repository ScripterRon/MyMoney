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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Bank transaction edit dialog
 */
public final class BankTransactionEditDialog extends JDialog implements ActionListener {

    /** The transaction panel */
    private TransactionPanel transactionPanel;
    
    /** The transaction account */
    private AccountRecord account;

    /** Transaction */
    private TransactionRecord transaction;

    /** Transaction splits */
    private List<TransactionSplit> splits;

    /** Transaction has splits */
    private boolean showSplits;

    /** This is a security transaction */
    private boolean securityTransaction;

    /** Transaction date field */
    private JFormattedTextField dateField;

    /** Check number field */
    private JFormattedTextField checkField;

    /** Transaction name field */
    private JTextField nameField;

    /** Category/Account field */
    private JComboBox categoryAccountField;
    
    /** Category/Account model */
    private TransferComboBoxModel categoryAccountModel;
    
    /** Splits model */
    private DefaultComboBoxModel splitsModel;

    /** Memo field */
    private JTextField memoField;

    /** Payment field */
    private JFormattedTextField paymentField;

    /** Deposit field */
    private JFormattedTextField depositField;

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
     * @param       transaction         Transaction to edit or null for new transaction
     */
    public BankTransactionEditDialog(JFrame parent, String title, TransactionPanel transactionPanel,
                                     TransactionRecord transaction) {
        super(parent, title, Dialog.ModalityType.DOCUMENT_MODAL);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.transactionPanel = transactionPanel;
        this.transaction = transaction;
        account = transactionPanel.getTransactionAccount();

        //
        // Get the transaction specifics for an existing transaction
        //
        AccountRecord transferAccount = null;
        CategoryRecord category = null;
        String name = null;
        double payment = 0.00;
        double deposit = 0.00;
        int reconciledState = 0;
        double amount;
        securityTransaction = false;
        splits = null;
        showSplits = false;
        if (transaction != null) {
            splits = transaction.getSplits();
            if (transaction.getAccount() == account) {
                transferAccount = transaction.getTransferAccount();
                category = transaction.getCategory();
                name = transaction.getName();
                amount = transaction.getAmount();
                if (amount < 0.0)
                    payment = -amount;
                else if (amount > 0.0)
                    deposit = amount;
                int reconciled = transaction.getReconciled();
                if ((reconciled&TransactionRecord.SOURCE_PENDING) != 0)
                    reconciledState = 1;
                else if ((reconciled&TransactionRecord.SOURCE_RECONCILED) != 0)
                    reconciledState = 2;
            } else if (transaction.getTransferAccount() == account) {
                transferAccount = transaction.getAccount();
                category = transaction.getCategory();
                SecurityRecord s = transaction.getSecurity();
                if (s != null) {
                    name = s.getName();
                    securityTransaction = true;
                } else {
                    name = transaction.getName();
                }
                
                amount = transaction.getAmount();
                if (amount > 0.0)
                    payment = amount;
                else if (amount < 0.0)
                    deposit = -amount;
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
                            payment = amount;
                        else if (amount < 0.0)
                            deposit = -amount;
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
        // The date cannot be changed for an investment transaction
        //
        dateField = new JFormattedTextField(new EditDate());
        dateField.setColumns(8);
        dateField.setInputVerifier(new EditInputVerifier(false));
        dateField.addActionListener(new FormattedTextFieldListener(this));
        if (transaction != null) {
            dateField.setValue(transaction.getDate());
            if (securityTransaction)
                dateField.setEditable(false);
        } else {
            dateField.setValue(Main.getCurrentDate());
        }

        //
        // Get the check number
        //
        // The check number field is not displayed for a transfer account
        //
        if (transaction == null || transaction.getAccount() == account) {
            checkField = new JFormattedTextField(new EditNumber(0, false));
            checkField.setColumns(5);
            checkField.setInputVerifier(new EditInputVerifier(true));
            checkField.addActionListener(new FormattedTextFieldListener(this));
            if (transaction != null) {
                int number = transaction.getCheckNumber();
                if (number != 0)
                    checkField.setValue(new Integer(number));
            }
        }

        //
        // Get the transaction name
        //
        // The name cannot be changed for an investment transaction
        //
        if (name != null)
            nameField = new JTextField(name, 20);
        else
            nameField = new JTextField(20);
        
        if (securityTransaction) {
            nameField.setEditable(false);
        } else {
            JTable table = transactionPanel.getTransactionTable();
            AccountTableModel model = (AccountTableModel)table.getModel();
            NameDocumentListener.addInstance(nameField, model.getTransactionNames());
        }

        //
        // Get the category or transfer account
        //
        // The source account cannot be changed from a transfer account.
        //
        categoryAccountModel = new TransferComboBoxModel(account, transferAccount, category);
        splitsModel = new DefaultComboBoxModel();
        if (transaction != null && transaction.getAccount() != account) {
            categoryAccountField = new JComboBox(splitsModel);
            categoryAccountField.addItem("["+transaction.getAccount().getName()+"]");
            categoryAccountField.setSelectedIndex(0);
        } else if (splits != null) {
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
        // Get the memo
        //
        if (transaction != null)
            memoField = new JTextField(transaction.getMemo(), 25);
        else
            memoField = new JTextField(25);

        //
        // Get the payment amount
        //
        // Investment amounts cannot be changed from the linked account.
        // The payment amount cannot be changed if there are splits.
        //
        paymentField = new JFormattedTextField(new EditNumber(2, true));
        paymentField.setColumns(10);
        paymentField.setInputVerifier(new EditInputVerifier(true));
        paymentField.addActionListener(new FormattedTextFieldListener(this));
        if (payment != 0.00)
            paymentField.setValue(new Double(payment));
        if (securityTransaction || splits != null)
            paymentField.setEditable(false);

        //
        // Get the deposit amount
        //
        // Investment amounts cannot be changed from the linked account
        // The deposit amount cannot be changed if there are splits.
        //
        depositField = new JFormattedTextField(new EditNumber(2, true));
        depositField.setColumns(10);
        depositField.setInputVerifier(new EditInputVerifier(true));
        depositField.addActionListener(new FormattedTextFieldListener(this));
        if (deposit != 0.00)
            depositField.setValue(new Double(deposit));
        if (securityTransaction || splits != null)
            depositField.setEditable(false);

        //
        // Create the edit pane
        //
        //    Date:              <text-field>
        //    Check number:      <text-field>  (Optional)
        //    Name:              <text-field>
        //    Category/Account:  <combo-box>
        //    Memo:              <text-field>
        //    Payment:           <text-field>
        //    Deposit:           <text-field>
        //
        JPanel editPane = new JPanel(new GridLayout(0, 2, 5, 5));

        editPane.add(new JLabel("Date:", JLabel.RIGHT));
        editPane.add(dateField);

        if (checkField != null) {
            editPane.add(new JLabel("Check number:", JLabel.RIGHT));
            editPane.add(checkField);
        }

        editPane.add(new JLabel("Name:", JLabel.RIGHT));
        editPane.add(nameField);

        editPane.add(new JLabel("Category/Account:", JLabel.RIGHT));
        editPane.add(categoryAccountField);

        editPane.add(new JLabel("Memo:", JLabel.RIGHT));
        editPane.add(memoField);

        editPane.add(new JLabel("Payment:", JLabel.RIGHT));
        editPane.add(paymentField);

        editPane.add(new JLabel("Deposit:", JLabel.RIGHT));
        editPane.add(depositField);

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
        // Create the buttons (OK, Edit Splits, Cancel, Help)
        //
        // The "Edit Splits" button is not displayed for a transfer account since
        // the splits can be edited only from the source account
        //
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));

        JButton button = new JButton("OK");
        button.setActionCommand("ok");
        button.addActionListener(this);
        buttonPane.add(button);
        getRootPane().setDefaultButton(button);
        
        buttonPane.add(Box.createHorizontalStrut(10));

        if (transaction == null || transaction.getAccount() == account) {
            button = new JButton("Edit Splits");
            button.setActionCommand("edit splits");
            button.addActionListener(this);
            buttonPane.add(button);
            
            buttonPane.add(Box.createHorizontalStrut(10));
        }

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
     * Show the bank transaction edit dialog
     *
     * @param       parent              Parent window for the dialog
     * @param       transactionPanel    Transaction panel
     * @param       transaction         Transaction or null for a new transaction
     */
    public static void showDialog(JFrame parent, TransactionPanel transactionPanel, 
                                  TransactionRecord transaction) {
        try {
            String title = (transaction==null ? "New Transaction" : "Edit Transaction");
            JDialog dialog = new BankTransactionEditDialog(parent, title, transactionPanel, transaction);
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
        // "edit splits" - Edit the transaction splits
        // "cancel" - Cancel the request
        // "help" - Display help for bank accounts
        //
        try {
            switch (ae.getActionCommand()) {
                case "ok":
                    if (processFields()) {
                        setVisible(false);
                        dispose();
                    }
                    break;
                    
                case "edit splits":
                    processSplits(this);
                    break;
                    
                case "cancel":
                    setVisible(false);
                    dispose();
                    break;
                    
                case "help":
                    Main.mainWindow.displayHelp(HelpWindow.BANK_ACCOUNT);
                    break;
            }
        } catch (Exception exc) {
            Main.logException("Exception while processing action event", exc);
        }
    }

    /**
     * Process transaction splits
     *
     * @param       parent          Parent window
     */
    private void processSplits(JDialog parent) {

        //
        // Display the splits dialog
        //
        if (splits == null)
            splits = new ArrayList<>(5);

        SplitsDialog.showDialog(parent, splits, account);

        //
        // Update the category/account combo box if necessary
        //
        // The payment and deposit fields are set from the transaction splits
        // and are not editable when there are transaction splits.
        //
        if (splits.isEmpty()) {
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

        if (!paymentField.isEditValid() && !depositField.isEditValid()) {
            JOptionPane.showMessageDialog(this, "You must specify a payment or deposit amount",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Date date = (Date)dateField.getValue();
        JTable table = transactionPanel.getTransactionTable();
        AccountTableModel tableModel = (AccountTableModel)table.getModel();

        //
        // Remove an existing transaction (an investment transaction is not
        // removed since the investment fields cannot be changed from the
        // linked account)
        //
        if (transaction != null) {
            a = transaction.getAccount();
            t = transaction.getTransferAccount();
            name = transaction.getName();
            amount = transaction.getAmount();
            reconciled = transaction.getReconciled();
            if (!securityTransaction) {
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
        if (name.length() > 0 && !securityTransaction)
            tableModel.getTransactionNames().add(name);
        
        //
        // Build the new transaction
        //
        transaction.setMemo(memoField.getText());
        if (checkField != null && checkField.isEditValid())
            transaction.setCheckNumber(((Number)checkField.getValue()).intValue());
        
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
        if (paymentField.isEditValid())
            amount -= ((Number)paymentField.getValue()).doubleValue();
        if (depositField.isEditValid())
            amount += ((Number)depositField.getValue()).doubleValue();

        if (a == account) {
            transaction.setName(name);
            transaction.setAmount(amount);
            reconciled = transaction.getReconciled();
            if (reconciledField.isSelected())
                reconciled |= TransactionRecord.SOURCE_RECONCILED;
            else if (reconcilePendingField.isSelected())
                reconciled |= TransactionRecord.SOURCE_PENDING;
            transaction.setReconciled(reconciled);
        } else  if (t == account) {
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
        // Add the new transaction and scroll the table to display the
        // transaction.  A security transaction is not added since it was
        // not removed earlier.
        //
        if (!securityTransaction) {
            if (transactionPosition >= 0)
                TransactionRecord.transactions.add(transactionPosition, transaction);
            else
                TransactionRecord.insertTransaction(transaction);

            int modelRow = tableModel.transactionAdded(modelPosition, transaction);
            transactionPanel.showSelectedRow(table.convertRowIndexToView(modelRow));
        } else {
            tableModel.transactionModified(transaction);
        }

        Main.dataModified = true;
        return true;
    }    
}
