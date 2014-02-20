package Report;

/**
 * PageFunction is a report function that returns the current page number
 * as an Integer object.
 */
public class PageFunction extends AbstractFunction {

    /** The current page number */
    int pageNumber, startNumber;

    /**
     * Construct a function.  The function name is used to identify
     * the function and must not be the same as a data column name or
     * the name of another expression or function.
     *
     * @param       name            The function name
     */
    public PageFunction(String name) {
        super(name);
    }

    /**
     * Return the object class for the function value.
     *
     * @return                      The function class
     */
    public Class<?> getValueClass() {
        return Integer.class;
    }

    /**
     * Return the page number as an Integer object
     *
     * @return                      The expression value
     */
    public Object getValue() {
        return new Integer(pageNumber);
    }

    /**
     * Reset the page number when regenerating a report page
     */
    public void resetValue() {
        pageNumber = startNumber;
    }

    /**
     * The report has been started.  This method is called before
     * the report header is printed.  This method may be called multiple
     * times as the report is repaginated during preview and printing.
     *
     * @param       event           The report event
     */
    public void reportStarted(ReportEvent event) {
        pageNumber = 0;
        startNumber = 0;
    }

    /**
     * A new page is starting.  This method is called before the
     * page header is printed.
     *
     * @param       event           The report event
     */
    public void pageStarted(ReportEvent event) {
        pageNumber++;
    }

    /**
     * Clone the function
     *
     * @return                      The cloned report function
     */
    public Object clone() {
        Object clonedObject = super.clone();
        PageFunction clonedFunction = (PageFunction)clonedObject;
        clonedFunction.startNumber = clonedFunction.pageNumber;
        return clonedObject;
    }
}

