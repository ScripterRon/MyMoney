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
 * Transaction report dialog
 */
public final class TransactionReportDialog extends JDialog implements ActionListener {

    /** Sort by date */
    private static final int SORT_BY_DATE = 1;

    /** Sort by name */
    private static final int SORT_BY_NAME = 2;

    /** Sort by account */
    private static final int SORT_BY_ACCOUNT = 3;

    /** Sort by category */
    private static final int SORT_BY_CATEGORY = 4;

    /** Report column names */
    private static final String[] columnNames = {
        "Date", "Check", "Account", "Name", "Category/Account", "Memo", "Amount"};

    /** Report element sizes */
    private static final int[] elementSizes = {70, 35, 105, 140, 105, 140, 70};

    /** Report element positions */
    private static final int[] elementPositions = {2, 80, 123, 236, 384, 497, 645};

    /** Report element alignments */
    private static final int[] elementAlignments = {
        ReportElement.LEFT_ALIGNMENT,  ReportElement.RIGHT_ALIGNMENT,
        ReportElement.LEFT_ALIGNMENT,  ReportElement.LEFT_ALIGNMENT,
        ReportElement.LEFT_ALIGNMENT,  ReportElement.LEFT_ALIGNMENT,
        ReportElement.RIGHT_ALIGNMENT};

    /** Start date field */
    private JFormattedTextField startField;

    /** End date field */
    private JFormattedTextField endField;
    
    /** Category model */
    private TransferComboBoxModel categoryModel;
    
    /** Category field */
    private JComboBox categoryField;

    /** Sort by date radio button */
    private JRadioButton sortDateField;

    /** Sort by account radio button */
    private JRadioButton sortAccountField;

    /** Sort by name radio button */
    private JRadioButton sortNameField;

    /** Sort by category radio button */
    private JRadioButton sortCategoryField;

    /**
     * Create the dialog
     *
     * @param       parent          Parent frame
     */
    public TransactionReportDialog(JFrame parent) {
        super(parent, "Transaction Report", true);
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
        // Get the category selection (optional)
        //
        categoryModel = new TransferComboBoxModel(null);
        categoryField = new JComboBox(categoryModel);
        categoryField.setSelectedIndex(0);

        //
        // Create the edit pane
        //
        //    Start Date:        <text-field>
        //    End Date:          <text-field>
        //    Category:          <combo-box>
        //
        JPanel editPane = new JPanel(new GridLayout(0, 2, 5, 5));

        editPane.add(new JLabel("Start Date:", JLabel.RIGHT));
        editPane.add(startField);

        editPane.add(new JLabel("End Date:", JLabel.RIGHT));
        editPane.add(endField);
        
        editPane.add(new JLabel("Category:", JLabel.RIGHT));
        editPane.add(categoryField);

        //
        // Create the sort radio buttons
        //
        sortDateField = new JRadioButton("Date");
        sortDateField.setSelected(true);

        sortAccountField = new JRadioButton("Account");

        sortNameField = new JRadioButton("Name");

        sortCategoryField = new JRadioButton("Category");

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(sortDateField);
        buttonGroup.add(sortAccountField);
        buttonGroup.add(sortNameField);
        buttonGroup.add(sortCategoryField);

        JPanel groupPane = new JPanel();
        groupPane.setLayout(new BoxLayout(groupPane, BoxLayout.X_AXIS));
        groupPane.add(new JLabel("Sort by: ", JLabel.LEADING));
        groupPane.add(Box.createHorizontalStrut(10));
        groupPane.add(sortDateField);
        groupPane.add(Box.createHorizontalStrut(10));
        groupPane.add(sortAccountField);
        groupPane.add(Box.createHorizontalStrut(10));
        groupPane.add(sortNameField);
        groupPane.add(Box.createHorizontalStrut(10));
        groupPane.add(sortCategoryField);

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
        contentPane.add(groupPane);
        contentPane.add(Box.createVerticalStrut(15));
        contentPane.add(buttonPane);
        setContentPane(contentPane);
    }

    /**
     * TransactionReportDialog action performed (ActionListener interface)
     *
     * @param       ae              Action event
     */
    public void actionPerformed(ActionEvent ae) {

        //
        // Process the action command
        //
        // "create report" - Create the report
        // "cancel" - All done
        //
        try {
            switch (ae.getActionCommand()) {
                case "create report":
                    Date startDate, endDate;
                    CategoryRecord category;
                    int sortMode;
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
                            int index = categoryField.getSelectedIndex();
                            if (index > 0)
                                category = (CategoryRecord)categoryModel.getDBElementAt(index);
                            else
                                category = null;

                            if (sortAccountField.isSelected())
                                sortMode = SORT_BY_ACCOUNT;
                            else if (sortNameField.isSelected())
                                sortMode = SORT_BY_NAME;
                            else if (sortCategoryField.isSelected())
                                sortMode = SORT_BY_CATEGORY;
                            else
                                sortMode = SORT_BY_DATE;

                            generateReport(startDate, endDate, category, sortMode);
                        }
                    }
                    break;
                    
                case "cancel":
                    setVisible(false);
                    dispose();
                    break;
            }
        } catch (ReportException exc) {
            Main.logException("Exception while generating transaction report", exc);
        } catch (Exception exc) {
            Main.logException("Exception while processing action event", exc);
        }
    }

    /**
     * Generate the report
     *
     * @param       startDate       The start date for the report
     * @param       endDate         The end date for the report
     * @param       category        The category or null
     * @param       sortMode        The sort mode
     */
    private void generateReport(Date startDate, Date endDate, CategoryRecord category,
                                int sortMode) throws ReportException {

        //
        // Create the report data model
        //
        ReportModel reportModel = new TransactionModel(startDate, endDate, category, sortMode);
        
        //
        // Create the list of loan account names
        //
        List<String> loanAccountNames = new ArrayList<>(10);
        for (AccountRecord a : AccountRecord.accounts) {
            if (a.getType() == AccountRecord.LOAN)
                loanAccountNames.add("["+a.getName()+"]");
        }

        //
        // Create the report
        //
        Report report = new Report("Transaction Report", reportModel);
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
        label = new ReportLabel(String.format("Transaction Report for %s to %s",
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
        totalFunction.setField(columnNames[6]);
        totalFunction.setTransferAccountField(columnNames[4]);
        totalFunction.setLoanAccountNames(loanAccountNames);
        reportState.addExpression(totalFunction);

        label = new ReportLabel("Income/Expense Total");
        label.setFont(boldFont);
        label.setBounds(new Rectangle(elementPositions[6]-145, 0, 140, 12));
        label.setHorizontalAlignment(ReportElement.RIGHT_ALIGNMENT);
        reportState.getReportFooter().addElement(label);

        field = new ReportField("Report Total");
        field.setFont(boldFont);
        field.setBounds(new Rectangle(elementPositions[6], 0, elementSizes[6], 12));
        field.setHorizontalAlignment(ReportElement.RIGHT_ALIGNMENT);
        field.setRenderer(new ReportAmountRenderer());
        reportState.getReportFooter().addElement(field);

        //
        // Create the sort group footer containing the group subtotal
        //
        // We will not generate subtotals when sorting by date
        //
        if (sortMode != SORT_BY_DATE) {
            int fieldIndex;
            if (sortMode == SORT_BY_ACCOUNT)
                fieldIndex = 2;
            else if (sortMode == SORT_BY_NAME)
                fieldIndex = 3;
            else
                fieldIndex = 4;

            ReportGroup sortGroup = new ReportGroup("Sort Group", columnNames[fieldIndex]);
            reportState.addGroup(sortGroup);

            AmountSumFunction subtotalFunction = new AmountSumFunction("Group Total");
            subtotalFunction.setGroupName("Sort Group");
            subtotalFunction.setField(columnNames[6]);
            subtotalFunction.setTransferAccountField(columnNames[4]);
            reportState.addExpression(subtotalFunction);

            label = new ReportLabel("Income/Expense Subtotal");
            label.setFont(boldFont);
            label.setBounds(new Rectangle(elementPositions[6]-166, 0, 160, 12));
            label.setHorizontalAlignment(ReportElement.RIGHT_ALIGNMENT);
            sortGroup.getFooter().addElement(label);

            field = new ReportField("Group Total");
            field.setFont(boldFont);
            field.setBounds(new Rectangle(elementPositions[6], 0, elementSizes[6], 12));
            field.setHorizontalAlignment(ReportElement.RIGHT_ALIGNMENT);
            field.setRenderer(new ReportAmountRenderer());
            sortGroup.getFooter().addElement(field);

            label = new ReportLabel(" ");
            label.setBounds(new Rectangle(0, 12, 7, 12));
            sortGroup.getFooter().addElement(label);
        }

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
     * Show the dialog
     *
     * @param       parent          Parent window for the dialog
     */
    public static void showDialog(JFrame parent) {
        try {
            JDialog dialog = new TransactionReportDialog(parent);
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

        /** The transaction records */
        private List<TransactionRecord> listData;

        /**
         * Create the report model
         *
         * @param       startDate   Report start date
         * @param       endDate     Report end date
         * @param       category    The category or null
         * @param       sortMode    Report sort mode
         */
        public TransactionModel(Date startDate, Date endDate, CategoryRecord category,
                                int sortMode) {

            //
            // Create the report transaction list
            //
            listData = new ArrayList<>(TransactionRecord.transactions.size());

            //
            // Build the report data using transactions within the specified date range
            // and sorted as specified by the sort mode.  All categories will be included
            // if no category selection is supplied.
            //
            // Splits will be expanded into a separate transaction for each split
            //
            // Transactions without a category will not be included when sorting by
            // category
            //
            for (TransactionRecord t : TransactionRecord.transactions) {
                String name;
                int index = 0;
                boolean addTransaction = true;

                //
                // Process transactions within the specified date range
                // and for the specified category (if any)
                //
                Date date = t.getDate();
                if (date.compareTo(startDate) < 0 || date.compareTo(endDate) > 0)
                    continue;
                
                List<TransactionSplit> splits = t.getSplits();
                if (splits == null && category != null && t.getCategory() != category)
                    continue;

                //
                // Process the transaction
                //
                if (splits != null) {
                    AccountRecord account = t.getAccount();
                    name = t.getName();

                    //
                    // We will suppress the date field for all but the first expanded
                    // transaction unless we are sorting by category, in which
                    // case we need to always display the date since the expanded
                    // transactions will not remain contiguous after being sorted
                    //
                    boolean expandedTransaction = false;

                    //
                    // Locate the insertion point for the new transactions
                    //
                    if (sortMode == SORT_BY_NAME) {
                        for (TransactionRecord x : listData) {
                            SecurityRecord s = x.getSecurity();
                            if (s != null) {
                                if (name.compareTo(s.getName()) < 0)
                                    break;
                            } else {
                                if (name.compareTo(x.getName()) < 0)
                                    break;
                            }

                            index++;
                        }
                    } else if (sortMode == SORT_BY_ACCOUNT) {
                        for (TransactionRecord x : listData) {
                            if (account.getName().compareTo(x.getAccount().getName()) < 0)
                                break;

                            index++;
                        }
                    } else if (sortMode == SORT_BY_DATE) {
                        index = listData.size();
                    }

                    //
                    // Create a temporary transaction for each split.  We will skip a
                    // split if we are sorting by category and the split doesn't have
                    // a category or if the split category doesn't match the report
                    // category (if any).
                    //
                    for (TransactionSplit split : splits) {
                        CategoryRecord c = split.getCategory();
                        if (sortMode == SORT_BY_CATEGORY && c == null)
                            continue;
                        
                        if (category != null && c != category)
                            continue;

                        TransactionRecord x = new TransactionRecord(date, account);
                        x.setName(name);
                        x.setMemo(split.getDescription());
                        x.setCategory(c);
                        x.setTransferAccount(split.getAccount());
                        x.setAmount(split.getAmount());
                        x.setExpandedTransaction(expandedTransaction);

                        if (sortMode != SORT_BY_CATEGORY)
                            expandedTransaction = true;

                        if (sortMode == SORT_BY_CATEGORY) {
                            index = 0;
                            addTransaction = true;
                            for (TransactionRecord xx : listData) {
                                if (c.getName().compareTo(xx.getCategory().getName()) < 0) {
                                    listData.add(index, x);
                                    addTransaction = false;
                                    break;
                                }

                                index++;
                            }

                            if (addTransaction)
                                listData.add(x);
                        } else {
                            listData.add(index, x);
                            index++;
                        }
                    }

                    addTransaction = false;

                } else if (sortMode == SORT_BY_NAME) {

                    //
                    // Sort based on the transaction name
                    //
                    SecurityRecord s = t.getSecurity();
                    if (s != null)
                        name = s.getName();
                    else
                        name = t.getName();

                    for (TransactionRecord x : listData) {
                        s = x.getSecurity();
                        if (s != null) {
                            if (name.compareTo(s.getName()) < 0) {
                                listData.add(index, t);
                                addTransaction = false;
                                break;
                            }
                        } else {
                            if (name.compareTo(x.getName()) < 0) {
                                listData.add(index, t);
                                addTransaction = false;
                                break;
                            }
                        }

                        index++;
                    }

                } else if (sortMode == SORT_BY_ACCOUNT) {

                    //
                    // Sort based on the transaction account
                    //
                    name = t.getAccount().getName();
                    for (TransactionRecord x : listData) {
                        if (name.compareTo(x.getAccount().getName()) < 0) {
                            listData.add(index, t);
                            addTransaction = false;
                            break;
                        }

                        index++;
                    }

                } else if (sortMode == SORT_BY_CATEGORY) {

                    //
                    // Sort based on the transaction category
                    //
                    CategoryRecord c = t.getCategory();
                    if (c == null) {
                        addTransaction = false;
                    } else {
                        name = c.getName();
                        for (TransactionRecord x : listData) {
                            if (name.compareTo(x.getCategory().getName()) < 0) {
                                listData.add(index, t);
                                addTransaction = false;
                                break;
                            }

                            index++;
                        }
                    }
                }

                //
                // Add the transaction at the end of the list if we haven't inserted
                // it yet
                if (addTransaction)
                    listData.add(t);
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
         * Get the value for a cell
         *
         * @param       row         Row index
         * @param       column      Column index
         * @return                  Cell value
         */
        public Object getValueAt(int row, int column) {
            if (row >= listData.size())
                throw new IndexOutOfBoundsException("Report row "+row+" is not valid");

            Object value;
            AccountRecord a;
            CategoryRecord c;
            SecurityRecord s;
            TransactionRecord t = listData.get(row);

            switch (column) {
                case 0:                             // Date (mm/dd/yyyy)
                    if (!t.isExpandedTransaction())
                        value = Main.getDateString(t.getDate());
                    else
                        value = new String();
                    break;

                case 1:                             // Check number (nnnnn)
                    if (!t.isExpandedTransaction()) {
                        int number = t.getCheckNumber();
                        if (number != 0)
                            value = String.format("%d", number);
                        else
                            value = new String();
                    } else {
                        value = new String();
                    }
                    break;

                case 2:                             // Account
                    value = t.getAccount().getName();
                    break;

                case 3:                             // Name
                    s = t.getSecurity();
                    if (s != null)
                        value = s.getName();
                    else
                        value = t.getName();
                    break;

                case 4:                             // Category/Account
                    if (t.getSplits() != null) {
                        value = "--Split--";
                    } else {
                        c = t.getCategory();
                        if (c != null) {
                            value = c.getName();
                        } else {
                            a = t.getTransferAccount();
                            if (a != null)
                                value = "["+a.getName()+"]";
                            else
                                value = new String();
                        }
                    }
                    break;

                case 5:                             // Memo
                    value = t.getMemo();
                    break;

                case 6:                             // Amount
                    double amount = t.getAmount();
                    if (t.getAccount().getType() == AccountRecord.INVESTMENT &&
                                        t.getAction() != TransactionRecord.REINVEST &&
                                        amount != 0.0)
                        amount = -amount;

                    value = String.format("%,.2f", amount);
                    break;

                default:
                    throw new IndexOutOfBoundsException("Report column "+column+" is not valid");
            }

            return value;
        }
    }
}
