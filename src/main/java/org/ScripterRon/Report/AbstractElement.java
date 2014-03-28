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

import java.awt.*;
import java.awt.geom.*;

/**
 * The report elements provide the text that make up a report.  A report consists of
 * one or more report bands and each report band contains one or more report elements.
 * <p>
 * This is the base class for all report elements.  New report elements
 * must extend this class and override the getText() method.
 */
public abstract class AbstractElement implements ReportElement {

    /** The report element font */
    private Font font;

    /** The report element color */
    private Color color = Color.BLACK;

    /** The report element presentation rectangle */
    private Rectangle rectangle;

    /** The report element horizontal alignment */
    private int horizontalAlignment = ReportElement.LEFT_ALIGNMENT;

    /** The report element vertical alignment */
    private int verticalAlignment = ReportElement.CENTER_ALIGNMENT;

    /** The report state */
    private ReportState state;

    /**
     * Construct a new report element.
     */
    public AbstractElement() {
    }

    /**
     * Return the report element font.
     *
     * @return                      The report element font
     */
    public Font getFont() {
        if (font == null)
            return state.getDefaultFont();

        return font;
    }

    /**
     * Set the report element font.  The default report font will be used if
     * the font is null.
     *
     * @param       font            The report element font or null to use the
     *                              default report font
     */
    public void setFont(Font font) {
        this.font = font;
    }

    /**
     * Return the color used to render the report element text.
     *
     * @return                      The report element color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Set the color used to render the report element text.  Black will be
     * used if no color has been set.
     *
     * @param       color           The color
     */
    public void setColor(Color color) {
        if (color == null)
            throw new NullPointerException("No color provided");

        this.color = color;
    }

    /**
     * Return the report element presentation rectangle.  The coordinates are
     * in points (1/72 of an inch) and are relative to the current position on
     * the page such that (0,0) is the upper left corner of the current line.
     * The coordinates are mapped to the imageable area (that is, the margins
     * are not included).
     *
     * @return                      The presentation rectangle or null
     */
    public Rectangle getBounds() {
        return rectangle;
    }

    /**
     * Set the report element presentation rectangle.  The coordinates are
     * in points (1/72 of an inch) and are relative to the current position on
     * the page such that (0,0) is the upper left corner of the current line.
     * The presentation rectangle should be high enough to accomodate both
     * the text and the interline spacing.  The presentation rectangle must be
     * set before the report element is added to a report band and must not be
     * modified.  The coordinates are mapped to the imageable area (that is,
     * the margins are not included).
     *
     * @param       rectangle       The presentation rectangle
     */
    public void setBounds(Rectangle rectangle) {
        if (rectangle == null)
            throw new NullPointerException("No presentation rectangle provided");

        this.rectangle = rectangle;
    }

    /**
     * Get the horizontal alignment for the report element text.
     *
     * @return                      The horizontal alignment
     */
    public int getHorizontalAlignment() {
        return horizontalAlignment;
    }

    /**
     * Set the horizontal alignment for the report element text.  The valid
     * values are:
     * <ul>
     * <li>ReportElement.LEFT_ALIGNMENT
     * <li>ReportElement.CENTER_ALIGNMENT
     * <li>ReportElement.RIGHT_ALIGNMENT
     * </ul>
     * <p>The default is LEFT_ALIGNMENT.
     *
     * @param       alignment       The horizontal alignment
     */
    public void setHorizontalAlignment(int alignment) {
        if (alignment != ReportElement.LEFT_ALIGNMENT &&
                            alignment != ReportElement.CENTER_ALIGNMENT &&
                            alignment != ReportElement.RIGHT_ALIGNMENT)
            throw new IllegalArgumentException("Horizontal alignment "+alignment+" is not valid");

        horizontalAlignment = alignment;
    }

    /**
     * Get the vertical alignment for the report element text.
     *
     * @return                      The vertical alignment
     */
    public int getVerticalAlignment() {
        return verticalAlignment;
    }

    /**
     * Set the vertical alignment for the report element text.  The valid
     * values are:
     * <ul>
     * <li>ReportElement.TOP_ALIGNMENT
     * <li>ReportElement.CENTER_ALIGNMENT
     * <li>ReportElement.BOTTOM_ALIGNMENT
     * </ul>
     * <p>The default is CENTER_ALIGNMENT.
     *
     * @param       alignment       The vertical alignment
     */
    public void setVerticalAlignment(int alignment) {
        if (alignment != ReportElement.TOP_ALIGNMENT &&
                            alignment != ReportElement.CENTER_ALIGNMENT &&
                            alignment != ReportElement.BOTTOM_ALIGNMENT)
            throw new IllegalArgumentException("Vertical alignment "+alignment+" is not valid");

        verticalAlignment = alignment;
    }

    /**
     * Get the string representation of the report element.  This method must be
     * overridden by each report element subclass to provide the appropriate
     * string value.
     *
     * @return                      The text string
     */
    public abstract String getText();

    /**
     * Return the report state.  The report state is set for all elements during
     * report initialization and cannot be changed.
     *
     * @return                      The report state
     */
    protected ReportState getState() {
        return state;
    }

    /**
     * Set the report state.  The report state is set for all elements during
     * report initialization and cannot be changed.
     *
     * @param       state           The report state
     */
    void setState(ReportState state) {
        if (state == null)
            throw new NullPointerException("No report state provided");

        this.state = state;
    }
}

