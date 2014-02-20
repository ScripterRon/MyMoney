package MyMoney;

import java.text.NumberFormat;

import javax.swing.table.*;
import javax.swing.*;
import java.awt.*;

/**
 * PercentRenderer is a cell renderer for use with a JTable column. It formats
 * numbers as percentages.
 */

public class PercentRenderer extends DefaultTableCellRenderer {

    /** Number formatter */
    private NumberFormat formatter;

    /**
     * Create a percent renderer
     */
    public PercentRenderer() {
        super();
        setHorizontalAlignment(JLabel.RIGHT);
        formatter = NumberFormat.getPercentInstance();
        formatter.setMaximumFractionDigits(2);
        formatter.setMinimumFractionDigits(2);
    }

    /**
     * Set the text value for the cell.  The value must be a Double representing
     * a percentage (that is, 1.00 = 100%).
     *
     * @param       value           The value to format as a percentage
     */
    public void setValue(Object value) {
        if (value == null) {
            setText(new String());
            return;
        }

        if (!(value instanceof Double))
            throw new IllegalArgumentException("Value is not a Double");

        setText(formatter.format(value));
    }
}

