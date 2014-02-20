package MyMoney;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

/**
 * DateRenderer is a cell renderer for use with a JTable column. It formats
 * dates as "mm/dd/yyyy".
 */
public final class DateRenderer extends DefaultTableCellRenderer {

    /** Gregorian calendar */
    private GregorianCalendar cal;

    /**
     * Create a date renderer
     */
    public DateRenderer() {
        super();
        setHorizontalAlignment(JLabel.LEFT);
        cal = new GregorianCalendar();
    }

    /**
     * Set the text value for the cell.  The supplied value must be a Date.
     *
     * @param       value           The value for the cell
     */
    public void setValue(Object value) {
        if (value == null) {
            setText(new String());
            return;
        }

        if (!(value instanceof Date))
            throw new IllegalArgumentException("Value is not a Date");

        cal.setTime((Date)value);
        setText(String.format("%02d/%02d/%04d",
                              cal.get(Calendar.MONTH)+1,
                              cal.get(Calendar.DAY_OF_MONTH),
                              cal.get(Calendar.YEAR)));
    }
}

