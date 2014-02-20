package MyMoney;

import java.awt.Color;

import Report.*;

/**
 * ReportAmountRenderer is a renderer for use with a Report field.  It will
 * set the report field color to RED for a negative amount and to BLACK otherwise.
 */
public final class ReportAmountRenderer implements ReportRenderer {

    /**
     * Create a new report renderer
     */
    public ReportAmountRenderer() {
    }

    /**
     * Format the amount.  The report field color will be set to RED
     * for a negative amount and BLACK otherwise.
     *
     * @param       element         The report field
     * @param       value           The value to format
     * @return                      The formatted value
     */
    public String format(ReportField element, Object value) {
        if (value == null)
            return new String();

        //
        // Format the value using its toString() method
        //
        String formattedValue = value.toString();

        //
        // Set the report element color
        //
        if (formattedValue.length() != 0 && formattedValue.charAt(0) == '-')
            element.setColor(Color.RED);
        else
            element.setColor(Color.BLACK);

        return formattedValue;
    }
}

