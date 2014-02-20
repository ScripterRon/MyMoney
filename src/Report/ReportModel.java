package Report;

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

