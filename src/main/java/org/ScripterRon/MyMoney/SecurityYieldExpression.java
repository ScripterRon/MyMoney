/**
 * Copyright 2005-2014 Ronald W Hoffman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ScripterRon.MyMoney;
import org.ScripterRon.Report.*;

import java.util.List;

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
