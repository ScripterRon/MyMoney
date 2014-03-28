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
package org.ScripterRon.Report;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import java.awt.*;
import java.awt.print.*;

/**
 * The report state contains the report definitions and status.  It is available to
 * report elements, expressions and functions through the getState() method for each
 * class.
 * <p>
 * The application obtains the report state after creating a new report through the
 * Report.getState() method.  The application should then add report elements to the
 * header and footer bands if desired.  The application must add at least one report
 * element to the row band before a report can be generated.
 */
public final class ReportState {

    /** Report title */
    private String reportTitle;

    /** Report data row */
    private DataRow dataRow;

    /** Page data row */
    private DataRow pageDataRow;

    /** Report groups */
    private List<ReportGroup> groups;

    /** Report expressions and functions */
    private List<ReportExpression> expressions;

    /** The report header band */
    private ReportBand reportHeader;

    /** The report footer band */
    private ReportBand reportFooter;

    /** The page header band */
    private ReportBand pageHeader;

    /** The page footer band */
    private ReportBand pageFooter;

    /** The row band */
    private ReportBand rowBand;

    /** The page format */
    private PageFormat pageFormat;

    /** The default report font */
    private Font defaultFont;

    /**
     * Construct a new report state
     *
     * @param       reportTitle     The report title
     * @param       dataRow         The report data row
     */
    public ReportState(String reportTitle, DataRow dataRow) {
        if (reportTitle == null)
            throw new NullPointerException("No report title provided");

        if (dataRow == null)
            throw new NullPointerException("No data row provided");

        this.reportTitle = reportTitle;
        this.dataRow = dataRow;

        //
        // Create empty lists for the groups and expressions
        //
        groups = new ArrayList<ReportGroup>(5);
        expressions = new ArrayList<ReportExpression>(10);

        //
        // Create empty report bands
        //
        reportHeader = new ReportBand();
        reportFooter = new ReportBand();
        pageHeader = new ReportBand();
        pageFooter = new ReportBand();
        rowBand = new ReportBand();

        //
        // Create the default page format
        //
        pageFormat = new PageFormat();

        //
        // Set the default report font (10-point SansSerif)
        //
        defaultFont = new Font("SansSerif", Font.PLAIN, 10);

        //
        // Create the default group
        //
        groups.add(new ReportGroup());
    }

    /**
     * Return the report title
     *
     * @return                      The report title
     */
    public String getReportTitle() {
        return reportTitle;
    }

    /**
     * Return the data row.  The page data row will be returned if we have one,
     * otherwise the report data row will be returned.
     *
     * @return                      The data row
     */
    public DataRow getDataRow() {
        return (pageDataRow!=null ? pageDataRow : dataRow);
    }

    /**
     * Set the page data row.  The page data row overrides the report data row
     * and is used when displaying a saved report page.
     *
     * @param       dataRow         The page data row
     */
    public void setPageDataRow(DataRow dataRow) {
        pageDataRow = dataRow;
    }

    /**
     * Return the default report font
     *
     * @return                      The default font
     */
    public Font getDefaultFont() {
        return defaultFont;
    }

    /**
     * Set the default report font
     *
     * @param       defaultFont     The default font
     */
    public void setDefaultFont(Font defaultFont) {
        if (defaultFont == null)
            throw new NullPointerException("No default font provided");

        this.defaultFont = defaultFont;
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
     * Add a group to the report.  The group is added before the default group
     * so that the default group is always the last group in the group list.
     *
     * @param       group           The report group to add
     */
    public void addGroup(ReportGroup group) {
        if (group == null)
            throw new NullPointerException("No group provided");

        groups.add(groups.size()-1, group);
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
     * Add an expression to the report.  The dependency level determines the position
     * of the expression in the list.  Higher dependency levels are added before
     * lower dependency levels.
     *
     * @param       expression      The expression to add to the report state
     */
    public void addExpression(ReportExpression expression) {
        if (expression == null)
            throw new NullPointerException("No expression provided");

        //
        // Add the expression to the report state
        //
        int index = 0;
        int level = expression.getDependencyLevel();
        boolean expInserted = false;
        for (ReportExpression exp : expressions) {
            if (level > exp.getDependencyLevel()) {
                expressions.add(index, expression);
                expInserted = true;
                break;
            }

            index++;
        }

        if (!expInserted)
            expressions.add(expression);

        //
        // Add the expression to the data row
        //
        dataRow.addExpression(expression);
    }

    /**
     * Return the report header
     *
     * @return                      The report header band
     */
    public ReportBand getReportHeader() {
        return reportHeader;
    }

    /**
     * Return the report footer
     *
     * @return                      The report footer band
     */
    public ReportBand getReportFooter() {
        return reportFooter;
    }

    /**
     * Return the page header
     *
     * @return                      The page header band
     */
    public ReportBand getPageHeader() {
        return pageHeader;
    }

    /**
     * Return the page footer
     *
     * @return                      The page footer band
     */
    public ReportBand getPageFooter() {
        return pageFooter;
    }

    /**
     * Return the row band
     *
     * @return                      The row band
     */
    public ReportBand getRowBand() {
        return rowBand;
    }

    /**
     * Return the page format
     *
     * @return                      The page format
     */
    public PageFormat getPageFormat() {
        return pageFormat;
    }

    /**
     * Set the page format
     *
     * @param       pageFormat      The page format
     */
    public void setPageFormat(PageFormat pageFormat) {
        if (pageFormat == null)
            throw new NullPointerException("No page format provided");

        this.pageFormat = pageFormat;
    }
}

