package MyMoney;

import Report.*;
import java.text.NumberFormat;
import java.util.List;

/**
 * AmountSumFunction is a Report function.  It will sum the amounts for a column.
 * The setField() method specifies the name of the column to be summed.  Amounts
 * that are transferred to another account can be excluded from the sum by calling
 * the setTransferAccount() method to set the name of the transfer column.  Loan
 * payments can be included in the sum by calling the setLoanAccountNames() method.  The
 * sum can be reset at the start of a new group by calling the setGroupName()
 * method to set the group name.
 */
public final class AmountSumFunction extends AbstractFunction {

    /** The name of the field being summed */
    private String field;

    /** The name of the transfer account field */
    private String transferAccountField;

    /** The names of loan accounts to be included in the sum */
    private List<String> loanAccountNames;

    /** The current sum */
    private double sum, startSum;

    /**
     * Construct a new function.  The function name is used to identify
     * the function and must not be the same as a data column name or
     * the name of another expression or function.
     *
     * @param       name            The function name
     */
    public AmountSumFunction(String name) {
        super(name);
    }

    /**
     * Set the name of the field to be summed.
     *
     * @param       name            Field name
     */
    public void setField(String name) {
        field = name;
    }

    /**
     * Set the transfer account field name.  This field must be set if account
     * transfer amounts are to be excluded from the sum.  An account name in
     * the transfer column must start with '[' to be recognized as an account
     * name.
     *
     * @param       name            Transfer colum field name
     */
    public void setTransferAccountField(String name) {
        transferAccountField = name;
    }

    /**
     * Set the loan accounts names.  A transaction for a loan account will be
     * included in the sum.  The account name must include the enclosing brackets.
     *
     * @param       accountNames    List of loan account names
     */
    public void setLoanAccountNames(List<String> accountNames) {
        loanAccountNames = accountNames;
    }

    /**
     * The report has been started.  This method is called before
     * the report header is printed.  This method may be called multiple
     * times as the report is repaginated during preview and printing.
     *
     * @param       event           The report event
     */
    @Override
    public void reportStarted(ReportEvent event) {
        sum = 0.0;
        startSum = 0.0;
    }

    /**
     * A new group is starting.  This method is called before the
     * group header is printed.  The current group can be obtained
     * from the report event.
     *
     * @param       event           The report event
     */
    @Override
    public void groupStarted(ReportEvent event) {

        //
        // Reset the sum if a our group is starting a new value
        //
        String name = getGroupName();
        if (name != null && name.equals(event.getGroup().getName())) {
            sum = 0.0;
        }
    }

    /**
     * The report has advanced to the next row.  This method is called
     * before the row is printed.
     *
     * @param       event           The report event
     */
    @Override
    public void rowAdvanced(ReportEvent event) {
        NumberFormat nf = NumberFormat.getNumberInstance();

        //
        // Get the current report value
        //
        DataRow dataRow = event.getState().getDataRow();
        Object fieldValue = dataRow.getValue(field);
        if (fieldValue == null)
            return;

        //
        // Don't include the amount if this is an account transfer.  A
        // transfer account name starts with '['.  Loan account transfers
        // will be included in the sum.
        //
        if (transferAccountField != null) {
            Object transferAccount = dataRow.getValue(transferAccountField);
            if (transferAccount != null &&
                                transferAccount instanceof String &&
                                ((String)transferAccount).length() > 0 &&
                                ((String)transferAccount).charAt(0) == '[') {
                if (loanAccountNames == null)
                    return;

                String transferAccountName = (String)transferAccount;
                boolean skipSum = true;
                for (String name : loanAccountNames) {
                    if (name.equals(transferAccountName)) {
                        skipSum = false;
                        break;
                    }
                }

                if (skipSum)
                    return;
            }
        }

        //
        // Add the amount to the sum.  The column value can be a Number or
        // a String.
        //
        if (fieldValue instanceof Number) {
            sum += ((Number)fieldValue).doubleValue();
        } else if (fieldValue instanceof String) {
            try {
                sum += nf.parse((String)fieldValue).doubleValue();
            } catch (Exception exc) {

            }
        }
    }

    /**
     * Return the function value as a formatted string with 2 decimal digits.
     *
     * @return                      The formatted string
     */
    @Override
    public Object getValue() {
        if (Math.abs(sum) < 0.005)
            sum = 0.00;

        return String.format("%,.2f", sum);
    }

    /**
     * Reset the sum when regenerating a report page
     */
    @Override
    public void resetValue() {
        sum = startSum;
    }

    /**
     * Clone the function.  The report writer will clone all functions at the
     * start of a new page.  The cloned functions are then used to generate the
     * report page each time it is requested.
     *
     * @return                      The cloned report function
     */
    @Override
    public Object clone() {

        //
        // Clone the function
        //
        Object clonedObject = super.clone();

        //
        // Remember the sum at the start of the page
        //
        AmountSumFunction clonedFunction = (AmountSumFunction)clonedObject;
        clonedFunction.startSum = clonedFunction.sum;
        return clonedObject;
    }
}
