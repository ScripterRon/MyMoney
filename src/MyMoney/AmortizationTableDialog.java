package MyMoney;

import java.text.MessageFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 *  Display the bond accretion/amortization table and allow the user to print it
 */
public final class AmortizationTableDialog extends JDialog implements ActionListener {

    /** Amortization table column classes */
    private static final Class<?>[] columnClasses = {
        Date.class, Double.class, Double.class, Double.class};

    /** Amortization table column names */
    private static final String[] columnNames = {
        "Date", "Interest", "Adjustment", "Basis"};

    /** Amortization table column types */
    private static final int[] columnTypes = {
        SizedTable.DATE_COLUMN, SizedTable.AMOUNT_COLUMN, 
        SizedTable.AMOUNT_COLUMN, SizedTable.AMOUNT_COLUMN};

    /** Amortization table */
    private JTable table;
    
    /** Amortization table scroll pane */
    private JScrollPane scrollPane;
    
    /** Amortization table model */
    private AmortizationTableModel tableModel;
    
    /** Print header */
    private MessageFormat printHeader;

    /*
     * Create the dialog
     * 
     * @param   parent              Parent dialog
     * @param   bondName            Bond name
     * @param   faceValue           Face value
     * @param   couponYield         Coupon yield (%)
     * @param   purchaseDate        Purchase date
     * @param   maturityDate        Maturity date
     * @param   purchaseCost        Purchase cost
     * @param   accruedInterest     Accrued interest
     * @param   yieldToMaturity     Yield to maturity (%)
     * @param   paymentInterval     Payment interval (months)
     */
    public AmortizationTableDialog(JDialog parent,
                    String bondName, double faceValue, double couponYield,
                    Date purchaseDate, Date maturityDate,
                    double purchaseCost, double accruedInterest, double yieldToMaturity,
                    int paymentInterval) {
        super(parent, "Bond Amortization Table", true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setBackground(Color.white);
        
        //
        // Create the title containing the bond name, coupon yield and maturity date
        //
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(maturityDate);
        int maturityYear = calendar.get(Calendar.YEAR);
        String title = String.format("%s %.3f-%d", bondName, couponYield, maturityYear);
        printHeader = new MessageFormat(title);

        //
        // Create the title pane
        //
        JPanel titlePane = new JPanel();
        titlePane.setBackground(Color.white);
        JLabel nameLabel = new JLabel("<HTML><h1>"+title+"</h1></HTML>");
        titlePane.add(nameLabel);
        
        //
        // Create the buttons (Done, Print)
        //
        JPanel buttonPane = new JPanel();
        buttonPane.setBackground(Color.white);

        JButton button = new JButton("Done");
        button.setActionCommand("done");
        button.addActionListener(this);
        buttonPane.add(button);
        
        buttonPane.add(Box.createHorizontalStrut(10));

        button = new JButton("Print");
        button.setActionCommand("print");
        button.addActionListener(this);
        buttonPane.add(button);
        
        //
        // Create the amortization table model
        //
        tableModel = new AmortizationTableModel(columnNames, columnClasses,
                            faceValue, purchaseCost, couponYield,
                            purchaseDate, maturityDate,
                            accruedInterest, yieldToMaturity, paymentInterval);
        
        //
        // Create the amortization table
        //
        table = new SizedTable(tableModel, columnTypes);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Dimension tableSize = table.getPreferredSize();
        int rowHeight = table.getRowHeight();
        int tableRows = Math.max(5, (Main.mainWindow.getSize().height-230)/rowHeight);
        table.setPreferredScrollableViewportSize(new Dimension(tableSize.width, tableRows*rowHeight));

        //
        // Create the table scroll pane
        //
        scrollPane = new JScrollPane(table);
        
        //
        // Create the table pane
        //
        JPanel tablePane = new JPanel();
        tablePane.setBackground(Color.WHITE);
        tablePane.add(Box.createGlue());
        tablePane.add(scrollPane);
        tablePane.add(Box.createGlue());

        //
        // Set up the content pane
        //
        add(titlePane, BorderLayout.NORTH);
        add(tablePane, BorderLayout.CENTER);
        add(buttonPane, BorderLayout.SOUTH);
    }
    
    /**
     * Show the dialog
     *
     * @param   parent              Parent window
     * @param   bondName            Bond name
     * @param   faceValue           Face value
     * @param   couponYield         Coupon yield (%)
     * @param   purchaseDate        Purchase date
     * @param   maturityDate        Maturity date
     * @param   purchaseCost        Purchase cost
     * @param   accruedInterest     Accrued interest
     * @param   yieldToMaturity     Yield to maturity (%)
     * @param   paymentInterval     Payment interval (months)
     */
    public static void showDialog(JDialog parent,
                    String bondName, double faceValue, double couponYield,
                    Date purchaseDate, Date maturityDate,
                    double purchaseCost, double accruedInterest, double yieldToMaturity,
                    int paymentInterval) {
        try {
            JDialog dialog = new AmortizationTableDialog(parent,
                    bondName, faceValue, couponYield, purchaseDate, maturityDate,
                    purchaseCost, accruedInterest, yieldToMaturity, paymentInterval);
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
        try {
            switch (ae.getActionCommand()) {
                case "print":
                    table.print(JTable.PrintMode.FIT_WIDTH, printHeader, null);
                    break;
                    
                case "done":
                    setVisible(false);
                    dispose();
                    break;
            }
        } catch (Exception exc) {
            Main.logException("Exception while processing action event", exc);
        }
    }
}
