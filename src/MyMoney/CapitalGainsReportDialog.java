package MyMoney;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;

import Report.*;

/**
 * Capital gains report dialog
 */
public final class CapitalGainsReportDialog extends JDialog implements ActionListener {

    /** Report column names */
    private static final String[] columnNames = {
        "Security", "Bought", "Sold", "Shares", "Cost", "Amount", "Gain/Loss"};

    /** Report element sizes */
    private static final int[] elementSizes = {175,  80,  80,  80,  80,  80,  80};

    /** Report element positions */
    private static final int[] elementPositions = {2, 186, 275, 364, 453, 542, 631};

    /** Report element alignments */
    private static final int[] elementAlignments = {
        ReportElement.LEFT_ALIGNMENT,  ReportElement.LEFT_ALIGNMENT,
        ReportElement.LEFT_ALIGNMENT,  ReportElement.RIGHT_ALIGNMENT,
        ReportElement.RIGHT_ALIGNMENT, ReportElement.RIGHT_ALIGNMENT,
        ReportElement.RIGHT_ALIGNMENT};

    /** Start date field */
    private JFormattedTextField startField;

    /** End date field */
    private JFormattedTextField endField;

    /**
     * Construct the dialog
     *
     * @param       parent          Parent frame
     */
    public CapitalGainsReportDialog(JFrame parent) {
        super(parent, "Capital Gains Report", true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        //
        // Set the start date to the beginning of the year
        //
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(Main.getCurrentDate());
        cal.set(Calendar.DAY_OF_YEAR, 1);
        startField = new JFormattedTextField(new EditDate());
        startField.setColumns(8);
        startField.setInputVerifier(new EditInputVerifier(false));
        startField.addActionListener(new FormattedTextFieldListener(this));
        startField.setValue(cal.getTime());

        //
        // Set the end date to the current date
        //
        endField = new JFormattedTextField(new EditDate());
        endField.setColumns(8);
        endField.setInputVerifier(new EditInputVerifier(false));
        endField.addActionListener(new FormattedTextFieldListener(this));
        endField.setValue(Main.getCurrentDate());

        //
        // Create the edit pane
        //
        //    Start Date:        <text-field>
        //    End Date:          <text-field>
        //
        JPanel editPane = new JPanel(new GridLayout(0, 2, 5, 5));

        editPane.add(new JLabel("Start Date:", JLabel.RIGHT));
        editPane.add(startField);

        editPane.add(new JLabel("End Date:", JLabel.RIGHT));
        editPane.add(endField);

        //
        // Create the buttons (Create Report, Done)
        //
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));

        JButton button = new JButton("Create Report");
        button.setActionCommand("create report");
        button.addActionListener(this);
        buttonPane.add(button);
        getRootPane().setDefaultButton(button);
        
        buttonPane.add(Box.createHorizontalStrut(10));

        button = new JButton("Done");
        button.setActionCommand("done");
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
     * CapitalGainsReportDialog action performed (ActionListener interface)
     *
     * @param       ae              Action event
     */
    public void actionPerformed(ActionEvent ae) {

        //
        // Process the action command
        //
        // "create report" - Create the report
        // "done" - Done
        //
        try {
            switch (ae.getActionCommand()) {
                case "create report":
                    Date startDate, endDate;
                    if (!startField.isEditValid() || !endField.isEditValid()) {
                        JOptionPane.showMessageDialog(this, "You must specify start and end dates",
                                                      "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        startDate = (Date)startField.getValue();
                        endDate = (Date)endField.getValue();
                        if (endDate.compareTo(startDate) < 0) {
                            JOptionPane.showMessageDialog(this, "The end date is before the start date",
                                                          "Error", JOptionPane.ERROR_MESSAGE);
                        } else {
                            generateReport(startDate, endDate);
                        }
                    }
                    break;
                    
                case "done":
                    setVisible(false);
                    dispose();
                    break;
            }
        } catch (ReportException exc) {
            Main.logException("Exception while generating report", exc);
        } catch (Exception exc) {
            Main.logException("Exception while processing action event", exc);
        }
    }

    /**
     * Generate the report
     *
     * @param       startDate       The start date for the report
     * @param       endDate         The end date for the report
     * @exception   ReportException Error while generating the report
     */
    private void generateReport(Date startDate, Date endDate) throws ReportException {

        //
        // Create the report data model
        //
        ReportModel reportModel = new TransactionModel(startDate, endDate);

        //
        // Create the report
        //
        Report report = new Report("Capital Gains Report", reportModel);
        ReportState reportState = report.getState();
        ReportGroup defaultGroup = reportState.getGroup(reportState.getGroupCount()-1);

        //
        // Highlight the column headers
        //
        defaultGroup.getHeader().setBackgroundColor(new Color(235, 235, 235));
        defaultGroup.getHeader().setBorderColor(Color.BLACK);

        //
        // Set the page format using 8.5x11 paper in landscape mode with 1/2" side
        // margins and 1/4" top margins.
        //
        // All measurements are in points (a point is 1/72 of an inch).  Report element
        // coordinates are relative to the imageable area.  The printWidth and printHeight
        // variables refer to the rotated page, thus printWidth is along the paper y-axis
        // and printHeight is along the paper x-axis.
        //
        int paperWidth = 612;
        int paperHeight = 792;
        int topMargin = 36;
        int leftMargin = 18;
        int printWidth = paperHeight - 2*topMargin;
        int printHeight = paperWidth - 2*leftMargin;

        Paper paper = new Paper();
        paper.setSize(paperWidth, paperHeight);
        paper.setImageableArea(leftMargin, topMargin, printHeight, printWidth);

        PageFormat pageFormat = new PageFormat();
        pageFormat.setPaper(paper);
        pageFormat.setOrientation(PageFormat.LANDSCAPE);
        reportState.setPageFormat(pageFormat);

        //
        // Create the report fonts
        //
        Font plainFont = new Font("SansSerif", Font.PLAIN, 10);
        Font boldFont = new Font("SansSerif", Font.BOLD, 10);
        reportState.setDefaultFont(plainFont);
        ReportLabel label;
        ReportField field;

        //
        // Create the page header containing the report title
        //
        label = new ReportLabel(String.format("Capital Gains Report for %s to %s",
                                              Main.getDateString(startDate),
                                              Main.getDateString(endDate)));
        label.setFont(boldFont);
        label.setBounds(new Rectangle(0, 0, printWidth, 25));
        label.setHorizontalAlignment(ReportElement.CENTER_ALIGNMENT);
        label.setVerticalAlignment(ReportElement.TOP_ALIGNMENT);
        reportState.getPageHeader().addElement(label);

        //
        // Create the page footer containing the page number
        //
        reportState.addExpression(new PageFunction("Page Number"));

        label = new ReportLabel("Page");
        label.setFont(boldFont);
        label.setBounds(new Rectangle(printWidth/2-30, 0, 25, 25));
        label.setHorizontalAlignment(ReportElement.RIGHT_ALIGNMENT);
        label.setVerticalAlignment(ReportElement.BOTTOM_ALIGNMENT);
        reportState.getPageFooter().addElement(label);

        field = new ReportField("Page Number");
        field.setFont(boldFont);
        field.setBounds(new Rectangle(printWidth/2, 0, 25, 25));
        field.setHorizontalAlignment(ReportElement.LEFT_ALIGNMENT);
        field.setVerticalAlignment(ReportElement.BOTTOM_ALIGNMENT);
        reportState.getPageFooter().addElement(field);

        //
        // Create the report footer containing the gain/loss total
        //
        AmountSumFunction totalFunction = new AmountSumFunction("Gain/Loss Total");
        totalFunction.setField(columnNames[6]);
        reportState.addExpression(totalFunction);

        label = new ReportLabel("Total");
        label.setFont(boldFont);
        label.setBounds(new Rectangle(elementPositions[6]-30, 0, 30, 12));
        label.setHorizontalAlignment(ReportElement.RIGHT_ALIGNMENT);
        reportState.getReportFooter().addElement(label);

        field = new ReportField("Gain/Loss Total");
        field.setFont(boldFont);
        field.setBounds(new Rectangle(elementPositions[6], 0, elementSizes[6], 12));
        field.setHorizontalAlignment(ReportElement.RIGHT_ALIGNMENT);
        field.setRenderer(new ReportAmountRenderer());
        reportState.getReportFooter().addElement(field);

        //
        // Create the text elements for the report
        //
        for (int i=0; i<columnNames.length; i++) {

            //
            // Set the column header label
            //
            label = new ReportLabel(columnNames[i]);
            label.setBounds(new Rectangle(elementPositions[i], 0, elementSizes[i], 14));
            label.setFont(boldFont);
            label.setHorizontalAlignment(elementAlignments[i]);
            defaultGroup.getHeader().addElement(label);

            //
            // Set the column field
            //
            field = new ReportField(columnNames[i]);
            field.setBounds(new Rectangle(elementPositions[i], 0, elementSizes[i], 12));
            field.setHorizontalAlignment(elementAlignments[i]);
            if (i == 6)
                field.setRenderer(new ReportAmountRenderer());

            reportState.getRowBand().addElement(field);
        }

        //
        // Display the print preview dialog
        //
        report.showPreview(this);
    }

    /**
     * Show the capital gains report dialog
     *
     * @param       parent          Parent window for the dialog
     */
    public static void showDialog(JFrame parent) {
        try {
            JDialog dialog = new CapitalGainsReportDialog(parent);
            dialog.pack();
            dialog.setLocationRelativeTo(parent);
            dialog.setVisible(true);
        } catch (Exception exc) {
            Main.logException("Exception while displaying dialog", exc);
        }
    }

    /**
     * Transaction report model
     */
    private class TransactionModel implements ReportModel {
        
        /** Capital gains list */
        private List<CapitalGainRecord> capitalGains;
        
        /** Security holdings list */
        private List<SecurityHolding> securityHoldings;

        /**
         * Create the report model
         *
         * @param       startDate   Start date
         * @param       endDate     End date
         */
        TransactionModel(Date startDate, Date endDate) {

            //
            // Create the report lists
            //
            capitalGains = new ArrayList<>(50);
            securityHoldings = new ArrayList<>(SecurityRecord.securities.size());

            //
            // Process the investment transactions and build the security holdings
            //
            for (TransactionRecord t : TransactionRecord.transactions) {
                //
                // Stop when we pass the end date
                //
                if (t.getDate().compareTo(endDate) > 0)
                    break;
                
                //
                // Update the security holdings for an investment transaction
                // in a taxable account (income from tax-deferred accounts is
                // taxed as ordinary income)
                //
                if (t.getSecurity() != null && !t.getAccount().isTaxDeferred())
                    SecurityHolding.updateSecurityHolding(securityHoldings, t);
            }
            
            //
            // Build the capital gains list
            //
            for (SecurityHolding h : securityHoldings) {
                List<CapitalGainRecord> gainsList = h.getCapitalGains();
                for (CapitalGainRecord c : gainsList) {
                    Date sellDate = c.getSellDate();
                    if (sellDate.compareTo(startDate) < 0 || sellDate.compareTo(endDate) > 0)
                        continue;
                    
                    boolean addGain = true;
                    for (int index=0; index<capitalGains.size(); index++) {
                        CapitalGainRecord n = capitalGains.get(index);
                        if (sellDate.compareTo(n.getSellDate()) < 0) {
                            capitalGains.add(index, c);
                            addGain = false;
                            break;
                        }
                    }
                    
                    if (addGain)
                        capitalGains.add(c);
                }
            }
        }

        /**
         * Get the number of columns
         *
         * @return                  The number of columns
         */
        public int getColumnCount() {
            return columnNames.length;
        }

        /**
         * Get the number of rows
         *
         * @return                  The number of rows
         */
        public int getRowCount() {
            return capitalGains.size();
        }

        /**
         * Get the column name
         *
         * @param       column      Column index
         * @return                  Column name
         */
        public String getColumnName(int column) {
            return columnNames[column];
        }

        /**
         * Get the object class for a column
         *
         * @param       column      Column index
         * @return                  Object class
         */
        public Class<?> getColumnClass(int column) {
            return String.class;
        }

        /**
         * Get the value for a cell
         *
         * @param       row         Row index
         * @param       column      Column index
         * @return                  Cell value
         */
        public Object getValueAt(int row, int column) {
            if (row >= capitalGains.size())
                throw new IndexOutOfBoundsException("Report row "+row+" is not valid");

            CapitalGainRecord g = capitalGains.get(row);
            Object value = null;

            switch (column) {
                case 0:                         // Security
                    value = g.getSecurity().getName();
                    break;

                case 1:                         // Date bought
                    value = Main.getDateString(g.getPurchaseDate());
                    break;

                case 2:                         // Date sold
                    value = Main.getDateString(g.getSellDate());
                    break;

                case 3:                         // Number of shares
                    value = String.format("%,.4f", g.getShares());
                    break;

                case 4:                         // Cost
                    value = String.format("%,.2f", g.getCostBasis());
                    break;

                case 5:                         // Amount
                    value = String.format("%,.2f", g.getSellAmount());
                    break;

                case 6:                         // Gain/Loss
                    double gain = g.getSellAmount()-g.getCostBasis();
                    if (Math.abs(gain) < 0.005)
                        gain = 0.0;
                    
                    value = String.format("%,.2f", gain);
                    break;

                default:
                    throw new IndexOutOfBoundsException("Report column "+column+" is not valid");
            }

            return value;
        }
    }
}
