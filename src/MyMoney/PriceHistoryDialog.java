package MyMoney;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedSet;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

import Chart.*;

/**
 * Price history dialog
 */
public final class PriceHistoryDialog extends JDialog implements ActionListener {

    /** History table column classes */
    private static final Class<?>[] columnClasses = {Date.class, Double.class, String.class};

    /** History table column names */
    private static final String[] columnNames = {"Date", "Price", "Split"};
    
    /** History table column types */
    private static final int[] columnTypes = {SizedTable.DATE_COLUMN, SizedTable.PRICE_COLUMN,
                                                SizedTable.SPLIT_COLUMN};

    /** Security */
    private SecurityRecord security;

    /** History table model */
    private PriceHistoryTableModel tableModel;

    /** History table */
    private JTable table;

    /** History time chart */
    private TimeChart chart;

    /** History chart data */
    private List<TimeChartElement> chartData;

    /**
     * Create the dialog
     *
     * @param       parent          Parent window
     * @param       security        Security to edit
     */
    public PriceHistoryDialog(JDialog parent, SecurityRecord security) {
        super(parent, "Security Price History", true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.security = security;

        //
        // Create the price history table
        //
        tableModel = new PriceHistoryTableModel(security, columnNames, columnClasses);
        table = new SizedTable(tableModel, columnTypes);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        //
        // Create the title pane containing the security name
        //
        JPanel titlePane = new JPanel();
        titlePane.add(new JLabel("<html><h1>"+security.getName()+"</h1></html>"));

        //
        // Create the table scroll pane
        //
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        //
        // Create the buttons (New Entry, Delete Entry, Done)
        //
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
        
        JButton button = new JButton("New Entry");
        button.setActionCommand("new");
        button.addActionListener(this);
        buttonPane.add(button);
        
        buttonPane.add(Box.createHorizontalStrut(15));

        button = new JButton("Delete Entry");
        button.setActionCommand("delete");
        button.addActionListener(this);
        buttonPane.add(button);
        
        buttonPane.add(Box.createHorizontalStrut(15));

        button = new JButton("Done");
        button.setActionCommand("done");
        button.addActionListener(this);
        buttonPane.add(button);

        //
        // Create the price chart data
        //
        chartData = new ArrayList<>(security.getPriceHistory().size());
        createChartData();

        //
        // Create the price chart
        //
        chart = new TimeChart(chartData, "Date", "Price");
        chart.setOpaque(true);
        chart.setBackground(Color.WHITE);
        chart.setForeground(Color.BLACK);
        chart.setGridColor(new Color(235, 235, 235));
        chart.setGridSize(10);
        chart.setMinimumGridIncrement(0.1);
        chart.setPlotColor(Color.BLUE);
        chart.setMovingAverageDisplay(true);
        chart.setMovingAverageColor(Color.RED);
        chart.setMovingAveragePeriod(200);
        chart.setPreferredSize(new Dimension(400, 400));
        
        //
        // Set up the history pane containing the table and the chart.  The table viewport
        // will be set to display a maximum of 20 rows.
        //
        Dimension tableSize = table.getPreferredSize();
        table.setPreferredScrollableViewportSize(new Dimension(tableSize.width, 20*table.getRowHeight()));
        
        JPanel pricePane = new JPanel();
        pricePane.add(scrollPane);
        pricePane.add(Box.createHorizontalStrut(5));
        pricePane.add(chart);

        //
        // Set up the content pane
        //
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.setOpaque(true);
        contentPane.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        contentPane.add(titlePane);
        contentPane.add(pricePane);
        contentPane.add(Box.createVerticalStrut(15));
        contentPane.add(buttonPane);
        setContentPane(contentPane);
    }

    /**
     * Show the dialog
     *
     * @param       parent          Parent window
     * @param       security        Security to edit
     */
    public static void showDialog(JDialog parent, SecurityRecord security) {
        try {
            JDialog dialog = new PriceHistoryDialog(parent, security);
            dialog.pack();
            dialog.setLocationRelativeTo(parent);
            dialog.setVisible(true);
        } catch (Exception exc) {
            Main.logException("Exception while displaying dialog", exc);
        }
    }

    /**
     * Action performed (ActionListener interface)
     *
     * @param       ae              Action event
     */
    public void actionPerformed(ActionEvent ae) {

        //
        // Process the action command
        //
        // "new" - Add a price history element
        // "delete" - Delete a price history element
        // "done" - All done
        //
        try {
            String action = ae.getActionCommand();
            if (action.equals("new")) {
                PriceHistoryEditDialog.showDialog(this, security, tableModel);
                createChartData();
                chart.chartModified();
            } else if (action.equals("delete")) {
                int[] rows = table.getSelectedRows();
                if (rows.length == 0) {
                    JOptionPane.showMessageDialog(this, "You must select an entry to delete",
                                                  "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    for (int row : rows) {
                        PriceHistory ph = tableModel.getEntryAt(row);
                        SortedSet<PriceHistory> s = security.getPriceHistory();
                        s.remove(ph);
                    }
                    
                    tableModel.priceHistoryChanged();
                    createChartData();
                    chart.chartModified();
                    Main.dataModified = true;
                }
            } else if (action.equals("done")) {
                setVisible(false);
                dispose();
            }
        } catch (Exception exc) {
            Main.logException("Exception while processing action event", exc);
        }
    }
    
    /**
     * Create the chart data from the price history entries
     */
    private void createChartData() {
        
        //
        // Remove existing chart data entries
        //
        chartData.clear();
        SortedSet<PriceHistory> prices = security.getPriceHistory();
        
        //
        // Create the new chart data entries, adjusting for stock splits
        //
        for (PriceHistory ph : prices) {
            double splitRatio = ph.getSplitRatio();
            if (splitRatio != 0.0) {
                for (TimeChartElement elem : chartData) {
                    elem.setValue(elem.getValue()/splitRatio);
                }
            }
            
            chartData.add(new TimeChartElement(ph.getDate(), ph.getPrice()));
        }
    }
}
