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

import java.util.List;
import java.util.ListIterator;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

/**
 * The LineChart class provides an X-Y plot using lines to connect each plot point.
 * The data points are provided in a LineChartElement list and are drawn on a rectangular
 * grid.  The points are plotted in the order they appear in the list.
 *
 * <p>The x and y coordinates are displayed with two decimal digits.  The application
 * should scale the data if the data values are too large or too small and the
 * scaling factor should then be included as part of the appropriate axis label.
 *
 * <p>The chartModified() method should be called if the data point list is modified.
 * This will cause the chart to be redrawn with the new data.
 *
 * <p>The background color is used to paint the chart background if the component
 * is opaque.  The foreground color is used to draw the chart labels.  The grid
 * color is used to draw the rectangular chart grid.  The plot color is used to
 * draw the lines connecting the values.
 *
 * <p>The setTitle() method should be called if a chart title is desired.
 */
public class LineChart extends JComponent {

    /** Data point list */
    private List<LineChartElement> dataPoints;

    /** Label for the x-axis */
    private String xLabel;

    /** Label for the y-axis */
    private String yLabel;

    /** Grid size */
    private int gridSize = 10;

    /** Grid color */
    private Color gridColor = new Color(235, 235, 235);

    /** Minimum y-axis grid increment */
    private double minGridIncrement = 0.0;

    /** Plot color */
    private Color plotColor;

    /** Chart title */
    private String chartTitle;

    /** Minimum coordinate values */
    private LineChartElement minPoint = new LineChartElement();

    /** Maximum coordinate values */
    private LineChartElement maxPoint = new LineChartElement();

    /**
     * Construct a line chart using a data point list.
     *
     * @param       dataPoints      List of data points
     * @param       xLabel          The label for the x-axis or null for no label
     * @param       yLabel          The label for the y-axis or null for no label
     */
    public LineChart(List<LineChartElement> dataPoints, String xLabel, String yLabel) {
        super();

        //
        // Save the plot information
        //
        if (dataPoints == null)
            throw new NullPointerException("No data point list provided");

        this.dataPoints = dataPoints;
        this.xLabel = xLabel;
        this.yLabel = yLabel;
    }

    /**
     * Get the chart data points
     *
     * @return                      List of data points
     */
    public List<LineChartElement> getDataPoints() {
        return dataPoints;
    }

    /**
     * Set the chart title
     *
     * @param       title           Chart title
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
     * Set the number of cells in the grid.  The default is a 10x10 grid.
     *
     * @param       size            Grid size
     */
    public void setGridSize(int size) {
        if (size < 1)
            throw new IllegalArgumentException("The grid size must be non-zero");

        gridSize = size;
    }

    /**
     * Get the grid size.
     *
     * @return                      The grid size
     */
    public int getGridSize() {
        return gridSize;
    }

    /**
     * Set the grid color.  The default color is Color(235, 235, 235)
     *
     * @param       color           The grid color
     */
    public void setGridColor(Color color) {
        if (color == null)
            throw new NullPointerException("No grid color supplied");

        gridColor = color;
    }

    /**
     * Return the grid color.  The default color is Color(235, 235, 235)
     *
     * @return                      The grid color
     */
    public Color getGridColor() {
        return gridColor;
    }

    /**
     * Set the minimum y-axis grid increment.  The y-axis increment will then
     * be a multiple of the minimum increment.  There is no default minimum increment.
     *
     * @param       increment       Minimum grid increment
     */
    public void setMinimumGridIncrement(double increment) {
        minGridIncrement = increment;
    }

    /**
     * Return the minimum y-axis grid increment.
     *
     * @return                      Minimum grid increment
     */
    public double getMinimumGridIncrement() {
        return minGridIncrement;
    }

    /**
     * Set the plot color.  The default color is the component foreground color.
     *
     * @param       color           The plot color
     */
    public void setPlotColor(Color color) {
        if (color == null)
            throw new NullPointerException("No plot color supplied");

        plotColor = color;
    }

    /**
     * Get the plot color.  The component foreground color will be returned
     * if the plot color has not been set.
     *
     * @return                      The plot color
     */
    public Color getPlotColor() {
        if (plotColor != null)
            return plotColor;

        return getForeground();
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
     * been set, the size will be computed based on the current grid size and
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
        else
            font = font.deriveFont(Font.PLAIN, font.getSize()-2);

        //
        // Leave room for the chart title
        //
        if (chartTitle != null && chartTitle.length() != 0) {
            Font titleFont = font.deriveFont(Font.BOLD, font.getSize()+10);
            height += getFontMetrics(titleFont).getHeight();
        }

        //
        // Leave room for the chart grid
        //
        FontMetrics fm = getFontMetrics(font);
        int labelHeight = fm.getHeight();
        int labelWidth = fm.stringWidth("1234.56");
        int gridSize = getGridSize();
        width += labelWidth*gridSize + 6*labelHeight;
        height += labelWidth*gridSize + 6*labelHeight;
        return new Dimension(width, height);
    }

    /**
     * Return the minimum size for this component.  If no minimum size has been
     * set, the size will be computed based on the current grid size and
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
        else
            font = font.deriveFont(Font.PLAIN, font.getSize()-2);

        //
        // Leave room for the chart title
        //
        if (chartTitle != null && chartTitle.length() != 0) {
            Font titleFont = font.deriveFont(Font.BOLD, font.getSize()+10);
            height += getFontMetrics(titleFont).getHeight();
        }

        //
        // Leave room for the chart grid
        //
        FontMetrics fm = getFontMetrics(font);
        int labelHeight = fm.getHeight();
        int labelWidth = fm.stringWidth("1.23");
        int gridSize = getGridSize();
        width += labelWidth*gridSize + 6*labelHeight;
        height += labelWidth*gridSize + 6*labelHeight;
        return new Dimension(width, height);
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
        int i;
        int x, y, width, height;
        double x1, x2, y1, y2;
        double increment, value;
        String string;
        AffineTransform saveTransform;

        //
        // Get the minimum and maximum coordinate values
        //
        boolean firstPoint = true;
        for (LineChartElement dataPoint : dataPoints) {
            x1 = dataPoint.getX();
            y1 = dataPoint.getY();
            if (firstPoint) {
                minPoint.setX(x1);
                minPoint.setY(y1);
                maxPoint.setX(x1);
                maxPoint.setY(y1);
                firstPoint = false;
            } else {
                minPoint.setX(Math.min(minPoint.getX(), x1));
                minPoint.setY(Math.min(minPoint.getY(), y1));
                maxPoint.setX(Math.max(maxPoint.getX(), x1));
                maxPoint.setY(Math.max(maxPoint.getY(), y1));
            }
        }

        //
        // Set the minimum and maximum y-axis values to integral values
        //
        minPoint.setY(Math.floor(minPoint.getY()));
        maxPoint.setY(Math.ceil(maxPoint.getY()));

        //
        // Adjust the minimum and maximum y-axis coordinates if we have
        // a minimum grid increment.
        //
        if (minGridIncrement != 0.0) {
            minPoint.setY(Math.floor(minPoint.getY()/minGridIncrement)*minGridIncrement);
            increment = (maxPoint.getY()-minPoint.getY())/(double)gridSize;
            if (increment < minGridIncrement)
                increment = minGridIncrement;
            else
                increment = Math.ceil(increment/minGridIncrement)*minGridIncrement;

            maxPoint.setY(minPoint.getY()+increment*(double)gridSize);
        }

        //
        // Make a copy of the graphics context
        //
        Graphics2D g = (Graphics2D)context.create();

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
        // Surround the chart with space equivalent to one line of text on the
        // left and bottom and two lines of text on top and right
        //
        height = getFontMetrics(getFont()).getHeight();
        xBase += height;
        yBase += 2*height;
        componentWidth -= 3*height;
        componentHeight -= 3*height;

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
        // Draw the axis labels using the BOLD component font
        //
        font = getFont().deriveFont(Font.BOLD);
        fm = getFontMetrics(font);
        g.setPaint(getForeground());
        g.setFont(font);

        //
        // Draw the x-axis label
        //
        // The label will be truncated if it does not fit
        //
        if (xLabel != null && xLabel.length() != 0) {
            string = xLabel;
            width = fm.stringWidth(string);

            while (string.length() > 1 && width > componentWidth) {
                string = string.substring(0, string.length()-1);
                width = fm.stringWidth(string);
            }

            lm = fm.getLineMetrics(string, g);
            height = (int)lm.getHeight();
            x = xBase+componentWidth/2-width/2;
            y = yBase+componentHeight-height+(int)lm.getAscent();
            g.drawString(string, x, y);
            componentHeight -= height;
        }

        //
        // Draw the y-axis label
        //
        // The label will be truncated if it does not fit
        //
        if (yLabel != null && yLabel.length() != 0) {
            string = yLabel;
            width = fm.stringWidth(string);

            while (string.length() > 1 && width > componentHeight) {
                string = string.substring(0, string.length()-1);
                width = fm.stringWidth(string);
            }

            lm = fm.getLineMetrics(string, g);
            height = (int)lm.getHeight();
            x = xBase+(int)lm.getAscent();
            y = yBase+componentHeight/2-width/2;
            saveTransform = g.getTransform();
            g.rotate(Math.toRadians(-90), x, y);
            g.drawString(string, x, y);
            g.setTransform(saveTransform);
            xBase += height;
            componentWidth -= height;
        }

        //
        // Draw the axis coordinates using the component font with the
        // point size reduced by 2
        //
        font = getFont();
        font = font.deriveFont(Font.PLAIN, font.getSize()-2);
        fm = getFontMetrics(font);
        g.setPaint(getForeground());
        g.setFont(font);
        int indent = fm.getHeight();
        xBase += indent;
        componentWidth -= indent;
        componentHeight -= indent;;

        //
        // Compute the dimensions of a single grid cell
        //
        int cellWidth = componentWidth/gridSize;
        int cellHeight = componentHeight/gridSize;

        //
        // Draw the x-coordinates
        //
        increment = (maxPoint.getX()-minPoint.getX())/(double)gridSize;
        value = minPoint.getX();
        x = xBase;
        y = yBase+componentHeight+indent;

        for (i=0; i<=gridSize; i++) {
            string = String.format("%.2f", value);
            width = fm.stringWidth(string);

            while (string.length() > 1 && width > cellWidth) {
                string = string.substring(0, string.length()-1);
                width = fm.stringWidth(string);
            }

            if (i == 0) {
                lm = fm.getLineMetrics(string, g);
                y -= (int)lm.getDescent();
            }

            g.drawString(string, x-width/2, y);
            x += cellWidth;
            value += increment;
        }

        //
        // Draw the y-coordinates (we will not label the lowest grid line to
        // avoid overwriting the x-coordinate label)
        //
        increment = (maxPoint.getY()-minPoint.getY())/(double)gridSize;
        value = minPoint.getY()+increment;
        x = xBase-indent;
        y = yBase+componentHeight-cellHeight;

        for (i=0; i<gridSize; i++) {
            string = String.format("%.2f", value);
            width = fm.stringWidth(string);

            while (string.length() > 1 && width > cellHeight) {
                string = string.substring(0, string.length()-1);
                width = fm.stringWidth(string);
            }

            if (i == 0) {
                lm = fm.getLineMetrics(string, g);
                x += (int)lm.getAscent();
            }

            saveTransform = g.getTransform();
            g.rotate(Math.toRadians(-90), x, y+width/2);
            g.drawString(string, x, y+width/2);
            g.setTransform(saveTransform);
            y -= cellHeight;
            value += increment;
        }

        //
        // Compute the grid dimensions
        //
        int gridWidth = cellWidth*gridSize;
        int gridHeight = cellHeight*gridSize;
        int xGrid = componentWidth/2-gridWidth/2+xBase;
        int yGrid = componentHeight/2-gridHeight/2+yBase;

        //
        // Draw the chart grid and plot the data points
        //
        if (cellWidth > 2 && cellHeight > 2) {
            g.setFont(getFont());
            g.setPaint(gridColor);
            g.drawRect(xGrid, yGrid, gridWidth-1, gridHeight-1);

            //
            // Draw the vertical lines
            //
            for (i=1, x=xGrid+cellWidth; i<gridSize; i++, x+=cellWidth)
                g.draw(new Line2D.Double(x, yGrid, x, yGrid+gridHeight));

            //
            // Draw the horizontal lines
            //
            for (i=1, y=yGrid+cellHeight; i<gridSize; i++, y+=cellHeight)
                g.draw(new Line2D.Double(xGrid, y, xGrid+gridWidth, y));

            if (plotColor != null)
                g.setPaint(plotColor);
            else
                g.setPaint(getForeground());

            double xAdjust = (double)gridWidth/(maxPoint.getX()-minPoint.getX());
            double yAdjust = (double)gridHeight/(maxPoint.getY()-minPoint.getY());
            LineChartElement prevPoint = null;

            //
            // Plot the data points
            //
            for (LineChartElement dataPoint : dataPoints) {
                x2 = xGrid+(dataPoint.getX()-minPoint.getX())*xAdjust;
                y2 = yGrid+gridHeight-(dataPoint.getY()-minPoint.getY())*yAdjust;

                if (prevPoint != null) {
                    x1 = xGrid+(prevPoint.getX()-minPoint.getX())*xAdjust;
                    y1 = yGrid+gridHeight-(prevPoint.getY()-minPoint.getY())*yAdjust;
                    g.draw(new Line2D.Double(x1, y1, x2, y2));
                }

                g.fillOval((int)x2-2, (int)y2-2, 4, 4);
                prevPoint = dataPoint;
            }
        }
    }
}

