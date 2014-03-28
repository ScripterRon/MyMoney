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

