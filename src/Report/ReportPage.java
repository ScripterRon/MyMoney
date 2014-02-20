package Report;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.print.*;

import javax.swing.*;
import javax.swing.event.*;

/**
 * The ReportPage class generates and prints the pages of the report.
 */
public final class ReportPage implements Pageable, Printable {

    /** The report state */
    private ReportState state;

    /** The buffered report pages */
    private List<PageState> reportPages;

    /** The page size */
    private Dimension pageSize = new Dimension(0, 0);

    /** The printable area */
    private Rectangle printArea = new Rectangle(0, 0, 0, 0);

    /** The current page index */
    private int pageIndex;

    /**
     * Build a new report
     *
     * @param       reportState     The report state
     */
    public ReportPage(ReportState reportState) {

        //
        // Save the report state
        //
        state = reportState;

        //
        // Create the report page list
        //
        reportPages = new ArrayList<PageState>(10);

        //
        // Set the print area based on the imageable area
        //
        PageFormat pageFormat = state.getPageFormat();
        pageSize.setSize(pageFormat.getWidth(), pageFormat.getHeight());
        printArea.setBounds((int)pageFormat.getImageableX(),
                            (int)pageFormat.getImageableY(),
                            (int)pageFormat.getImageableWidth(),
                            (int)pageFormat.getImageableHeight());
    }

    /**
     * Return the number of pages (Pageable interface)
     *
     * @return                      The number of pages in the print job
     */
    public int getNumberOfPages() {
        return reportPages.size();
    }

    /**
     * Return the page format for a page (Pageable interface).  We will use
     * the report page format for all pages.
     *
     * @param       pageIndex       The page index
     * @return                      The page format
     */
    public PageFormat getPageFormat(int pageIndex) {
        return state.getPageFormat();
    }

    /**
     * Return the painter for a page (Pageable interface).  We will handle
     * the painter ourself.
     *
     * @param       pageIndex       The page index
     * @return                      The printable object
     */
    public Printable getPrintable(int pageIndex) {
        return this;
    }

    /**
     * Paginate the report.
     *
     * @return                      The number of pages in the report
     */
    public int paginate() {
        Point p = new Point(0,0);
        DataRow dataRow = state.getDataRow();
        int expCount = state.getExpressionCount();
        int groupCount = state.getGroupCount()-1;
        ReportGroup defaultGroup = state.getGroup(groupCount);
        ReportBand band, rowBand, footerBand;
        ReportExpression exp;
        ReportEvent event;
        Rectangle bandArea, rowArea, footerArea;
        ReportGroup group;

        //
        // Clear existing report pages
        //
        reportPages.clear();

        //
        // Reset the data row
        //
        dataRow.resetRow();

        //
        // Reset the group values to indicate the group has not been started
        //
        for (int i=0; i<groupCount; i++)
            state.getGroup(i).setValue(null);

        //
        // Notify the report listeners that we are starting a new report
        //
        event = new ReportEvent(ReportEvent.REPORT_STARTED, state);
        for (int i=0; i<expCount; i++) {
            exp = state.getExpression(i);
            if (exp instanceof ReportListener)
                ((ReportListener)exp).reportStarted(event);
        }

        //
        // Start the first page (the default group header is suppressed since
        // we need to print the report header before we print the group header)
        //
        startPage(null, p, true);

        //
        // Print the report header
        //
        band = state.getReportHeader();
        if (band.getElementCount() != 0) {
            bandArea = band.getBounds();
            p.setLocation(p.x, p.y+bandArea.height);
        }

        //
        // Print the default group header
        //
        band = defaultGroup.getHeader();
        if (band.getElementCount() != 0) {
            bandArea = band.getBounds();
            p.setLocation(p.x, p.y+bandArea.height);
        }

        //
        // Indicate the default group has been started
        //
        defaultGroup.setValue(defaultGroup);

        //
        // Get the presentation rectangles for the report row and the page footer
        //
        rowBand = state.getRowBand();
        rowArea = rowBand.getBounds();

        footerBand = state.getPageFooter();
        footerArea = footerBand.getBounds();

        //
        // Process each data source row
        //
        while (dataRow.hasNext()) {

            //
            // Start a new group if the data column value has changed
            //
            for (int i=0; i<groupCount; i++) {
                group = state.getGroup(i);
                Object groupValue = group.getValue();
                Object columnValue = dataRow.getNextValue(group.getField());
                if (columnValue == null)
                    continue;

                if (groupValue == null || !groupValue.equals(columnValue)) {
                    if (groupValue != null)
                        finishGroup(group, null, p);

                    startGroup(group, null, p);
                    group.setValue(columnValue);
                }
            }

            //
            // Start a new page if there is not enough room on the current page
            //
            if (p.y+rowArea.height+footerArea.height > printArea.y+printArea.height) {
                finishPage(null);
                startPage(null, p, false);
            }

            //
            // Advance to the next report row
            //
            dataRow.nextRow();

            //
            // Notify the report listeners that we have advanced to the next row
            //
            event = new ReportEvent(ReportEvent.ROW_ADVANCED, state);
            for (int i=0; i<expCount; i++) {
                exp = state.getExpression(i);
                if (exp instanceof ReportListener)
                    ((ReportListener)exp).rowAdvanced(event);
            }

            //
            // Print the report row
            //
            p.setLocation(p.x, p.y+rowArea.height);
        }

        //
        // Close out any open groups before we print the report footer
        //
        for (int i=0; i<groupCount; i++) {
            group = state.getGroup(i);
            if (group.getValue() != null)
                finishGroup(group, null, p);
        }

        //
        // Print the default group footer
        //
        band = defaultGroup.getFooter();
        if (band.getElementCount() != 0) {
            bandArea = band.getBounds();
            if (p.y+bandArea.height+footerArea.height > printArea.y+printArea.height) {
                finishPage(null);
                startPage(null, p, false);
            }

            p.setLocation(p.x, p.y+bandArea.height);
        }

        //
        // Indicate the default group has been finished
        //
        defaultGroup.setValue(null);

        //
        // Print the report footer
        //
        band = state.getReportFooter();
        if (band.getElementCount() != 0) {
            bandArea = band.getBounds();
            if (p.y+bandArea.height+footerArea.height > printArea.y+printArea.height) {
                finishPage(null);
                startPage(null, p, false);
            }

            p.setLocation(p.x, p.y+bandArea.height);
        }

        //
        // Finish the last page
        //
        finishPage(null);

        //
        // Return the number of pages in the report
        //
        return reportPages.size();
    }

    /**
     * Print a page (Printable interface)
     *
     * @param       gc              The graphics context.  This must be a
     *                              Graphics2D context.
     * @param       pageFormat      The page format.  All pages use the same
     *                              page format.  Consequently, the page format
     *                              provided on the print() call is ignored.
     * @param       pageIndex       The index of the page to be printed.
     * @return                      PAGE_EXISTS or NO_SUCH_PAGE.
     */
    public int print(Graphics gc, PageFormat pageFormat, int pageIndex) {
        Graphics2D g = (Graphics2D)gc;
        Point p = new Point(0,0);
        ReportBand band, rowBand, footerBand;
        ReportExpression exp;
        ReportEvent event;
        Rectangle bandArea, rowArea, footerArea;
        ReportGroup group;

        //
        // Return an error if the page does not exist
        //
        if (pageIndex >= reportPages.size())
            return Printable.NO_SUCH_PAGE;

        //
        // Get the saved page state
        //
        this.pageIndex = pageIndex;
        PageState pageState = reportPages.get(pageIndex);
        DataRow dataRow = pageState.getDataRow();
        int expCount = pageState.getExpressionCount();
        int groupCount = pageState.getGroupCount()-1;
        ReportGroup defaultGroup = pageState.getGroup(groupCount);

        //
        // Use the saved data row
        //
        state.setPageDataRow(dataRow);

        //
        // Reset the data row for the current page
        //
        dataRow.resetRow();

        //
        // Reset the group values for the current page
        //
        for (int i=0; i<groupCount; i++)
            pageState.getGroup(i).resetValue();

        //
        // Reset the function values for the current page
        //
        for (int i=0; i<expCount; i++) {
            exp = pageState.getExpression(i);
            if (exp instanceof AbstractFunction)
                ((AbstractFunction)exp).resetValue();
        }

        //
        // Paint the page background
        //
        g.setPaint(Color.WHITE);
        g.fillRect(0, 0, pageSize.width, pageSize.height);

        //
        // Start the page.  We need to suppress the default group header
        // if this is the first page since we want to print the report
        // header before we print the group header.
        //
        startPage(g, p, (pageIndex==0));

        //
        // Print the report header followed by the default group header
        // if this is the first page
        //
        if (pageIndex == 0) {
            band = state.getReportHeader();
            if (band.getElementCount() != 0)
                band.format(g, p);

            band = defaultGroup.getHeader();
            if (band.getElementCount() != 0)
                band.format(g, p);
        }

        //
        // Get the presentation rectangles for the report row and the page footer
        //
        rowBand = state.getRowBand();
        rowArea = rowBand.getBounds();

        footerBand = state.getPageFooter();
        footerArea = footerBand.getBounds();

        //
        // Process each data source row until we reach the end of the page
        //
        while (dataRow.hasNext()) {

            //
            // Start a new group if the data column value has changed
            //
            for (int i=0; i<groupCount && g!=null; i++) {
                group = pageState.getGroup(i);
                Object groupValue = group.getValue();
                Object columnValue = dataRow.getNextValue(group.getField());
                if (columnValue == null)
                    continue;

                if (groupValue == null || !groupValue.equals(columnValue)) {
                    if (groupValue != null) {
                        g = finishGroup(group, g, p);
                        if (g == null)
                            break;
                    }

                    g = startGroup(group, g, p);
                    if (g == null)
                        break;

                    group.setValue(columnValue);
                }
            }

            //
            // Stop now if we have finished the current page
            //
            if (g == null)
                break;

            //
            // Start a new page if there is not enough room on the current page
            //
            if (p.y+rowArea.height+footerArea.height > printArea.y+printArea.height) {
                finishPage(g);
                g = null;
                break;
            }

            //
            // Advance to the next report row
            //
            dataRow.nextRow();

            //
            // Notify the report listeners that we have advanced to the next row
            //
            event = new ReportEvent(ReportEvent.ROW_ADVANCED, state);
            for (int i=0; i<expCount; i++) {
                exp = pageState.getExpression(i);
                if (exp instanceof ReportListener)
                    ((ReportListener)exp).rowAdvanced(event);
            }

            //
            // Print the data row
            //
            rowBand.format(g, p);
        }

        //
        // Close out any open groups before we print the report footer
        //
        for (int i=0; i<groupCount && g!=null; i++) {
            group = pageState.getGroup(i);
            if (group.getValue() != null)
                g = finishGroup(group, g, p);
        }

        //
        // Print the default group footer
        //
        if (g != null && defaultGroup.getValue() != null) {
            band = defaultGroup.getFooter();
            if (band.getElementCount() != 0) {
                bandArea = band.getBounds();
                if (p.y+bandArea.height+footerArea.height > printArea.y+printArea.height) {
                    finishPage(g);
                    g = null;
                } else {
                    band.format(g, p);
                }
            }
        }

        //
        // Print the report footer
        //
        if (g != null) {
            band = state.getReportFooter();
            if (band.getElementCount() != 0) {
                bandArea = band.getBounds();
                if (p.y+bandArea.height+footerArea.height > printArea.y+printArea.height) {
                    finishPage(g);
                    g = null;
                } else {
                    band.format(g, p);
                }
            }
        }

        //
        // Finish the last page
        //
        if (g != null)
            finishPage(g);

        //
        // Stop using the saved data row
        //
        state.setPageDataRow(null);

        //
        // All done
        //
        return Printable.PAGE_EXISTS;
    }

    /**
     * Start a new page of the report.
     *
     * During the report pagination phase, the page state will be saved.
     * This saved state is then used when we need to display a report page.
     *
     * The page position will be set to the first line of the imageable area
     * and then the page header will be printed followed by the default group
     * header.
     *
     * @param       g               The graphics context or null
     * @param       p               The page position
     * @param       suppressGroup   TRUE to suppress the default group header
     */
    private void startPage(Graphics2D g, Point p, boolean suppressGroup) {
        ReportBand band;
        Rectangle bandArea;

        //
        // Save the page state if this is the pagination phase
        //
        if (g == null) {
            PageState pageState = new PageState((DataRow)state.getDataRow().clone());

            int expCount = state.getExpressionCount();
            for (int i=0; i<expCount; i++)
                pageState.addExpression(
                        (ReportExpression)((AbstractExpression)state.getExpression(i)).clone());

            int groupCount = state.getGroupCount();
            for (int i=0; i<groupCount; i++)
                pageState.addGroup((ReportGroup)state.getGroup(i).clone());

            reportPages.add(pageState);
        }

        //
        // Notify the report listeners that we are starting a new page
        //
        ReportEvent event = new ReportEvent(ReportEvent.PAGE_STARTED, state);
        int expCount;
        ReportExpression exp;
        if (g == null) {
            expCount = state.getExpressionCount();
            for (int i=0; i<expCount; i++) {
                exp = state.getExpression(i);
                if (exp instanceof ReportListener)
                    ((ReportListener)exp).pageStarted(event);
            }
        } else {
            PageState pageState = reportPages.get(pageIndex);
            expCount = pageState.getExpressionCount();
            for (int i=0; i<expCount; i++) {
                exp = pageState.getExpression(i);
                if (exp instanceof ReportListener)
                    ((ReportListener)exp).pageStarted(event);
            }
        }

        //
        // Set the page position to the first line of the print area
        //
        p.setLocation(printArea.x, printArea.y);

        //
        // Print the page header
        //
        band = state.getPageHeader();
        if (band.getElementCount() != 0) {
            if (g != null) {
                band.format(g, p);
            } else {
                bandArea = band.getBounds();
                p.setLocation(p.x, p.y+bandArea.height);
            }
        }

        //
        // Print the default group header unless it is suppressed
        //
        if (!suppressGroup) {
            band = state.getGroup(state.getGroupCount()-1).getHeader();
            if (band.getElementCount() != 0) {
                if (g != null) {
                    band.format(g, p);
                } else {
                    bandArea = band.getBounds();
                    p.setLocation(p.x, p.y+bandArea.height);
                }
            }
        }
    }

    /**
     * Finish the current page of the report.  The page footer will be printed
     * at the bottom of the page.
     *
     * @param       g               The graphics context or null
     */
    private void finishPage(Graphics2D g) {

        //
        // Notify the report listeners that we are at the end of the current page
        //
        ReportEvent event = new ReportEvent(ReportEvent.PAGE_FINISHED, state);
        int expCount;
        ReportExpression exp;
        if (g != null) {
            PageState pageState = reportPages.get(pageIndex);
            expCount = pageState.getExpressionCount();
            for (int i=0; i<expCount; i++) {
                exp = pageState.getExpression(i);
                if (exp instanceof ReportListener)
                    ((ReportListener)exp).pageFinished(event);
            }
        } else {
            expCount = state.getExpressionCount();
            for (int i=0; i<expCount; i++) {
                exp = state.getExpression(i);
                if (exp instanceof ReportListener)
                    ((ReportListener)exp).pageFinished(event);
            }
        }

        //
        // Print the page footer
        //
        if (g != null) {
            ReportBand band = state.getPageFooter();
            if (band.getElementCount() != 0) {
                Rectangle bandArea = band.getBounds();
                Point p = new Point(printArea.x, printArea.y+printArea.height-bandArea.height);
                band.format(g, p);
            }
        }
    }

    /**
     * Start a new group.  The report listeners will be notified that a new
     * group is starting and then the group header will be printed.  The
     * returned graphics context will be null if a new page needs to be started
     * in order to print the group header (the graphics context is always null
     * during the report pagination phase).
     *
     * @param       group           The report group
     * @param       g               The graphics context or null
     * @param       p               The page position
     * @return                      The graphics context or null
     */
    private Graphics2D startGroup(ReportGroup group, Graphics2D g, Point p) {

        //
        // Notify the report listeners that we are starting a new group
        //
        ReportEvent event = new ReportEvent(ReportEvent.GROUP_STARTED, state);
        event.setGroup(group);
        int expCount;
        ReportExpression exp;
        if (g != null) {
            PageState pageState = reportPages.get(pageIndex);
            expCount = pageState.getExpressionCount();
            for (int i=0; i<expCount; i++) {
                exp = pageState.getExpression(i);
                if (exp instanceof ReportListener)
                    ((ReportListener)exp).groupStarted(event);
            }
        } else {
            expCount = state.getExpressionCount();
            for (int i=0; i<expCount; i++) {
                exp = state.getExpression(i);
                if (exp instanceof ReportListener)
                    ((ReportListener)exp).groupStarted(event);
            }
        }

        //
        // Print the group header
        //
        ReportBand band = group.getHeader();
        if (band.getElementCount() != 0) {
            Rectangle bandArea = band.getBounds();
            Rectangle footerArea = state.getPageFooter().getBounds();
            if (g == null) {
                if (p.y+bandArea.height+footerArea.height > printArea.y+printArea.height) {
                    finishPage(null);
                    startPage(null, p, false);
                }

                p.setLocation(p.x, p.y+bandArea.height);
            } else {
                if (p.y+bandArea.height+footerArea.height > printArea.y+printArea.height) {
                    finishPage(g);
                    g = null;
                } else {
                    band.format(g, p);
                }
            }
        }

        return g;
    }

    /**
     * Finish a group.  The report listeners will be notified that a group
     * is finished and then the group footer will be printed.  The
     * returned graphics context will be null if a new page needs to be started
     * in order to print the group footer (the graphics context is always null
     * during the report pagination phase).  The group value will be set to null
     * if the group footer is successfully printed.
     *
     * @param       group           The report group
     * @param       g               The graphics context or null
     * @param       p               The page position
     * @return                      The graphics context or null
     */
    private Graphics2D finishGroup(ReportGroup group, Graphics2D g, Point p) {

        //
        // Notify the report listeners that the current group has ended
        //
        ReportEvent event = new ReportEvent(ReportEvent.GROUP_FINISHED, state);
        event.setGroup(group);
        int expCount;
        ReportExpression exp;
        if (g != null) {
            PageState pageState = reportPages.get(pageIndex);
            expCount = pageState.getExpressionCount();
            for (int i=0; i<expCount; i++) {
                exp = pageState.getExpression(i);
                if (exp instanceof ReportListener)
                    ((ReportListener)exp).groupFinished(event);
            }
        } else {
            expCount = state.getExpressionCount();
            for (int i=0; i<expCount; i++) {
                exp = state.getExpression(i);
                if (exp instanceof ReportListener)
                    ((ReportListener)exp).groupFinished(event);
            }
        }

        //
        // Print the group footer
        //
        ReportBand band = group.getFooter();
        if (band.getElementCount() != 0) {
            Rectangle bandArea = band.getBounds();
            Rectangle footerArea = state.getPageFooter().getBounds();
            if (g == null) {
                if (p.y+bandArea.height+footerArea.height > printArea.y+printArea.height) {
                    finishPage(null);
                    startPage(null, p, false);
                }

                p.setLocation(p.x, p.y+bandArea.height);
                group.setValue(null);
            } else {
                if (p.y+bandArea.height+footerArea.height > printArea.y+printArea.height) {
                    finishPage(g);
                    g = null;
                } else {
                    band.format(g, p);
                    group.setValue(null);
                }
            }
        }

        return g;
    }

    /**
     * The page state contains the report state at the beginning of a page
     */
    private class PageState {

        /** Data row */
        private DataRow dataRow;

        /** Groups */
        private List<ReportGroup> groups;

        /** Functions */
        private List<ReportExpression> expressions;

        /**
         * Construct a new page state.
         *
         * @param   dataRow         The data row for the page state
         */
        public PageState(DataRow dataRow) {
            this.dataRow = dataRow;
            expressions = new ArrayList<ReportExpression>(10);
            groups = new ArrayList<ReportGroup>(5);
        }

        /**
         * Return the data row
         *
         * @return                      The data row
         */
        public DataRow getDataRow() {
            return dataRow;
        }

        /**
         * Return the number of report groups
         *
         * @return                      The number of groups
         */
        public int getGroupCount() {
            return groups.size();
        }

        /**
         * Return the group at the specified index
         *
         * @param       groupIndex      The group index
         * @return                      The report group
         */
        public ReportGroup getGroup(int groupIndex) {
            if (groupIndex < 0 || groupIndex >= groups.size())
                throw new IndexOutOfBoundsException("Group index "+groupIndex+" is not valid");

            return groups.get(groupIndex);
        }

        /**
         * Add a group to the report
         *
         * @param       group           The report group to add
         */
        public void addGroup(ReportGroup group) {
            if (group == null)
                throw new NullPointerException("No group provided");

            groups.add(group);
        }

        /**
         * Return the number of report expressions and functions
         *
         * @return                      The number of expressions and functions
         */
        public int getExpressionCount() {
            return expressions.size();
        }

        /**
         * Return the expression/function at the specified index
         *
         * @param       expIndex        The expression/function index
         * @return                      The expression/function
         */
        public ReportExpression getExpression(int expIndex) {
            if (expIndex < 0 || expIndex >= expressions.size())
                throw new IndexOutOfBoundsException("Expression index "+expIndex+" is not valid");

            return expressions.get(expIndex);
        }

        /**
         * Add a report expression or function to the page state
         *
         * @param   expression      The expression to add to the page state
         */
        public void addExpression(ReportExpression expression) {
            expressions.add(expression);
            dataRow.addExpression(expression);
        }

    }
}

