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
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.event.*;

/**
 * The ReportView is the view component for a JScrollPane.  It shows the
 * current report page after applying the zoom factor.
 */
public final class ReportView extends JComponent implements Scrollable {

    /** The screen resolution in DPI */
    int screenResolution;

    /** The adjustment from points to pixels */
    double screenAdjustment;

    /** The current page image */
    private BufferedImage image;

    /**
     * Construct a new report view
     */
    public ReportView() {

        //
        // Create the Swing component
        //
        super();

        //
        // Turn off double buffering since we are already using a buffer
        //
        setDoubleBuffered(false);

        //
        // Get the screen resolution
        //
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        screenResolution = toolkit.getScreenResolution();
        screenAdjustment = (double)screenResolution/72.0;
    }

    /**
     * Get a page buffer.  The setPageBuffer() method should be called to
     * make this the active page buffer after the application has finished
     * rendering the page.
     *
     * @return                      A buffered page image
     */
    public BufferedImage getPageBuffer() {
        Dimension size = getPreferredSize();
        return (BufferedImage)createImage(size.width, size.height);
    }

    /**
     * Set the current report page buffer.  This is the buffered image that
     * will be rendered when we are called to paint our component.
     *
     * @param       image           The buffered page image
     */
    public void setPageBuffer(BufferedImage image) {
        this.image = image;
    }

    /**
     * Return the scrollable unit increment (Scrollable interface).  The unit
     * increment will be 10 points.
     *
     * @param       visibleRect     The visible rectangle
     * @param       orientation     The scroll bar orientation (VERTICAL or HORIZONTAL)
     * @param       direction       The scroll direction (negative or positive)
     * @return                      The block increment
     */
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return (int)(10.0*screenAdjustment);
    }

    /**
     * Return the scrollable block increment (Scrollable interface).  The block
     * increment will be the size of the viewport minus 10 points.
     *
     * @param       visibleRect     The visible rectangle
     * @param       orientation     The scroll bar orientation (VERTICAL or HORIZONTAL)
     * @param       direction       The scroll direction (negative or positive)
     * @return                      The block increment
     */
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        int blockIncrement;
        if (orientation == SwingConstants.VERTICAL) {
            blockIncrement = visibleRect.height-(int)(10.0*screenAdjustment);
            if (direction < 0) {
                if (visibleRect.y < blockIncrement)
                    blockIncrement = visibleRect.y;
            } else {
                int y = visibleRect.y+visibleRect.height;
                Dimension size = getPreferredSize();
                if (y+blockIncrement > size.height)
                    blockIncrement = size.height-y;
            }
        } else {
            blockIncrement = visibleRect.width-(int)(10.0*screenAdjustment);
            if (direction < 0) {
                if (visibleRect.x < blockIncrement)
                    blockIncrement = visibleRect.x;
            } else {
                int x = visibleRect.x+visibleRect.width;
                Dimension size = getPreferredSize();
                if (x+blockIncrement > size.width)
                    blockIncrement = size.width-x;
            }
        }

        return blockIncrement;
    }

    /**
     * Return the preferred viewport size (Scrollable interface).  We will return
     * our preferred component size.
     *
     * @return                      The preferred size
     */
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    /**
     * Return the scrollable tracks viewport height mode (Scrollable interface).
     * We do not track the viewport height (that is, we want to use scroll bars).
     *
     * @return                      TRUE if the view height should be forced
     *                              to be the same as the viewport height
     */
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    /**
     * Return the scrollable tracks viewport width mode (Scrollable interface).
     * We do not track the viewport width (that is, we want to use scroll bars).
     *
     * @return                      TRUE if the view width should be forced
     *                              to be the same as the viewport width
     */
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    /**
     * Paint the view
     *
     * <p> The paint() method should not be called directly by the application.  Instead,
     * the repaint() method should be used to schedule the component for redrawing.  Swing
     * processes a call to the paint() method by calling paintComponent(), paintBorder()
     * and finally paintChildren().
     *
     * @param       context         Graphics context
     */
    protected void paintComponent(Graphics context) {
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        //
        // Nothing to paint if we are minimized
        //
        if (viewWidth <= 0 || viewHeight <= 0)
            return;

        //
        // Make a copy of the graphics context
        //
        Graphics2D g = (Graphics2D)context.create();

        //
        // Paint the background if the component is opaque
        //
        if (isOpaque()) {
            g.setPaint(getBackground());
            g.fillRect(0, 0, viewWidth-1, viewHeight-1);
        }

        //
        // Display the current page image
        //
        // Our image will normally be displayed at (0,0) since JScrollPane
        // will not force our size to match the viewport size, thus the
        // view size will match the image size.  The amount actually displayed
        // will depend on the viewport clipping rectangle.
        //
        if (image != null) {
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            int x = Math.max((viewWidth-imageWidth)/2, 0);
            int y = Math.max((viewHeight-imageHeight)/2, 0);
            g.drawImage(image, null, x, y);
        }
    }
}

