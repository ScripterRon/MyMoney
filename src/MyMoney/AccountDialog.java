package MyMoney;

import java.util.SortedSet;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Dialog to manage accounts for the MyMoney application.
 */
public final class AccountDialog extends JDialog implements ActionListener {

    /** Account list model */
    private DBElementListModel listModel;

    /** Account list */
    private JList list;

    /**
     * Create the dialog
     *
     * @param       parent          Parent frame
     */
    public AccountDialog(JFrame parent) {
        super(parent, "Accounts", Dialog.ModalityType.DOCUMENT_MODAL);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        //
        // Create the list model
        //
        listModel = new DBElementListModel(AccountRecord.accounts);

        //
        // Create the account list
        //
        list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(-1);
        list.setVisibleRowCount(15);
        list.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        //
        // Put the list in a scroll pane
        //
        JScrollPane scrollPane = new JScrollPane(list);

        //
        // Create the buttons (New Account, Edit Account, Delete Account, Done, Help)
        //
        JPanel buttonPane = new JPanel(new GridLayout(0, 1, 0, 5));

        JButton button = new JButton("New Account");
        button.setActionCommand("new");
        button.addActionListener(this);
        buttonPane.add(button);

        button = new JButton("Edit Account");
        button.setActionCommand("edit");
        button.addActionListener(this);
        buttonPane.add(button);

        button = new JButton("Delete Account");
        button.setActionCommand("delete");
        button.addActionListener(this);
        buttonPane.add(button);

        button = new JButton("Done");
        button.setActionCommand("done");
        button.addActionListener(this);
        buttonPane.add(button);
        
        button = new JButton("Help");
        button.setActionCommand("help");
        button.addActionListener(this);
        buttonPane.add(button);

        //
        // Set up the content pane
        //
        JPanel contentPane = new JPanel();
        contentPane.setOpaque(true);
        contentPane.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        contentPane.add(scrollPane);
        contentPane.add(Box.createHorizontalStrut(15));
        contentPane.add(buttonPane);
        setContentPane(contentPane);
    }

    /**
     * Show the account dialog
     *
     * @param       parent          Parent window for the dialog
     */
    public static void showDialog(JFrame parent) {
        try {
            JDialog dialog = new AccountDialog(parent);
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
     * @param       ae              Action event
     */
    public void actionPerformed(ActionEvent ae) {

        //
        // Process the action command
        //
        // "new" - Add an account
        // "edit" - Edit an account
        // "delete" - Delete an account
        // "done" - All done
        // "help" - Display help for accounts
        //
        try {
            String action = ae.getActionCommand();
            if (action.equals("new")) {
                AccountEditDialog.showDialog(this, listModel, -1);
            } else if (action.equals("edit")) {
                int index = list.getSelectedIndex();
                if (index < 0) {
                    JOptionPane.showMessageDialog(this, "You must select an account to edit",
                                                  "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    AccountEditDialog.showDialog(this, listModel, index);
                }
            } else if (action.equals("delete")) {
                int index = list.getSelectedIndex();
                if (index < 0) {
                    JOptionPane.showMessageDialog(this, "You must select an account to delete",
                                                  "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    AccountRecord account = (AccountRecord)listModel.getDBElementAt(index);
                    int option = JOptionPane.showConfirmDialog(this,
                                        "Do you want to delete '"+account.getName()+"'?",
                                        "Confirm Delete", JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.YES_OPTION) {
                        if (account.isReferenced()) {
                            JOptionPane.showMessageDialog(this,
                                        "Account is referenced by one or more transactions",
                                        "Error", JOptionPane.ERROR_MESSAGE);
                        } else if (account.getLinkCount() != 0) {
                            JOptionPane.showMessageDialog(this,
                                        "Account is linked to another account",
                                        "Error", JOptionPane.ERROR_MESSAGE);
                        } else {
                            account.setLinkedAccount(null);
                            AccountRecord.accounts.remove(account);
                            listModel.removeDBElement(account);
                            Main.dataModified = true;
                        }
                    }
                }
            } else if (action.equals("done")) {
                setVisible(false);
                dispose();
            } else if (action.equals("help")) {
                Main.mainWindow.displayHelp(HelpWindow.ACCOUNTS);
            }
        } catch (Exception exc) {
            Main.logException("Exception while processing action event", exc);
        }
    }
}
