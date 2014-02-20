package MyMoney;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ListIterator;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Calculate federal and state taxes for a single tax year
 */
public final class TaxDialog implements ActionListener {

    /** Dialog action */
    public int dialogAction;

    /** Dialog index */
    public int dialogIndex;

    /** Parent frame */
    private JFrame parent;

    /** Current dialog */
    private JDialog dialog;

    /** Tax year */
    private int taxYear;

    /** Salary/Wages/Tips income */
    private double salaryIncome;

    /** Salary/Wages/Tips field */
    private JFormattedTextField salaryField;

    /** Pension income */
    private double pensionIncome;

    /** Pension field */
    private JFormattedTextField pensionField;

    /** Dividend income */
    private double dividendIncome;

    /** Dividend field */
    private JFormattedTextField dividendField;

    /** Taxable interest income */
    private double taxableInterestIncome;

    /** Taxable interest field */
    private JFormattedTextField taxableInterestField;

    /** Federal tax-exempt interest income */
    private double federalTaxExemptInterestIncome;

    /** Federal tax-exempt interest field */
    private JFormattedTextField federalTaxExemptInterestField;

    /** State tax-exempt interest income */
    private double stateTaxExemptInterestIncome;

    /** State tax-exempt interest field */
    private JFormattedTextField stateTaxExemptInterestField;

    /** Long-term capital gain income */
    private double longTermCapitalGainIncome;

    /** Long-term capital gain field */
    private JFormattedTextField longTermCapitalGainField;

    /** Short-term capital gain income */
    private double shortTermCapitalGainIncome;

    /** Short-term capital gain field */
    private JFormattedTextField shortTermCapitalGainField;

    /** Long-term capital loss carry-over */
    private double longTermCapitalLossCarryOver;

    /** Long-term capital loss carry-over field */
    private JFormattedTextField longTermCapitalLossCarryOverField;

    /** Short-term capital loss carry-over */
    private double shortTermCapitalLossCarryOver;

    /** Short-term capital loss carry-over field */
    private JFormattedTextField shortTermCapitalLossCarryOverField;

    /** Mortgage interest expense */
    private double mortgageInterestExpense;

    /** Mortgage interest field */
    private JFormattedTextField mortgageInterestField;

    /** Interest expense */
    private double interestExpense;

    /** Interest expense field */
    private JFormattedTextField interestField;

    /** Accrued interest expense */
    private double accruedInterestExpense;

    /** Accrued interest field */
    private JFormattedTextField accruedInterestField;

    /** Federal tax payments */
    private double federalTaxExpense;

    /** Federal tax field */
    private JFormattedTextField federalTaxField;

    /** Estimated federal tax payments */
    private double estimatedFederalTaxExpense;

    /** Estimated federal tax field */
    private JFormattedTextField estimatedFederalTaxField;

    /** State tax payments */
    private double stateTaxExpense;

    /** State tax field */
    private JFormattedTextField stateTaxField;

    /** Estimated state tax */
    private double estimatedStateTaxExpense;

    /** Estimated state tax field */
    private JFormattedTextField estimatedStateTaxField;

    /** State tax refund */
    private double stateTaxRefund;

    /** State tax refund field */
    private JFormattedTextField stateTaxRefundField;

    /** Property tax expense */
    private double propertyTaxExpense;

    /** Property tax field */
    private JFormattedTextField propertyTaxField;

    /** Charitable contributions */
    private double charitableExpense;

    /** Charitable contributions field */
    private JFormattedTextField charitableField;

    /** Medical expense */
    private double medicalExpense;

    /** Medical expense field */
    private JFormattedTextField medicalField;

    /** 2006 Federal tax rates (Single) */
    private final double[][] federal2006Rates = {
        {     0.00,      0.00,  0.10},
        {  7550.00,    755.00,  0.15},
        { 30650.00,   4220.00,  0.25},
        { 74200.00,  15107.50,  0.28},
        {154800.00,  37675.50,  0.33},
        {336550.00,  97653.50,  0.35}};
    
    /** 2007 Federal tax rates (Single) */
    private final double[][] federal2007Rates = {
        {     0.00,       0.00, 0.10},
        {  7825.00,     782.50, 0.15},
        { 31850.00,    4386.25, 0.25},
        { 77100.00,   15698.75, 0.28},
        {160850.00,   39148.75, 0.33},
        {349700.00,  101469.25, 0.35}};
    
    /** 2008 Federal tax rates (Single) */
    private final double[][] federal2008Rates = {
        {     0.00,       0.00, 0.10},
        {  7825.00,     782.50, 0.15},
        { 31850.00,    4386.25, 0.25},
        { 77100.00,   15698.75, 0.28},
        {160850.00,   39148.75, 0.33},
        {349700.00,  101469.25, 0.35}};

    /** 2009 Federal tax rates (Single) */
    private final double[][] federal2009Rates = {
        {     0.00,       0.00, 0.10},
        {  8350.00,     835.00, 0.15},
        { 33950.00,    4675.00, 0.25},
        { 82250.00,   16750.00, 0.28},
        {171550.00,   41754.00, 0.33},
        {372950.00,  108216.00, 0.35}};
    
    /** 2010 Federal tax rates (Single) */
    private final double[][] federal2010Rates = {
        {     0.00,       0.00, 0.10},
        {  8375.00,     837.50, 0.15},
        { 34000.00,    4681.25, 0.25},
        { 82400.00,   16781.25, 0.28},
        {171850.00,   41827.25, 0.33},
        {373650.00,  108421.25, 0.35}};

    /** 2011 Federal tax rates (Single) */
    private final double[][] federal2011Rates = {
        {     0.00,       0.00, 0.10},
        {  8500.00,     850.00, 0.15},
        { 34500.00,    4750.00, 0.25},
        { 83600.00,   17025.00, 0.28},
        {174000.00,   42449.00, 0.33},
        {379150.00,  110016.50, 0.35}};
    
    /** 2012 Federal tax rates (Single) */
    private final double[][] federal2012Rates = {
        {     0.00,       0.00, 0.10},
        { 10850.00,     870.00, 0.15},
        { 37500.00,    4867.50, 0.25},
        { 87800.00,   17442.50, 0.28},
        {180800.00,   43482.50, 0.33},
        {390500.00,  112683.50, 0.35}};
    
    /** 2006 NY state tax rates (Single) */
    private final double[][] state2006Rates = {
        {     0.00,      0.00,  0.04},
        {  8000.00,    320.00,  0.045},
        { 11000.00,    455.00,  0.0525},
        { 13000.00,    560.00,  0.059},
        { 20000.00,    973.00,  0.0685},
        {100000.00,   6453.00,  0.0725},
        {500000.00,  35453.00,  0.077}};

    /** 2007 NY state tax rates (Single) */
    private final double[][] state2007Rates = {
        {     0.00,      0.00,  0.04},
        {  8000.00,    320.00,  0.045},
        { 11000.00,    455.00,  0.0525},
        { 13000.00,    560.00,  0.059},
        { 20000.00,    973.00,  0.0685},
        {100000.00,   6453.00,  0.0764},
        {150000.00,  10275.00,  0.0685}};
    
    /** 2008 NY state tax rates (Single) */
    private final double[][] state2008Rates = {
        {     0.00,      0.00,  0.04},
        {  8000.00,    320.00,  0.045},
        { 11000.00,    455.00,  0.0525},
        { 13000.00,    560.00,  0.059},
        { 20000.00,    973.00,  0.0685},
        {100000.00,   6453.00,  0.0764},
        {150000.00,  10275.00,  0.0685}};

    /** 2009 NY state tax rates (Single) */
    private final double[][] state2009Rates = {
        {     0.00,      0.00,  0.04},
        {  8000.00,    320.00,  0.045},
        { 11000.00,    455.00,  0.0525},
        { 13000.00,    560.00,  0.059},
        { 20000.00,    973.00,  0.0685},
        { 200000.00, 13303.00,  0.0785},
        { 500000.00, 36853.00,  0.0897}};
    
    /** 2010 NY state tax rates (Single) */
    private final double[][] state2010Rates = {
        {     0.00,      0.00,  0.04},
        {  8000.00,    320.00,  0.045},
        { 11000.00,    455.00,  0.0525},
        { 13000.00,    560.00,  0.059},
        { 20000.00,    973.00,  0.0685},
        { 200000.00, 13303.00,  0.0785},
        { 500000.00, 36853.00,  0.0897}};
    
    /** 2011 NY state tax rates (Single) */
    private final double[][] state2011Rates = {
        {     0.00,      0.00,  0.04},
        {  8000.00,    320.00,  0.045},
        { 11000.00,    455.00,  0.0525},
        { 13000.00,    560.00,  0.059},
        { 20000.00,    973.00,  0.0685},
        { 200000.00, 13303.00,  0.0785},
        { 500000.00, 36853.00,  0.0897}};

    /** 2012 NY state tax rates (Single) */
    private final double[][] state2012Rates = {
        {      0.00,     0.00,  0.04},
        {   8000.00,   320.00,  0.045},
        {  11000.00,   455.00,  0.0525},
        {  13000.00,   560.00,  0.059},
        {  20000.00,   973.00,  0.0645},
        {  75000.00,  4521.00,  0.0665},
        { 200000.00, 12933.00,  0.0685},
        {1000000.00, 67633.00,  0.0882}};
    
    /**
     * Create the tax dialog instance
     *
     * @param       parent          Parent frame
     */
    public TaxDialog(JFrame parent) {
        this.parent = parent;
        dialogIndex = 0;
    }

    /**
     * Get the tax year
     */
    public void getTaxYear() {
        boolean haveYear = false;
        while (!haveYear) {
            String year = JOptionPane.showInputDialog(parent, "Enter tax year");
            if (year == null) {
                dialogAction = 0;
                return;
            }

            if (year.length() == 0) {
                JOptionPane.showMessageDialog(parent, "You must enter the tax year",
                                              "Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            try {
                taxYear = Integer.valueOf(year).intValue();
                if (taxYear < 2006)
                    JOptionPane.showMessageDialog(parent, "The tax year must be 2005 or later",
                                                  "Error", JOptionPane.ERROR_MESSAGE);
                else if (taxYear > 2012)
                    JOptionPane.showMessageDialog(parent, "The tax year must be 2008 or earlier",
                                                  "Error", JOptionPane.ERROR_MESSAGE);
                else
                    haveYear = true;
            } catch (NumberFormatException exc) {
                JOptionPane.showMessageDialog(parent, "The tax year is not a valid number",
                                              "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        dialogAction = 1;
    }

    /**
     * Scan the transaction database and get the initial income and expense values
     */
    public void scanDatabase() {
        List<SecurityHolding> holdings = new ArrayList<>(SecurityRecord.securities.size());

        //
        // Get the tax year start and end dates
        //
        GregorianCalendar cal = new GregorianCalendar();
        cal.set(Calendar.YEAR, taxYear);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();
        cal.set(Calendar.MONTH, 11);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        Date endDate = cal.getTime();

        //
        // Scan the transactions
        //
        for (TransactionRecord t : TransactionRecord.transactions) {
            Date date = t.getDate();
            double amount = t.getAmount();

            //
            // Stop if we are finished with the tax year
            //
            if (date.compareTo(endDate) > 0)
                break;

            //
            // Process a security transaction
            //
            SecurityRecord security = t.getSecurity();
            if (security != null && !t.getAccount().isTaxDeferred())
                SecurityHolding.updateSecurityHolding(holdings, t);

            //
            // Skip the transaction if it is not for the current tax year
            //
            if (date.compareTo(startDate) < 0)
                continue;

            //
            // Process the transaction categories
            //
            // ACCRUED_INTEREST                 accruedInterestExpense
            // CHARITY                          charitableExpense
            // DIVIDEND                         dividendIncome
            // ESTIMATED_FEDERAL_TAX            estimatedFederalTaxExpense
            // ESTIMATED_STATE_TAX              estimatedStateTaxExpense
            // FEDERAL_TAX                      federalTaxExpense
            // FEDERAL_TAX_EXEMPT_INTEREST      federalTaxExemptInterestIncome
            // INTEREST_EXPENSE                 interestExpense
            // LONG_TERM_CAPITAL_GAIN           longTermCapitalGainIncome
            // MEDICAL                          medicalExpense
            // MORTGAGE_INTEREST                mortgageInterestExpense
            // PENSION                          pensionIncome
            // PROPERTY_TAX                     propertyTaxExpense
            // SHORT_TERM_CAPITAL_GAIN          shortTermCapitalGainIncome
            // STATE_TAX                        stateTaxRefund / stateTaxExpense
            // STATE_TAX_EXEMPT_INTEREST        stateTaxExemptInterestIncome
            // TAXABLE_INTEREST                 taxableInterestIncome
            // WAGES                            salaryIncome
            //
            CategoryRecord category = t.getCategory();
            List<TransactionSplit> splits = t.getSplits();
            if (category == null && splits == null)
                continue;

            ListIterator<TransactionSplit> lit = null;
            if (splits != null)
                lit = splits.listIterator();

            while (true) {
                if (splits != null) {
                    if (!lit.hasNext())
                        break;

                    TransactionSplit split = lit.next();
                    category = split.getCategory();
                    if (category == null)
                        continue;

                    amount = split.getAmount();
                } else if (t.getAccount().getType() == AccountRecord.INVESTMENT && amount != 0.0) {
                    int action = t.getAction();
                    if (action != TransactionRecord.REINVEST)
                        amount = -amount;
                }

                switch (category.getType()) {
                    case CategoryRecord.WAGES:
                        salaryIncome += amount;
                        break;

                    case CategoryRecord.PENSION:
                        pensionIncome += amount;
                        break;

                    case CategoryRecord.DIVIDEND:
                        dividendIncome += amount;
                        break;

                    case CategoryRecord.TAXABLE_INTEREST:
                        taxableInterestIncome += amount;
                        break;

                    case CategoryRecord.FEDERAL_TAX_EXEMPT_INTEREST:
                        federalTaxExemptInterestIncome += amount;
                        break;

                    case CategoryRecord.STATE_TAX_EXEMPT_INTEREST:
                        stateTaxExemptInterestIncome += amount;
                        break;

                    case CategoryRecord.LONG_TERM_CAPITAL_GAIN:
                        longTermCapitalGainIncome += amount;
                        break;

                    case CategoryRecord.SHORT_TERM_CAPITAL_GAIN:
                        shortTermCapitalGainIncome += amount;
                        break;

                    case CategoryRecord.ACCRUED_INTEREST:
                        accruedInterestExpense -= amount;
                        break;

                    case CategoryRecord.FEDERAL_TAX:
                        if (amount < 0.0)
                            federalTaxExpense -= amount;
                        break;

                    case CategoryRecord.ESTIMATED_FEDERAL_TAX:
                        estimatedFederalTaxExpense -= amount;
                        break;

                    case CategoryRecord.STATE_TAX:
                        if (amount >= 0.0)
                            stateTaxRefund += amount;
                        else
                            stateTaxExpense -= amount;
                        break;

                    case CategoryRecord.ESTIMATED_STATE_TAX:
                        estimatedStateTaxExpense -= amount;
                        break;

                    case CategoryRecord.MORTGAGE_INTEREST:
                        mortgageInterestExpense -= amount;
                        break;

                    case CategoryRecord.PROPERTY_TAX:
                        propertyTaxExpense -= amount;
                        break;

                    case CategoryRecord.CHARITY:
                        charitableExpense -= amount;
                        break;

                    case CategoryRecord.INTEREST_EXPENSE:
                        interestExpense -= amount;
                        break;

                    case CategoryRecord.MEDICAL:
                        medicalExpense -= amount;
                        break;
                }

                if (splits == null)
                    break;
            }
        }
        
        //
        // Process capital gain/loss from investment sales
        //
        Date buyDate, sellDate;
        long shortTermInterval = 365*24*60*60*1000;
        for (SecurityHolding h : holdings) {
            List<CapitalGainRecord> gains = h.getCapitalGains();
            for (CapitalGainRecord c : gains) {
                buyDate = c.getPurchaseDate();
                sellDate = c.getSellDate();
                double gain = c.getSellAmount()-c.getCostBasis();
                if (sellDate.compareTo(startDate) >= 0) {
                    cal.setTime(buyDate);
                    long buyTime = cal.getTimeInMillis();
                    cal.setTime(sellDate);
                    long sellTime = cal.getTimeInMillis();
                    if (sellTime-buyTime < shortTermInterval) {
                        shortTermCapitalGainIncome += gain;
                    } else {
                        longTermCapitalGainIncome += gain;
                    }
                }
            }
        }
    }

    /**
     * Get the income values
     */
    public void getIncome() {

        //
        // Create the dialog
        //
        dialog = new JDialog(parent, "Income", true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        //
        // Display the income fields
        //
        JPanel editPane = new JPanel(new GridLayout(0, 2, 5, 5));

        editPane.add(new JLabel("Salary/Wages/Tips:", JLabel.RIGHT));
        salaryField = new JFormattedTextField(new EditNumber(2, true));
        salaryField.setHorizontalAlignment(JTextField.RIGHT);
        salaryField.setInputVerifier(new EditInputVerifier(false));
        salaryField.addActionListener(new FormattedTextFieldListener(dialog));
        salaryField.setValue(new Double(salaryIncome));
        editPane.add(salaryField);

        editPane.add(new JLabel("Pension:", JLabel.RIGHT));
        pensionField = new JFormattedTextField(new EditNumber(2, true));
        pensionField.setHorizontalAlignment(JTextField.RIGHT);
        pensionField.setInputVerifier(new EditInputVerifier(false));
        pensionField.addActionListener(new FormattedTextFieldListener(dialog));
        pensionField.setValue(new Double(pensionIncome));
        editPane.add(pensionField);

        editPane.add(new JLabel("Dividends:", JLabel.RIGHT));
        dividendField = new JFormattedTextField(new EditNumber(2, true));
        dividendField.setHorizontalAlignment(JTextField.RIGHT);
        dividendField.setInputVerifier(new EditInputVerifier(false));
        dividendField.addActionListener(new FormattedTextFieldListener(dialog));
        dividendField.setValue(new Double(dividendIncome));
        editPane.add(dividendField);

        editPane.add(new JLabel("Taxable Interest:", JLabel.RIGHT));
        taxableInterestField = new JFormattedTextField(new EditNumber(2, true));
        taxableInterestField.setHorizontalAlignment(JTextField.RIGHT);
        taxableInterestField.setInputVerifier(new EditInputVerifier(false));
        taxableInterestField.addActionListener(new FormattedTextFieldListener(dialog));
        taxableInterestField.setValue(new Double(taxableInterestIncome));
        editPane.add(taxableInterestField);

        editPane.add(new JLabel("Federal Tax-Exempt Interest: ", JLabel.RIGHT));
        federalTaxExemptInterestField = new JFormattedTextField(new EditNumber(2, true));
        federalTaxExemptInterestField.setHorizontalAlignment(JTextField.RIGHT);
        federalTaxExemptInterestField.setInputVerifier(new EditInputVerifier(false));
        federalTaxExemptInterestField.addActionListener(new FormattedTextFieldListener(dialog));
        federalTaxExemptInterestField.setValue(new Double(federalTaxExemptInterestIncome));
        editPane.add(federalTaxExemptInterestField);

        editPane.add(new JLabel("State Tax-Exempt Interest:", JLabel.RIGHT));
        stateTaxExemptInterestField = new JFormattedTextField(new EditNumber(2, true));
        stateTaxExemptInterestField.setHorizontalAlignment(JTextField.RIGHT);
        stateTaxExemptInterestField.setInputVerifier(new EditInputVerifier(false));
        stateTaxExemptInterestField.addActionListener(new FormattedTextFieldListener(dialog));
        stateTaxExemptInterestField.setValue(new Double(stateTaxExemptInterestIncome));
        editPane.add(stateTaxExemptInterestField);

        editPane.add(new JLabel("Long-Term Capital Gain:", JLabel.RIGHT));
        longTermCapitalGainField = new JFormattedTextField(new EditNumber(2, true));
        longTermCapitalGainField.setHorizontalAlignment(JTextField.RIGHT);
        longTermCapitalGainField.setInputVerifier(new EditInputVerifier(false));
        longTermCapitalGainField.addActionListener(new FormattedTextFieldListener(dialog));
        longTermCapitalGainField.setValue(new Double(longTermCapitalGainIncome));
        editPane.add(longTermCapitalGainField);

        editPane.add(new JLabel("Short-Term Capital Gain:", JLabel.RIGHT));
        shortTermCapitalGainField = new JFormattedTextField(new EditNumber(2, true));
        shortTermCapitalGainField.setHorizontalAlignment(JTextField.RIGHT);
        shortTermCapitalGainField.setInputVerifier(new EditInputVerifier(false));
        shortTermCapitalGainField.addActionListener(new FormattedTextFieldListener(dialog));
        shortTermCapitalGainField.setValue(new Double(shortTermCapitalGainIncome));
        editPane.add(shortTermCapitalGainField);

        editPane.add(new JLabel("State Tax Refund:", JLabel.RIGHT));
        stateTaxRefundField = new JFormattedTextField(new EditNumber(2, true));
        stateTaxRefundField.setHorizontalAlignment(JTextField.RIGHT);
        stateTaxRefundField.setInputVerifier(new EditInputVerifier(false));
        stateTaxRefundField.addActionListener(new FormattedTextFieldListener(dialog));
        stateTaxRefundField.setValue(new Double(stateTaxRefund));
        editPane.add(stateTaxRefundField);

        //
        // Create the buttons (Next, Cancel)
        //
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));

        JButton button = new JButton("Next");
        button.setActionCommand("next");
        button.addActionListener(this);
        buttonPane.add(button);
        dialog.getRootPane().setDefaultButton(button);
        
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
        dialog.setContentPane(contentPane);

        //
        // Display the dialog
        //
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    /**
     * Get the expense values
     */
    public void getExpense() {

        //
        // Create the dialog
        //
        dialog = new JDialog(parent, "Expense", true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        //
        // Display the expense fields
        //
        JPanel editPane = new JPanel(new GridLayout(0, 2, 5, 5));

        editPane.add(new JLabel("Medical Expense:", JLabel.RIGHT));
        medicalField = new JFormattedTextField(new EditNumber(2, true));
        medicalField.setHorizontalAlignment(JTextField.RIGHT);
        medicalField.setInputVerifier(new EditInputVerifier(false));
        medicalField.addActionListener(new FormattedTextFieldListener(dialog));
        medicalField.setValue(new Double(medicalExpense));
        editPane.add(medicalField);

        editPane.add(new JLabel("Charitable Contributions:", JLabel.RIGHT));
        charitableField = new JFormattedTextField(new EditNumber(2, true));
        charitableField.setHorizontalAlignment(JTextField.RIGHT);
        charitableField.setInputVerifier(new EditInputVerifier(false));
        charitableField.addActionListener(new FormattedTextFieldListener(dialog));
        charitableField.setValue(new Double(charitableExpense));
        editPane.add(charitableField);

        editPane.add(new JLabel("Mortgage Interest:", JLabel.RIGHT));
        mortgageInterestField = new JFormattedTextField(new EditNumber(2, true));
        mortgageInterestField.setHorizontalAlignment(JTextField.RIGHT);
        mortgageInterestField.setInputVerifier(new EditInputVerifier(false));
        mortgageInterestField.addActionListener(new FormattedTextFieldListener(dialog));
        mortgageInterestField.setValue(new Double(mortgageInterestExpense));
        editPane.add(mortgageInterestField);

        editPane.add(new JLabel("Property tax: ", JLabel.RIGHT));
        propertyTaxField = new JFormattedTextField(new EditNumber(2, true));
        propertyTaxField.setHorizontalAlignment(JTextField.RIGHT);
        propertyTaxField.setInputVerifier(new EditInputVerifier(false));
        propertyTaxField.addActionListener(new FormattedTextFieldListener(dialog));
        propertyTaxField.setValue(new Double(propertyTaxExpense));
        editPane.add(propertyTaxField);

        editPane.add(new JLabel("Interest Expense:", JLabel.RIGHT));
        interestField = new JFormattedTextField(new EditNumber(2, true));
        interestField.setHorizontalAlignment(JTextField.RIGHT);
        interestField.setInputVerifier(new EditInputVerifier(false));
        interestField.addActionListener(new FormattedTextFieldListener(dialog));
        interestField.setValue(new Double(interestExpense));
        editPane.add(interestField);

        editPane.add(new JLabel("Accrued Interest:", JLabel.RIGHT));
        accruedInterestField = new JFormattedTextField(new EditNumber(2, true));
        accruedInterestField.setHorizontalAlignment(JTextField.RIGHT);
        accruedInterestField.setInputVerifier(new EditInputVerifier(false));
        accruedInterestField.addActionListener(new FormattedTextFieldListener(dialog));
        accruedInterestField.setValue(new Double(accruedInterestExpense));
        editPane.add(accruedInterestField);

        editPane.add(new JLabel("Long-Term Capital Loss Carry-Over: ", JLabel.RIGHT));
        longTermCapitalLossCarryOverField = new JFormattedTextField(new EditNumber(2, true));
        longTermCapitalLossCarryOverField.setHorizontalAlignment(JTextField.RIGHT);
        longTermCapitalLossCarryOverField.setInputVerifier(new EditInputVerifier(false));
        longTermCapitalLossCarryOverField.addActionListener(new FormattedTextFieldListener(dialog));
        longTermCapitalLossCarryOverField.setValue(new Double(longTermCapitalLossCarryOver));
        editPane.add(longTermCapitalLossCarryOverField);

        editPane.add(new JLabel("Short-Term Capital Loss Carry-Over: ", JLabel.RIGHT));
        shortTermCapitalLossCarryOverField = new JFormattedTextField(new EditNumber(2, true));
        shortTermCapitalLossCarryOverField.setHorizontalAlignment(JTextField.RIGHT);
        shortTermCapitalLossCarryOverField.setInputVerifier(new EditInputVerifier(false));
        shortTermCapitalLossCarryOverField.addActionListener(new FormattedTextFieldListener(dialog));
        shortTermCapitalLossCarryOverField.setValue(new Double(shortTermCapitalLossCarryOver));
        editPane.add(shortTermCapitalLossCarryOverField);

        editPane.add(new JLabel("Federal Tax Payments:", JLabel.RIGHT));
        federalTaxField = new JFormattedTextField(new EditNumber(2, true));
        federalTaxField.setHorizontalAlignment(JTextField.RIGHT);
        federalTaxField.setInputVerifier(new EditInputVerifier(false));
        federalTaxField.addActionListener(new FormattedTextFieldListener(dialog));
        federalTaxField.setValue(new Double(federalTaxExpense));
        editPane.add(federalTaxField);

        editPane.add(new JLabel("Estimated Federal Tax Payments: ", JLabel.RIGHT));
        estimatedFederalTaxField = new JFormattedTextField(new EditNumber(2, true));
        estimatedFederalTaxField.setHorizontalAlignment(JTextField.RIGHT);
        estimatedFederalTaxField.setInputVerifier(new EditInputVerifier(false));
        estimatedFederalTaxField.addActionListener(new FormattedTextFieldListener(dialog));
        estimatedFederalTaxField.setValue(new Double(estimatedFederalTaxExpense));
        editPane.add(estimatedFederalTaxField);

        editPane.add(new JLabel("State Tax Payments:", JLabel.RIGHT));
        stateTaxField = new JFormattedTextField(new EditNumber(2, true));
        stateTaxField.setHorizontalAlignment(JTextField.RIGHT);
        stateTaxField.setInputVerifier(new EditInputVerifier(false));
        stateTaxField.addActionListener(new FormattedTextFieldListener(dialog));
        stateTaxField.setValue(new Double(stateTaxExpense));
        editPane.add(stateTaxField);

        editPane.add(new JLabel("Estimated State Tax Payments:", JLabel.RIGHT));
        estimatedStateTaxField = new JFormattedTextField(new EditNumber(2, true));
        estimatedStateTaxField.setHorizontalAlignment(JTextField.RIGHT);
        estimatedStateTaxField.setInputVerifier(new EditInputVerifier(false));
        estimatedStateTaxField.addActionListener(new FormattedTextFieldListener(dialog));
        estimatedStateTaxField.setValue(new Double(estimatedStateTaxExpense));
        editPane.add(estimatedStateTaxField);

        //
        // Create the buttons (Next, Back, Cancel)
        //
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));

        JButton button = new JButton("Next");
        button.setActionCommand("next");
        button.addActionListener(this);
        buttonPane.add(button);
        dialog.getRootPane().setDefaultButton(button);
        
        buttonPane.add(Box.createHorizontalStrut(10));

        button = new JButton("Back");
        button.setActionCommand("back");
        button.addActionListener(this);
        buttonPane.add(button);
        
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
        dialog.setContentPane(contentPane);

        //
        // Display the dialog
        //
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    /**
     * Compute the federal and state tax
     */
    private void computeTax() {
        double federalAGI = 0.0, federalTaxableIncome = 0.0;
        double federalPersonalExemption = 0.0, federalStandardDeduction = 0.0;
        double federalTax = 0.0, federalTaxDue = 0.0;
        double stateAGI = 0.0, stateTaxableIncome = 0.0, stateStandardDeduction = 0.0;
        double stateTax = 0.0, stateTaxDue = 0.0;
        double[][] federalRates;
        double[][] stateRates;
        int index;

        //
        // Get the tax schedules
        //
        switch (taxYear) {
            case 2006:
                federalRates = federal2006Rates;
                federalStandardDeduction = 5150.0;
                federalPersonalExemption = 3200.0;
                stateRates = state2006Rates;
                stateStandardDeduction = 7500.0;
                break;
                
            case 2007:
                federalRates = federal2007Rates;
                federalStandardDeduction = 5350.0;
                federalPersonalExemption = 3200.0;
                stateRates = state2007Rates;
                stateStandardDeduction = 7500.0;
                break;
                
            case 2008:
                federalRates = federal2008Rates;
                federalStandardDeduction = 5450.0;
                federalPersonalExemption = 3200.0;
                stateRates = state2008Rates;
                stateStandardDeduction = 7500.0;
                break;
                
            case 2009:
                federalRates = federal2009Rates;
                federalStandardDeduction = 5700.0;
                federalPersonalExemption = 3650.0;
                stateRates = state2009Rates;
                stateStandardDeduction = 7500.0;
                break;
                
            case 2010:
                federalRates = federal2010Rates;
                federalStandardDeduction = 5700.0;
                federalPersonalExemption = 3650.0;
                stateRates = state2010Rates;
                stateStandardDeduction = 7500.0;
                break;
                
            case 2011:
                federalRates = federal2011Rates;
                federalStandardDeduction = 5800.0;
                federalPersonalExemption = 3700.0;
                stateRates = state2011Rates;
                stateStandardDeduction = 7500.0;
                break;
                                
            case 2012:
                federalRates = federal2012Rates;
                federalStandardDeduction = 5950.0;
                federalPersonalExemption = 3800.0;
                stateRates = state2012Rates;
                stateStandardDeduction = 7500.0;
                break;

            default:
                throw new IllegalArgumentException("Tax year "+taxYear+" is not supported");
        }

        //
        // Process wages, salary, tips, pension
        //
        federalAGI = salaryIncome+pensionIncome;

        //
        // Add any state tax refund since we deducted it previously (the user
        // is responsible for adjusting the entered value to account for the
        // Alternative Minimum Tax calculations for the prior year tax return)
        //
        // The user should enter 0 for the state tax refund if the standard deduction was
        // used in the previous year
        //
        federalAGI += stateTaxRefund;

        //
        // Process interest and dividend income (Schedule B)
        //
        // Accrued interest is deducted from the interest paid to the user.
        // The user is responsible for adjusting this value if a bond is bought
        // at the end of the year and the interest isn't paid until the next year.
        //
        double interestIncome = taxableInterestIncome+stateTaxExemptInterestIncome;
        if (accruedInterestExpense > interestIncome)
            interestIncome = 0.0;
        else
            interestIncome -= accruedInterestExpense;
        federalAGI += dividendIncome+interestIncome;

        //
        // Process security transactions (Schedule D)
        //
        // A capital loss is limited to $3,000 (the remainder of the loss
        // is then carried over to the next tax year)
        //
        double shortTermGain = shortTermCapitalGainIncome-shortTermCapitalLossCarryOver;
        double longTermGain = longTermCapitalGainIncome-longTermCapitalLossCarryOver;
        double capitalGain = shortTermGain+longTermGain;
        if (capitalGain < -3000.00)
            capitalGain = -3000.00;

        federalAGI += capitalGain;

        //
        // Process deductions (Schedule A)
        //
        // Medical deductions are allowed only in excess of 7.5% of the Federal AGI
        //
        // Use the standard deduction if it is greater than the itemized deductions
        //
        double federalDeductions = interestExpense;
        if (medicalExpense > 0.0) {
            double medicalBase = federalAGI*0.075;
            if (medicalExpense > medicalBase)
                federalDeductions += medicalExpense-medicalBase;
        }

        federalDeductions += stateTaxExpense+estimatedStateTaxExpense;
        federalDeductions += propertyTaxExpense+mortgageInterestExpense;
        federalDeductions += charitableExpense;

        if (federalDeductions < federalStandardDeduction)
            federalDeductions = federalStandardDeduction;

        if (federalDeductions < federalAGI)
            federalTaxableIncome = federalAGI - federalDeductions;
        else
            federalTaxableIncome = 0.0;

        //
        // Process the personal exemption
        //
        if (federalTaxableIncome < federalPersonalExemption) {
            federalTaxableIncome = 0.0;
        } else {
            federalTaxableIncome -= federalPersonalExemption;
        }

        //
        // Compute the Federal tax (Form 1040)
        //
        // The calculations are for a single taxpayer.  In addition, we are
        // assuming that the taxable income excluding dividends and capital gains
        // qualifies for at least the 28% tax bracket (which means that all of the
        // capital gains and dividends are taxed at the 15% rate)
        //
        for (index=0; index<federalRates.length-1; index++)
            if (federalTaxableIncome < federalRates[index+1][0])
                break;

        federalTax = federalRates[index][1]+
                        (federalTaxableIncome-federalRates[index][0])*federalRates[index][2];
        federalTax = Math.floor(federalTax*100.0)/100.0;

        double gainsDividends = dividendIncome;
        if (capitalGain > 0.0 && longTermGain > 0.0) {
            gainsDividends += longTermGain;
            if (shortTermGain < 0.0)
                gainsDividends -= shortTermGain;
        }

        if (gainsDividends > 0.0) {
            double adjustedAGI = federalTaxableIncome-gainsDividends;

            for (index=0; index<federalRates.length-1; index++)
                if (adjustedAGI < federalRates[index+1][0])
                    break;

            double adjustedTax = federalRates[index][1]+
                                    (adjustedAGI-federalRates[index][0])*federalRates[index][2];
            adjustedTax += gainsDividends*0.15;
            adjustedTax = Math.floor(adjustedTax*100.0)/100.0;
            federalTax = Math.min(federalTax, adjustedTax);
        }

        federalTaxDue = federalTax-federalTaxExpense-estimatedFederalTaxExpense;

        //
        // Compute the NY state AGI (Form IT-201)
        //
        stateAGI = federalAGI-stateTaxRefund;
        stateAGI += federalTaxExemptInterestIncome-stateTaxExemptInterestIncome;
        
        //
        // NY does not tax the first $20,000 of pension income
        //
        if (pensionIncome > 0.0) {
            double pensionExclusion = Math.min(pensionIncome, 20000.0);
            if (stateAGI > pensionExclusion)
                stateAGI -= pensionExclusion;
            else
                stateAGI = 0.0;
        }

        //
        // Compute the NY state deductions
        //
        // The NY state standard deduction is used if the Federal standard deduction
        // was taken.  Otherwise, the NY state deductions are calculated.
        //
        double stateDeductions;
        if (federalDeductions > federalStandardDeduction) {
            stateDeductions = federalDeductions-stateTaxExpense-estimatedStateTaxExpense;
            if (stateDeductions < stateStandardDeduction)
                stateDeductions = stateStandardDeduction;
        } else {
            stateDeductions = stateStandardDeduction;
        }

        stateTaxableIncome = stateAGI - stateDeductions;

        //
        // Compute the state tax (Form IT-201)
        //
        for (index=0; index<stateRates.length-1; index++)
            if (stateTaxableIncome < stateRates[index+1][0])
                break;

        stateTax = stateRates[index][1]+
                        (stateTaxableIncome-stateRates[index][0])*stateRates[index][2];
        stateTax = Math.floor(stateTax*100.0)/100.0;
        stateTaxDue = stateTax-stateTaxExpense-estimatedStateTaxExpense;

        //
        // Create the dialog
        //
        dialog = new JDialog(parent, "Tax Summary", true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        //
        // Display the tax summary
        //
        JPanel summaryPane = new JPanel(new GridLayout(0, 2, 5, 5));
        summaryPane.setOpaque(false);

        summaryPane.add(new JLabel("Federal Adjusted Gross Income:", JLabel.RIGHT));
        summaryPane.add(new JLabel(String.format("%.2f", federalAGI), JLabel.TRAILING));

        summaryPane.add(new JLabel("Federal Deductions:", JLabel.RIGHT));
        summaryPane.add(new JLabel(String.format("%.2f", federalDeductions), JLabel.TRAILING));

        summaryPane.add(new JLabel("Federal Taxable Income:", JLabel.RIGHT));
        summaryPane.add(new JLabel(String.format("%.2f", federalTaxableIncome), JLabel.TRAILING));

        summaryPane.add(new JLabel("Total Federal Tax:", JLabel.RIGHT));
        summaryPane.add(new JLabel(String.format("%.2f", federalTax), JLabel.TRAILING));

        summaryPane.add(new JLabel("Federal Tax Due:", JLabel.RIGHT));
        summaryPane.add(new JLabel(String.format("%.2f", federalTaxDue), JLabel.TRAILING));

        summaryPane.add(Box.createVerticalStrut(15));
        summaryPane.add(Box.createVerticalStrut(15));

        summaryPane.add(new JLabel("State Adjusted Gross Income:", JLabel.RIGHT));
        summaryPane.add(new JLabel(String.format("%.2f", stateAGI), JLabel.TRAILING));

        summaryPane.add(new JLabel("State Deductions:", JLabel.RIGHT));
        summaryPane.add(new JLabel(String.format("%.2f", stateDeductions), JLabel.TRAILING));

        summaryPane.add(new JLabel("State Taxable Income:", JLabel.RIGHT));
        summaryPane.add(new JLabel(String.format("%.2f", stateTaxableIncome), JLabel.TRAILING));

        summaryPane.add(new JLabel("Total State Tax:", JLabel.RIGHT));
        summaryPane.add(new JLabel(String.format("%.2f", stateTax), JLabel.TRAILING));

        summaryPane.add(new JLabel("State Tax Due:", JLabel.RIGHT));
        summaryPane.add(new JLabel(String.format("%.2f", stateTaxDue), JLabel.TRAILING));

        //
        // Create the buttons (Done, Back)
        //
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));

        JButton button = new JButton("Done");
        button.setActionCommand("cancel");
        button.addActionListener(this);
        buttonPane.add(button);
        dialog.getRootPane().setDefaultButton(button);
        
        buttonPane.add(Box.createHorizontalStrut(10));

        button = new JButton("Back");
        button.setActionCommand("back");
        button.addActionListener(this);
        buttonPane.add(button);

        //
        // Set up the content pane
        //
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.setOpaque(true);
        contentPane.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        contentPane.add(summaryPane);
        contentPane.add(Box.createVerticalStrut(15));
        contentPane.add(buttonPane);
        dialog.setContentPane(contentPane);

        //
        // Display the dialog
        //
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
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
        // "next" - Advance to the next dialog
        // "back" - Backup to the previous dialog
        // "cancel" - Cancel the request
        //
        try {
            switch (ae.getActionCommand()) {
                case "next":
                    processFields();
                    dialogAction = 1;
                    dialog.setVisible(false);
                    dialog.dispose();
                    break;
                    
                case "back":
                    processFields();
                    dialogAction = -1;
                    dialog.setVisible(false);
                    dialog.dispose();
                    break;
                            
                case "cancel":
                    dialogAction = 0;
                    dialog.setVisible(false);
                    dialog.dispose();
                    break;
            }
        } catch (Exception exc) {
            Main.logException("Exception while processing action event", exc);
        }
    }

    /**
     * Process the dialog fields
     */
    private void processFields() {
        if (dialogIndex == 0) {
            
            //
            // Process the fields from the first dialog
            //
            salaryIncome = ((Number)salaryField.getValue()).doubleValue();
            pensionIncome = ((Number)pensionField.getValue()).doubleValue();
            dividendIncome = ((Number)dividendField.getValue()).doubleValue();
            taxableInterestIncome = ((Number)taxableInterestField.getValue()).doubleValue();
            federalTaxExemptInterestIncome = ((Number)federalTaxExemptInterestField.getValue()).doubleValue();
            stateTaxExemptInterestIncome = ((Number)stateTaxExemptInterestField.getValue()).doubleValue();
            longTermCapitalGainIncome = ((Number)longTermCapitalGainField.getValue()).doubleValue();
            shortTermCapitalGainIncome = ((Number)shortTermCapitalGainField.getValue()).doubleValue();
            stateTaxRefund = ((Number)stateTaxRefundField.getValue()).doubleValue();
        } else if (dialogIndex == 1) {
            //
            // Process the fields from the second dialog
            //
            medicalExpense = ((Number)medicalField.getValue()).doubleValue();
            charitableExpense = ((Number)charitableField.getValue()).doubleValue();
            mortgageInterestExpense = ((Number)mortgageInterestField.getValue()).doubleValue();
            propertyTaxExpense = ((Number)propertyTaxField.getValue()).doubleValue();
            interestExpense = ((Number)interestField.getValue()).doubleValue();
            accruedInterestExpense = ((Number)accruedInterestField.getValue()).doubleValue();
            longTermCapitalLossCarryOver = ((Number)longTermCapitalLossCarryOverField.getValue()).doubleValue();
            shortTermCapitalLossCarryOver = ((Number)shortTermCapitalLossCarryOverField.getValue()).doubleValue();
            federalTaxExpense = ((Number)federalTaxField.getValue()).doubleValue();
            estimatedFederalTaxExpense = ((Number)estimatedFederalTaxField.getValue()).doubleValue();
            stateTaxExpense = ((Number)stateTaxField.getValue()).doubleValue();
            estimatedStateTaxExpense = ((Number)estimatedStateTaxField.getValue()).doubleValue();

            //
            // We want the capital loss values to be positive for our calculations
            //
            if (longTermCapitalLossCarryOver < 0.0)
                longTermCapitalLossCarryOver = -longTermCapitalLossCarryOver;
            if (shortTermCapitalLossCarryOver < 0.0)
                shortTermCapitalLossCarryOver = -shortTermCapitalLossCarryOver;
        }
    }

    /**
     * Show the tax dialogs
     *
     * @param       parent          Parent frame
     */
    public static void showDialog(JFrame parent) {
        TaxDialog taxDialog = new TaxDialog(parent);

        //
        // Get the tax year
        //
        taxDialog.getTaxYear();
        if (taxDialog.dialogAction == 0)
            return;

        //
        // Scan our database for the initial field values
        //
        taxDialog.scanDatabase();

        //
        // Display the tax dialogs
        //
        while (true) {
            switch (taxDialog.dialogIndex) {
                case 0:                             // Get income values
                    taxDialog.getIncome();
                    break;

                case 1:                             // Get expense values
                    taxDialog.getExpense();
                    break;

                case 2:                             // Display tax summary
                    taxDialog.computeTax();
                    break;
            }

            //
            // Stop if we are done or the user has canceled the request
            //
            if (taxDialog.dialogAction == 0)
                break;

            //
            // Advance/Backup to the next dialog
            //
            taxDialog.dialogIndex += taxDialog.dialogAction;
        }
    }
}
