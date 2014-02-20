package MyMoney;

import java.util.SortedSet;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Dialog to manage categories
 */
public final class CategoryDialog extends JDialog implements ActionListener {

    /** Category list model */
    private DBElementListModel listModel;

    /** Category list */
    private JList list;

    /**
     * Create the dialog
     *
     * @param       parent          Parent frame
     */
    public CategoryDialog(JFrame parent) {
        super(parent, "Categories", Dialog.ModalityType.DOCUMENT_MODAL);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        //
        // Create the list model
        //
        listModel = new DBElementListModel(CategoryRecord.categories);

        //
        // Create the category list
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
        // Create the buttons (New Category, Edit Category, Delete Category, Done, Help)
        //
        JPanel buttonPane = new JPanel(new GridLayout(0, 1, 0, 5));

        JButton button = new JButton("New Category");
        button.setActionCommand("new");
        button.addActionListener(this);
        buttonPane.add(button);

        button = new JButton("Edit Category");
        button.setActionCommand("edit");
        button.addActionListener(this);
        buttonPane.add(button);

        button = new JButton("Delete Category");
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
     * Show the category dialog
     *
     * @param       parent          Parent window for the dialog
     */
    public static void showDialog(JFrame parent) {
        try {
            JDialog dialog = new CategoryDialog(parent);
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
        // "new" - Add a new category
        // "edit" - Edit a category
        // "delete" - Delete a category
        // "done" - All done
        // "help" - Display help for categories
        //
        try {
            String action = ae.getActionCommand();
            if (action.equals("new")) {
                CategoryEditDialog.showDialog(this, listModel, -1);
            } else if (action.equals("edit")) {
                int index = list.getSelectedIndex();
                if (index < 0) {
                    JOptionPane.showMessageDialog(this, "You must select a category to edit",
                                                  "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    CategoryEditDialog.showDialog(this, listModel, index);
                }
            } else if (action.equals("delete")) {
                int index = list.getSelectedIndex();
                if (index < 0) {
                    JOptionPane.showMessageDialog(this, "You must select a category to delete",
                                                  "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    CategoryRecord category = (CategoryRecord)listModel.getDBElementAt(index);
                    int option = JOptionPane.showConfirmDialog(this,
                                            "Do you want to delete '"+category.getName()+"'?",
                                            "Confirm Delete", JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.YES_OPTION) {
                        if (category.isReferenced()) {
                            JOptionPane.showMessageDialog(this,
                                            "Category is referenced by one or more transactions",
                                            "Error", JOptionPane.ERROR_MESSAGE);
                        } else {
                            CategoryRecord.categories.remove(category);
                            listModel.removeDBElement(category);
                            Main.dataModified = true;
                        }
                    }
                }
            } else if (action.equals("done")) {
                setVisible(false);
                dispose();
            } else if (action.equals("help")) {
                Main.mainWindow.displayHelp(HelpWindow.CATEGORIES);
            }
        } catch (Exception exc) {
            Main.logException("Exception while processing action event", exc);
        }
    }
}
