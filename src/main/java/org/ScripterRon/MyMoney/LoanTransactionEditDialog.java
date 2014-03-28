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
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Loan transaction edit dialog
 */
public final class LoanTransactionEditDialog extends JDialog implements ActionListener {

    /** Transaction panel */
    private TransactionPanel transactionPanel;
    
    /** Transaction account */
    private AccountRecord account;
    
    /** Transaction */
    private TransactionRecord transaction;

    /** Transaction splits */
    private List<TransactionSplit> splits;

    /** Date field */
    private JFormattedTextField dateField;

    /** Memo field */
    private JTextField memoField;

    /** Transfer account field */
    private JComboBox accountField;
    
    /** Transfer account model */
    private TransferComboBoxModel accountModel;

    /** Loan amount field */
    private JFormattedTextField loanField;

    /** Payment field */
    private JFormattedTextField paymentField;

    /**
     * Create the dialog
     *
     * @param       parent              Parent window
     * @param       title               Dialog title
     * @param       transactionPanel    The transaction panel
     * @param       transaction         Transaction to edit or null
     */
    public LoanTransactionEditDialog(JFrame parent, String title, TransactionPanel transactionPanel,
                                     TransactionRecord transaction) {
        super(parent, title, Dialog.ModalityType.DOCUMENT_MODAL);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.transactionPanel = transactionPanel;
        this.account = transactionPanel.getTransactionAccount();
        this.transaction = transaction;

        //
        // Get the transaction specifics for an existing transaction
        //
        AccountRecord transferAccount = null;
        double loan = 0.00;
        double payment = 0.00;
        double amount;
        splits = null;
        if (transaction != null) {
            splits = transaction.getSplits();
            if (transaction.getAccount() == account) {
                transferAccount = transaction.getTransferAccount();
                amount = transaction.getAmount();
                if (amount < 0.0)
                    loan = -amount;
                else if (amount > 0.0)
                    payment = amount;
            } else if (transaction.getTransferAccount() == account) {
                transferAccount = transaction.getAccount();
                amount = transaction.getAmount();
                if (amount > 0.0)
                    loan = amount;
                else if (amount < 0.0)
                    payment = -amount;
            } else if (splits != null) {
                for (TransactionSplit split : splits) {
                    if (split.getAccount() == account) {
                        transferAccount = transaction.getAccount();
                        amount = split.getAmount();
                        if (amount > 0.0)
                            loan = amount;
                        else if (amount < 0.0)
                            payment = -amount;
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
        // Get the transfer account
        //
        // The current account cannot be a transfer account.  We also do
        // not allow transfers to an investment account (the linked bank
        // account handles all cash transactions) or to an asset account.
        //
        // The source account cannot be changed from a transfer account
        //
        if (transaction != null && transaction.getAccount() != account) {
            accountField = new JComboBox();
            accountField.addItem("["+transaction.getAccount().getName()+"]");
            accountField.setSelectedIndex(0);
        } else {
            accountModel = new TransferComboBoxModel(account, transferAccount);
            accountField = new JComboBox(accountModel);
            if (transferAccount != null)
                accountModel.setSelectedItem("["+transferAccount.getName()+"]");
            else
                accountField.setSelectedIndex(0);
        }

        //
        // Get the memo
        //
        if (transaction != null)
            memoField = new JTextField(transaction.getMemo(), 25);
        else
            memoField = new JTextField(25);

        //
        // Get the loan value
        //
        // Split amounts cannot be changed from the transfer account
        //
        loanField = new JFormattedTextField(new EditNumber(2, true));
        loanField.setColumns(10);
        loanField.setInputVerifier(new EditInputVerifier(true));
        loanField.addActionListener(new FormattedTextFieldListener(this));
        if (loan != 0.00)
            loanField.setValue(new Double(loan));
        if (splits != null)
            loanField.setEditable(false);

        //
        // Get the loan payment
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
        //    Account:           <combo-box>
        //    Memo:              <text-field>
        //    Loan:              <text-field>
        //    Payment:           <text-field>
        //
        JPanel editPane = new JPanel(new GridLayout(0, 2, 5, 5));
        
        editPane.add(new JLabel("Date:", JLabel.RIGHT));
        editPane.add(dateField);

        editPane.add(new JLabel("Account:", JLabel.RIGHT));
        editPane.add(accountField);

        editPane.add(new JLabel("Memo:", JLabel.RIGHT));
        editPane.add(memoField);

        editPane.add(new JLabel("Loan:", JLabel.RIGHT));
        editPane.add(loanField);

        editPane.add(new JLabel("Payment:", JLabel.RIGHT));
        editPane.add(paymentField);

        //
        // Create the buttons (OK, Cancel, Help)
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
        contentPane.add(buttonPane);
        setContentPane(contentPane);
    }

    /**
     * Show the loan transaction edit dialog
     *
     * @param       parent              Parent window for the dialog
     * @param       transactionPanel    Transaction panel
     * @param       transaction         Transaction or null for a new transaction
     */
    public static void showDialog(JFrame parent, TransactionPanel transactionPanel, 
                                  TransactionRecord transaction) {
        try {
            String title = (transaction==null ? "New Transaction" : "Edit Transaction");
            JDialog dialog = new LoanTransactionEditDialog(parent, title, transactionPanel, transaction);
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
        // "cancel" - Cancel the request
        // "help" - Display help for loan accounts
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
                    Main.mainWindow.displayHelp(HelpWindow.LOAN_ACCOUNT);
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
        int transactionPosition = -1, modelPosition = -1;
        int index, reconciled;
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

        if (!loanField.isEditValid() && !paymentField.isEditValid()) {
            JOptionPane.showMessageDialog(this, "You must specify a loan or payment amount",
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
            transaction.setName(name);
            transaction.setReconciled(reconciled);
            if (t != null && t == account)
                transaction.setTransferAccount(t);

            if (a != account && splits != null)
                transaction.setAmount(amount);
        } else {
            transaction = new TransactionRecord(date, account);
        }

        //
        // Build the new transaction
        //
        transaction.setMemo(memoField.getText());
        index = accountField.getSelectedIndex();
        if (index > 0)
            transaction.setTransferAccount((AccountRecord)accountModel.getDBElementAt(index));

        a = transaction.getAccount();
        t = transaction.getTransferAccount();
        amount = 0.00;
        if (loanField.isEditValid())
            amount -= ((Number)loanField.getValue()).doubleValue();
        if (paymentField.isEditValid())
             amount += ((Number)paymentField.getValue()).doubleValue();
        if (a == account) {
            transaction.setAmount(amount);
        } else if (t == account) {
            transaction.setAmount(-amount);
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
