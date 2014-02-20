package MyMoney;

import java.util.List;
import Report.*;

/**
 * SecuritySharesExpression is a Report expression.  It will return the number of
 * shares for the current security.
 */
public final class SecuritySharesExpression extends AbstractExpression {

    /** Security field name */
    private String securityField;
    
    /**
     * Create a new expression.  The expression name is used to identify the expression
     * and must not be the same as a data column name or the name of another expression
     * or function.
     */
    public SecuritySharesExpression(String name) {
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
     * @return                      The number of shares as a formatted string
     */
    public Object getValue() {
        DataRow dataRow = getState().getDataRow();
        InvestmentReportModel reportModel = (InvestmentReportModel)dataRow.getReportModel();
        double shares = 0.0;
        
        String securityName = (String)dataRow.getValue(securityField);
        if (securityName.length() > 0) {
            List<SecurityHolding> holdings = reportModel.getSecurityHoldings();
            for (SecurityHolding h : holdings) {
                if (h.getSecurity().getName().equals(securityName)) {
                    shares = h.getTotalShares();
                    break;
                }
            }
        }
        
        return String.format("%,.4f", shares);
    }
}
