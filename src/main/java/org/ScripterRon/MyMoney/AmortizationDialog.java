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
 * Calculate bond accretion/amortization
 */
public final class AmortizationDialog extends JDialog implements ActionListener {

    /** Bond name */
    private JTextField bondNameField;
    private String bondName;
    
    /** Face value */
    private JFormattedTextField faceValueField;
    private double faceValue;
    
    /** Coupon yield */
    private JFormattedTextField couponYieldField;
    private double couponYield;
    
    /** Purchase date */
    private JFormattedTextField purchaseDateField;
    private Date purchaseDate;
    
    /** Maturity date */
    private JFormattedTextField maturityDateField;
    private Date maturityDate;
    
    /** Cost */
    private JFormattedTextField costField;
    private double purchaseCost;
    
    /** Accrued interest */
    private JFormattedTextField accruedInterestField;
    private double accruedInterest;
    
    /** Yield to maturity */
    private JFormattedTextField yieldToMaturityField;
    private double yieldToMaturity;
    
    /** Payment interval (number of months) */
    private JFormattedTextField paymentIntervalField;
    private int paymentInterval;
    
    /**
     * Create the amortization dialog instance
     *
     * @param       parent          Parent frame
     */
    public AmortizationDialog(JFrame parent) {
        super(parent, "Bond Amortization", true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        //
        // Create the edit pane
        //
        //    Name: <text-field>
        //    Face Value: <numeric-field>
        //    Coupon Yield: <numeric-field>
        //    Purchase Date: <date-field>
        //    Maturity Date: <date-field>
        //    Purchase Cost: <numeric-field>
        //    Accrued Interest: <numeric-field>
        //    Yield to Maturity: <numeric-field>
        //    Payment Interval: <numeric-field>
        //
        JPanel editPane = new JPanel(new GridLayout(0, 2, 5, 5));

        editPane.add(new JLabel("Bond Name:", JLabel.RIGHT));
        bondNameField = new JTextField();
        editPane.add(bondNameField);
        
        editPane.add(new JLabel("Face Value:", JLabel.RIGHT));
        faceValueField = new JFormattedTextField(new EditNumber(2, true));
        faceValueField.setColumns(12);
        faceValueField.setInputVerifier(new EditInputVerifier(false));
        faceValueField.addActionListener(new FormattedTextFieldListener(this));
        editPane.add(faceValueField);
        
        editPane.add(new JLabel("Coupon Yield (%):", JLabel.RIGHT));
        couponYieldField = new JFormattedTextField(new EditNumber(4, false));
        couponYieldField.setColumns(12);
        couponYieldField.setInputVerifier(new EditInputVerifier(false));
        couponYieldField.addActionListener(new FormattedTextFieldListener(this));
        editPane.add(couponYieldField);
        
        editPane.add(new JLabel("Purchase Date:", JLabel.RIGHT));
        purchaseDateField = new JFormattedTextField(new EditDate());
        purchaseDateField.setColumns(12);
        purchaseDateField.setInputVerifier(new EditInputVerifier(false));
        purchaseDateField.addActionListener(new FormattedTextFieldListener(this));
        purchaseDateField.setValue(Main.getCurrentDate());
        editPane.add(purchaseDateField);
        
        editPane.add(new JLabel("Maturity Date:", JLabel.RIGHT));
        maturityDateField = new JFormattedTextField(new EditDate());
        maturityDateField.setColumns(12);
        maturityDateField.setInputVerifier(new EditInputVerifier(false));
        maturityDateField.addActionListener(new FormattedTextFieldListener(this));
        maturityDateField.setValue(Main.getCurrentDate());
        editPane.add(maturityDateField);

        editPane.add(new JLabel("Purchase Cost:", JLabel.RIGHT));
        costField = new JFormattedTextField(new EditNumber(2, true));
        costField.setColumns(12);
        costField.setInputVerifier(new EditInputVerifier(false));
        costField.addActionListener(new FormattedTextFieldListener(this));
        editPane.add(costField);
        
        editPane.add(new JLabel("Accrued Interest:", JLabel.RIGHT));
        accruedInterestField = new JFormattedTextField(new EditNumber(2, true));
        accruedInterestField.setColumns(12);
        accruedInterestField.setInputVerifier(new EditInputVerifier(false));
        accruedInterestField.addActionListener(new FormattedTextFieldListener(this));
        editPane.add(accruedInterestField);
        
        editPane.add(new JLabel("Yield to Maturity (%):", JLabel.RIGHT));
        yieldToMaturityField = new JFormattedTextField(new EditNumber(4, true));
        yieldToMaturityField.setColumns(12);
        yieldToMaturityField.setInputVerifier(new EditInputVerifier(false));
        yieldToMaturityField.addActionListener(new FormattedTextFieldListener(this));
        editPane.add(yieldToMaturityField);
        
        editPane.add(new JLabel("Payment Interval (months):", JLabel.RIGHT));
        paymentIntervalField = new JFormattedTextField(new EditNumber(0, false));
        paymentIntervalField.setColumns(12);
        paymentIntervalField.setInputVerifier(new EditInputVerifier(false));
        paymentIntervalField.addActionListener(new FormattedTextFieldListener(this));
        paymentIntervalField.setValue(new Integer(6));
        editPane.add(paymentIntervalField);

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
     */
    public static void showDialog(JFrame parent) {
        try {
            JDialog dialog = new AmortizationDialog(parent);
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
                case "ok":
                    if (processFields()) {
                        AmortizationTableDialog.showDialog(this, 
                                bondName, faceValue, couponYield, purchaseDate, 
                                maturityDate, purchaseCost, accruedInterest, 
                                yieldToMaturity, paymentInterval);
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
        // Get the bond name
        //
        bondName = bondNameField.getText();
        if (bondName.length() == 0) {
            JOptionPane.showMessageDialog(this, "You must enter the bond name",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        //
        // Get the face value
        //
        if (!faceValueField.isEditValid()) {
            JOptionPane.showMessageDialog(this, "You must specify a valid face value",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        faceValue = ((Double)faceValueField.getValue()).doubleValue();
        
        //
        // Get the coupon yield
        //
        if (!couponYieldField.isEditValid()) {
            JOptionPane.showMessageDialog(this, "You must specify a valid coupon yield",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        couponYield = ((Double)couponYieldField.getValue()).doubleValue();

        //
        // Get the purchase date
        //
        if (!purchaseDateField.isEditValid()) {
            JOptionPane.showMessageDialog(this, "You must specify a valid purchase date",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        purchaseDate = (Date)purchaseDateField.getValue();

        //
        // Get the maturity date
        //
        if (!maturityDateField.isEditValid()) {
            JOptionPane.showMessageDialog(this, "You must specify a valid maturity date",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        maturityDate = (Date)maturityDateField.getValue();
        
        //
        // The maturity date must be later than the purchase date
        //
        if (maturityDate.compareTo(purchaseDate) <= 0) {
            JOptionPane.showMessageDialog(this, "The maturity date must be later than the purchase date",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        //
        // Get the purchase cost
        //
        if (!costField.isEditValid()) {
            JOptionPane.showMessageDialog(this, "You must specify a valid purchase cost",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        purchaseCost = ((Double)costField.getValue()).doubleValue();
        
        //
        // Get the accrued interest
        //
        if (!accruedInterestField.isEditValid()) {
            JOptionPane.showMessageDialog(this, "You must specify a valid accrued interest",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        accruedInterest = ((Double)accruedInterestField.getValue()).doubleValue();
        
        //
        // Get the yield to maturity
        //
        if (!yieldToMaturityField.isEditValid()) {
            JOptionPane.showMessageDialog(this, "You must specify a valid yield to maturity",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        yieldToMaturity = ((Double)yieldToMaturityField.getValue()).doubleValue();
        
        //
        // Get the payment interval
        //
        if (!paymentIntervalField.isEditValid()) {
            JOptionPane.showMessageDialog(this, "You must specify a valid payment interval",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        paymentInterval = ((Integer)paymentIntervalField.getValue()).intValue();
        
        //
        // All done
        //
        return true;
    }
}
