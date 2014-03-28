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
import org.ScripterRon.Chart.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Investment account transaction panel
 */
public final class InvestmentTransactionPanel extends TransactionPanel implements ActionListener, ChangeListener {

    /** Transaction table column classes */
    private static final Class<?>[] columnClasses = {
        Date.class, String.class, String.class, String.class,
        Double.class, Double.class, Double.class, Double.class};

    /** Transaction table column names */
    private static final String[] columnNames = {
        "Date", "Action", "Security", "Category", "Shares", "Price", "Commission", "Amount"};
    
    /** Transaction table column types */
    private static final int[] columnTypes = {
        SizedTable.DATE_COLUMN, SizedTable.TYPE_COLUMN, SizedTable.SECURITY_COLUMN,
        SizedTable.CATEGORY_COLUMN, SizedTable.PRICE_COLUMN, SizedTable.PRICE_COLUMN,
        SizedTable.AMOUNT_COLUMN, SizedTable.AMOUNT_COLUMN};

    /** Position table column classes */
    private static final Class<?>[] positionClasses = {
        String.class, String.class, Double.class, Double.class, Double.class, Double.class,
        Double.class, Double.class, Date.class};

    /** Position table column names */
    private static final String[] positionNames = {
        "Security", "Symbol", "Shares", "Price", "Value", "Basis", "Gain", "Income", "Purchased"};

    /** Position table column types */
    private static final int[] positionTypes = {
        SizedTable.SECURITY_COLUMN, SizedTable.TYPE_COLUMN, SizedTable.PRICE_COLUMN, 
        SizedTable.PRICE_COLUMN, SizedTable.AMOUNT_COLUMN, SizedTable.AMOUNT_COLUMN, 
        SizedTable.PERCENT_COLUMN, SizedTable.PERCENT_COLUMN, SizedTable.DATE_COLUMN};
    
    /** Pie chart colors */
    private static final Color[] colors = {            
        Color.RED,  Color.YELLOW, Color.GREEN, Color.BLUE, Color.ORANGE, Color.MAGENTA};

    /** Transaction table model */
    private InvestmentTableModel tableModel;

    /** Active tabbed pane */
    private int activeTabbedPane;

    /** Position table */
    private JTable positionTable;

    /** Position table model */
    private PositionModel positionModel;

    /** Asset allocation pie chart */
    private PieChart chart;

    /** Pie chart data */
    private List<PieChartElement> chartData;

    /**
     * Create the investment account transaction panel
     * <p>
     * We will use a tabbed pane to display the account transactions in
     * the first pane and the account positions in the second pane.
     *
     * @param       account         The investment account
     */
    public InvestmentTransactionPanel(AccountRecord account) {

        //
        // Create the content pane for the main window
        //
        super(new BorderLayout());
        setOpaque(true);
        setBackground(Color.white);
        setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

        //
        // Remember the account
        //
        this.account = account;

        //
        // Create the title pane containing the account name
        //
        JPanel titlePane = new JPanel();
        titlePane.setBackground(Color.white);
        nameLabel = new JLabel("<HTML><h1>"+account.getName()+"</h1></HTML>");
        titlePane.add(nameLabel);

        //
        // Create the buttons (New Transaction, Edit Transaction, 
        // Delete Transaction and Help)
        //
        JPanel buttonPane = new JPanel();

        JButton button = new JButton("New Transaction");
        button.setActionCommand("new");
        button.addActionListener(this);
        buttonPane.add(button);
        
        buttonPane.add(Box.createHorizontalStrut(10));

        button = new JButton("Edit Transaction");
        button.setActionCommand("edit");
        button.addActionListener(this);
        buttonPane.add(button);
        
        buttonPane.add(Box.createHorizontalStrut(10));

        button = new JButton("Delete Transaction");
        button.setActionCommand("delete");
        button.addActionListener(this);
        buttonPane.add(button);

        buttonPane.add(Box.createHorizontalStrut(10));

        button = new JButton("Help");
        button.setActionCommand("help");
        button.addActionListener(this);
        buttonPane.add(button);
        
        //
        // Create the transaction table
        //
        tableModel = new InvestmentTableModel(account, columnNames, columnClasses);
        table = new SizedTable(tableModel, columnTypes);
        table.setRowSorter(new TableRowSorter<TableModel>(tableModel));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Dimension tableSize = table.getPreferredSize();
        int rowHeight = table.getRowHeight();
        int tableRows = Math.max(5, (Main.mainWindow.getSize().height-250)/rowHeight);
        table.setPreferredScrollableViewportSize(new Dimension(tableSize.width, tableRows*rowHeight));

        //
        // Create the table scroll pane for the transaction panel
        //
        scrollPane = new JScrollPane(table);
        
        //
        // Create the table pane
        //
        JPanel tablePane = new JPanel();
        tablePane.add(Box.createGlue());
        tablePane.add(scrollPane);
        tablePane.add(Box.createGlue());

        //
        // Set up the transaction panel
        //
        JPanel transactionPane = new JPanel(new BorderLayout());
        transactionPane.add(tablePane, BorderLayout.CENTER);
        transactionPane.add(buttonPane, BorderLayout.SOUTH);

        //
        // Create the position table
        //
        positionModel = new PositionModel();
        positionTable = new SizedTable(positionModel, positionTypes);
        positionTable.setRowSorter(new TableRowSorter<TableModel>(positionModel));
        positionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        positionTable.setCellSelectionEnabled(true);
        positionTable.setSurrendersFocusOnKeystroke(true);

        //
        // Column 3 (price) is editable and will have the default background
        // color.  The other columns will have a different background color
        // to indicate they are not editable.
        //
        TableColumnModel columnModel = positionTable.getColumnModel();
        for (int i=0; i<positionNames.length; i++) {
            TableColumn column = columnModel.getColumn(i);
            if (i == 3) {
                column.setCellEditor(new AmountEditor(4, true));
            } else {
                TableCellRenderer renderer = (DefaultTableCellRenderer)column.getCellRenderer();
                if (renderer == null)
                    renderer = positionTable.getDefaultRenderer(positionClasses[i]);
                ((DefaultTableCellRenderer)renderer).setBackground(new Color(240, 240, 240));
            }
        }

        //
        // Create the table scroll pane for the position panel
        //
        JScrollPane positionScrollPane = new JScrollPane(positionTable);

        //
        // Create the asset allocation pie chart
        //
        updateChartData();
        chart = new PieChart(chartData, "{0} - ${1} ({2})");
        chart.setOpaque(true);
        chart.setBackground(Color.WHITE);
        chart.setLabelBackground(Color.WHITE);
        chart.setForeground(Color.BLACK);

        //
        // Set the size of the position table
        //
        tableSize = positionTable.getPreferredSize();
        rowHeight = positionTable.getRowHeight();
        tableRows = Math.max(5, (Main.mainWindow.getSize().height-chart.getPreferredSize().height-250)/rowHeight);
        positionTable.setPreferredScrollableViewportSize(new Dimension(tableSize.width, tableRows*rowHeight));
        
        //
        // Set up the position panel
        //
        JPanel topPane = new JPanel();
        topPane.add(Box.createGlue());
        topPane.add(positionScrollPane);
        topPane.add(Box.createGlue());

        JPanel bottomPane = new JPanel();
        bottomPane.add(Box.createGlue());
        bottomPane.add(chart);
        bottomPane.add(Box.createGlue());

        JPanel positionPane = new JPanel();
        positionPane.setLayout(new BoxLayout(positionPane, BoxLayout.Y_AXIS));
        positionPane.add(topPane);
        positionPane.add(Box.createVerticalStrut(10));
        positionPane.add(bottomPane);

        //
        // Set up the tabbed pane containing the transaction pane and the
        // position pane
        //
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Transactions", transactionPane);
        tabbedPane.addTab("Positions", positionPane);
        tabbedPane.addChangeListener(this);

        //
        // Set up the content pane
        //
        add(titlePane, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * Update the asset allocation chart data
     */
    private void updateChartData() {

        //
        // Get the asset allocations
        //
        int[] types = SecurityRecord.getTypes();
        double[] amounts = new double[types.length];
        int rows = positionModel.getRowCount();
        for (int row=0; row<rows; row++) {
            SecurityRecord s = positionModel.getSecurityAt(row);
            int type = s.getType();
            for (int i=0; i<types.length; i++) {
                if (type == types[i]) {
                    amounts[i] += s.getPrice()*positionModel.getSharesAt(row);
                    break;
                }
            }
        }

        //
        // Fill in the chart data
        //
        if (chartData == null)
            chartData = new ArrayList<PieChartElement>(types.length);
        else
            chartData.clear();

        int colorIndex = 0;
        for (int i=0; i<types.length; i++) {
            if (amounts[i] != 0.0) {
                chartData.add(new PieChartElement(colors[colorIndex++],
                                                  SecurityRecord.getTypeString(types[i]),
                                                  amounts[i]));
                if (colorIndex == colors.length)
                    colorIndex = 0;
            }
        }

        //
        // Update the plot with the new data
        //
        if (chart != null)
            chart.chartModified();
    }

    /**
     * Validate the position panel when security names have changed
     */
    public void validate() {

        //
        // Make sure the position table is correct
        //
        if (positionTable != null && activeTabbedPane == 1) {
            TableModel model = positionTable.getModel();
            if (model instanceof AbstractTableModel) {
                AbstractTableModel tableModel = (AbstractTableModel)model;
                if (tableModel.getRowCount() > 0) {
                    int row = positionTable.getSelectedRow();
                    tableModel.fireTableDataChanged();
                    if (row >= 0)
                        positionTable.setRowSelectionInterval(row, row);
                }
            }
        }

        //
        // Pass the validate request up the component hierarchy (this will validate
        // the title and the transaction table)
        //
        super.validate();
    }

    /**
     * Action performed (ActionListener interface)
     *
     * @param   ae                  Action event
     */
    public void actionPerformed(ActionEvent ae) {

        //
        // Process the action command
        //
        // "new" - Add a new transaction
        // "edit" - Edit a transaction
        // "delete" - Delete a transaction
        // "help" - Display help for bank accounts
        //
        try {
            int row, modelRow, option;
            
            switch (ae.getActionCommand()) {
                case "new":
                    if (SecurityRecord.securities.size() == 0) {
                       JOptionPane.showMessageDialog(this, "There are no securities defined",
                                                      "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        InvestmentTransactionEditDialog.showDialog(Main.mainWindow, this, null);
                    }
                    break;
                    
                case "edit":
                    row = table.getSelectedRow();
                    if (row < 0) {
                        JOptionPane.showMessageDialog(this, "You must select a transaction to edit",
                                                      "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        modelRow = table.convertRowIndexToModel(row);
                        InvestmentTransactionEditDialog.showDialog(Main.mainWindow, this,
                                                       tableModel.getTransactionAt(modelRow));
                    }
                    break;
                    
                case "delete":
                    row = table.getSelectedRow();
                    if (row < 0) {
                        JOptionPane.showMessageDialog(this, "You must select a transaction to delete",
                                                      "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        option = JOptionPane.showConfirmDialog(this,
                                            "Do you want to delete the selected transaction?",
                                            "Confirm Delete", JOptionPane.YES_NO_OPTION);
                        if (option == JOptionPane.YES_OPTION) {
                            modelRow = table.convertRowIndexToModel(row);
                            TransactionRecord transaction = tableModel.getTransactionAt(modelRow);
                            TransactionRecord.transactions.remove(transaction);
                            transaction.clearReferences();
                            tableModel.transactionRemoved(transaction);
                            Main.dataModified = true;
                            row = Math.min(row, table.getRowCount()-1);
                            if (row >= 0)
                                table.setRowSelectionInterval(row, row);
                        }
                    }
                    break;
                    
                case "help":
                    Main.mainWindow.displayHelp(HelpWindow.INVESTMENT_ACCOUNT);
                    break;
            }
        } catch (Exception exc) {
            Main.logException("Exception while processing action event", exc);
        }
    }

    /**
     * State changed (ChangeListener interface)
     *
     * @param   ce                  Change event
     */
    public void stateChanged(ChangeEvent ce) {
        Object source = ce.getSource();
        if (source instanceof JTabbedPane) {

            //
            // Rebuild the position table if the user has selected the Positions tab
            //
            try {
                JTabbedPane tabbedPane = (JTabbedPane)source;
                int index = tabbedPane.getSelectedIndex();
                if (index == 1 && index != activeTabbedPane) {
                    positionModel.updatePositions();
                    updateChartData();
                }

                activeTabbedPane = index;
            } catch (Exception exc) {
                Main.logException("Exception while processing state change", exc);
            }
        }
    }

    /**
     * Investment account position table model
     */
    private class PositionModel extends AbstractTableModel {

        /** List data */
        private List<SecurityHolding> listData;

        /**
         * Create the investment account position table model
         */
        public PositionModel() {
            listData = new ArrayList<>(SecurityRecord.securities.size());
            buildPositions();
        }

        /**
         * Get the number of columns in the table
         *
         * @return                  The number of columns
         */
        public int getColumnCount() {
            return positionNames.length;
        }

        /**
         * Get the column class
         *
         * @param       column      Column number
         * @return                  The column class
         */
        public Class<?> getColumnClass(int column) {
            return positionClasses[column];
        }

        /**
         * Get the column name
         *
         * @param       column      Column number
         * @return                  Column name
         */
        public String getColumnName(int column) {
            return positionNames[column];
        }

        /**
         * Get the number of rows in the table
         *
         * @return                  The number of rows
         */
        public int getRowCount() {
            return listData.size();
        }

        /**
         * Check if the specified cell is editable
         *
         * @param       row         Row number
         * @param       column      Column number
         * @return                  TRUE if the cell is editable
         */
        public boolean isCellEditable(int row, int column) {
            boolean editable;
            if (column == 3)
                editable = true;
            else
                editable = false;

            return editable;
        }

        /**
         * Get the value for a cell
         *
         * @param       row         Row number
         * @param       column      Column number
         * @return                  Returns the object associated with the cell
         */
        public Object getValueAt(int row, int column) {
            if (row >= listData.size())
                throw new IndexOutOfBoundsException("Table row "+row+" is not valid");

            SecurityHolding h = listData.get(row);
            Object value;

            switch (column) {
                case 0:                             // Security
                    value = h.getSecurity().getName();
                    break;

                case 1:                             // Symbol
                    value = h.getSecurity().getSymbol();
                    break;

                case 2:                             // Shares
                    value = new Double(h.getTotalShares());
                    break;

                case 3:                             // Price
                    value = new Double(h.getSecurity().getPrice());
                    break;

                case 4:                             // Value
                    value = new Double(h.getSecurity().getPrice()*h.getTotalShares());
                    break;
                    
                case 5:                             // Basis
                    value = new Double(h.getTotalCost());
                    break;
                    
                case 6:                             // Gain
                    value = new Double((h.getSecurity().getPrice()*h.getTotalShares()-h.getTotalCost())/h.getTotalCost());
                    break;

                case 7:                             // Income
                    value = new Double(h.getAnnualYield());
                    break;

                case 8:                             // Purchase date
                    value = h.getPurchaseDate();
                    break;

                default:
                    throw new IndexOutOfBoundsException("Table column "+column+" is not valid");
            }

            return value;
        }

        /**
         * Set the value for a cell
         *
         * @param       value       Cell value
         * @param       row         Row number
         * @param       column      Column number
         */
        public void setValueAt(Object value, int row, int column) {
            if (row >= listData.size())
                throw new IndexOutOfBoundsException("Table row "+row+" is not valid");

            SecurityHolding h = listData.get(row);

            switch (column) {
                case 3:                             // Price
                    if (value != null) {
                        double amount = ((Double)value).doubleValue();
                        h.getSecurity().setPrice(amount);
                    }
                    break;

                default:
                    throw new IndexOutOfBoundsException("Table column "+column+" is not valid");
            }

            fireTableRowsUpdated(row, row);
            Main.dataModified = true;
            Main.mainWindow.setTitle(null);
        }

        /**
         * Get the security associated with a table row
         *
         * @param       row         Table row
         * @return                  Security record
         */
        public SecurityRecord getSecurityAt(int row) {
            if (row >= listData.size())
                throw new IndexOutOfBoundsException("Table row "+row+" is not valid");

            SecurityHolding h = listData.get(row);
            return h.getSecurity();
        }

        /**
         * Get the number of shares associated with a table row
         *
         * @param       row         Table row
         * @return                  Number of shares
         */
        public double getSharesAt(int row) {
            if (row >= listData.size())
                throw new IndexOutOfBoundsException("Table row "+row+" is not valid");

            SecurityHolding h = listData.get(row);
            return h.getTotalShares();
        }

        /**
         * Update the position table
         */
        public void updatePositions() {
            buildPositions();
            fireTableDataChanged();
        }

        /**
         * Build the positions list for the account
         */
        private void buildPositions() {

            //
            // Remove all of the existing security holdings
            //
            listData.clear();

            //
            // Run through the transactions and accumulate the positions
            //
            for (TransactionRecord t : TransactionRecord.transactions) {
                if (t.getAccount() == account)
                    SecurityHolding.updateSecurityHolding(listData, t);
            }

            //
            // Remove positions with no shares (this happens when there are
            // matching buy and sell transactions)
            //
            ListIterator<SecurityHolding> lit = listData.listIterator();
            while (lit.hasNext()) {
                SecurityHolding h = lit.next();
                if (Math.abs(h.getTotalShares()) < 0.00005)
                    lit.remove();
            }
        }
    }
}
