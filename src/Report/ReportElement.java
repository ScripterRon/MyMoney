package Report;

import java.awt.*;
import java.awt.geom.*;

/**
 * The report elements provide the text that make up a report.  A report consists of
 * one or more report bands and each report band contains one or more report elements.
 */
public interface ReportElement {

    /**
     * Align the report element text at the bottom of the presentation rectangle.
     */
    int BOTTOM_ALIGNMENT=1;

    /**
     * Align the report element text in the center of the presentation rectangle.
     */
    int CENTER_ALIGNMENT=2;

    /**
     * Align the report element text to the left of the presentation rectangle.
     */
    int LEFT_ALIGNMENT=3;

    /**
     * Align the report element text to the right of the presentation rectangle.
     */
    int RIGHT_ALIGNMENT=4;

    /**
     * Align the report element text at the top of the presentation rectangle.
     */
    int TOP_ALIGNMENT=5;

    /**
     * Return the report element font.
     *
     * @return                      The report element font
     */
    public Font getFont();

    /**
     * Return the color used to render the report element text.
     *
     * @return                      The report element color
     */
    public Color getColor();

    /**
     * Return the report element presentation rectangle.  The coordinates are
     * in points (1/72 of an inch) and are relative to the current position on
     * the page such that (0,0) is the upper left corner of the current line.
     * The coordinates are mapped to the imageable area (that is, the margins
     * are not included).
     *
     * @return                      The presentation rectangle
     */
    public Rectangle getBounds();

    /**
     * Get the horizontal alignment for the report element text.  The valid
     * values are:
     * <ul>
     * <li>ElementAlignment.LEFT_ALIGNMENT
     * <li>ElementAlignment.CENTER_ALIGNMENT
     * <li>ElementAlignment.RIGHT_ALIGNMENT
     * </ul>
     *
     * @return                      The horizontal alignment
     */
    public int getHorizontalAlignment();

    /**
     * Get the vertical alignment for the report element text.  The valid
     * values are:
     * <ul>
     * <li>ElementAlignment.TOP_ALIGNMENT
     * <li>ElementAlignment.CENTER_ALIGNMENT
     * <li>ElementAlignment.BOTTOM_ALIGNMENT
     * </ul>
     *
     * @return                      The vertical alignment
     */
    public int getVerticalAlignment();

    /**
     * Get the report element text string
     *
     * @return                      The text string
     */
    public String getText();
}

