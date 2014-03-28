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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.Format;
import java.text.MessageFormat;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

/**
 * The PieChart class provides a pie chart.  The pie sections are defined by a
 * PieChartElement list.  Absolute values are used when determining the size of
 * each section of the pie.
 *
 * <p>The chartModified() method should be called if the data point list is modified.
 * This will cause the chart to be redrawn with the new data.
 *
 * <p>The background color is used to paint the chart background if the component
 * is opaque.  The foreground color is used to draw the plot and associated labels.
 * The section colors are used to fill in each section of the pie.
 *
 * <p>The setTitle() method should be called if a chart title is desired.
 */
public class PieChart extends JComponent {

    /** Data point list */
    private List<PieChartElement> dataPoints;

    /** Chart title */
    private String chartTitle;

    /** Format pattern */
    private String labelPattern;

    /** Label background color */
    private Color labelBackground = new Color(255, 255, 196);

    /**
     * Construct a pie chart using a data point list
     *
     * @param       dataPoints      List of data points
     */
    public PieChart(List<PieChartElement> dataPoints) {
        super();

        if (dataPoints == null)
            throw new NullPointerException("No data point list provided");

        this.dataPoints = dataPoints;
    }

    /**
     * Construct a pie chart using a data point list and a label pattern.  Refer
     * to the setLabelPattern() method for more information on the label pattern.
     *
     * @param       dataPoints      List of data points
     * @param       labelPattern    The format pattern for the section labels
     */
    public PieChart(List<PieChartElement> dataPoints, String labelPattern) {
        super();

        if (dataPoints == null)
            throw new NullPointerException("No data point list provided");

        this.dataPoints = dataPoints;
        this.labelPattern = labelPattern;
    }

    /**
     * Get the chart data points
     *
     * @return                      List of data points
     */
    public List<PieChartElement> getDataPoints() {
        return dataPoints;
    }

    /**
     * Set the chart title
     *
     * @param       title           The chart title
     */
    public void setTitle(String title) {
        chartTitle = title;
    }

    /**
     * Get the chart title
     *
     * @return                      The chart title or null if there is no title
     */
    public String getTitle() {
        return chartTitle;
    }

    /**
     * Set the label format pattern for the pie sections.  Refer to the MessageFormat
     * class for more information about message formatting.  The default is to display
     * just the section label.
     *
     * <p>Three substitution parameters are supplied when the label is formatted:
     * <ul>
     * <li> Argument 0 - The section label as a string value
     * <li> Argument 1 - The section value as a double value
     * <li> Argument 2 - The section percentage as a string value
     * </ul>
     *
     * @param       labelPattern    The label format pattern
     */
    public void setLabelPattern(String labelPattern) {
        this.labelPattern = labelPattern;
    }

    /**
     * Get the label format pattern for the pie sections.
     *
     * @return                      The label format pattern
     */
    public String getLabelPattern() {
        return labelPattern;
    }

    /**
     * Set the label background color.  The default is Color(255, 255, 196).
     *
     * @param       color           The label background color
     */
    public void setLabelBackground(Color color) {
        if (color == null)
            throw new NullPointerException("No label background color supplied");

        labelBackground = color;
    }

    /**
     * Get the label background color
     *
     * @return                      The label background color
     */
    public Color getLabelBackground() {
        return labelBackground;
    }

    /**
     * Indicate that the chart data has been modified.  This will cause the
     * chart to be redrawn.
     */
    public void chartModified() {
        repaint();
    }

    /**
     * Return the preferred size for this component.  If no preferred size has
     * been set, the size will be computed based on the data point list and the
     * component font.
     */
    public Dimension getPreferredSize() {
        int height = 0;
        int width = 0;

        //
        // Use the size set by the application
        //
        if (isPreferredSizeSet())
            return super.getPreferredSize();

        //
        // Leave room for the component border
        //
        Border border = getBorder();
        if (border != null) {
            Insets insets = border.getBorderInsets(this);
            width += insets.left + insets.right;
            height += insets.top + insets.bottom;
        }

        //
        // Get the label font
        //
        Font font = getFont();
        if (font == null)
            font = new Font("Dialog", Font.PLAIN, 8);

        //
        // Leave room for the chart title
        //
        if (chartTitle != null && chartTitle.length() != 0) {
            Font titleFont = font.deriveFont(Font.BOLD, font.getSize()+10);
            height += getFontMetrics(titleFont).getHeight();
        }

        //
        // Leave room for the pie chart
        //
        FontMetrics fm = getFontMetrics(font);
        int labelHeight = fm.getHeight();
        int labelWidth = fm.stringWidth("mmmmmmmmmmmmmmmm");
        width += 200 + 2*labelWidth;
        height += 200 + 4*labelHeight;
        return new Dimension(width, Math.max(height, dataPoints.size()*labelHeight));
    }

    /**
     * Return the minimum size for this component.  If no minimum size has been
     * set, the size will be computed based on the data point list and the
     * component font.
     */
    public Dimension getMinimumSize() {
        int height = 0;
        int width = 0;

        //
        // Use the size set by the application
        //
        if (isPreferredSizeSet())
            return super.getPreferredSize();

        //
        // Leave room for the component border
        //
        Border border = getBorder();
        if (border != null) {
            Insets insets = border.getBorderInsets(this);
            width += insets.left + insets.right;
            height += insets.top + insets.bottom;
        }

        //
        // Get the label font
        //
        Font font = getFont();
        if (font == null)
            font = new Font("Dialog", Font.PLAIN, 8);

        //
        // Leave room for the chart title
        //
        if (chartTitle != null && chartTitle.length() != 0) {
            Font titleFont = font.deriveFont(Font.BOLD, font.getSize()+10);
            height += getFontMetrics(titleFont).getHeight();
        }

        //
        // Leave room for the pie chart
        //
        FontMetrics fm = getFontMetrics(font);
        int labelHeight = fm.getHeight();
        int labelWidth = fm.stringWidth("mmmmmmmmmmmmmmmm");
        width += 50 + 2*labelHeight;
        height += 50 + 4*labelHeight;
        return new Dimension(width, Math.max(height, dataPoints.size()*labelHeight));
    }

    /**
     * Paint the chart
     *
     * <p> The paint() method should not be called directly by the application.  Instead,
     * the repaint() method should be used to schedule the component for redrawing.  Swing
     * processes a call to the paint() method by calling paintComponent(), paintBorder()
     * and finally paintChildren().
     *
     * @param       context         Graphics context
     */
    protected void paintComponent(Graphics context) {
        Font font;
        FontMetrics fm;
        LineMetrics lm;
        String string;
        int x, y, width, height;

        //
        // Make a copy of the graphics context
        //
        Graphics2D g = (Graphics2D)context.create();

        //
        // Enable antialiasing to smooth the pie chart arcs
        //
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //
        // Get the size of our component rectangle
        //
        int xBase = 0;
        int yBase = 0;
        int componentWidth = getWidth();
        int componentHeight = getHeight();

        //
        // Nothing to paint if we are minimized
        //
        if (componentWidth <= 0 || componentHeight <= 0)
            return;

        //
        // Paint the background if the component is opaque
        //
        if (isOpaque()) {
            g.setPaint(getBackground());
            g.fillRect(xBase, yBase, componentWidth-1, componentHeight-1);
        }

        //
        // Don't draw over the border area
        //
        Border border = getBorder();
        if (border != null) {
            Insets insets = border.getBorderInsets(this);
            xBase += insets.left;
            yBase += insets.top;
            componentWidth -= insets.left+insets.right;
            componentHeight -= insets.top+insets.bottom;
        }

        //
        // Draw the chart title using the BOLD component font with the
        // point size increased by 8
        //
        // The title will be truncated if it does not fit
        //
        if (chartTitle != null && chartTitle.length() != 0) {
            font = getFont();
            font = font.deriveFont(Font.BOLD, font.getSize()+8);
            fm = getFontMetrics(font);
            string = chartTitle;
            width = fm.stringWidth(string);

            while (string.length() > 1 && width > componentWidth) {
                string = string.substring(0, string.length()-1);
                width = fm.stringWidth(string);
            }

            g.setPaint(getForeground());
            g.setFont(font);
            lm = fm.getLineMetrics(string, g);
            x = xBase+componentWidth/2-width/2;
            y = yBase+(int)lm.getAscent();
            g.drawString(string, x, y);
            height = (int)lm.getHeight();
            yBase += height;
            componentHeight -= height;
        }

        //
        // We will use the component font with the point size decreased by 2
        // to draw the section labels
        //
        font = getFont();
        font = font.deriveFont(Font.PLAIN, font.getSize()-2);
        fm = getFontMetrics(font);
        int labelWidth = fm.stringWidth("mmmmmmmmmmmmmmmm")+4;
        int labelHeight = fm.getHeight();
        g.setFont(font);

        //
        // Compute the rectangle enclosing the circle for the pie chart.
        // We need to leave room on the left and right for up to 16 characters
        // of text.  We will also leave room on each side equivalent to one line of text.
        //
        int chartWidth = componentWidth-2*labelWidth-2*labelHeight;
        int chartHeight = componentHeight-2*labelHeight;
        int chartDiameter = Math.min(chartWidth, chartHeight);
        int chartRadius = chartDiameter/2;
        int xCenter = xBase+componentWidth/2;
        int yCenter = yBase+componentHeight/2;
        int xCircle = xCenter-chartRadius;
        int yCircle = yCenter-chartRadius;

        //
        // Compute the sum of the individual section values
        //
        double value, totalValue = 0.0;
        int percent, totalPercent = 0;

        for (PieChartElement elem : dataPoints)
            totalValue += elem.getValue();

        //
        // Draw the pie sections
        //
        int angle = 0;
        double radians;
        double c1 = chartRadius;
        double c2 = chartRadius+20;
        double x1, x2, y1, y2;
        Stroke thinLine = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        ListIterator<PieChartElement> li = dataPoints.listIterator();

        while (li.hasNext()) {
            PieChartElement elem = li.next();
            value = elem.getValue();

            //
            // Fill in the pie section with the appropriate color and draw the
            // closing lines around the section
            //
            int arc = (int)Math.round((value/totalValue)*360.0);
            if (arc == 0)
                arc = 1;

            if (angle+arc > 360 || !li.hasNext())
                arc = 360-angle;

            Shape section = new Arc2D.Double(xCircle, yCircle, chartDiameter-1, chartDiameter-1,
                                             angle, arc, Arc2D.PIE);
            g.setPaint(elem.getColor());
            g.fill(section);
            g.setStroke(thinLine);
            g.setPaint(getForeground());
            g.draw(section);

            //
            // Draw the lines from the pie section to the label box
            //
            // For a right-angle triangle with acute angle A and sides a, b and c
            // where c is the hypotenuse and a is the side opposite angle A:
            //
            //    a = c*sin(A)
            //    b = c*cos(A)
            //
            // We will orient our right-angle triangle such that one side is along
            // the x-axis and the other side is along the Y-axis.  The hypotenuse is
            // then the line from the center of the circle out to the points we
            // are going to draw.  Calculating the values for a and b will thus
            // give us the X and Y coordinates for drawing the line.
            //
            double textAngle = (double)angle+(double)arc/2.0;
            int xText, yText;
            boolean left;

            if (textAngle <= 90.0) {
                radians = Math.toRadians(textAngle);
                x1 = (double)xCenter+c1*Math.cos(radians);
                y1 = (double)yCenter-c1*Math.sin(radians);
                x2 = (double)xCenter+c2*Math.cos(radians);
                y2 = (double)yCenter-c2*Math.sin(radians);
            } else if (textAngle <= 180.0) {
                radians = Math.toRadians(180.0-textAngle);
                x1 = (double)xCenter-c1*Math.cos(radians);
                y1 = (double)yCenter-c1*Math.sin(radians);
                x2 = (double)xCenter-c2*Math.cos(radians);
                y2 = (double)yCenter-c2*Math.sin(radians);
            } else if (textAngle <= 270.0) {
                radians = Math.toRadians(270.0-textAngle);
                x1 = (double)xCenter-c1*Math.sin(radians);
                y1 = (double)yCenter+c1*Math.cos(radians);
                x2 = (double)xCenter-c2*Math.sin(radians);
                y2 = (double)yCenter+c2*Math.cos(radians);
            } else {
                radians = Math.toRadians(360.0-textAngle);
                x1 = (double)xCenter+c1*Math.cos(radians);
                y1 = (double)yCenter+c1*Math.sin(radians);
                x2 = (double)xCenter+c2*Math.cos(radians);
                y2 = (double)yCenter+c2*Math.sin(radians);
            }

            //
            // Draw the angled line
            //
            g.setStroke(thinLine);
            g.setPaint(getForeground());
            g.draw(new Line2D.Double(x1, y1, x2, y2));

            //
            // Draw the horizontal line to either the left or the right
            // depending on the current angle
            //
            if (textAngle >= 90.0 && textAngle <= 270.0) {
                x1 = (double)(xCenter-chartRadius-10);
                left = true;
            } else {
                x1 = (double)(xCenter+chartRadius+10);
                left = false;
            }

            g.draw(new Line2D.Double(x2, y2, x1, y2));

            //
            // Position the label rectangle adjacent to the horizontal line
            //
            xText = (int)x1;
            yText = (int)y2 - labelHeight/2;

            //
            // Label the section
            //
            String label = elem.getLabel();
            if (label != null && label.length() != 0) {

                //
                // Get the label text.  We will use the message format
                // pattern if we have one.  Otherwise, the label text
                // will just be the section name.
                //
                if (labelPattern != null && labelPattern.length() != 0) {
                    percent = (int)Math.round((value/totalValue)*100.0);
                    if (percent+totalPercent > 100)
                        percent = 100 - totalPercent;

                    totalPercent += percent;

                    MessageFormat fmt = new MessageFormat(labelPattern);
                    Object[] fmtArgs = new Object[3];
                    fmtArgs[0] = label;
                    fmtArgs[1] = new Double(value);
                    fmtArgs[2] = String.format("%d%%", percent);
                    string = fmt.format(fmtArgs);
                } else {
                    string = label;
                }

                //
                // Format the label text into one or more lines that will fit
                // within the label rectangle
                //
                FontRenderContext frc = g.getFontRenderContext();
                AttributedString atString = new AttributedString(string);
                AttributedCharacterIterator aci = atString.getIterator();
                LineBreakMeasurer lbm = new LineBreakMeasurer(aci, frc);
                List<TextLayout> textList = new ArrayList<TextLayout>(4);
                float wrapWidth = labelWidth-4;
                int endIndex = aci.getEndIndex();

                while (lbm.getPosition() < endIndex)
                    textList.add(lbm.nextLayout(wrapWidth));

                //
                // Compute the label rectangle parameters
                //
                height = 0;
                width = 0;

                for (TextLayout layout : textList) {
                    Rectangle2D bounds = layout.getBounds();
                    width = Math.max(width, (int)Math.round(bounds.getWidth()));
                    height += (int)Math.round(bounds.getHeight());
                }

                if (left)
                    xText -= width+4;

                //
                // Fill in the text box around the label text
                //
                g.setPaint(labelBackground);
                g.fillRect(xText, yText, width+3, height+3);

                //
                // Draw the label text
                //
                g.setPaint(getForeground());
                for (TextLayout layout : textList) {
                    yText += (int)layout.getAscent();
                    layout.draw(g, (float)(xText+2), (float)yText);
                    yText += (int)(layout.getDescent()+layout.getLeading());
                }
            }

            //
            // Advance to the next pie section
            //
            angle += arc;
        }
    }
}

