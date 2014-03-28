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
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;

import javax.swing.*;
import javax.swing.event.*;

/**
 * A report consists of one or more report bands.  Each report band contains zero or
 * more report elements that provide the text for the report.  A report band with no
 * report elements will not be printed.  The report bands are created when a new
 * report or group is created.
 */
public final class ReportBand {

    /** The report elements in the band */
    private List<ReportElement> elements;

    /** The presentation rectangle for the band */
    private Rectangle rectangle = new Rectangle(0, 0, 0, 0);

    /** The background color for the band */
    private Color backgroundColor = Color.WHITE;

    /** The border color for the band */
    private Color borderColor = Color.WHITE;

    /**
     * Construct a new report band
     */
    ReportBand() {

        //
        // Create an empty element list
        //
        elements = new ArrayList<ReportElement>(5);
    }

    /**
     * Set the background color for the band.  The default color is WHITE.  The
     * report band background is a rectangle with a 2-point side margin and a 1-point
     * top margin.  The application should allow for this when specifying a color
     * other than WHITE.
     *
     * @param       color           The background color
     */
    public void setBackgroundColor(Color color) {
        backgroundColor = color;
    }

    /**
     * Return the background color for the band.
     *
     * @return                      The background color
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Set the border color for the band.  The default color is WHITE.  The
     * report band background is a rectangle with a 2-point side margin and a 1-point
     * top margin.  The application should allow for this when specifying a color
     * other than WHITE.
     *
     * @param       color           The border color
     */
    public void setBorderColor(Color color) {
        borderColor = color;
    }

    /**
     * Return the border color for the band.
     *
     * @return                      The border color
     */
    public Color getBorderColor(Color color) {
        return borderColor;
    }

    /**
     * Add a report element to the report band
     *
     * @param       element         The report element to add to the band
     */
    public void addElement(ReportElement element) {
        if (element == null)
            throw new NullPointerException("No report element provided");

        //
        // Add the report element presentation rectangle to the report band
        // presentation rectangle
        //
        Rectangle elemRectangle = element.getBounds();
        if (elemRectangle == null)
            throw new IllegalStateException("No presentation rectangle has been set");

        if (elements.size() == 0)
            rectangle.setBounds(elemRectangle);
        else
            rectangle = rectangle.union(elemRectangle);

        //
        // Add the report element to the band
        //
        elements.add(element);
    }

    /**
     * Return the number of report elements in the report band
     *
     * @return                      The element count
     */
    public int getElementCount() {
        return elements.size();
    }

    /**
     * Return the report element at the specified index
     *
     * @param       elementIndex    The element index
     * @return                      The report element
     */
    public ReportElement getElement(int elementIndex) {
        if (elementIndex < 0 || elementIndex >= elements.size())
            throw new IndexOutOfBoundsException("Element index "+elementIndex+" is not valid");

        return elements.get(elementIndex);
    }

    /**
     * Return the presentation rectangle for the band.
     *
     * @return                      The presentation rectangle
     */
    public Rectangle getBounds() {
        return rectangle;
    }

    /**
     * Draw the report elements in the band.  The page position will be updated
     * after all of the report elements have been rendered.  The caller is
     * reponsible for ensuring that there is sufficient room remaining on the
     * page as specified by the report band presentation rectangle before calling
     * this method.
     *
     * @param       g               The graphics context
     * @param       p               The current position on the page
     */
    public void format(Graphics2D g, Point p) {
        int x, y;

        //
        // Paint the report band background if its color is not WHITE.  The fill
        // rectangle will have a 2-point side margin and a 1-point top margin
        // to avoid overlaying the report element rectangles.
        //
        if (!backgroundColor.equals(Color.WHITE)) {
            x = Math.min(p.x, p.x+rectangle.x-2);
            y = Math.min(p.y, p.y+rectangle.y-1);
            g.setPaint(backgroundColor);
            g.fillRect(x, y, rectangle.width+3, rectangle.height+1);
        }

        //
        // Draw the report band border if its color is not WHITE.  The draw
        // rectangle will be slightly larger than the report band rectangle to
        // avoid overlaying the report element rectangles.
        //
        if (!borderColor.equals(Color.WHITE)) {
            x = Math.min(p.x, p.x+rectangle.x-2);
            y = Math.min(p.y, p.y+rectangle.y-1);
            g.setPaint(borderColor);
            g.drawRect(x, y, rectangle.width+3, rectangle.height+1);
        }

        //
        // Process each report element in the report band
        //
        for (ReportElement elem : elements) {

            //
            // Get the report element text.  We need to do this before setting
            // the graphics context since the report element may change its
            // font and/or color based on the text value.
            //
            String text = elem.getText();

            //
            // Set the graphics context
            //
            g.setPaint(elem.getColor());
            g.setFont(elem.getFont());
            FontMetrics fm = g.getFontMetrics();
            Rectangle elemArea = elem.getBounds();
            x = p.x+elemArea.x;
            y = p.y+elemArea.y;

            //
            // Trim the element text to fit within the report element
            // presentation rectangle
            //
            int width = fm.stringWidth(text);
            while (text.length() > 1 && width > elemArea.width) {
                text = text.substring(0, text.length()-1);
                width = fm.stringWidth(text);
            }

            //
            // Align the element text within the report element
            // presentation rectangle
            //
            switch (elem.getHorizontalAlignment()) {
                case ReportElement.LEFT_ALIGNMENT:
                    break;

                case ReportElement.RIGHT_ALIGNMENT:
                    x += elemArea.width-width;
                    break;

                case ReportElement.CENTER_ALIGNMENT:
                    x += elemArea.width/2-width/2;
                    break;
            }

            switch (elem.getVerticalAlignment()) {
                case ReportElement.TOP_ALIGNMENT:
                    y += fm.getAscent();
                    break;

                case ReportElement.BOTTOM_ALIGNMENT:
                    y += elemArea.height-fm.getDescent()-fm.getLeading();
                    break;

                case ReportElement.CENTER_ALIGNMENT:
                    y += Math.max(elemArea.height/2, fm.getAscent());
                    break;
            }

            //
            // Draw the report element text
            //
            g.drawString(text, x, y);
        }

        //
        // Advance to the next line on the page based on the height of the
        // report band presentation rectangle
        //
        p.setLocation(p.x, p.y+rectangle.height);
    }
}

