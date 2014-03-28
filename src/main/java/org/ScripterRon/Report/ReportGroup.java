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

/**
 * A report group consists of report rows with the same value for a data column.
 * In order to use a report group, the report data must be pre-sorted such that
 * all of the rows with the same value for the group column are adjacent in the
 * report.  A report event will be posted whenever the group column value changes.
 * <p>
 * The default group is a group without a field name.  This group starts with the
 * first report row and ends after the last report row.  The group header for the
 * default group will be printed at the top of each page of the report and the
 * group footer will be printed after the last report row.
 * <p>
 * Groups are cloned at the start of each report page when the report preview
 * is displayed.  This allows a specific page to be displayed without regenerating
 * the preceding pages of the report.
 */
public final class ReportGroup implements Cloneable {

    /** The group name */
    private String groupName;

    /** The field name */
    private String fieldName;

    /** The group header band */
    private ReportBand groupHeader;

    /** The group footer band */
    private ReportBand groupFooter;

    /** The current value of the group column */
    private Object groupValue;

    /** The starting value of the group column */
    private Object startGroupValue;

    /**
     * Construct the default report group.  This is an unnamed group which
     * is not associated with a data column.  The default group is created
     * when the report state is created.
     */
    ReportGroup() {

        //
        // Create empty header and footer bands
        //
        groupHeader = new ReportBand();
        groupFooter = new ReportBand();
    }

    /**
     * Construct a new report group.  The field name must be a column name
     * defined in the report model.  A new group will be started whenever
     * the value of this column changes.
     *
     * @param       groupName       The group name
     * @param       fieldName       The field name
     */
    public ReportGroup(String groupName, String fieldName) {
        if (groupName == null)
            throw new NullPointerException("No group name provided");

        if (fieldName == null)
            throw new NullPointerException("No field name provided");

        //
        // Set the group and field names
        //
        this.groupName = groupName;
        this.fieldName = fieldName;

        //
        // Create empty header and footer bands
        //
        groupHeader = new ReportBand();
        groupFooter = new ReportBand();
    }

    /**
     * Return the group name
     *
     * @return                      The group name or null for the default group
     */
    public String getName() {
        return groupName;
    }

    /**
     * Return the field name
     *
     * @return                      The field name or null for the default group
     */
    public String getField() {
        return fieldName;
    }

    /**
     * Return the group header band
     *
     * @return                      The group header band
     */
    public ReportBand getHeader() {
        return groupHeader;
    }

    /**
     * Return the group footer band
     *
     * @return                      The group footer band
     */
    public ReportBand getFooter() {
        return groupFooter;
    }

    /**
     * Reset the group value
     */
    public void resetValue() {
        groupValue = startGroupValue;
    }

    /**
     * Return the current group column value
     *
     * @return                      The column value
     */
    public Object getValue() {
        return groupValue;
    }

    /**
     * Set the current group column value
     *
     * @param       groupValue      The column value
     */
    public void setValue(Object groupValue) {
        this.groupValue = groupValue;
    }

    /**
     * Clone the report group
     *
     * @return                      The cloned group
     */
    public Object clone() {
        Object clonedObject;

        try {
            clonedObject = super.clone();
            ReportGroup clonedGroup = (ReportGroup)clonedObject;
            clonedGroup.startGroupValue = clonedGroup.groupValue;
        } catch (CloneNotSupportedException exc) {
            throw new UnsupportedOperationException("Unable to clone report group", exc);
        }

        return clonedObject;
    }
}

