package MyMoney;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Dialog to manage securities
 */
public final class SecurityDialog extends JDialog implements ActionListener {

    /** Security list model */
    private DBElementListModel listModel;

    /** Security list */
    private JList list;

    /**
     * Create the security dialog
     *
     * @param       parent          Parent window
     */
    public SecurityDialog(JFrame parent) {
        super(parent, "Securities", Dialog.ModalityType.DOCUMENT_MODAL);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        //
        // Create the list model
        //
        listModel = new DBElementListModel(SecurityRecord.securities);

        //
        // Create the security list
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
        // Create the buttons (New Security, Edit Security, Edit History,
        // Stock Split, Delete Security, Done, Help)
        //
        JPanel buttonPane = new JPanel(new GridLayout(0, 1, 0, 5));

        JButton button = new JButton("New Security");
        button.setActionCommand("new");
        button.addActionListener(this);
        buttonPane.add(button);

        button = new JButton("Edit Security");
        button.setActionCommand("edit");
        button.addActionListener(this);
        buttonPane.add(button);

        button = new JButton("Edit History");
        button.setActionCommand("history");
        button.addActionListener(this);
        buttonPane.add(button);

        button = new JButton("Stock Split");
        button.setActionCommand("split");
        button.addActionListener(this);
        buttonPane.add(button);

        button = new JButton("Delete Security");
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
     * Show the security dialog
     *
     * @param       parent          Parent window for the dialog
     */
    public static void showDialog(JFrame parent) {
        try {
            JDialog dialog = new SecurityDialog(parent);
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
        // "new" - Add a security
        // "edit" - Edit a security
        // "history" - Edit the price history
        // "split" - Update price history for a stock split
        // "delete" - Delete a security
        // "done" - All done
        // "help" - Display help for securities
        //
        try {
            String action = ae.getActionCommand();
            if (action.equals("new")) {
                SecurityEditDialog.showDialog(this, listModel, -1);
            } else if (action.equals("edit")) {
                int index = list.getSelectedIndex();
                if (index < 0) {
                    JOptionPane.showMessageDialog(this, "You must select a security to edit",
                                                  "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    SecurityEditDialog.showDialog(this, listModel, index);
                }
            } else if (action.equals("history")) {
                int index = list.getSelectedIndex();
                if (index < 0) {
                    JOptionPane.showMessageDialog(this, "You must select a security to edit",
                                                  "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    PriceHistoryDialog.showDialog(this, (SecurityRecord)listModel.getDBElementAt(index));
                }
            } else if (action.equals("split")) {
                int index = list.getSelectedIndex();
                if (index < 0) {
                    JOptionPane.showMessageDialog(this, "You must select a security for the split",
                                                  "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    StockSplitDialog.showDialog(this, (SecurityRecord)listModel.getDBElementAt(index));
                }
            } else if (action.equals("delete")) {
                int index = list.getSelectedIndex();
                if (index < 0) {
                    JOptionPane.showMessageDialog(this, "You must select a security to delete",
                                                  "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    SecurityRecord security = (SecurityRecord)listModel.getDBElementAt(index);
                    int option = JOptionPane.showConfirmDialog(this,
                                        "Do you want to delete '"+security.getName()+"'?",
                                        "Confirm Delete", JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.YES_OPTION) {
                        if (security.isReferenced()) {
                            JOptionPane.showMessageDialog(this,
                                        "Security is referenced by one or more transactions",
                                        "Error", JOptionPane.ERROR_MESSAGE);
                        } else {
                            SecurityRecord.securities.remove(security);
                            listModel.removeDBElement(security);
                            Main.dataModified = true;
                        }
                    }
                }
            } else if (action.equals("done")) {
                setVisible(false);
                dispose();
            } else if (action.equals("help")) {
                Main.mainWindow.displayHelp(HelpWindow.SECURITIES);
            }
        } catch (Exception exc) {
            Main.logException("Exception while processing action event", exc);
        }
    }
}
