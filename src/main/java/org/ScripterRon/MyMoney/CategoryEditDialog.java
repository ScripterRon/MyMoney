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
 * Dialog to edit a category
 */
public final class CategoryEditDialog extends JDialog implements ActionListener {

    /** List model */
    private DBElementListModel listModel;
    
    /** Current category or null for a new category */
    private CategoryRecord category;

    /** Category type field */
    private JComboBox categoryType;
    
    /** Category type model */
    private DBElementTypeComboBoxModel categoryTypeModel;

    /** Category name field */
    private JTextField categoryName;
    
    /** Category is hidden */
    private JCheckBox categoryHidden;

    /**
     * Create the dialog
     *
     * @param       parent          Parent window
     * @param       title           Dialog title
     * @param       listModel       List model
     * @param       category        Category to edit or null for a new category
     */
    public CategoryEditDialog(JDialog parent, String title, DBElementListModel listModel, CategoryRecord category) {
        super(parent, title, Dialog.ModalityType.DOCUMENT_MODAL);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        //
        // Save the category information
        //
        this.listModel = listModel;
        this.category = category;

        //
        // Create the edit pane
        //
        //    Category Type:   <combo-box>
        //    Category Name:   <text-field>
        //    Category Hidden: <check-box>
        //
        JPanel editPane = new JPanel(new GridLayout(0, 2, 5, 5));

        editPane.add(new JLabel("Category Type:", JLabel.RIGHT));
        categoryTypeModel = new DBElementTypeComboBoxModel(CategoryRecord.getTypes(),
                                                           CategoryRecord.getTypeStrings());
        categoryType = new JComboBox(categoryTypeModel);
        if (category != null)
            categoryTypeModel.setSelectedItem(CategoryRecord.getTypeString(category.getType()));
        else
            categoryTypeModel.setSelectedItem(null);
        editPane.add(categoryType);

        editPane.add(new JLabel("Category Name:", JLabel.RIGHT));
        if (category != null)
            categoryName = new JTextField(category.getName());
        else
            categoryName = new JTextField(15);
        editPane.add(categoryName);
        
        editPane.add(Box.createGlue());
        categoryHidden = new JCheckBox("Category Hidden");
        if (category != null)
            categoryHidden.setSelected(category.isHidden());
        else
            categoryHidden.setSelected(false);
        editPane.add(categoryHidden);

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
     * Show the category edit dialog
     *
     * @param       parent          Parent window for the dialog
     * @param       listModel       List model
     * @param       index           List index or -1 for a new category
     */
    public static void showDialog(JDialog parent, DBElementListModel listModel, int index) {
        try {
            String title;
            CategoryRecord category;
            if (index >= 0) {
                title = "Edit Category";
                category = (CategoryRecord)listModel.getDBElementAt(index);
            } else {
                title = "Add Category";
                category = null;
            }
            
            JDialog dialog = new CategoryEditDialog(parent, title, listModel, category);
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
        // "help" - Display help for categories
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
                Main.mainWindow.displayHelp(HelpWindow.CATEGORIES);
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
        // Validate the category information
        //
        int type = categoryType.getSelectedIndex();
        if (type < 0) {
            JOptionPane.showMessageDialog(this, "You must select a category type",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        type = categoryTypeModel.getTypeAt(type);
        
        String name = categoryName.getText();
        if (name.length() == 0) {
            JOptionPane.showMessageDialog(this, "You must specify a category name",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        for (CategoryRecord c : CategoryRecord.categories) {
            if (name.equals(c.getName()) && c != category) {
                JOptionPane.showMessageDialog(this, "Category name '"+name+"' is already in use",
                                              "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        
        //
        // Create a new category or update an existing category
        //
        if (category == null) {
            category = new CategoryRecord(name, type);
            category.setHide(categoryHidden.isSelected());
            if (CategoryRecord.categories.contains(category)) {
                JOptionPane.showMessageDialog(this, "Category '"+name+"' already exists",
                                              "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            CategoryRecord.categories.add(category);
            listModel.addDBElement(category);
        } else {
            if (!category.getName().equals(name)) {
                CategoryRecord.categories.remove(category);
                category.setName(name);
                CategoryRecord.categories.add(category);
            }
            
            category.setType(type);
            category.setHide(categoryHidden.isSelected());
            listModel.updateDBElement();
        }

        Main.dataModified = true;
        return true;
    }
}
