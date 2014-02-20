package MyMoney;

import java.text.NumberFormat;

import javax.swing.table.*;
import javax.swing.*;
import java.awt.*;

/**
 * ReconcileRenderer is a cell renderer for use with a JTable column. It centers
 * the string in the column.
 */
public class ReconcileRenderer extends DefaultTableCellRenderer {

    /**
     * Create a reconcile renderer
     */
    public ReconcileRenderer() {
        super();
        setHorizontalAlignment(JLabel.CENTER);
    }
}

