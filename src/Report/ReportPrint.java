package Report;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.print.*;

import javax.swing.*;
import javax.swing.event.*;

/**
 * Print the report.
 */
public final class ReportPrint {

    /** The report state */
    private ReportState state;

    /**
     * Create a new report print
     *
     * @param       state           The report state
     */
    public ReportPrint(ReportState state) {
        this.state = state;
    }

    /**
     * Print the report
     */
    public void printReport() throws ReportException {

        //
        // Build the report
        //
        ReportPage reportPage = new ReportPage(state);
        int numPages = reportPage.paginate();
        if (numPages == 0) {
            JOptionPane.showMessageDialog(null, "The report is empty", "Information",
                                          JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        //
        // Get a printer job
        //
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        if (printerJob.getPrintService() == null) {
            JOptionPane.showMessageDialog(null, "No printer available", "Error",
                                          JOptionPane.ERROR_MESSAGE);
            throw new ReportException("No printer available");
        }

        //
        // Set up our pageable document
        //
        printerJob.setPageable(reportPage);

        //
        // Display the printer dialog
        //
        boolean printPages = printerJob.printDialog();

        //
        // Print the document
        //
        if (printPages) {
            try {
                printerJob.print();
            } catch (PrinterException exc) {
                throw new ReportException("Printer exception while printing report", exc);
            }
        }
    }
}

