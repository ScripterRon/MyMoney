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

