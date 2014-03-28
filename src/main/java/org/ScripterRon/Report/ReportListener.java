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
 * A report listener receives notifications when the report state changes.
 */
public interface ReportListener {

    /**
     * The report has been initialized.  This method is called after all
     * initialization processing is completed and before any output is
     * generated.  This method is called just once for a given report.
     *
     * @param       event           The report event
     */
    public void reportInitialized(ReportEvent event);

    /**
     * The report has been started.  This method is called before
     * the report header is printed.  This method may be called multiple
     * times as the report is repaginated during preview and printing.
     * The report function should reset any variables to their initial states.
     *
     * @param       event           The report event
     */
    public void reportStarted(ReportEvent event);

    /**
     * A new page is starting.  This method is called before the
     * page header is printed.
     *
     * @param       event           The report event
     */
    public void pageStarted(ReportEvent event);

    /**
     * The current page is finished.  This method is called before
     * the page footer is printed.
     *
     * @param       event           The report event
     */
    public void pageFinished(ReportEvent event);

    /**
     * A new group is starting.  This method is called before the
     * group header is printed.  The current group can be obtained
     * from the report event.
     *
     * @param       event           The report event
     */
    public void groupStarted(ReportEvent event);

    /**
     * The current group is finished.  This method is called before the
     * group footer is printed.  The current group can be obtained
     * from the report event.
     *
     * @param       event           The report event
     */
    public void groupFinished(ReportEvent event);

    /**
     * The report has advanced to the next row.  This method is called
     * before the row is printed.
     *
     * @param       event           The report event
     */
    public void rowAdvanced(ReportEvent event);
}

