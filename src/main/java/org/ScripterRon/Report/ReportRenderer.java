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
package org.ScripterRon.Report;

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

