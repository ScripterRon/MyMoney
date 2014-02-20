package Report;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;

import javax.swing.*;
import javax.swing.event.*;

/**
 * A report prints tabular data and consists of several report bands.  A report
 * band is included in the report only if it has at least one report element.  However,
 * the row band must contain at least one report element.
 * <p><ul>
 * <li>Report header
 * <li>Report footer
 * <li>Page header
 * <li>Page footer
 * <li>Group header
 * <li>Group footer
 * <li>Report row
 * </ul>
 * <p>The report header is printed on the first page of the report following the page
 * header.  The report footer is printed on the last page of the report before the
 * page footer and following any group footers.
 * <p>The page header is printed at the top of each page.  The report title will
 * be used for the page header if the application has not provided its own header.
 * The page footer is printed at the bottom of each page.
 * <p>The group header is printed when a group starts and the group footer is
 * printed when the group finishes.  A group starts when the value of the group
 * column for the current row is not the same as the value for the previous row.
 * A group finishes when the value of the group column for the next row is not the
 * same as the value for the current row.
 * <p>The ReportModel interface specifies the methods that the report will use to
 * interrogate the tabular data.  The data is assumed to be unchanging while the
 * report is being generated.  The application must provide a report model when
 * creating a report.
 * <p>A report group consists of report rows containing the same value in a column.
 * A new group is started whenever the column value changes.  A report can contain
 * multiple groups, in which case the columns are checked in the same order as the groups
 * were added to the report.  This means that a subgroup should be added before the
 * group containing the subgroup.
 * <p>The default group is created when the report is created.  It is not associated
 * with a data column.  Instead, the default group starts with the first report row
 * and ends after the last report row.  The default group is the last group in the
 * group list and can be accessed as ReportState.getGroup(ReportState.getGroupCount()-1).
 * <p>Report elements provide the text that make up a report.  Report elements are
 * added to a report band to provide the text for the band.
 * <p>Expressions are used to calculate values within a single row of a report.
 * The expression dependency level determines the order in which the expressions
 * are evaluated (a higher dependency level is evaluated before a lower dependency level).
 * The expression name is used to refer to an expression value.  The AbstractExpression
 * class must be extended when creating a new expression.
 * <p>A function is an expression which is notified of changes in the report state.
 * This allows the function to compute its value based on the current report state.
 * For example, a function could sum the values of a column within a group.  The
 * AbstractFunction class must be extended when creating a new function.
 * <p>A ReportLabel is a report element with static text.  The text string
 * is set when the report element is created and does not change.
 * <p>A ReportField is a report element whose text is obtained as needed
 * during report generation by querying a data column, report expression or
 * report function.
 * <p>A report renderer returns the string representation of a value.  The toString()
 * method is normally used to convert a report field value to a text string.  A
 * report field can specify a custom report renderer if a different format is
 * desired for the element value.  A custom report renderer must implement the
 * ReportRenderer interface.
 * <p>The showPreview() method will display a dialog containing the report pages.  The
 * report can be viewed and printed directly from the dialog.  No changes to the
 * report are allowed after calling the showPreview() method.
 * <p>The printReport() method will print the report without showing the report preview.
 * No changes to the report are allowed after calling the printReport() method.
 * <p>The getState() method will return the report state.  The application must add
 * the desired elements, groups, expressions and functions to the report state before
 * generating the report.  The default page definition is 8.5x11 paper in portrait mode.
 * This can be changed by calling the setPageFormat() method to set a new page definition
 * in the report state before generating the report.  The default report element font
 * is 10-point SansSerif.  This can be changed by calling the setDefaultFont() method
 * to set a new default font for the report.
 */
public final class Report {

    /** The report state */
    private ReportState state;

    /** Report finalized flag */
    private boolean reportFinalized = false;

    /**
     * Construct a new report
     *
     * @param       title           The report title
     * @param       model           The report model for the tabular data
     */
    public Report(String title, ReportModel model) {
        if (title == null)
            throw new NullPointerException("No report title provided");

        if (model == null)
            throw new NullPointerException("No report model provided");

        //
        // Create the report state
        //
        state = new ReportState(title, new DataRow(model));
    }

    /**
     * Return the report state
     *
     * @return                      The report state
     */
    public ReportState getState() {
        return state;
    }

    /**
     * Show the report preview dialog.  This is a modal dialog and it will be
     * positioned relative to the parent frame.  Control will not return to
     * the caller until the preview dialog is closed.
     */
    public void showPreview(JFrame parent) throws ReportException {
        if (!reportFinalized)
            finalizeReport();

        ReportPreview preview = new ReportPreview(parent, state);
        preview.setVisible(true);
    }

    /**
     * Show the report preview dialog.  This is a modal dialog and it will be
     * positioned relative to the parent dialog.  Control will not return to
     * the caller until the preview dialog is closed.
     */
    public void showPreview(JDialog parent) throws ReportException {
        if (!reportFinalized)
            finalizeReport();

        ReportPreview preview = new ReportPreview(parent, state);
        preview.setVisible(true);
    }

    /**
     * Print the report without showing the report preview.  Control will not
     * return to the caller until the report has been printed.
     */
    public void printReport() throws ReportException {
        if (!reportFinalized)
            finalizeReport();

        ReportPrint reportPrint = new ReportPrint(state);
        reportPrint.printReport();
    }

    /**
     * Finalize the report.  This is done just once no matter how may times
     * the report is viewed or printed.
     */
    private void finalizeReport() throws ReportException {
        int i, j, k;
        int elemCount, expCount, groupCount;
        ReportBand band;

        //
        // Create the default page header if the application has not provided
        // a page header.  We will use the report title in 10-point bold
        // SansSerif.
        //
        band = state.getPageHeader();
        if (band.getElementCount() == 0) {
            ReportLabel label = new ReportLabel(state.getReportTitle());
            label.setFont(new Font("SansSerif", Font.BOLD, 10));
            label.setColor(Color.BLACK);
            label.setBounds(new Rectangle(0, 0, (int)state.getPageFormat().getImageableWidth(), 14));
            label.setHorizontalAlignment(ReportElement.CENTER_ALIGNMENT);
            label.setVerticalAlignment(ReportElement.CENTER_ALIGNMENT);
            band.addElement(label);
        }

        //
        // Finalize the expressions and functions
        //
        expCount = state.getExpressionCount();
        for (i=0; i<expCount; i++)
            ((AbstractExpression)state.getExpression(i)).setState(state);

        //
        // Finalize the report elements
        //
        for (i=0; i<5; i++) {
            if (i == 0)
                band = state.getReportHeader();
            else if (i == 1)
                band = state.getReportFooter();
            else if (i == 2)
                band = state.getPageHeader();
            else if (i == 3)
                band = state.getPageFooter();
            else
                band = state.getRowBand();

            elemCount = band.getElementCount();
            if (elemCount == 0 && i == 4)
                throw new ReportException("No report row elements");

            for (j=0; j<elemCount; j++)
                ((AbstractElement)band.getElement(j)).setState(state);
        }

        //
        // Finalize the groups
        //
        groupCount = state.getGroupCount();
        for (k=0; k<groupCount; k++) {
            ReportGroup group = state.getGroup(k);

            for (i=0; i<2; i++) {
                if (i == 0)
                    band = group.getHeader();
                else
                    band = group.getFooter();

                elemCount = band.getElementCount();
                for (j=0; j<elemCount; j++)
                    ((AbstractElement)band.getElement(j)).setState(state);
            }
        }

        //
        // Notify any report listeners that initialization is complete
        //
        ReportEvent event = new ReportEvent(ReportEvent.REPORT_INITIALIZED, state);
        expCount = state.getExpressionCount();
        for (i=0; i<expCount; i++) {
            ReportExpression exp = state.getExpression(i);
            if (exp instanceof ReportListener)
                ((ReportListener)exp).reportInitialized(event);
        }

        //
        // Indicate the report has been finalized
        //
        reportFinalized = true;
    }
}

