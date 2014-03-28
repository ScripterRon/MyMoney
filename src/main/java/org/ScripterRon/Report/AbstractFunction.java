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
 * This is the base class for all report functions.  New report functions
 * must extend this class and override the getValue() and resetValue() methods.
 * Default methods are provided for the various report events which will just
 * ignore the event.  These methods should be overridden for those report events
 * that are of interest to the report function.
 * <p>
 * A function is invoked each time a report event occurs or another function or
 * expression requests its value.
 * <p>
 * Report expressions are cloned at the start of each page of the report when the
 * report preview is displayed.  This allows a specific page to be displayed without
 * regenerating the preceding pages of the report.  The resetValue() method is called
 * before generating the page.  The function should reset its value to the value at
 * the time the function was cloned.
 */
public abstract class AbstractFunction extends AbstractExpression implements ReportListener {

    /** The group name for the function */
    private String groupName;

    /**
     * Construct a new function.  The function name is used to identify the function
     * and must not be the same as a data column name or the name of another expression
     * or function.
     *
     * @param       name            The function name
     */
    public AbstractFunction(String name) {
        super(name);
    }

    /**
     * Reset the function value when regenerating a report page.  The value
     * should be reset to the value at the time the function was cloned.
     */
    public abstract void resetValue();

    /**
     * The report has been initialized.  This method is called after all report
     * initialization processing is complete and before any output is
     * generated.  This method is called just once for a given report.
     *
     * @param       event           The report event
     */
    public void reportInitialized(ReportEvent event) {
    }

    /**
     * The report has been started.  This method is called before
     * the report header is printed.  This method may be called multiple
     * times as the report is repaginated during preview and printing.
     * The report function should reset any variables to their initial states.
     *
     * @param       event           The report event
     */
    public void reportStarted(ReportEvent event) {
    }

    /**
     * A new page is starting.  This method is called before the
     * page header is printed.
     *
     * @param       event           The report event
     */
    public void pageStarted(ReportEvent event) {
    }

    /**
     * The current page is finished.  This method is called before
     * the page footer is printed.
     *
     * @param       event           The report event
     */
    public void pageFinished(ReportEvent event) {
    }

    /**
     * A new group is starting.  This method is called before the
     * group header is printed.  The current group can be obtained
     * from the report event.
     *
     * @param       event           The report event
     */
    public void groupStarted(ReportEvent event) {
    }

    /**
     * The current group is finished.  This method is called before the
     * group footer is printed.  The current group can be obtained
     * from the report event.
     *
     * @param       event           The report event
     */
    public void groupFinished(ReportEvent event) {
    }

    /**
     * The report has advanced to the next row.  This method is called
     * before the row is printed.
     *
     * @param       event           The report event
     */
    public void rowAdvanced(ReportEvent event) {
    }

    /**
     * Return the group name for the function
     *
     * @return                      The group name or null if there is no group
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Set the group name for the function.  The groupStarted() and groupFinished()
     * methods will be called whenever the value changes for the group column.
     *
     * @param       groupName       The group name
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}

