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
 * ReportEvent describes a report event
 */
public class ReportEvent {

    /**
     * This event is posted after the report has been initialized and
     * before any output has been generated.
     */
    public static final int REPORT_INITIALIZED = 1;

    /**
     * This event is posted before the report header is printed.
     */
    public static final int REPORT_STARTED = 2;

    /**
     * This event is posted before the page header is printed.
     */
    public static final int PAGE_STARTED = 3;

    /**
     * This event is posted before the page footer is printed.
     */
    public static final int PAGE_FINISHED = 4;

    /**
     * This event is posted before the group header is printed.
     */
    public static final int GROUP_STARTED = 5;

    /**
     * This event is posted before the group footer is printed.
     */
    public static final int GROUP_FINISHED = 6;

    /**
     * This event is posted when advancing to a new report row.
     */
    public static final int ROW_ADVANCED = 7;

    /** The event type */
    private int eventType;

    /** The report state */
    private ReportState reportState;

    /** The event group */
    private ReportGroup eventGroup;

    /**
     * Construct a new report event
     *
     * @param       eventType       The event type
     * @param       reportState     The report state
     */
    public ReportEvent(int eventType, ReportState reportState) {
        if (reportState == null)
            throw new NullPointerException("No report state provided");

        this.eventType = eventType;
        this.reportState = reportState;
    }

    /**
     * Return the event type
     *
     * @return                      The event type
     */
    public int getType() {
        return eventType;
    }

    /**
     * Return the report state
     *
     * @return                      The report state
     */
    public ReportState getState() {
        return reportState;
    }

    /**
     * Return the event group.  This is meaningful only for the GROUP_STARTED
     * and GROUP_FINISHED events.
     *
     * @return                      The event group or null if there is no group
     */
    public ReportGroup getGroup() {
        return eventGroup;
    }

    /**
     * Set the event group.
     *
     * @param       group           The event group
     */
    public void setGroup(ReportGroup group) {
        eventGroup = group;
    }
}

