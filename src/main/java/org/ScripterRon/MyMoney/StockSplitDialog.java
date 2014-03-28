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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedSet;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 *
 * Stock split dialog
 */
public final class StockSplitDialog extends JDialog implements ActionListener {

    /** Security record */
    private SecurityRecord security;
    
    
    /** Date field */
    private JFormattedTextField dateField;
    
    /** Price field */
    JFormattedTextField priceField;

    /** Ratio field */
    private JTextField ratioField;
    
    /**
     * Display the dialog
     *
     * @param       parent          Parent window
     * @param       security        Security to edit
     */
    public StockSplitDialog(JDialog parent, SecurityRecord security) {
        super(parent, "Stock Split", true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.security = security;

        //
        // Create the edit pane
        //
        //    Date:  <date-field>
        //    Price: <numeric-field>
        //    Ratio: <text-field>
        //
        JPanel editPane = new JPanel(new GridLayout(0, 2, 5, 5));

        editPane.add(new JLabel("Date:", JLabel.RIGHT));
        dateField = new JFormattedTextField(new EditDate());
        dateField.setColumns(8);
        dateField.setInputVerifier(new EditInputVerifier(false));
        dateField.addActionListener(new FormattedTextFieldListener(this));
        dateField.setValue(Main.getCurrentDate());
        editPane.add(dateField);

        editPane.add(new JLabel("Price:", JLabel.RIGHT));
        priceField = new JFormattedTextField(new EditNumber(4, true));
        priceField.setColumns(8);
        priceField.setInputVerifier(new EditInputVerifier(false));
        priceField.addActionListener(new FormattedTextFieldListener(this));
        editPane.add(priceField);
        
        editPane.add(new JLabel("Ratio:", JLabel.RIGHT));
        ratioField = new JTextField("2:1");
        editPane.add(ratioField);

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
     * Show the dialog
     *
     * @param       parent          Parent window
     * @param       security        Security to edit
     */
    public static void showDialog(JDialog parent, SecurityRecord security) {
        try {
            JDialog dialog = new StockSplitDialog(parent, security);
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
        
        //
        // Get the split date
        //
        if (!dateField.isEditValid()) {
            JOptionPane.showMessageDialog(this, "You must specify a valid date",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        Date splitDate = (Date)dateField.getValue();
        
        //
        // Get the split price
        //
        if (!priceField.isEditValid()) {
            JOptionPane.showMessageDialog(this, "You must specify a valid price",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        double splitPrice = ((Number)priceField.getValue()).doubleValue();
        
        //
        // Get the split ratio
        //
        String ratio = ratioField.getText();
        if (ratio.length() == 0) {
            JOptionPane.showMessageDialog(this, "You must enter the split ratio",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        double splitRatio = 0.0;
        boolean validSplit;
        try {
            int sep = ratio.indexOf(':');
            if (sep > 0 && sep < ratio.length()-1) {
                double x = Double.valueOf(ratio.substring(0, sep)).doubleValue();
                double y = Double.valueOf(ratio.substring(sep+1)).doubleValue();
                splitRatio = x/y;
                validSplit = true;
            } else {
                validSplit = false;
            }
        } catch (NumberFormatException exc) {
            validSplit = false;
        }

        if (!validSplit) {
            JOptionPane.showMessageDialog(this, "The split ratio is not valid",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        //
        // Create a new price history entry for the stock split
        //
        PriceHistory ph = new PriceHistory(splitDate, splitPrice, splitRatio);
        SortedSet<PriceHistory> s = security.getPriceHistory();
        s.remove(ph);
        s.add(ph);
        Main.dataModified = true;
        return true;
    }    
}
