package MyMoney;

import java.text.NumberFormat;

import javax.swing.table.*;
import javax.swing.*;
import java.awt.*;

/**
 * AmountRenderer is a cell renderer for use with a JTable column. It formats
 * amounts with a fixed number of decimal digits.  Negative values will be
 * displayed in red while positive values will be displayed in black.  A
 * minus sign will be removed if the vavlue is zero.
 */
public final class AmountRenderer extends DefaultTableCellRenderer {

    /** Number formatter */
    private NumberFormat formatter;

    /**
     * Create an amount renderer.
     *
     * @param       digits          Number of decimal digits
     */
    public AmountRenderer(int digits) {
        super();
        setHorizontalAlignment(JLabel.RIGHT);
        formatter = NumberFormat.getInstance();
        formatter.setMaximumFractionDigits(digits);
        formatter.setMinimumFractionDigits(digits);
    }

    /**
     * Set the text value for the cell.  The supplied value must be a Number.
     *
     * @param       value           The value for the cell
     */
    public void setValue(Object value) {

        //
        // Return an empty string if the value is null
        //
        if (value == null) {
            setText(new String());
            return;
        }

        //
        // We must have a Number
        //
        if (!(value instanceof Number))
            throw new IllegalArgumentException("Value is not a Number");

        //
        // Format the number as a double value
        //
        String text = formatter.format(((Number)value).doubleValue());

        //
        // Remove a leading minus sign if the value is zero
        //
        if (text.charAt(0) == '-') {
            int index;
            int length = text.length();
            for (index=1; index<length; index++) {
                char c = text.charAt(index);
                if (c != '0' && c != '.')
                    break;
            }

            if (index == length)
                text = text.substring(1);
        }

        //
        // Set the foreground color to red if the value is negative
        //
        if (text.charAt(0) == '-')
            setForeground(Color.RED);
        
        //
        // Set the text value for the number
        //
        setText(text);
    }
    
    /**
     * Get the table cell renderer component
     *
     * @param       table           The table
     * @param       value           The cell value
     * @param       isSelected      TRUE if the cell is selected
     * @param       hasFocus        TRUE if the cell has the focus
     * @param       row             Table row
     * @param       column          Table column
     */
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        //
        // Set the foreground color in case it has been changed by setValue()
        //
        if (isSelected)
            setForeground(Color.WHITE);
        else
            setForeground(Color.BLACK);
        
        //
        // Return the table cell renderer component
        //
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
}
