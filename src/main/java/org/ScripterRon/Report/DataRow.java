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
 * A data row represents the data for the current row in a report.  The
 * columns in the data row are generated from the report data model as well
 * as report expressions and functions.
 */
public final class DataRow implements Cloneable {

    /** The report model */
    private ReportModel reportModel;

    /** The current row index */
    private int rowIndex = -1;

    /** The start row index */
    private int startIndex = -1;

    /** End of report indicator */
    private boolean endOfReport = false;

    /** The number of columns in the data row */
    private int columns;

    /** The column names */
    private String[] columnNames;

    /** The data sources */
    private Object[] dataSources;

    /**
     * Construct a data row from a report model
     *
     * @param       reportModel     The report model
     */
    public DataRow(ReportModel reportModel) {
        if (reportModel == null)
            throw new NullPointerException("No report model provided");

        this.reportModel = reportModel;

        //
        // Set the column names and data sources for the report model
        //
        columns = reportModel.getColumnCount();
        if (columns < 1)
            throw new IllegalArgumentException("No report data columns");

        columnNames = new String[columns];
        dataSources = new Object[columns];

        for (int i=0; i<columns; i++) {
            columnNames[i] = reportModel.getColumnName(i);
            dataSources[i] = reportModel;
        }
    }
    
    /**
     * Get the report model
     * 
     * @return                      The report model
     */
    public ReportModel getReportModel() {
        return reportModel;
    }

    /**
     * Add a report expression or function to the data row
     *
     * @param       expression      The report expression or function
     */
    public void addExpression(ReportExpression expression) {
        String[] newNames = new String[columns+1];
        System.arraycopy(columnNames, 0, newNames, 0, columns);
        columnNames = newNames;
        columnNames[columns] = expression.getName();

        Object[] newSources = new Object[columns+1];
        System.arraycopy(dataSources, 0, newSources, 0, columns);
        dataSources = newSources;
        dataSources[columns] = expression;

        columns++;
    }

    /**
     * Reset to the first report row
     */
    public void resetRow() {
        rowIndex = startIndex;
        endOfReport = false;
    }

    /**
     * Check if there is another report row
     *
     * @return                      TRUE if there is another row
     */
    public boolean hasNext() {
        return (rowIndex+1 < reportModel.getRowCount());
    }

    /**
     * Advance to the next report row
     */
    public void nextRow() {
        if (!endOfReport) {
            rowIndex++;
            if (rowIndex >= reportModel.getRowCount())
                endOfReport = true;
        }
    }

    /**
     * Return the number of columns in the data row
     *
     * @return                      The number of columns
     */
    public int getColumnCount() {
        return columns;
    }

    /**
     * Return the name of a column
     *
     * @param       columnIndex     The column index
     * @return                      The column name
     */
    public String getColumnName(int columnIndex) {
        if (columnIndex < 0 || columnIndex >= columns)
            throw new IndexOutOfBoundsException("Column index "+columnIndex+" is not valid");

        return columnNames[columnIndex];
    }

    /**
     * Return the object class for a column
     *
     * @param       columnIndex     The column index
     * @return                      The object class
     */
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex < 0 || columnIndex >= columns)
            throw new IndexOutOfBoundsException("Column index "+columnIndex+" is not valid");

        if (dataSources[columnIndex] instanceof ReportModel)
            return ((ReportModel)dataSources[columnIndex]).getColumnClass(columnIndex);

        return ((ReportExpression)dataSources[columnIndex]).getValueClass();
    }

    /**
     * Return the column index for the specified column name.  The return value
     * will be -1 if the column name is not found.
     *
     * @param       columnName      The column name
     * @return                      The column index
     */
    public int findColumn(String columnName) {
        if (columnName == null)
            throw new NullPointerException("No column name provided");

        int index;
        for (index=0; index<columns; index++)
            if (columnNames[index].equals(columnName))
                break;

        if (index == columns)
            index = -1;

        return index;
    }

    /**
     * Return the value for a data row column.  The return value will be null
     * if the column is not found or the end of the report has been reached.
     *
     * @param       columnIndex     The column index
     * @return                      The column value
     */
    public Object getValue(int columnIndex) {
        if (endOfReport || columnIndex < 0 || columnIndex >= columns)
            return null;

        if (dataSources[columnIndex] instanceof ReportModel)
            return reportModel.getValueAt(rowIndex, columnIndex);

        return ((ReportExpression)dataSources[columnIndex]).getValue();
    }

    /**
     * Return the value for a data row column.  The return value will be null if the
     * column name is not found.
     *
     * @param       columnName      The name of the column
     * @return                      The value of the column
     */
    public Object getValue(String columnName) {
        return getValue(findColumn(columnName));
    }

    /**
     * Return the value for a column in the next row of the report mode.
     * The return value will be null if the column is not found, the end
     * of the report has been reached, or the column is not in the report model.
     *
     * @param       columnIndex     The column index
     * @return                      The column value
     */
    public Object getNextValue(int columnIndex) {
        int nextIndex = rowIndex + 1;
        if (endOfReport || columnIndex < 0 || columnIndex >= columns ||
                    nextIndex >= reportModel.getRowCount() ||
                    columnIndex >= reportModel.getColumnCount())
            return null;

        return reportModel.getValueAt(nextIndex, columnIndex);
    }

    /**
     * Return the value for a column in the next row of the report mode.
     * The return value will be null if the column is not found, the end
     * of the report has been reached, or the column is not in the report model.
     *
     * @param       columnName      The column name
     * @return                      The column value
     */
    public Object getNextValue(String columnName) {
        return getNextValue(findColumn(columnName));
    }

    /**
     * Clone the data row.  The cloned data row will contain just the report
     * model columns.  Report expressions will need to be added again.  We
     * do this because we will be cloning the report expressions as well when
     * we save the report state.
     *
     * @return                      The cloned data row
     */
    public Object clone() {
        Object clonedObject;

        try {
            clonedObject = super.clone();
            DataRow clonedDataRow = (DataRow)clonedObject;
            clonedDataRow.startIndex = clonedDataRow.rowIndex;

            //
            // Keep just the report model columns
            //
            clonedDataRow.columns = reportModel.getColumnCount();
            clonedDataRow.columnNames = new String[clonedDataRow.columns];
            clonedDataRow.dataSources = new Object[clonedDataRow.columns];

            for (int i=0; i<clonedDataRow.columns; i++) {
                clonedDataRow.columnNames[i] = reportModel.getColumnName(i);
                clonedDataRow.dataSources[i] = reportModel;
            }
        } catch (CloneNotSupportedException exc) {
            throw new UnsupportedOperationException("Unable to clone data row", exc);
        }

        return clonedObject;
    }
}
