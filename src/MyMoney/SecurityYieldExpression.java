package MyMoney;

import java.util.List;
import Report.*;

/**
 * SecurityYieldExpression is a Report expression.  It computes the percent
 * yield for each security group.
 */
public final class SecurityYieldExpression extends AbstractExpression {

    /** Security field name */
    private String securityField;

    /**
     * Create a new expression.  The expression name is used to identify the expression
     * and must not be the same as a data column name or the name of another expression
     * or function.
     */
    public SecurityYieldExpression(String name) {
        super(name);
    }

    /**
     * Set the security field name
     *
     * @param       name            Security field name
     */
    public void setSecurityField(String name) {
        securityField = name;
    }
    /**
     * Get the expression value
     *
     * @return                      The current sum as a formatted string
     */
    public Object getValue() {
        DataRow dataRow = getState().getDataRow();
        InvestmentReportModel reportModel = (InvestmentReportModel)dataRow.getReportModel();
        double yield = 0.0;
        
        String securityName = (String)dataRow.getValue(securityField);
        if (securityName.length() > 0) {
            List<SecurityHolding> holdings = reportModel.getSecurityHoldings();
            for (SecurityHolding h : holdings) {
                SecurityRecord s = h.getSecurity();
                if (s.getName().equals(securityName)) {
                    yield = h.getAnnualYield()*100.0;
                    break;
                }
            }
        }

        return String.format("%.2f%%", yield);
    }
}
