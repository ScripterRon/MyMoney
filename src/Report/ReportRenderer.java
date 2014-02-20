package Report;

/**
 * A report renderer returns the string representation of a value.
 */
public interface ReportRenderer {

    /**
     * Return the string representation of a value
     *
     * @param       element         The report field
     * @param       value           The value to convert to a string
     * @return                      The string representation of the value
     */
    public String format(ReportField element, Object value);
}

