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
package org.ScripterRon.Chart;

import java.util.Date;

/**
 * The TimeChartElement class describes the data points for a time chart.
 */
public class TimeChartElement {

    /** The element date */
    private Date date;

    /** The element value */
    private double value;

    /**
     * Create a new time chart element for the current date with a value of 0.
     */
    public TimeChartElement() {
        date = new Date();
        value = 0.0;
    }

    /**
     * Create a new time chart element for the specified date and value
     *
     * @param       date            The date for the data point
     * @param       value           The value for the data point
     */
    public TimeChartElement(Date date, double value) {
        if (date == null)
            throw new NullPointerException("No date supplied");

        this.date = date;
        this.value = value;
    }

    /**
     * Set the date for the data point
     *
     * @param       date            Date
     */
    public void setDate(Date date) {
        if (date == null)
            throw new NullPointerException("No date supplied");

        this.date = date;
    }

    /**
     * Get the date for the data point
     *
     * @return                      Date
     */
    public Date getDate() {
        return date;
    }

    /**
     * Set the value for the data point
     *
     * @param       value           Value
     */
    public void setValue(double value) {
        this.value = value;
    }

    /**
     * Get the value for the data point
     *
     * @return                      Value
     */
    public double getValue() {
        return value;
    }

    /**
     * Return a hash code for the data point
     *
     * @return                      Hash code
     */
    public int hashCode() {
        long bits = Double.doubleToLongBits(getValue()) ^ getDate().getTime();
        return (int)bits ^ (int)(bits>>32);
    }

    /**
     * Determine if two data points are equal.  The data points are equal
     * if they have the same date and value.
     */
    public boolean equals(Object object) {
        boolean retValue = false;

        if (this == object) {
            retValue = true;
        } else if (object instanceof TimeChartElement) {
            if (getDate().equals(((TimeChartElement)object).getDate()) &&
                        getValue() == ((TimeChartElement)object).getValue())
                retValue = true;
        }

        return retValue;
    }
}

