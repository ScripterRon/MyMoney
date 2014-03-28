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
import org.ScripterRon.Report.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;

/**
 * Amortization report dialog
 */
public final class AmortizationReportDialog extends JDialog implements ActionListener {

    /** Report column names */
    private static final String[] columnNames = {
        "Date", "Security", "Category", "Amount"};

    /** Report element sizes */
    private static final int[] elementSizes = {80, 185, 140, 80};

    /** Report element positions */
    private static final int[] elementPositions = {2, 91, 285, 434};

     /** Report element alignments */
    private static final int[] elementAlignments = {
        ReportElement.LEFT_ALIGNMENT,  ReportElement.LEFT_ALIGNMENT,
        ReportElement.LEFT_ALIGNMENT,  ReportElement.RIGHT_ALIGNMENT};
    
    /** Start date field */
    private JFormattedTextField startField;

    /** End date field */
    private JFormattedTextField endField;
    
    /** Investment account field model */
    private DBElementComboBoxModel accountFieldModel;
    
    /** Investment account field */
    private JComboBox accountField;

    /** Investment accounts */
    private AccountRecord[] accounts;
    
    /**
     * Create the dialog
     *
     * @param       parent          Parent frame
     */
    public AmortizationReportDialog(JFrame parent) {
        super(parent, "Amortization Report", true);
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
        // Get the investment account
        //
        accountFieldModel = new DBElementComboBoxModel(AccountRecord.accounts,
                                                       AccountRecord.INVESTMENT);
        accountField = new JComboBox(accountFieldModel);
        if (accountFieldModel.getSize() > 0)
            accountField.setSelectedIndex(0);

        //
        // Create the edit pane
        //
        //    Start Date:        <text-field>
        //    End Date:          <text-field>
        //    Account:           <combo-box>
        //
        JPanel editPane = new JPanel(new GridLayout(0, 2, 5, 5));

        editPane.add(new JLabel("Start Date:", JLabel.RIGHT));
        editPane.add(startField);

        editPane.add(new JLabel("End Date:", JLabel.RIGHT));
        editPane.add(endField);
        
        editPane.add(new JLabel("Account:", JLabel.RIGHT));
        editPane.add(accountField);
        
        //
        // Create the button pane (Create Report, Done)
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
     * AmortizationReportDialog action performed (ActionListener interface)
     *
     * @param       ae              Action event
     */
    @Override
    public void actionPerformed(ActionEvent ae) {

        //
        // Process the action command
        //
        // "create report" - Create the report
        // "done" - All done
        //
        try {
            switch (ae.getActionCommand()) {
                case "create report":
                    Date startDate, endDate;
                    AccountRecord account;
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
                            int index = accountField.getSelectedIndex();
                            if (index < 0) {
                                JOptionPane.showMessageDialog(this, "You must specify an investment account",
                                                              "Error", JOptionPane.ERROR_MESSAGE);
                            } else {
                                account = (AccountRecord)accountFieldModel.getDBElementAt(index);
                                generateReport(startDate, endDate, account);
                            }
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
     * @param       account         The investment account
     */
    private void generateReport(Date startDate, Date endDate, AccountRecord account)
                                throws ReportException {
        //
        // Create the report data model
        //
        ReportModel reportModel = new TransactionModel(startDate, endDate, account);

        //
        // Create the report
        //
        Report report = new Report("Amortization Report", reportModel);
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
        label = new ReportLabel(String.format("%s Accretion/Amortization Report for %s to %s",
                                              account.getName(),
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
        // Create the report footer containing the transaction total
        //
        AmountSumFunction totalFunction = new AmountSumFunction("Report Total");
        totalFunction.setField(columnNames[3]);
        reportState.addExpression(totalFunction);

        label = new ReportLabel("Accretion/Amortization Total");
        label.setFont(boldFont);
        label.setBounds(new Rectangle(elementPositions[3]-145, 0, 140, 12));
        label.setHorizontalAlignment(ReportElement.RIGHT_ALIGNMENT);
        reportState.getReportFooter().addElement(label);

        field = new ReportField("Report Total");
        field.setFont(boldFont);
        field.setBounds(new Rectangle(elementPositions[3], 0, elementSizes[3], 12));
        field.setHorizontalAlignment(ReportElement.RIGHT_ALIGNMENT);
        field.setRenderer(new ReportAmountRenderer());
        reportState.getReportFooter().addElement(field);

        //
        // Create the group footer containing the group subtotal
        //
        ReportGroup sortGroup = new ReportGroup("Sort Group", columnNames[1]);
        reportState.addGroup(sortGroup);

        AmountSumFunction subtotalFunction = new AmountSumFunction("Group Total");
        subtotalFunction.setGroupName("Sort Group");
        subtotalFunction.setField(columnNames[3]);
        reportState.addExpression(subtotalFunction);

        label = new ReportLabel("Accretion/Amortization Subtotal");
        label.setFont(boldFont);
        label.setBounds(new Rectangle(elementPositions[3]-166, 0, 160, 12));
        label.setHorizontalAlignment(ReportElement.RIGHT_ALIGNMENT);
        sortGroup.getFooter().addElement(label);

        field = new ReportField("Group Total");
        field.setFont(boldFont);
        field.setBounds(new Rectangle(elementPositions[3], 0, elementSizes[3], 12));
        field.setHorizontalAlignment(ReportElement.RIGHT_ALIGNMENT);
        field.setRenderer(new ReportAmountRenderer());
        sortGroup.getFooter().addElement(field);

        label = new ReportLabel(" ");
        label.setBounds(new Rectangle(0, 12, 7, 12));
        sortGroup.getFooter().addElement(label);

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
            if (i == 3)
                field.setRenderer(new ReportAmountRenderer());

            reportState.getRowBand().addElement(field);
        }

        //
        // Display the print preview dialog
        //
        report.showPreview(this);
    }
    
    /**
     * Show the amortization report dialog
     *
     * @param       parent          Parent window for the dialog
     */
    public static void showDialog(JFrame parent) {
        try {
            JDialog dialog = new AmortizationReportDialog(parent);
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

        /** The investment transactions data */
        private List<TransactionRecord> listData;

        /**
         * Create the report model
         *
         * @param       startDate       The start date
         * @param       endDate         The end date
         * @param       account         The investment account
         */
        public TransactionModel(Date startDate, Date endDate, AccountRecord account) {

            //
            // Create the report transaction list
            //
            listData = new ArrayList<>(TransactionRecord.transactions.size());

            //
            // Build the report data using transactions for the specified investment account.
            //
            for (TransactionRecord t : TransactionRecord.transactions) {

                //
                // Skip the transaction if it is not for the requested account,
                // is not a security transaction, is not within the specified date range,
                // or is not an accretion/amortization action.
                //
                if (t.getAccount() != account)
                    continue;

                SecurityRecord s = t.getSecurity();
                if (s == null)
                    continue;

                Date date = t.getDate();
                if (date.compareTo(startDate) < 0 || date.compareTo(endDate) > 0)
                    continue;
                
                int action = t.getAction();
                if (action != TransactionRecord.ACCRETION && action != TransactionRecord.AMORTIZATION)
                    continue;
                
                //
                // Add the security to our list based on the security name
                //
                String name = s.getName();
                int lastElem = listData.size()-1;
                if (lastElem < 0) {
                    listData.add(t);
                } else if (name.compareTo(listData.get(lastElem).getSecurity().getName()) >= 0) {
                    listData.add(t);
                } else {
                    int lowIndex = -1;
                    int highIndex = lastElem;
                    while (highIndex-lowIndex > 1) {
                        int index = (highIndex-lowIndex)/2+lowIndex;
                        if (name.compareTo(listData.get(index).getSecurity().getName()) < 0)
                            highIndex = index;
                        else
                            lowIndex = index;
                    }

                    listData.add(highIndex, t);
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
            return listData.size();
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
         * Get the value for a cell.  
         * 
         * Amortization values will be negative while accretion values will be positive.
         *
         * @param       row         Row index
         * @param       column      Column index
         * @return                  Cell value
         */
        public Object getValueAt(int row, int column) {
            if (row >= listData.size())
                throw new IndexOutOfBoundsException("Report row "+row+" is not valid");

            Object value;
            TransactionRecord t = listData.get(row);

            switch (column) {
                case 0:                             // Date (mm/dd/yyyy)
                    value = Main.getDateString(t.getDate());
                    break;

                case 1:                             // Security
                    value = t.getSecurity().getName();
                    break;

                case 2:                             // Category
                    CategoryRecord c = t.getCategory();
                    if (c != null)
                        value = c.getName();
                    else
                        value = " ";
                    break;
                    
                case 3:                             // Amount
                    value = String.format("%,.2f", -t.getAmount());
                    break;

                default:
                    throw new IndexOutOfBoundsException("Report column "+column+" is not valid");
            }

            return value;
        }
    }
}