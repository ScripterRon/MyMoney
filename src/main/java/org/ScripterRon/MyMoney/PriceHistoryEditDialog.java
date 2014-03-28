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
import java.util.SortedSet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Dialog to create a new price history entry
 */
public class PriceHistoryEditDialog extends JDialog implements ActionListener {

    /** Security record */
    private SecurityRecord security;
    
    /** Price history table model */
    private PriceHistoryTableModel tableModel;

    /** Date field */
    JFormattedTextField dateField;

    /** Price field */
    JFormattedTextField priceField;

    /**
     * Create the dialog
     *
     * @param       parent          Parent window
     * @param       security        Security to edit
     * @param       tableModel      Price history table model
     */
    public PriceHistoryEditDialog(JDialog parent, SecurityRecord security, 
                                    PriceHistoryTableModel tableModel) {
        super(parent, "Price History Entry", true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.security = security;
        this.tableModel = tableModel;

        //
        // Get the date field
        //
        dateField = new JFormattedTextField(new EditDate());
        dateField.setColumns(8);
        dateField.setInputVerifier(new EditInputVerifier(false));
        dateField.addActionListener(new FormattedTextFieldListener(this));
        dateField.setValue(Main.getCurrentDate());

        //
        // Get the price field
        //
        priceField = new JFormattedTextField(new EditNumber(4, true));
        priceField.setColumns(8);
        priceField.setInputVerifier(new EditInputVerifier(false));
        priceField.addActionListener(new FormattedTextFieldListener(this));

        //
        // Create the edit pane
        //
        //    Date:   <date-field>
        //    Price:  <numeric-field>
        //
        JPanel editPane = new JPanel(new GridLayout(0, 2, 5, 5));
        
        editPane.add(new JLabel("Date:", JLabel.RIGHT));
        editPane.add(dateField);
        
        editPane.add(new JLabel("Price:", JLabel.RIGHT));
        editPane.add(priceField);

        //
        // Create the buttons (OK, Cancel)
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
     * Show the price history edit dialog
     *
     * @param       parent          Parent window for the dialog
     * @param       security        Security to edit
     * @param       tableModel      Price history table model
     */
    public static void showDialog(JDialog parent, SecurityRecord security, 
                                    PriceHistoryTableModel tableModel) {
        try {
            JDialog dialog = new PriceHistoryEditDialog(parent, security, tableModel);
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
        if (!dateField.isEditValid()) {
            JOptionPane.showMessageDialog(this, "You must specify a valid date",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!priceField.isEditValid()) {
            JOptionPane.showMessageDialog(this, "You must specify a valid price",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Date date = (Date)dateField.getValue();
        double price = ((Number)priceField.getValue()).doubleValue();

        //
        // Create the new price history entry and remove an existing entry
        // with the same date
        //
        PriceHistory ph = new PriceHistory(date, price);
        SortedSet<PriceHistory> s = security.getPriceHistory();
        s.remove(ph);
        s.add(ph);
        Main.dataModified = true;
        tableModel.priceHistoryChanged();
        return true;
    }    
}
