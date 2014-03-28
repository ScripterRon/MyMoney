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

import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedSet;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Investment transaction edit dialog
 */
public final class InvestmentTransactionEditDialog extends JDialog implements ActionListener {

    /** Transaction panel */
    private TransactionPanel transactionPanel;
    
    /** Transaction account */
    private AccountRecord account;
    
    /** Transaction */
    private TransactionRecord transaction;

    /** Transaction date field */
    private JFormattedTextField dateField;

    /** Number of shares field */
    private JFormattedTextField sharesField;
    
    /** Number of new shares field */
    private JFormattedTextField newSharesField;

    /** Share price field */
    private JFormattedTextField sharePriceField;

    /** Commission field */
    private JFormattedTextField commissionField;

    /** Amount field */
    private JFormattedTextField amountField;

    /** Action field */
    private JComboBox actionField;
    
    /** Action model */
    private DBElementTypeComboBoxModel actionModel;

    /** Security field */
    private JComboBox securityField;
    
    /** Security model */
    private DBElementComboBoxModel securityModel;
    
    /** New security field */
    private JComboBox newSecurityField;
    
    /** New security model */
    private DBElementComboBoxModel newSecurityModel;

    /** Category field */
    private JComboBox categoryField;
    
    /** Category model */
    private TransferComboBoxModel categoryModel;
    
    /** "FIFO" radio button */
    private JRadioButton fifoField;

    /** "LIFO" radio button */
    private JRadioButton lifoField;

    /** "AVG_COST" radio button */
    private JRadioButton avgCostField;
    
    /**
     * Create the investment transaction dialog
     *
     * @param       parent              Parent window
     * @param       title               Dialog title
     * @param       transactionPanel    The transaction panel
     * @param       transaction         Transaction to edit or null
     */
    public InvestmentTransactionEditDialog(JFrame parent, String title, TransactionPanel transactionPanel,
                                           TransactionRecord transaction) {
        super(parent, title, Dialog.ModalityType.DOCUMENT_MODAL);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.transactionPanel = transactionPanel;
        this.account = transactionPanel.getTransactionAccount();
        this.transaction = transaction;

        //
        // Get the transaction details
        //
        SecurityRecord security = null;
        SecurityRecord newSecurity = null;
        CategoryRecord category = null;
        int action = TransactionRecord.BUY;
        int accountingMethod = TransactionRecord.FIFO;
        double shares = 0.00;
        double newShares = 0.00;
        double sharePrice = 0.00;
        double commission = 0.00;
        double amount = 0.00;
        if (transaction != null) {
            action = transaction.getAction();
            accountingMethod = transaction.getAccountingMethod();
            security = transaction.getSecurity();
            newSecurity = transaction.getNewSecurity();
            category = transaction.getCategory();
            shares = transaction.getShares();
            newShares = transaction.getNewShares();
            sharePrice = transaction.getSharePrice();
            commission = transaction.getCommission();
            amount = transaction.getAmount();
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
        // Get the action
        //
        actionModel = new DBElementTypeComboBoxModel(TransactionRecord.getActions(),
                                                     TransactionRecord.getActionStrings());
        actionField = new JComboBox(actionModel);
        actionModel.setSelectedItem(TransactionRecord.getActionString(action));

        //
        // Get the security
        //
        securityModel = new DBElementComboBoxModel(SecurityRecord.securities, security);
        securityField = new JComboBox(securityModel);
        if (security != null)
            securityModel.setSelectedItem(security.getName());
        else
            securityField.setSelectedIndex(0);
        
        //
        // Get the new security
        //
        newSecurityModel = new DBElementComboBoxModel(SecurityRecord.securities, newSecurity);
        newSecurityField = new JComboBox(newSecurityModel);
        if (newSecurity != null)
            newSecurityModel.setSelectedItem(newSecurity.getName());
        else
            newSecurityField.setSelectedIndex(-1);
        
        //
        // Get the category
        //
        categoryModel = new TransferComboBoxModel(category);
        categoryField = new JComboBox(categoryModel);
        if (category != null)
            categoryModel.setSelectedItem(category.getName());
        else
            categoryField.setSelectedIndex(0);

        //
        // Get the number of shares
        //
        sharesField = new JFormattedTextField(new EditNumber(4, false));
        sharesField.setColumns(10);
        sharesField.setInputVerifier(new EditInputVerifier(true));
        sharesField.addActionListener(new FormattedTextFieldListener(this));
        if (shares != 0.0)
            sharesField.setValue(new Double(shares));
        
        //
        // Get the number of new shares
        //
        newSharesField = new JFormattedTextField(new EditNumber(4, false));
        newSharesField.setColumns(10);
        newSharesField.setInputVerifier(new EditInputVerifier(true));
        newSharesField.addActionListener(new FormattedTextFieldListener(this));
        if (newShares != 0.0)
            newSharesField.setValue(new Double(newShares));

        //
        // Get the share price
        //
        sharePriceField = new JFormattedTextField(new EditNumber(4, false));
        sharePriceField.setColumns(10);
        sharePriceField.setInputVerifier(new EditInputVerifier(true));
        sharePriceField.addActionListener(new FormattedTextFieldListener(this));
        if (sharePrice != 0.0)
            sharePriceField.setValue(new Double(sharePrice));

        //
        // Get the commission
        //
        commissionField = new JFormattedTextField(new EditNumber(2, true));
        commissionField.setColumns(10);
        commissionField.setInputVerifier(new EditInputVerifier(true));
        commissionField.addActionListener(new FormattedTextFieldListener(this));
        if (commission != 0.0)
            commissionField.setValue(new Double(commission));

        //
        // Get the transaction amount
        //
        amountField = new JFormattedTextField(new EditNumber(2, true));
        amountField.setColumns(10);
        amountField.setInputVerifier(new EditInputVerifier(true));
        amountField.addActionListener(new FormattedTextFieldListener(this));
        if (amount < 0.0)
            amount = -amount;
        if (amount != 0.0)
            amountField.setValue(new Double(amount));

        //
        // Create the accounting method radio buttons
        //
        fifoField = new JRadioButton("FIFO");
        lifoField = new JRadioButton("LIFO");
        avgCostField = new JRadioButton("Avg Cost");

        switch (accountingMethod) {
            case TransactionRecord.FIFO:
                fifoField.setSelected(true);
                break;

            case TransactionRecord.LIFO:
                lifoField.setSelected(true);
                break;

            case TransactionRecord.AVG_COST:
                avgCostField.setSelected(true);
                break;
        }

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(fifoField);
        buttonGroup.add(lifoField);
        buttonGroup.add(avgCostField);
        
        JPanel groupPane = new JPanel();
        groupPane.setLayout(new BoxLayout(groupPane, BoxLayout.X_AXIS));
        groupPane.add(fifoField);
        groupPane.add(Box.createHorizontalStrut(15));
        groupPane.add(lifoField);
        groupPane.add(Box.createHorizontalStrut(15));
        groupPane.add(avgCostField);
        
        //
        // Create the edit pane
        //
        //    Date:              <date-field>
        //    Action:            <combo-box>
        //    Security:          <combo-box>
        //    Category:          <combo-box>
        //    Shares:            <numeric-field>
        //    Price:             <numeric-field>
        //    Commission:        <numeric-field>
        //    Amount:            <numeric-field>
        //    Accounting Method: <radio-buttons>
        //    New Security:      <combo-box>
        //    New Shares:        <numeric-field>
        //
        JPanel editPane = new JPanel(new GridLayout(0, 2, 5, 5));
        
        editPane.add(new JLabel("Date:", JLabel.RIGHT));
        editPane.add(dateField);
        
        editPane.add(new JLabel("Action:", JLabel.RIGHT));
        editPane.add(actionField);
        
        editPane.add(new JLabel("Security:", JLabel.RIGHT));
        editPane.add(securityField);
        
        editPane.add(new JLabel("Category:", JLabel.RIGHT));
        editPane.add(categoryField);
        
        editPane.add(new JLabel("Shares:", JLabel.RIGHT));
        editPane.add(sharesField);
        
        editPane.add(new JLabel("Price:", JLabel.RIGHT));
        editPane.add(sharePriceField);
        
        editPane.add(new JLabel("Commission:", JLabel.RIGHT));
        editPane.add(commissionField);
        
        editPane.add(new JLabel("Amount:", JLabel.RIGHT));
        editPane.add(amountField);
        
        editPane.add(new JLabel("Accounting Method:", JLabel.RIGHT));
        editPane.add(groupPane);
        
        editPane.add(new JLabel("New Security:", JLabel.RIGHT));
        editPane.add(newSecurityField);
        
        editPane.add(new JLabel("New Shares:", JLabel.RIGHT));
        editPane.add(newSharesField);
        
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
     * Show the investment transaction edit dialog
     *
     * @param       parent              Parent window for the dialog
     * @param       transactionPanel    Transaction panel
     * @param       transaction         Transaction or null for a new transaction
     */
    public static void showDialog(JFrame parent, TransactionPanel transactionPanel, 
                                  TransactionRecord transaction) {
        try {
            String title = (transaction==null ? "New Transaction" : "Edit Transaction");
            JDialog dialog = new InvestmentTransactionEditDialog(parent, title, transactionPanel, transaction);
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
        // "help" - Display help for investment accounts
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
                    Main.mainWindow.displayHelp(HelpWindow.INVESTMENT_ACCOUNT);
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
        int index;
        int reconciled;
        int transactionPosition = -1, modelPosition = -1;

        //
        // Get the transaction date
        //
        if (!dateField.isEditValid()) {
            JOptionPane.showMessageDialog(this, "You must specify a transaction date",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Date date = (Date)dateField.getValue();
        
        //
        // Get action, security and category
        //
        int action = actionModel.getTypeAt(actionField.getSelectedIndex());
        SecurityRecord security = (SecurityRecord)securityModel.getDBElementAt(securityField.getSelectedIndex());
        CategoryRecord category = null;
        index = categoryField.getSelectedIndex();
        if (index > 0)
            category = (CategoryRecord)categoryModel.getDBElementAt(index);
        
        //
        // Get the accounting method
        //
        int accountingMethod;
        if (fifoField.isSelected()) {
            accountingMethod = TransactionRecord.FIFO;
        } else if (lifoField.isSelected()) {
            accountingMethod = TransactionRecord.LIFO;
        } else {
            accountingMethod = TransactionRecord.AVG_COST;
        }
        
        //
        // Get the number of shares
        //
        double shares = 0.0;
        if (sharesField.isEditValid())
            shares = ((Number)sharesField.getValue()).doubleValue();
        
        if (shares == 0.0) {
            if (action == TransactionRecord.BUY || action == TransactionRecord.SELL ||
                                                   action == TransactionRecord.REINVEST || 
                                                   action == TransactionRecord.EXCHANGE) {
                JOptionPane.showMessageDialog(this, "You must specify the number of shares",
                                              "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            if (action == TransactionRecord.SPLIT) {
                JOptionPane.showMessageDialog(this, "You must specify the share increase or decrease",
                                              "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            if (action == TransactionRecord.INCOME || action == TransactionRecord.EXPENSE) {
                JOptionPane.showMessageDialog(this,
                                "Shares may not be specified for an income or expense transaction",
                                "Error", JOptionPane.ERROR_MESSAGE);
                sharesField.setValue(null);
                return false;
            }

            if (action == TransactionRecord.RETURN_OF_CAPITAL) {
                JOptionPane.showMessageDialog(this,
                                "Shares may not be specified for a return of capital transaction",
                                "Error", JOptionPane.ERROR_MESSAGE);
                sharesField.setValue(null);
                return false;
            }
            
            if (action == TransactionRecord.SPIN_OFF) {
                JOptionPane.showMessageDialog(this,
                                "Shares may not be specified for a spin-off transaction",
                                "Error", JOptionPane.ERROR_MESSAGE);
                sharesField.setValue(null);
                return false;
            }
            
            if (action == TransactionRecord.AMORTIZATION || action == TransactionRecord.ACCRETION) {
                JOptionPane.showMessageDialog(this,
                                "Shares may not be specified for an amortization or accretion transaction",
                                "Error", JOptionPane.ERROR_MESSAGE);
                sharesField.setValue(null);
                return false;
            }
        }
        
        //
        // Get the new security
        //
        SecurityRecord newSecurity = null;
        index = newSecurityField.getSelectedIndex();
        if (index >= 0)
            newSecurity = (SecurityRecord)newSecurityModel.getDBElementAt(index);        

        if (newSecurity == null) {
            if (action == TransactionRecord.EXCHANGE || action == TransactionRecord.SPIN_OFF) {
                JOptionPane.showMessageDialog(this, "You must specify the new security for an exchange or spin-off transaction",
                                              "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            if (action != TransactionRecord.EXCHANGE && action != TransactionRecord.SPIN_OFF) {
                JOptionPane.showMessageDialog(this,
                                "A new security may be specified only for exchange and spin-off transactions",
                                "Error", JOptionPane.ERROR_MESSAGE);
                newSecurityField.setSelectedIndex(-1);
                return false;
            }                
        }
            
        //
        // Get the number of new shares
        //
        double newShares = 0.0;
        if (newSharesField.isEditValid())
            newShares = ((Number)newSharesField.getValue()).doubleValue();
        
        if (newShares == 0.0) {
            if (action == TransactionRecord.EXCHANGE || action == TransactionRecord.SPIN_OFF) {
                JOptionPane.showMessageDialog(this, "You must specify the number of new shares for an exchange or spin-off transaction",
                                              "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            if (action != TransactionRecord.EXCHANGE && action != TransactionRecord.SPIN_OFF) {
                JOptionPane.showMessageDialog(this,
                                "New shares may be specified only for exchange and spin-off transactions",
                                "Error", JOptionPane.ERROR_MESSAGE);
                newSharesField.setValue(null);
                return false;
            }
        }

        //
        // Get the share price
        //
        double sharePrice = 0.0;
        if (sharePriceField.isEditValid())
            sharePrice = ((Number)sharePriceField.getValue()).doubleValue();

        //
        // Get the commission
        //
        double commission = 0.0;
        if (commissionField.isEditValid())
            commission = ((Number)commissionField.getValue()).doubleValue();

        //
        // Get the transaction amount
        //
        double amount = 0.0;
        if (amountField.isEditValid())
            amount = ((Number)amountField.getValue()).doubleValue();

        if (amount == 0.0) {
            if (action == TransactionRecord.INCOME || action == TransactionRecord.EXPENSE) {
                JOptionPane.showMessageDialog(this,
                                "You must specify the amount for an income or expense transaction",
                                "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            if (action == TransactionRecord.RETURN_OF_CAPITAL) {
                JOptionPane.showMessageDialog(this,
                                "You must specify the amount for a return of capital transaction",
                                "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            if (action == TransactionRecord.AMORTIZATION || action == TransactionRecord.ACCRETION) {
                JOptionPane.showMessageDialog(this,
                                "You must specify the amount for an amortization or accretion transaction",
                                "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            if (action == TransactionRecord.EXCHANGE || action == TransactionRecord.SPIN_OFF) {
                JOptionPane.showMessageDialog(this,
                                "You must specify the new security basis amount for an exchange or spin-off transaction",
                                "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        //
        // The amount is negative for SELL, INCOME, ACCRETION and RETURN_OF_CAPITAL
        // transactions
        //
        if (action == TransactionRecord.SELL || action == TransactionRecord.INCOME ||
                        action == TransactionRecord.ACCRETION ||
                        action == TransactionRecord.RETURN_OF_CAPITAL)
            if (amount != 0.0)
                amount = -amount;

        //
        // Remove an existing transaction and set the transfer account
        //
        // There is no transfer account for accretion, amortization, exchange,
        // reinvest and spin-off transactions since these transaction affect
        // just the cost basis for the security
        //
        JTable table = transactionPanel.getTransactionTable();
        InvestmentTableModel tableModel = (InvestmentTableModel)table.getModel();
        
        if (transaction != null) {
            a = transaction.getAccount();
            t = transaction.getTransferAccount();
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
            transaction.setReconciled(reconciled);
            if (amount != 0.0 && action != TransactionRecord.AMORTIZATION && 
                            action != TransactionRecord.ACCRETION &&
                            action != TransactionRecord.EXCHANGE &&
                            action != TransactionRecord.SPIN_OFF &&
                            action != TransactionRecord.REINVEST) {
                if (t != null)
                    transaction.setTransferAccount(t);
                else
                    transaction.setTransferAccount(account.getLinkedAccount());
            }
        } else {
            transaction = new TransactionRecord(date, account);
            if (amount != 0.0 && action != TransactionRecord.AMORTIZATION &&
                            action != TransactionRecord.ACCRETION &&
                            action != TransactionRecord.EXCHANGE &&
                            action != TransactionRecord.SPIN_OFF &&
                            action != TransactionRecord.REINVEST)
                transaction.setTransferAccount(account.getLinkedAccount());
        }

        //
        // Build the new transaction
        //
        transaction.setAction(action);
        transaction.setSecurity(security);
        transaction.setCategory(category);
        transaction.setShares(shares);
        transaction.setSharePrice(sharePrice);
        transaction.setCommission(commission);
        transaction.setAmount(amount);
        
        if (action == TransactionRecord.SELL)
            transaction.setAccountingMethod(accountingMethod);
        
        if (newShares != 0.0) {
            transaction.setNewSecurity(newSecurity);
            transaction.setNewShares(newShares);
        }
        
        switch (action) {
            case TransactionRecord.BUY:
            case TransactionRecord.REINVEST:
                transaction.setMemo("Bought "+transaction.getShares()+" shares of "+security.getName());
                break;

            case TransactionRecord.SELL:
                transaction.setMemo("Sold "+transaction.getShares()+" shares of "+security.getName());
                break;

            case TransactionRecord.INCOME:
            case TransactionRecord.RETURN_OF_CAPITAL:
                transaction.setMemo("Income from "+security.getName());
                break;

            case TransactionRecord.EXPENSE:
                transaction.setMemo("Expense for "+security.getName());
                break;

            case TransactionRecord.SPLIT:
                transaction.setMemo("Stock split for "+security.getName());
                break;
                
            case TransactionRecord.AMORTIZATION:
                transaction.setMemo("Amortization for "+security.getName());
                break;
                
            case TransactionRecord.ACCRETION:
                transaction.setMemo("Accretion for "+security.getName());
                break;
                
            case TransactionRecord.EXCHANGE:
                transaction.setMemo("Stock exchange for "+security.getName());
                break;
                
            case TransactionRecord.SPIN_OFF:
                transaction.setMemo("Stock spin-off for "+security.getName());
                break;
        }

        //
        // Add the new transaction and scroll the table to display the transaction
        //
        if (transactionPosition >= 0)
            TransactionRecord.transactions.add(transactionPosition, transaction);
        else
            TransactionRecord.insertTransaction(transaction);

        int modelRow = tableModel.transactionAdded(modelPosition, transaction);
        transactionPanel.showSelectedRow(table.convertRowIndexToView(modelRow));

        //
        // Update the price history for the security
        //
        if ((action == TransactionRecord.BUY || action == TransactionRecord.SELL ||
                                                action == TransactionRecord.REINVEST) && 
                                    sharePrice != 0.0) {
            SortedSet<PriceHistory> history = security.getPriceHistory();
            PriceHistory ph = new PriceHistory(date, sharePrice);
            history.remove(ph);
            history.add(ph);
        }

        //
        // All done
        //
        Main.dataModified = true;
        return true;
    }
}
