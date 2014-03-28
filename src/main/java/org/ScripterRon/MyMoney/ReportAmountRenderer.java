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

import java.awt.Color;

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

