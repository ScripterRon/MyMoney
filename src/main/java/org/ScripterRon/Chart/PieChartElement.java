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

import java.awt.Color;

/**
 * The PieChartElement class describes the data points for a pie chart.  The
 * absolute value is used when generating a pie chart.
 */
public class PieChartElement {

    /** The element color */
    private Color color;

    /** The element label */
    private String label;

    /** The element value */
    private double value;

    /**
     * Create a new pie chart element with the color set to black, no label,
     * and a value of 0.0
     */
    public PieChartElement() {
        color = Color.BLACK;
        label = null;
        value = 0.0;
    }

    /**
     * Create a new pie chart element for the specified color, label and value
     *
     * @param       color           The color of the pie section
     * @param       label           The label for the pie section
     * @param       value           The value for the pie section
     */
    public PieChartElement(Color color, String label, double value) {
        if (color == null)
            throw new NullPointerException("No color supplied");

        this.color = color;
        this.label = label;
        this.value = Math.abs(value);
    }

    /**
     * Set the color for the pie section
     *
     * @param       color           The section color
     */
    public void setColor(Color color) {
        if (color == null)
            throw new NullPointerException("No color supplied");

        this.color = color;
    }

    /**
     * Get the color for the pie section
     *
     * @return                      The section color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Set the label for the pie section
     *
     * @param       label           The section label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Get the label for the pie section
     *
     * @return                      The section label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Set the value for the data point
     *
     * @param       value           The section value
     */
    public void setValue(double value) {
        this.value = Math.abs(value);
    }

    /**
     * Get the value for the data point
     *
     * @return                      The section value
     */
    public double getValue() {
        return value;
    }
}

