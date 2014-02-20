package MyMoney;

import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Asset transaction edit dialog
 */
public final class AssetTransactionEditDialog extends JDialog implements ActionListener {

    /** The transaction panel */
    private TransactionPanel transactionPanel;
    
    /** The transaction account */
    private AccountRecord account;
    
    /** The transaction */
    private TransactionRecord transaction;

    /** Date field */
    private JFormattedTextField dateField;

    /** Asset description field */
    private JTextField nameField;

    /** Memo field */
    private JTextField memoField;

    /** Asset value decrease field */
    private JFormattedTextField decreaseField;

    /** Asset value increase field */
    private JFormattedTextField increaseField;

    /**
     * Create the dialog
     *
     * @param       parent              Parent window for the dialog
     * @param       title               Dialog title
     * @param       transactionPanel    Transaction panel
     * @param       transaction         Transaction or null for a new transaction
     */
    public AssetTransactionEditDialog(JFrame parent, String title, TransactionPanel transactionPanel,
                                      TransactionRecord transaction) {
        super(parent, title, Dialog.ModalityType.DOCUMENT_MODAL);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.transactionPanel = transactionPanel;
        this.transaction = transaction;
        account = transactionPanel.getTransactionAccount();

        //
        // Get the transaction specifics for an existing transaction
        //
        String name = null;
        double decrease = 0.00;
        double increase = 0.00;
        double amount;
        if (transaction != null) {
            name = transaction.getName();
            amount = transaction.getAmount();
            if (amount < 0.0)
                decrease = -amount;
            else if (amount > 0.0)
                increase = amount;
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
        // Get the description
        //
        if (name != null)
            nameField = new JTextField(name, 20);
        else
            nameField = new JTextField(20);

        //
        // Get the memo
        //
        if (transaction != null)
            memoField = new JTextField(transaction.getMemo(), 25);
        else
            memoField = new JTextField(25);

        //
        // Get the decrease in value
        //
        decreaseField = new JFormattedTextField(new EditNumber(2, true));
        decreaseField.setColumns(10);
        decreaseField.setInputVerifier(new EditInputVerifier(true));
        decreaseField.addActionListener(new FormattedTextFieldListener(this));
        if (decrease != 0.00)
            decreaseField.setValue(new Double(decrease));

        //
        // Get the increase in value
        //
        increaseField = new JFormattedTextField(new EditNumber(2, true));
        increaseField.setColumns(10);
        increaseField.setInputVerifier(new EditInputVerifier(true));
        increaseField.addActionListener(new FormattedTextFieldListener(this));
        if (increase != 0.00)
            increaseField.setValue(new Double(increase));

        //
        // Create the edit pane
        //
        //    Date:              <text-field>
        //    Description:       <text-field>
        //    Memo:              <text-field>
        //    Decrease:          <text-field>
        //    Increase:          <text-field>
        //
        JPanel editPane = new JPanel(new GridLayout(0, 2, 5, 5));
        
        editPane.add(new JLabel("Date:", JLabel.RIGHT));
        editPane.add(dateField);

        editPane.add(new JLabel("Description:", JLabel.RIGHT));
        editPane.add(nameField);

        editPane.add(new JLabel("Memo:", JLabel.RIGHT));
        editPane.add(memoField);

        editPane.add(new JLabel("Decrease:", JLabel.RIGHT));
        editPane.add(decreaseField);

        editPane.add(new JLabel("Increase:", JLabel.RIGHT));
        editPane.add(increaseField);

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
     * Show the asset transaction edit dialog
     *
     * @param       parent              Parent window for the dialog
     * @param       transactionPanel    Transaction panel
     * @param       transaction         Transaction or null for a new transaction
     */
    public static void showDialog(JFrame parent, TransactionPanel transactionPanel, 
                                  TransactionRecord transaction) {
        try {
            String title = (transaction==null ? "New Transaction" : "Edit Transaction");
            JDialog dialog = new AssetTransactionEditDialog(parent, title, transactionPanel, transaction);
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
        // "help" - Display help for asset accounts
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
                    Main.mainWindow.displayHelp(HelpWindow.ASSET_ACCOUNT);
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
        int transactionPosition = -1;
        int modelPosition = -1;

        //
        // Validate the transaction information
        //
        if (!dateField.isEditValid()) {
            JOptionPane.showMessageDialog(this, "You must specify a transaction date",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!decreaseField.isEditValid() && !increaseField.isEditValid()) {
            JOptionPane.showMessageDialog(this, "You must specify a decrease or increase amount",
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
        }

        //
        // Build the new transaction
        //
        transaction = new TransactionRecord(date, account);
        transaction.setName(nameField.getText());
        transaction.setMemo(memoField.getText());
        double amount = 0.00;
        if (decreaseField.isEditValid())
            amount -= ((Number)decreaseField.getValue()).doubleValue();

        if (increaseField.isEditValid())
            amount += ((Number)increaseField.getValue()).doubleValue();

        transaction.setAmount(amount);

        //
        // Add the transaction and scroll the table to display the transaction.
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
