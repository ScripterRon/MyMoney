package Report;

/**
 * An expression is a lightweight function that does not maintain a state.  Expressions
 * are used to calculate values within a single row of a report.  The expression
 * dependency level determines the order in which expressions are evaluated
 * (a higher dependency level is evaluated before a lower dependency level).  The
 * expression name is used to refer to an expression value from a report element.
 */
public interface ReportExpression {

    /**
     * Return the expression name
     *
     * @return                      The expression name
     */
    public String getName();

    /**
     * Return the object class for the expression value
     *
     * @return                      The expression class
     */
    public Class<?> getValueClass();

    /**
     * Return the expression value
     *
     * @return                      The expression value
     */
    public Object getValue();

    /**
     * Return the expression dependency level
     *
     * @return                      The dependency level
     */
    public int getDependencyLevel();
}

