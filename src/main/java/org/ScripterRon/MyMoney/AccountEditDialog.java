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

import java.util.SortedSet;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Dialog to edit an account.
 */
public final class AccountEditDialog extends JDialog implements ActionListener {
    
    /** List model */
    private DBElementListModel listModel;

    /** Current account or null for a new account */
    private AccountRecord account;

    /** Account type */
    private JComboBox accountType;
    
    /** Account type model */
    private DBElementTypeComboBoxModel accountTypeModel;

    /** Linked account */
    private JComboBox linkedAccount;
    
    /** Linked account model */
    private LinkAccountComboBoxModel linkedAccountModel;

    /** Account name */
    private JTextField accountName;

    /** Account number */
    private JTextField accountNumber;

    /** Loan rate */
    private JFormattedTextField loanRate;

    /** Account is hidden */
    private JCheckBox accountHidden;
    
    /** Account is tax deferred */
    private JCheckBox taxDeferred;

    /**
     * Create the dialog
     *
     * @param       parent          Parent window
     * @param       title           Dialog title
     * @param       listModel       List model
     * @param       account         Account to edit or null for a new account
     */
    public AccountEditDialog(JDialog parent, String title, DBElementListModel listModel, AccountRecord account) {
        super(parent, title, Dialog.ModalityType.DOCUMENT_MODAL);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        //
        // Save the account information
        //
        this.listModel = listModel;
        this.account = account;

        //
        // Build the account types for a new account (the account
        // type for an existing account cannot be changed)
        //
        if (account == null) {
            accountTypeModel = new DBElementTypeComboBoxModel(AccountRecord.getTypes(), 
                                                              AccountRecord.getTypeStrings());
            accountType = new JComboBox(accountTypeModel);
            accountType.setSelectedIndex(-1);
        }

        //
        // Build the linked account list for a new account or for an
        // existing investment account.  An existing linked account will
        // be set as the initial selection.  The initial selection will
        // be "--None--" for a new account.
        //
        if (account == null || (account != null && account.getType() == AccountRecord.INVESTMENT)) {
            linkedAccountModel = new LinkAccountComboBoxModel(account);
            linkedAccount = new JComboBox(linkedAccountModel);
        }

        //
        // Create the edit pane
        //
        //    Account Type:   <combo-box>
        //    Account Name:   <text-field>
        //    Account Number: <text-field>
        //    Loan Rate (%):  <text-field> (optional)
        //    Linked Account: <combo-box>  (optional)
        //
        //    Account Hidden: <check-box>
        //    Tax Deferred:   <check-box>
        //
        JPanel editPane = new JPanel(new GridLayout(0, 2, 5, 5));

        editPane.add(new JLabel("Account Type:", JLabel.RIGHT));
        if (account != null)
            editPane.add(new JLabel(AccountRecord.getTypeString(account.getType())));
        else
            editPane.add(accountType);

        editPane.add(new JLabel("Account Name:", JLabel.RIGHT));
        if (account != null)
            accountName = new JTextField(account.getName(), Math.max(account.getName().length(), 15));
        else
            accountName = new JTextField(15);
        editPane.add(accountName);

        editPane.add(new JLabel("Account Number:", JLabel.RIGHT));
        if (account != null)
            accountNumber = new JTextField(account.getNumber());
        else
            accountNumber = new JTextField(15);
        editPane.add(accountNumber);

        if (account == null || account.getType() == AccountRecord.LOAN) {
            editPane.add(new JLabel("Loan Rate (%):", JLabel.RIGHT));
            loanRate = new JFormattedTextField(new EditNumber(4, true));
            loanRate.setColumns(8);
            loanRate.setInputVerifier(new EditInputVerifier(true));
            loanRate.addActionListener(new FormattedTextFieldListener(this));
            if (account != null)
                loanRate.setValue(new Double(account.getLoanRate()*100.0));

            editPane.add(loanRate);
        }

        if (linkedAccount != null) {
            editPane.add(new JLabel("Linked Account:", JLabel.RIGHT));
            editPane.add(linkedAccount);
        }
        
        editPane.add(Box.createGlue());
        accountHidden = new JCheckBox("Account Hidden");
        if (account != null)
            accountHidden.setSelected(account.isHidden());
        else
            accountHidden.setSelected(false);
        editPane.add(accountHidden);
        
        editPane.add(Box.createGlue());
        taxDeferred = new JCheckBox("Tax Deferred");
        if (account != null)
            taxDeferred.setSelected(account.isTaxDeferred());
        else
            taxDeferred.setSelected(false);
        editPane.add(taxDeferred);

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
        contentPane.add(buttonPane);
        setContentPane(contentPane);
    }

    /**
     * Show the account edit dialog
     *
     * @param       parent          Parent window for the dialog
     * @param       listModel       List model
     * @param       index           List index or -1 for a new account
     */
    public static void showDialog(JDialog parent, DBElementListModel listModel, int index) {
        try {
            String title;
            AccountRecord account;
            if (index >= 0) {
                title = "Edit Account";
                account = (AccountRecord)listModel.getDBElementAt(index);
            } else {
                title = "Add Account";
                account = null;
            }
            
            JDialog dialog = new AccountEditDialog(parent, title, listModel, account);
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
        // "help" - Display help for accounts
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
                    Main.mainWindow.displayHelp(HelpWindow.ACCOUNTS);
                    break;
            }
        } catch (Exception exc) {
            Main.logException("Exception while processing action event", exc);
        }
    }

    /**
     * Process the account fields
     *
     * @return                  TRUE if the entered data is valid
     */
    private boolean processFields() {

        //
        // Validate the account information
        //
        int type;
        String name;
        String number;
        boolean hidden, deferred, newAccount;
        double rate;

        if (account == null) {
            type = accountType.getSelectedIndex();
            if (type < 0) {
                JOptionPane.showMessageDialog(this, "You must select an account type",
                                              "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            type = accountTypeModel.getTypeAt(type);
        } else {
            type = account.getType();
        }

        name = accountName.getText();
        if (name.length() == 0) {
            JOptionPane.showMessageDialog(this, "You must specify an account name",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        for (AccountRecord a : AccountRecord.accounts) {
            if (name.equals(a.getName()) && a != account) {
                JOptionPane.showMessageDialog(this, "Account name '"+name+"' is already in use",
                                              "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        if (linkedAccount != null) {
            if (linkedAccount.getSelectedIndex() > 0) {
                if (type != AccountRecord.INVESTMENT) {
                    JOptionPane.showMessageDialog(this,
                            "A linked account can be specified only for an investment account",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } else if (type == AccountRecord.INVESTMENT) {
                JOptionPane.showMessageDialog(this,
                            "A linked account must be specified for an investment account",
                            "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        number = accountNumber.getText();
        hidden = accountHidden.isSelected();
        deferred = taxDeferred.isSelected();

        if (loanRate != null && loanRate.isEditValid())
            rate = (((Number)loanRate.getValue()).doubleValue())/100.0;
        else
            rate = 0.0;

        if (type == AccountRecord.LOAN) {
            if (rate == 0.0) {
                JOptionPane.showMessageDialog(this,
                                    "You must specify the rate for a loan account",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            if (rate != 0.0) {
                JOptionPane.showMessageDialog(this,
                                    "A rate may be specified only for a loan account",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        //
        // Create a new account or update an existing account
        //
        if (account == null) {
            newAccount = true;
            account = new AccountRecord(name, type);
            if (AccountRecord.accounts.contains(account)) {
                JOptionPane.showMessageDialog(this, "Account '"+name+"' already exists",
                                              "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            AccountRecord.accounts.add(account);
        } else {
            newAccount = false;
            if (!account.getName().equals(name)) {
                AccountRecord.accounts.remove(account);
                account.setName(name);
                AccountRecord.accounts.add(account);
            }
        }

        if (type == AccountRecord.INVESTMENT)
            account.setLinkedAccount(linkedAccountModel.getAccountAt(linkedAccount.getSelectedIndex()));

        account.setNumber(number);
        account.setHide(hidden);
        account.setLoanRate(rate);
        account.setTaxDeferred(deferred);

        if (newAccount)
            listModel.addDBElement(account);
        else
            listModel.updateDBElement();

        Main.dataModified = true;
        return true;
    }
}
