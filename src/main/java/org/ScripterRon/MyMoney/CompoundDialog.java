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

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Calculate compound interest
 */
public final class CompoundDialog extends JDialog implements ActionListener {
    
    /** Principal */
    private JFormattedTextField principalField;
    private double principal;
    
    /** Annual withdrawal */
    private JFormattedTextField withdrawalField;
    private double withdrawal;
    
    /** Annual interest rate (%) */
    private JFormattedTextField annualRateField;
    private double annualRate;
    
    /** Duration (years) */
    private JFormattedTextField durationField;
    private int duration;
    
    /** Compounded principal */
    private double compoundedPrincipal;
    
    /**
     * Create the compound interest dialog instance
     *
     * @param       parent          Parent frame
     */
    public CompoundDialog(JFrame parent) {
        super(parent, "Compound Interest", true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        //
        // Create the edit pane
        //
        //    Starting Principal: <numeric-field>
        //    Annual Withdrawal: <numeric-field>
        //    Annual Rate: <numeric-field>
        //    Duration: <numeric-field>
        JPanel editPane = new JPanel(new GridLayout(0, 2, 5, 5));

        editPane.add(new JLabel("Starting Principal:", JLabel.RIGHT));
        principalField = new JFormattedTextField(new EditNumber(2, true));
        principalField.setColumns(12);
        principalField.setInputVerifier(new EditInputVerifier(false));
        principalField.addActionListener(new FormattedTextFieldListener(this));
        editPane.add(principalField);
                
        editPane.add(new JLabel("Annual Withdrawal:", JLabel.RIGHT));
        withdrawalField = new JFormattedTextField(new EditNumber(2, true));
        withdrawalField.setColumns(12);
        withdrawalField.setInputVerifier(new EditInputVerifier(false));
        withdrawalField.addActionListener(new FormattedTextFieldListener(this));
        withdrawalField.setValue(new Double(0));
        editPane.add(withdrawalField);
        
        editPane.add(new JLabel("Annual Rate (%):", JLabel.RIGHT));
        annualRateField = new JFormattedTextField(new EditNumber(4, false));
        annualRateField.setColumns(12);
        annualRateField.setInputVerifier(new EditInputVerifier(false));
        annualRateField.addActionListener(new FormattedTextFieldListener(this));
        editPane.add(annualRateField);
        
        editPane.add(new JLabel("Duration (years):", JLabel.RIGHT));
        durationField = new JFormattedTextField(new EditNumber(0, false));
        durationField.setColumns(12);
        durationField.setInputVerifier(new EditInputVerifier(false));
        durationField.addActionListener(new FormattedTextFieldListener(this));
        editPane.add(durationField);

        //
        // Create the buttons (Calculate, Cancel)
        //
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));

        JButton button = new JButton("Calculate");
        button.setActionCommand("calculate");
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
     */
    public static void showDialog(JFrame parent) {
        try {
            JDialog dialog = new CompoundDialog(parent);
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
        try {
            switch (ae.getActionCommand()) {
                case "calculate":
                    if (processFields()) {
                        String compoundedResult = String.format("Compounded principal = $%,.2f", 
                                                        compoundedPrincipal);
                        JOptionPane.showMessageDialog(this, compoundedResult,
                                          "Result", JOptionPane.INFORMATION_MESSAGE);
                    }
                    break;
                    
                case "cancel":
                    setVisible(false);
                    dispose();
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

        //
        // Get the starting principal
        //
        if (!principalField.isEditValid()) {
            JOptionPane.showMessageDialog(this, "You must specify a valid starting principal",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        principal = ((Double)principalField.getValue()).doubleValue();
    
        //
        // Get the annual withdrawal
        //
        if (!withdrawalField.isEditValid()) {
            JOptionPane.showMessageDialog(this, "You must specify a valid annual withdrawal",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        withdrawal = ((Double)withdrawalField.getValue()).doubleValue();
        
        //
        // Get the annual rate
        //
        if (!annualRateField.isEditValid()) {
            JOptionPane.showMessageDialog(this, "You must specify a valid annual rate",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        annualRate = ((Double)annualRateField.getValue()).doubleValue();
        
        //
        // Get the duration
        //
        if (!durationField.isEditValid()) {
            JOptionPane.showMessageDialog(this, "You must specify a valid duration",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        duration = ((Integer)durationField.getValue()).intValue();
        if (duration <= 0) {
            JOptionPane.showMessageDialog(this, "The duration must be greater than zero",
                                            "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        //
        // Calculate the compounded result
        //
        compoundedPrincipal = principal;
        for (int year=0; year<duration; year++) {
            if (compoundedPrincipal > withdrawal) {
                compoundedPrincipal = (compoundedPrincipal-withdrawal)*(1.0+annualRate/100.0);
            } else {
                compoundedPrincipal = 0.0;
                break;
            }
        }
        
        return true;
    }
}
