package Report;

/**
 * This is the base class for all report expressions.  New report expressions
 * must extend this class and override the getValue() method.
 * <p>
 * An expression is invoked each time another expression or function requests its value.
 * <p>
 * Report expressions are cloned at the start of each page of the report when the
 * report preview is displayed.  This allows a specific page to be displayed without
 * regenerating the preceding pages of the report.
 */
public abstract class AbstractExpression implements ReportExpression, Cloneable {

    /** The expression name */
    private String name;

    /** The dependency level */
    private int level;

    /** The report state */
    private ReportState state;

    /**
     * Construct a new expression.  The expression name is used to identify the expression
     * and must not be the same as a data column name or the name of another expression
     * or function.
     *
     * @param       name            The expression name
     */
    public AbstractExpression(String name) {
        if (name == null)
            throw new NullPointerException("No expression name provided");

        if (name.length() == 0)
            throw new IllegalArgumentException("Zero-length expression name is not valid");

        this.name = name;
    }

    /**
     * Return the expression name
     *
     * @return                      The expression name
     */
    public String getName() {
        return name;
    }

    /**
     * Return the object class for the expression value.  The application must
     * override this method if it wants the object class to be something other
     * than String.
     *
     * @return                      The expression class
     */
    public Class<?> getValueClass() {
        return String.class;
    }

    /**
     * Return the expression value.  The application must override this
     * method to return the value for the expression.
     *
     * @return                      The expression value
     */
    public abstract Object getValue();

    /**
     * Return the expression dependency level
     *
     * @return                      The dependency level
     */
    public int getDependencyLevel() {
        return level;
    }

    /**
     * Set the expression dependency level.  Expressions and functions are evaluated
     * based on the dependency level (a higher dependency level is evaluated before
     * a lower dependency level).
     *
     * @param       level           The dependency level
     */
    public void setDependencyLevel(int level) {
        this.level = level;
    }

    /**
     * Return the report state.  The report state is set for all expressions
     * and functions during report initialization and cannot be changed.
     *
     * @return                      The report state
     */
    protected ReportState getState() {
        return state;
    }

    /**
     * Set the report state.  The report state is set for all expressions
     * and functions during report initialization and cannot be changed.
     *
     * @param       state           The report state
     */
    void setState(ReportState state) {
        if (state == null)
            throw new NullPointerException("No report state provided");

        this.state = state;
    }

    /**
     * Clone the expression
     *
     * @return                      The cloned report expression
     */
    public Object clone() {
        Object clonedObject;

        try {
            clonedObject = super.clone();
        } catch (CloneNotSupportedException exc) {
            throw new UnsupportedOperationException("Unable to clone report expression", exc);
        }

        return clonedObject;
    }
}

