package MyMoney;

import java.util.SortedSet;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Dialog to edit a security
 */
public final class SecurityEditDialog extends JDialog implements ActionListener {
    
    /** List model */
    private DBElementListModel listModel;

    /** Security or null */
    private SecurityRecord security;

    /** Security type field */
    private JComboBox securityType;
    
    /** Security type model */
    private DBElementTypeComboBoxModel securityTypeModel;

    /** Security name field */
    private JTextField securityName;

    /** Ticker symbol field */
    private JTextField tickerSymbol;
    
    /** Income payment field */
    private JComboBox incomePayment;
    
    /** Income payment model */
    private DBElementTypeComboBoxModel incomePaymentModel;

    /** Security hidden field */
    private JCheckBox securityHidden;

    /**
     * Create the dialog
     *
     * @param       parent          Parent window
     * @param       title           Dialog title
     * @param       listModel       List model
     * @param       security        Security to edit or null for a new security
     */
    public SecurityEditDialog(JDialog parent, String title, DBElementListModel listModel, SecurityRecord security) {
        super(parent, title, Dialog.ModalityType.DOCUMENT_MODAL);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        //
        // Save the security information
        //
        this.listModel = listModel;
        this.security = security;

        //
        // Create the edit pane
        //
        //    Security Type:   <combo-box>
        //    Security Name:   <text-field>
        //    Ticker Symbol:   <text-field>
        //    Income Payments: <combo-box>
        //    Security Hidden: <check-box>
        //
        JPanel editPane = new JPanel(new GridLayout(0, 2, 5, 5));

        editPane.add(new JLabel("Security Type:", JLabel.RIGHT));
        securityTypeModel = new DBElementTypeComboBoxModel(SecurityRecord.getTypes(),
                                                           SecurityRecord.getTypeStrings());
        securityType = new JComboBox(securityTypeModel);
        if (security != null)
            securityTypeModel.setSelectedItem(SecurityRecord.getTypeString(security.getType()));
        else
            securityTypeModel.setSelectedItem(null);
        editPane.add(securityType);

        editPane.add(new JLabel("Security Name:", JLabel.RIGHT));
        if (security != null)
            securityName = new JTextField(security.getName());
        else
            securityName = new JTextField(15);
        editPane.add(securityName);

        editPane.add(new JLabel("Ticker Symbol:", JLabel.RIGHT));
        if (security != null)
            tickerSymbol = new JTextField(security.getSymbol());
        else
            tickerSymbol = new JTextField(15);
        editPane.add(tickerSymbol);
        
        editPane.add(new JLabel("Income Payments:", JLabel.RIGHT));
        incomePaymentModel = new DBElementTypeComboBoxModel(SecurityRecord.getPaymentTypes(),
                                                            SecurityRecord.getPaymentTypeStrings());
        incomePayment = new JComboBox(incomePaymentModel);
        if (security != null)
            incomePaymentModel.setSelectedItem(SecurityRecord.getPaymentTypeString(security.getPaymentType()));
        else
            incomePaymentModel.setSelectedItem(SecurityRecord.getPaymentTypeString(SecurityRecord.NO_PAYMENTS));
        editPane.add(incomePayment);

        editPane.add(Box.createGlue());
        securityHidden = new JCheckBox("Security Hidden");
        if (security != null)
            securityHidden.setSelected(security.isHidden());
        else
            securityHidden.setSelected(false);
        editPane.add(securityHidden);

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
     * Show the security edit dialog
     *
     * @param       parent          Parent window for the dialog
     * @param       listModel       List model
     * @param       index           List index or -1 for a new security
     */
    public static void showDialog(JDialog parent, DBElementListModel listModel, int index) {
        try {
            String title;
            SecurityRecord security;
            if (index >= 0) {
                title = "Edit Security";
                security = (SecurityRecord)listModel.getDBElementAt(index);
            } else {
                title = "Add Security";
                security = null;
            }
            
            JDialog dialog = new SecurityEditDialog(parent, title, listModel, security);
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
        // "cancel" - Cancel request
        // "help" - Display help for securities
        //
        try {
            String action = ae.getActionCommand();
            if (action.equals("ok")) {
                if (processFields()) {
                    setVisible(false);
                    dispose();
                }
            } else if (action.equals("cancel")) {
                setVisible(false);
                dispose();
            } else if (action.equals("help")) {
                Main.mainWindow.displayHelp(HelpWindow.SECURITIES);
            }
        } catch (Exception exc) {
            Main.logException("Exception while processing action event", exc);
        }
    }

    /**
     * Process the security fields
     *
     * @return                  TRUE if the entered data is valid
     */
    private boolean processFields() {
        boolean newSecurity;

        //
        // Validate the security information
        //
        int type = securityType.getSelectedIndex();
        if (type < 0) {
            JOptionPane.showMessageDialog(this, "You must select a security type",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        type = securityTypeModel.getTypeAt(type);
        
        String name = securityName.getText();
        if (name.length() == 0) {
            JOptionPane.showMessageDialog(this, "You must specify a security name",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        for (SecurityRecord s : SecurityRecord.securities) {
            if (name.equals(s.getName()) && s != security) {
                JOptionPane.showMessageDialog(this, "Security name '"+name+"' is already in use",
                                              "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        String symbol = tickerSymbol.getText().toUpperCase();
        int paymentType = incomePaymentModel.getTypeAt(incomePayment.getSelectedIndex());
        boolean hidden = securityHidden.isSelected();

        //
        // Create a new security or update an existing security
        //
        if (security == null) {
            newSecurity = true;
            security = new SecurityRecord(name, type);
            if (SecurityRecord.securities.contains(security)) {
                JOptionPane.showMessageDialog(this, "Security '"+name+"' already exists",
                                              "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            SecurityRecord.securities.add(security);
        } else {
            newSecurity = false;
            security.setType(type);
            if (!security.getName().equals(name)) {
                SecurityRecord.securities.remove(security);
                security.setName(name);
                SecurityRecord.securities.add(security);
            }
        }

        security.setSymbol(symbol);
        security.setPaymentType(paymentType);
        security.setHide(hidden);
        
        if (newSecurity)
            listModel.addDBElement(security);
        else
            listModel.updateDBElement();

        Main.dataModified = true;
        return true;
    }    
}
