package MyMoney;

import java.util.List;
import Report.*;

/**
 * SecurityCostExpression is a Report expression.  It will return the
 * cost basis for the current security.
 */
public final class SecurityCostExpression extends AbstractExpression {

    /** The name of the security column */
    private String securityField;

    /**
     * Create a new expression.  The expression name is used to identify the expression
     * and must not be the same as a data column name or the name of another expression
     * or function.
     */
    public SecurityCostExpression(String name) {
        super(name);
    }

    /**
     * Set the security column field name
     *
     * @param       name            Security column field name
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
        double cost = 0.0;

        String securityName = (String)dataRow.getValue(securityField);
        if (securityName.length() > 0) {
            List<SecurityHolding> holdings = reportModel.getSecurityHoldings();
            for (SecurityHolding h : holdings) {
                if (h.getSecurity().getName().equals(securityName)) {
                    cost = h.getTotalCost();
                    break;
                }
            }
        }
        
        return String.format("%,.2f", cost);
    }
}
