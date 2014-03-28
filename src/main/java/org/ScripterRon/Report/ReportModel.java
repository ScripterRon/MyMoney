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
 * The ReportModel interface specifies the methods that the Report class
 * will use to interrogate tabular data.
 */
public interface ReportModel {

    /**
     * Return the number of data columns.  The report model must have at
     * least one data column.
     *
     * @return                      The number of columns
     */
    public int getColumnCount();

    /**
     * Return the number of data rows
     *
     * @return                      The number of rows
     */
    public int getRowCount();

    /**
     * Return the class for a data column
     *
     * @param       columnIndex     The column index
     * @return                      The data column class
     */
    public Class<?> getColumnClass(int columnIndex);

    /**
     * Return the name for a data column.  The data column names must be
     * unique.
     *
     * @param       columnIndex     The column index
     * @return                      The column name
     */
    public String getColumnName(int columnIndex);

    /**
     * Return the value for a data cell
     *
     * @param       rowIndex        The row index
     * @param       columnIndex     The column index
     * @return                      The cell value
     */
    public Object getValueAt(int rowIndex, int columnIndex);
}

