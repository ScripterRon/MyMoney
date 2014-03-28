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

/**
 * The LineChartElement class describes the data points for a line chart.
 */
public class LineChartElement {

    /** The X-coordinate value */
    private double x;

    /** The Y-coordinate value */
    private double y;

    /**
     * Create a new line chart element initialized to (0,0)
     */
    public LineChartElement() {
        x = 0.0;
        y = 0.0;
    }

    /**
     * Create a new line chart element initialized to (x,y)
     *
     * @param       x               Initial value for X
     * @param       y               Initial value for Y
     */
    public LineChartElement(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Set the value for X
     *
     * @param       x               X value
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Get the current value for X
     *
     * @return                      X value
     */
    public double getX() {
        return x;
    }

    /**
     * Set the value for Y
     *
     * @param       y               Y value
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Get the current value for Y
     *
     * @return                      Y value
     */
    public double getY() {
        return y;
    }

    /**
     * Set the X and Y values
     *
     * @param       x               X value
     * @param       y               Y value
     */
    public void setXY(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Return a hash code for the data point
     *
     * @return                      Hash code
     */
    public int hashCode() {
        long bits = Double.doubleToLongBits(getX()) ^ Double.doubleToLongBits(getY());
        return (int)bits ^ (int)(bits>>32);
    }

    /**
     * Determine if two data points are equal.  The data points are equal
     * if they have the same X and Y values.
     */
    public boolean equals(Object object) {
        boolean retValue = false;

        if (this == object) {
            retValue = true;
        } else if (object instanceof LineChartElement) {
            if (getX() == ((LineChartElement)object).getX() &&
                        getY() == ((LineChartElement)object).getY())
                retValue = true;
        }

        return retValue;
    }
}

