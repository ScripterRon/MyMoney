package MyMoney;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Calculate bond yield to maturity
 */
public final class YieldToMaturityDialog extends JDialog implements ActionListener {
    
    /** Face value */
    private JFormattedTextField faceValueField;
    private double faceValue;
    
    /** Purchase cost */
    private JFormattedTextField costField;
    private double purchaseCost;
    
    /** Coupon yield (percentage) */
    private JFormattedTextField couponYieldField;
    private double couponYield;
    
    /** Purchase date */
    private JFormattedTextField purchaseDateField;
    private Date purchaseDate;
    
    /** Maturity date */
    private JFormattedTextField maturityDateField;
    private Date maturityDate;
    
    /** Yield to maturity (percentage) */
    private double yieldToMaturity;
    
    /**
     * Create the yield to maturity dialog instance
     *
     * @param       parent          Parent frame
     */
    public YieldToMaturityDialog(JFrame parent) {
        super(parent, "Bond Yield to Maturity", true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        //
        // Create the edit pane
        //
        //    Face Value: <numeric-field>
        //    Coupon Yield: <numeric-field>
        //    Purchase Date: <date-field>
        //    Maturity Date: <date-field>
        //    Purchase Cost: <numeric-field>
        //
        JPanel editPane = new JPanel(new GridLayout(0, 2, 5, 5));

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
            JDialog dialog = new YieldToMaturityDialog(parent);
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
                        String yieldResult = String.format("Yield to maturity = %.3f%%", 
                                                        yieldToMaturity);
                        JOptionPane.showMessageDialog(this, yieldResult,
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
        // Get the purchase date
        //
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(purchaseDate);
        int purchaseYear = calendar.get(Calendar.YEAR);
        int purchaseMonth = calendar.get(Calendar.MONTH);
                        
        //
        // Get the maturity date
        //
        calendar.setTime(maturityDate);
        int maturityYear = calendar.get(Calendar.YEAR);
        int maturityMonth = calendar.get(Calendar.MONTH);
        
        //
        // Calculate the number of full years until maturity
        //
        int fullYears = maturityYear - purchaseYear;
        if (purchaseMonth > maturityMonth)
            fullYears--;
        
        //
        // Calculate the estimated yield to maturity
        //
        // YTM = (C + (F - P) / N) / ((F + P ) / 2)
        //
        //  C = Annual coupon payment
        //  F = Face value of bond
        //  P = Purchase cost of bond
        //  N = Number of years to maturity
        //
        double couponPayment = faceValue*(couponYield/100.0);
        yieldToMaturity = (couponPayment + (faceValue-purchaseCost)/(double)Math.max(fullYears, 1)) /
                                ((faceValue+purchaseCost)/2.0) * 100.0;
        
        //
        // Starting with the estimated yield to maturity, calculate the
        // purchase value for the bond and then interate by adjusting the
        // yield for each iteration based on the previous result.  We will
        // stop when the purchase value reaches the purchase cost.
        //
        // PV = C/(1+R) + C/(1+R)**2 + C/(1+R)**3 ... C/(1+R)**N + F/(1+R)**N
        //
        //  C = Annual coupon payment
        //  R = Estimated yield to maturity
        //  F = Face value of bond
        //  N = Number of years to maturity
        //
        // The coupon payment and interest rate will be pro-rated for a partial
        // year
        //
        int direction = 0;
        double previousDifference = 0.0;
        double previousYield = 0.0;
        
        while (true) {
            double purchaseValue = 0.0;
            double rateProduct = 1.0;
            double rate = 1.0 + (yieldToMaturity/100.0);
            
            //
            // Process the full years
            //
            for (int year=1; year<=fullYears; year++) {
                rateProduct = rateProduct * rate;
                purchaseValue += couponPayment / rateProduct;
            }

            //
            // Process a partial year
            //
            if (purchaseMonth != maturityMonth) {
                int months;
                if (purchaseMonth < maturityMonth)
                    months = maturityMonth - purchaseMonth;
                else
                    months = 12 - (purchaseMonth - maturityMonth);
                
                double ratio = (double)months/12.0;
                double partialPayment = couponPayment*ratio;
                double partialRate = yieldToMaturity/100.0*ratio;
                rateProduct = rateProduct * (1.0+partialRate);
                purchaseValue += partialPayment / rateProduct;
            }
            
            //
            // Calculate the final purchase value
            //
            purchaseValue += faceValue / rateProduct;
            
            //
            // Stop when the purchase value reaches the purchase cost.  Otherwise,
            // adjust the estimated yield to maturity and repeat.
            //
            double difference = purchaseValue - purchaseCost;
            if (Math.abs(difference) < 0.01)
                break;
            
            if (direction == 0) {
                if (difference > 0.0) {
                    direction = 1;
                } else {
                    direction = -1;
                }
            } else if (direction == 1 ) {
                if (difference < 0.0) {
                    if (Math.abs(previousDifference) < Math.abs(difference))
                        yieldToMaturity = previousYield;
                    
                    break;
                }
            } else {
                if (difference > 0.0) {
                    if (Math.abs(previousDifference) < Math.abs(difference))
                        yieldToMaturity = previousYield;
                    
                    break;
                }
            }
            
            previousYield = yieldToMaturity;
            previousDifference = difference;
            yieldToMaturity += 0.005 * direction;
        }
        
        //
        // All done
        //
        return true;
    }
}
