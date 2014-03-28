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

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;

/**
 * Investment report dialog
 */
public final class InvestmentReportDialog extends JDialog implements ActionListener {

    /** Report column names */
    private static final String[] columnNames = {
        "Date", "Action", "Security", "Category", "Shares", "Price", "Comm", "Amount"};

    /** Report element sizes */
    private static final int[] elementSizes = {70, 60, 175, 105,  70,  56,  42,  70};

    /** Report element positions */
    private static final int[] elementPositions = {2, 81, 150, 334, 448, 527, 592, 643};

     /** Report element alignments */
    private static final int[] elementAlignments = {
        ReportElement.LEFT_ALIGNMENT,  ReportElement.LEFT_ALIGNMENT,
        ReportElement.LEFT_ALIGNMENT,  ReportElement.LEFT_ALIGNMENT,
        ReportElement.RIGHT_ALIGNMENT, ReportElement.RIGHT_ALIGNMENT,
        ReportElement.RIGHT_ALIGNMENT, ReportElement.RIGHT_ALIGNMENT};

    /** Investment account model */
    private DBElementComboBoxModel accountModel;
    
    /** Investment account field */
    private JComboBox accountField;

    /** Security field model */
    private DBElementComboBoxModel securityModel;
    
    /** Security field */
    private JComboBox securityField;

    /**
     * Create the dialog
     *
     * @param       parent          Parent frame
     */
    public InvestmentReportDialog(JFrame parent) {
        super(parent, "Investment Report", true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        //
        // Get the investment account
        //
        accountModel = new DBElementComboBoxModel(AccountRecord.accounts, AccountRecord.INVESTMENT);
        accountField = new JComboBox(accountModel);
        if (accountModel.getSize() > 0)
            accountField.setSelectedIndex(0);

        //
        // Get the security (optional)
        //
        // No security is selected when the combo box is displayed, resulting
        // in all securities being processed.  If the user selects a security,
        // only that security will be processed.  The selection will then be cleared
        // when the dialog is redisplayed.
        //
        securityModel = new DBElementComboBoxModel(SecurityRecord.securities);
        securityField = new JComboBox(securityModel);

        //
        // Create the edit pane
        //
        //    Account:           <combo-box>
        //    Security:          <combo-box>
        //
        JPanel editPane = new JPanel(new GridLayout(0, 2, 5, 5));

        editPane.add(new JLabel("Account:", JLabel.RIGHT));
        editPane.add(accountField);

        editPane.add(new JLabel("Security:", JLabel.RIGHT));
        editPane.add(securityField);

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
     * InvestmentReportDialog action performed (ActionListener interface)
     *
     * @param       ae              Action event
     */
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
                    AccountRecord account;
                    SecurityRecord security;
                    int index = accountField.getSelectedIndex();
                    if (index < 0) {
                        JOptionPane.showMessageDialog(this, "You must specify an investment account",
                                                      "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        account = (AccountRecord)accountModel.getDBElementAt(index);
                        index = securityField.getSelectedIndex();
                        if (index >= 0)
                            security = (SecurityRecord)securityModel.getDBElementAt(index);
                        else
                            security = null;

                        securityField.setSelectedIndex(-1);
                        generateReport(account, security);
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
     * @param       account         The investment account
     * @param       security        The security or null for all securities
     */
    private void generateReport(AccountRecord account, SecurityRecord security)
                                throws ReportException {

        //
        // Create the report data model
        //
        ReportModel reportModel = new InvestmentReportModel(columnNames, account, security);

        //
        // Create the report
        //
        Report report = new Report("Investment Report", reportModel);
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
        label = new ReportLabel(String.format("Investment Report for %s", account.getName()));
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
        // Create the security group footer
        //
        // "Shares: nnnnn.nnnn  Cost Basis: nnnnn.nn  Gain: nn.nn%  Yield: nn.nn%
        //
        ReportGroup securityGroup = new ReportGroup("Security Group", columnNames[2]);
        reportState.addGroup(securityGroup);

        SecuritySharesExpression sharesFunction = new SecuritySharesExpression("Security Shares");
        sharesFunction.setSecurityField(columnNames[2]);
        sharesFunction.setDependencyLevel(0);
        reportState.addExpression(sharesFunction);

        SecurityCostExpression costFunction = new SecurityCostExpression("Cost Basis");
        costFunction.setSecurityField(columnNames[2]);
        costFunction.setDependencyLevel(0);
        reportState.addExpression(costFunction);

        SecurityGainExpression gainExpression = new SecurityGainExpression("Security Gain");
        gainExpression.setSecurityField(columnNames[2]);
        gainExpression.setDependencyLevel(0);
        reportState.addExpression(gainExpression);

        SecurityYieldExpression yieldExpression = new SecurityYieldExpression("Security Yield");
        yieldExpression.setSecurityField(columnNames[2]);
        yieldExpression.setDependencyLevel(0);
        reportState.addExpression(yieldExpression);

        label = new ReportLabel("Shares:");
        label.setBounds(new Rectangle(7, 0, 50, 12));
        label.setHorizontalAlignment(ReportElement.RIGHT_ALIGNMENT);
        securityGroup.getFooter().addElement(label);

        field = new ReportField("Security Shares");
        field.setBounds(new Rectangle(58, 0, 70, 12));
        field.setHorizontalAlignment(ReportElement.RIGHT_ALIGNMENT);
        securityGroup.getFooter().addElement(field);

        label = new ReportLabel("Cost Basis:");
        label.setBounds(new Rectangle(138, 0, 70, 12));
        label.setHorizontalAlignment(ReportElement.RIGHT_ALIGNMENT);
        securityGroup.getFooter().addElement(label);

        field = new ReportField("Cost Basis");
        field.setBounds(new Rectangle(208, 0, 56, 12));
        field.setHorizontalAlignment(ReportElement.RIGHT_ALIGNMENT);
        securityGroup.getFooter().addElement(field);

        label = new ReportLabel("Gain:");
        label.setBounds(new Rectangle(274, 0, 56, 12));
        label.setHorizontalAlignment(ReportElement.RIGHT_ALIGNMENT);
        securityGroup.getFooter().addElement(label);

        field = new ReportField("Security Gain");
        field.setBounds(new Rectangle(330, 0, 42, 12));
        field.setHorizontalAlignment(ReportElement.RIGHT_ALIGNMENT);
        securityGroup.getFooter().addElement(field);

        label = new ReportLabel("Yield:");
        label.setBounds(new Rectangle(382, 0, 63, 12));
        label.setHorizontalAlignment(ReportElement.RIGHT_ALIGNMENT);
        securityGroup.getFooter().addElement(label);

        field = new ReportField("Security Yield");
        field.setBounds(new Rectangle(445, 0, 42, 12));
        field.setHorizontalAlignment(ReportElement.RIGHT_ALIGNMENT);
        securityGroup.getFooter().addElement(field);

        label = new ReportLabel(" ");
        label.setBounds(new Rectangle(0, 12, 7, 12));
        securityGroup.getFooter().addElement(label);

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
            reportState.getRowBand().addElement(field);
        }

        //
        // Display the print preview dialog
        //
        report.showPreview(this);
    }

    /**
     * Show the investment report dialog
     *
     * @param       parent          Parent window for the dialog
     */
    public static void showDialog(JFrame parent) {
        try {
            JDialog dialog = new InvestmentReportDialog(parent);
            dialog.pack();
            dialog.setLocationRelativeTo(parent);
            dialog.setVisible(true);
        } catch (Exception exc) {
            Main.logException("Exception while displaying dialog", exc);
        }
    }
}
