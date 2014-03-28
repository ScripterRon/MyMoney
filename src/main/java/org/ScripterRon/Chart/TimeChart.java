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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
 * The TimeChart class provides an X-Y plot using lines to connect each plot point.
 * The data points are provided in a TimeChartElement list and are drawn on a rectangular
 * grid.  The points are plotted in the order they appear in the list.
 * <p>
 * The date is displayed along the X-axis and the values are displayed along the
 * Y-axis.  The date is displayed as month/day/year while the values are displayed
 * with two decimal digits.  The application should scale the data if the data values
 * are too large or too small and the scaling factor should then be included as part
 * of the Y-axis label.
 * <p>
 * An optional moving average curve can be plotted along with the X-Y plot.  This is a
 * simple moving average (SMA) and is computed using the data points in the preceding
 * period.
 * <p>
 * The chartModified() method should be called if the data point list is modified.
 * This will cause the chart to be redrawn with the new data.
 * <p>
 * The background color is used to paint the chart background if the component
 * is opaque.  The foreground color is used to draw the chart labels, the grid
 * color is used to draw the rectangular chart grid, the plot color is used to
 * draw the lines connecting the values and the moving average color is used
 * to draw the moving average curve.
 * <p>
 * The setTitle() method should be called if a chart title is desired.
 */
public class TimeChart extends JComponent {

    /** Data point list */
    private List<TimeChartElement> dataPoints;

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

    /** Moving average color */
    private Color movingAverageColor;

    /** Display the moving average */
    private boolean displayMovingAverage = false;

    /** Number of days in the moving average period */
    private int movingAveragePeriod = 100;

    /** Chart title */
    private String chartTitle;

    /** Minimum coordinate values */
    private TimeChartElement minPoint = new TimeChartElement();

    /** Maximum coordinate values */
    private TimeChartElement maxPoint = new TimeChartElement();

    /** Number of points in each segment of the spline curve */
    private final int precision = 10;

    /**
     * Create a time chart using a data point list.
     *
     * @param       dataPoints      List of data points
     * @param       xLabel          The label for the x-axis or null for no label
     * @param       yLabel          The label for the y-axis or null for no label
     */
    public TimeChart(List<TimeChartElement> dataPoints, String xLabel, String yLabel) {
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
    public List<TimeChartElement> getDataPoints() {
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
     * Return the chart title
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
     * Return the grid size.  The default is a 10x10 grid.
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
     * Return the plot color.  The component foreground color will be returned
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
     * Set the moving average color.  The default color is the component
     * foreground color.
     *
     * @param       color           The moving average color
     */
    public void setMovingAverageColor(Color color) {
        if (color == null)
            throw new NullPointerException("No moving average color supplied");

        movingAverageColor = color;
    }

    /**
     * Return the moving average color.  The component foreground color will be
     * returned if the moving average color has not been set.
     *
     * @return                      The moving average color
     */
    public Color getMovingAverageColor() {
        if (movingAverageColor != null)
            return movingAverageColor;

        return getForeground();
    }

    /**
     * Set the moving average display mode.  The default is to not display
     * the moving average.
     *
     * @param       displayMode     TRUE to display the moving average.
     */
    public void setMovingAverageDisplay(boolean displayMode) {
        displayMovingAverage = displayMode;
    }

    /**
     * Return the moving average display mode.  The default is to not display
     * the moving average.
     *
     * @return                      TRUE if the moving average is displayed
     */
    public boolean getMovingAverageDisplay() {
        return displayMovingAverage;
    }

    /**
     * Set the moving average period.  The default is 100 days.
     *
     * @param       days            The number of days in the period.
     */
    public void setMovingAveragePeriod(int days) {
        if (days < 1)
            throw new IllegalArgumentException("The number of days is not valid");

        movingAveragePeriod = days;
    }

    /**
     * Return the moving average period.
     *
     * @return                      The number of days in the period.
     */
    public int getMovingAveragePeriod() {
        return movingAveragePeriod;
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
        height += labelWidth*gridSize + 7*labelHeight;
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
        int labelWidth = fm.stringWidth("11/11");
        int gridSize = getGridSize();
        width += labelWidth*gridSize + 6*labelHeight;
        height += labelWidth*gridSize + 7*labelHeight;
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
        long dateValue, dateRange;
        String string;
        AffineTransform saveTransform;
        GregorianCalendar cal = new GregorianCalendar();

        //
        // Get the minimum and maximum coordinate values
        //
        boolean firstPoint = true;
        for (TimeChartElement dataPoint : dataPoints) {
            Date date = dataPoint.getDate();
            value = dataPoint.getValue();
            if (firstPoint) {
                minPoint.setDate(date);
                minPoint.setValue(value);
                maxPoint.setDate(date);
                maxPoint.setValue(value);
                firstPoint = false;
            } else {
                if (date.compareTo(minPoint.getDate()) < 0)
                    minPoint.setDate(date);

                if (value < minPoint.getValue())
                    minPoint.setValue(value);

                if (date.compareTo(maxPoint.getDate()) > 0)
                    maxPoint.setDate(date);

                if (value > maxPoint.getValue())
                    maxPoint.setValue(value);
            }
        }

        //
        // Set the minimum and maximum y-axis values to integral values
        //
        minPoint.setValue(Math.floor(minPoint.getValue()));
        maxPoint.setValue(Math.ceil(maxPoint.getValue()));

        //
        // Adjust the minimum and maximum y-axis coordinates if we have
        // a minimum grid increment.
        //
        if (minGridIncrement != 0.0) {
            minPoint.setValue(Math.floor(minPoint.getValue()/minGridIncrement)*minGridIncrement);
            increment = (maxPoint.getValue()-minPoint.getValue())/(double)gridSize;
            if (increment < minGridIncrement)
                increment = minGridIncrement;
            else
                increment = Math.ceil(increment/minGridIncrement)*minGridIncrement;

            maxPoint.setValue(minPoint.getValue()+increment*(double)gridSize);
        }

        //
        // Make a copy of the graphics context
        //
        Graphics2D g = (Graphics2D)context.create();

        //
        // Enable antialiasing to smooth the arcs
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
        // Draw the axis coordinates using the component font with
        // the font size reduced by 2
        //
        font = getFont();
        font = font.deriveFont(Font.PLAIN, font.getSize()-2);
        fm = getFontMetrics(font);
        g.setPaint(getForeground());
        g.setFont(font);
        int indent = fm.getHeight();
        xBase += indent;
        componentWidth -= indent;
        componentHeight -= 2*indent;;

        //
        // Compute the dimensions of a single grid cell
        //
        int cellWidth = componentWidth/gridSize;
        int cellHeight = componentHeight/gridSize;

        //
        // Don't draw anything if a cell is not at least 3x3 pixels
        //
        if (cellWidth < 3 || cellHeight < 3)
            return;

        //
        // Draw the x-coordinates
        //
        // The month and day will be displayed on the first line as "mm/dd".
        // The year will be displayed on the second line as "yyyy" if it
        // has changed from the previous coordinate line.
        //
        dateValue = minPoint.getDate().getTime();
        dateRange = maxPoint.getDate().getTime()-dateValue;
        increment = dateRange/(long)gridSize;
        x = xBase;
        y = yBase+componentHeight+indent;
        int year, prevYear = 0;

        for (i=0; i<=gridSize; i++) {
            cal.setTime(new Date(dateValue));
            string = String.format("%02d/%02d",
                                   cal.get(Calendar.MONTH)+1,
                                   cal.get(Calendar.DAY_OF_MONTH));
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

            year = cal.get(Calendar.YEAR);
            if (year != prevYear) {
                string = String.format("%04d", year);
                width = fm.stringWidth(string);

                while (string.length() > 1 && width > cellWidth) {
                    string = string.substring(0, string.length()-1);
                    width = fm.stringWidth(string);
                }

                g.drawString(string, x-width/2, y+indent);
                prevYear = year;
            }

            x += cellWidth;
            dateValue += increment;
        }

        //
        // Draw the y-coordinates (we will not label the lowest grid line to
        // avoid overwriting the x-coordinate label)
        //
        increment = (maxPoint.getValue()-minPoint.getValue())/(double)gridSize;
        value = minPoint.getValue()+increment;
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
        // Draw the chart rectangle
        //
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

        //
        // Compute the X and Y adjustment values
        //
        dateRange = maxPoint.getDate().getTime()-minPoint.getDate().getTime();
        double xAdjust = (double)gridWidth/(double)dateRange;
        double yAdjust = (double)gridHeight/(maxPoint.getValue()-minPoint.getValue());

        //
        // Plot the data points
        //
        if (plotColor != null)
            g.setPaint(plotColor);
        else
            g.setPaint(getForeground());

        Point2D[] points = new Point2D[dataPoints.size()];
        TimeChartElement prevPoint = null;
        i = 0;

        for (TimeChartElement dataPoint : dataPoints) {
            dateRange = dataPoint.getDate().getTime()-minPoint.getDate().getTime();
            x2 = (double)dateRange*xAdjust;
            y2 = (dataPoint.getValue()-minPoint.getValue())*yAdjust;
            points[i] = new Point2D.Double(x2, y2);
            x2 = xGrid+x2;
            y2 = yGrid+gridHeight-y2;

            if (prevPoint != null) {
                Point2D point = points[i-1];
                x1 = xGrid+point.getX();
                y1 = yGrid+gridHeight-point.getY();
                g.draw(new Line2D.Double(x1, y1, x2, y2));
            }

            g.fillOval((int)x2-2, (int)y2-2, 4, 4);
            prevPoint = dataPoint;
            i++;
        }

        //
        // Draw the moving average curve using a natural cubic spline
        //
        if (displayMovingAverage && points.length > 1) {
            int np = points.length;

            //
            // Compute the moving averages using the requested period.
            // We use a simple moving average which just sums the values
            // over the preceding period to get the next point of the curve.
            //
            double[] avg = new double[np];
            avg[0] = points[0].getY();
            for (i=1; i<np; i++) {
                cal.setTime(dataPoints.get(i).getDate());
                cal.add(Calendar.DAY_OF_YEAR, -movingAveragePeriod);
                Date minDate = cal.getTime();
                double sum = points[i].getY();
                int count = 1;
                for (int j=0; j<i; j++) {
                    if (minDate.compareTo(dataPoints.get(j).getDate()) <= 0) {
                        sum += points[j].getY();
                        count++;
                    }
                }

                avg[i] = sum/(double)count;
            }

            //
            // Compute the distance between knots on the interpolation curve
            // (h[0] is not used)
            //
            double[] h = new double[np];
            for (i=1; i<np; i++)
                h[i] = points[i].getX() - points[i-1].getX();

            //
            // Solve the linear system
            //
            // Note that a[] is initialized to 0.0 by the 'new' operator
            //
            double[] a = new double[np];
            if (np > 2) {
                double[] sub = new double[np-1];
                double[] diag = new double[np-1];
                double[] sup = new double[np-1];
                for (i=1; i<np-1; i++) {
                    diag[i] = (h[i]+h[i+1])/3.0;
                    sup[i] = h[i+1]/6.0;
                    sub[i] = h[i]/6.0;
                    a[i] = (avg[i+1]-avg[i])/h[i+1]-(avg[i]-avg[i-1])/h[i];
                }

                solveTridiag(sub, diag, sup, a, np-2);
            }

            //
            // Draw the spline curve using a series of short line segments (the
            // number of segments is determined by 'precision')
            //
            if (movingAverageColor != null)
                g.setPaint(movingAverageColor);
            else
                g.setPaint(getForeground());

            x1 = points[0].getX();
            y1 = avg[0];
            for (i=1; i<np; i++) {
                for (int j=1; j<=precision; j++) {
                    double t1 = (h[i]*(double)j)/(double)precision;
                    double t2 = h[i] - t1;
                    y2 = ((-a[i-1]/6.0*(t2+h[i])*t1+avg[i-1])*t2+
                                        (-a[i]/6.0*(t1+h[i])*t2+avg[i])*t1)/h[i];
                    x2 = points[i-1].getX()+t1;
                    g.draw(new Line2D.Double(xGrid+x1, yGrid+gridHeight-y1,
                                             xGrid+x2, yGrid+gridHeight-y2));
                    x1 = x2;
                    y1 = y2;
                }
            }
        }
    }

    /**
     * Solve the linear system A[n,n]X[n]=B[n] using Gaussian elimination
     * without pivoting where:
     * <ul>
     * <li> a(i,i-1) = sub[i] for 2<=i<=n
     * <li> a(i,i)   = diag[i] for 1<=i<=n
     * <li> a(i,i+1) = sup[i] for 1<=i<=n-1
     * </ul>
     * The values sub[1] and sup[n] are ignored and the first array positions
     * are not used.
     *
     * @param       sub             Matrix A value below the diagonal
     * @param       diag            Matrix A value along the diagonal
     * @param       sup             Matrix A value above the diagonal
     * @param       b               Vector X on input and Vector B on return.
     * @param       n               Vector/Matrix dimension
     */
    private void solveTridiag(double sub[], double diag[], double sup[], double b[], int n){
      for(int i=2; i<=n; i++) {
          sub[i] = sub[i]/diag[i-1];
          diag[i] = diag[i]-sub[i]*sup[i-1];
          b[i] = b[i]-sub[i]*b[i-1];
      }

      b[n] = b[n]/diag[n];

      for(int i=n-1;i>=1;i--)
          b[i] = (b[i]-sup[i]*b[i+1])/diag[i];
    }
}
