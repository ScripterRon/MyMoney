package MyMoney;

import java.util.Date;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * The SizedTable class is a JTable with column sizes based on the column data types
 */
public final class SizedTable extends JTable {

    /** Date column */
    public static final int DATE_COLUMN = 1;
    
    /** Check number column */
    public static final int CHECK_COLUMN = 2;
    
    /** Name column */
    public static final int NAME_COLUMN = 3;
    
    /** Category column */
    public static final int CATEGORY_COLUMN = 4;
    
    /** Memo column */
    public static final int MEMO_COLUMN = 5;
    
    /** Price column */
    public static final int PRICE_COLUMN = 6;
    
    /** Amount column */
    public static final int AMOUNT_COLUMN = 7;
    
    /** Reconciled column */
    public static final int RECONCILED_COLUMN = 8;
    
    /** Type column */
    public static final int TYPE_COLUMN = 9;
    
    /** Percent column */
    public static final int PERCENT_COLUMN = 10;
    
    /** Split column */
    public static final int SPLIT_COLUMN = 11;
    
    /** Security column */
    public static final int SECURITY_COLUMN = 12;
    
    /**
     * Create a new sized table
     *
     * @param       tableModel      The table model
     * @param       columnTypes     Array of column types
     */
    public SizedTable(TableModel tableModel, int[] columnTypes) {
        
        //
        // Create the table
        //
        super(tableModel);

        //
        // Set the cell renderers and column widths
        //
        Component component;
        TableCellRenderer renderer;
        TableColumn column;
        TableColumnModel columnModel = getColumnModel();
        TableCellRenderer headRenderer = getTableHeader().getDefaultRenderer();
        if (headRenderer instanceof DefaultTableCellRenderer) {
            DefaultTableCellRenderer defaultRenderer = (DefaultTableCellRenderer)headRenderer;
            defaultRenderer.setHorizontalAlignment(JLabel.CENTER);
        }
        
        int columnCount = tableModel.getColumnCount();
        if (columnCount > columnTypes.length)
            throw new IllegalArgumentException("More columns than column types");

        for (int i=0; i<columnCount; i++) {
            Object value = null;
            column = columnModel.getColumn(i);
            switch (columnTypes[i]) {
                case DATE_COLUMN:
                    column.setCellRenderer(new DateRenderer());
                    value = new Date();
                    break;
                    
                case CHECK_COLUMN:
                    value = new Integer(1234);
                    break;
                    
                case NAME_COLUMN:
                    value = "mmmmmmmmmmmmmmmmmmmm";             // 20 characters
                    break;
                    
                case SECURITY_COLUMN:                           
                    value = "mmmmmmmmmmmmmmmmmmmmmmmmm";        // 25 characters
                    break;
                    
                case CATEGORY_COLUMN:
                    value = "mmmmmmmmmmmmmmmmmmmm";             // 20 characters
                    break;
                    
                case MEMO_COLUMN:
                    value = "mmmmmmmmmmmmmmmmmmmmmmmmm";        // 25 characters
                    break;
                    
                case PRICE_COLUMN:
                    column.setCellRenderer(new AmountRenderer(4));
                    value = new Double(12345.1234);
                    break;
                    
                case AMOUNT_COLUMN:
                    column.setCellRenderer(new AmountRenderer(2));
                    value = new Double(1234567.12);
                    break;
                    
                case RECONCILED_COLUMN:
                    column.setCellRenderer(new ReconcileRenderer());
                    value = "C";
                    break;
                    
                case TYPE_COLUMN:
                    value = "mmmmmmmmmm";                       // 10 characters
                    break;
                    
                case PERCENT_COLUMN:
                    column.setCellRenderer(new PercentRenderer());
                    value = new Double(12.1234);
                    break;
                    
                case SPLIT_COLUMN:
                    value = "nn:nn";
                    break;
                    
                default:
                    throw new IllegalArgumentException("Unsupported column type "+columnTypes[i]);
            }

            component = headRenderer.getTableCellRendererComponent(this, tableModel.getColumnName(i),
                                                                   false, false, 0, i);
            int headWidth = component.getPreferredSize().width;
            renderer = column.getCellRenderer();
            if (renderer == null)
                renderer = getDefaultRenderer(tableModel.getColumnClass(i));
            component = renderer.getTableCellRendererComponent(this, value, false, false, 0, i);
            int cellWidth = component.getPreferredSize().width;
            column.setPreferredWidth(Math.max(headWidth+5, cellWidth+5));
        }
        
        //
        // Resize all column proportionally
        //
        setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS);
    }
}
