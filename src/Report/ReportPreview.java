package Report;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.print.*;

import javax.swing.*;
import javax.swing.event.*;

import java.net.URL;

/**
 * Display the report preview
 */
public final class ReportPreview extends JDialog implements ActionListener {

    /** The report state */
    private ReportState state;

    /** The report view */
    private ReportView reportView;

    /** The report pages */
    private ReportPage reportPage;

    /** The screen resolution in DPI */
    private int screenResolution;

    /** The adjustment from points to pixels */
    private double screenAdjustment;

    /** The number of pages in the report */
    private int numPages = 0;

    /** The current page index */
    private int pageIndex = 0;

    /** The view zoom factor */
    private int zoomFactor = 100;

    /** The view size in pixels */
    private Dimension viewSize = new Dimension(0, 0);

    /** The zoom size in pixels */
    private Dimension zoomSize = new Dimension(0, 0);

    /**
     * Create the report preview dialog when the parent is a JFrame
     *
     * @param       parent          Parent frame
     * @param       state           Report state
     */
    public ReportPreview(JFrame parent, ReportState state) {
        super(parent, state.getReportTitle(), true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        initPreview(parent, state);
    }

    /**
     * Create the report preview dialog when the parent is a JDialog
     *
     * @param       parent          Parent dialog
     * @param       state           Report state
     */
    public ReportPreview(JDialog parent, ReportState state) {
        super(parent, state.getReportTitle(), true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        initPreview(parent, state);
    }

    /**
     * Initialize the report preview
     */
    private void initPreview(Component parent, ReportState state) {

        //
        // Save the report state
        //
        this.state = state;

        //
        // Create the report view
        //
        reportView = new ReportView();
        reportView.setOpaque(true);

        //
        // Create the scroll pane containing the report view
        //
        JScrollPane scrollPane = new JScrollPane(reportView);

        //
        // Create the dialog tool bar using both icons and text descriptions
        //
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        URL imageURL;
        JButton button;

        imageURL = ReportPreview.class.getResource("/images/Back24.gif");
        button = new JButton("Back");
        button.setActionCommand("back");
        button.addActionListener(this);
        if (imageURL != null)
            button.setIcon(new ImageIcon(imageURL));
        toolBar.add(button);

        imageURL = ReportPreview.class.getResource("/images/Forward24.gif");
        button = new JButton("Forward");
        button.setActionCommand("forward");
        button.addActionListener(this);
        if (imageURL != null)
            button.setIcon(new ImageIcon(imageURL));
        toolBar.add(button);
        toolBar.addSeparator();

        imageURL = ReportPreview.class.getResource("/images/ZoomIn24.gif");
        button = new JButton("Zoom In");
        button.setActionCommand("zoom in");
        button.addActionListener(this);
        if (imageURL != null)
            button.setIcon(new ImageIcon(imageURL));
        toolBar.add(button);

        imageURL = ReportPreview.class.getResource("/images/ZoomOut24.gif");
        button = new JButton("Zoom Out");
        button.setActionCommand("zoom out");
        button.addActionListener(this);
        if (imageURL != null)
            button.setIcon(new ImageIcon(imageURL));
        toolBar.add(button);
        toolBar.addSeparator();

        imageURL = ReportPreview.class.getResource("/images/Print24.gif");
        button = new JButton("Print");
        button.setActionCommand("print");
        button.addActionListener(this);
        if (imageURL != null)
            button.setIcon(new ImageIcon(imageURL));
        toolBar.add(button);
        toolBar.addSeparator();

        //
        // Get the display configuration
        //
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();

        Dimension screenSize = toolkit.getScreenSize();
        int baseX = 0;
        int baseY = 0;
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        screenResolution = toolkit.getScreenResolution();
        screenAdjustment = (double)screenResolution/72.0;

        Insets screenInsets = toolkit.getScreenInsets(gc);
        if (screenInsets != null) {
            baseX = screenInsets.left;
            baseY = screenInsets.top;
            screenWidth -= screenInsets.left+screenInsets.right;
            screenHeight -= screenInsets.top+screenInsets.bottom;
        }

        //
        // Size the report view based on the page format and the
        // screen resolution
        //
        PageFormat pageFormat = state.getPageFormat();
        viewSize.setSize(pageFormat.getWidth()*screenAdjustment,
                         pageFormat.getHeight()*screenAdjustment);
        zoomSize.setSize(viewSize);
        reportView.setPreferredSize(viewSize);
        reportView.setMinimumSize(viewSize);

        //
        // Set the viewport size based on the screen size
        //
        JViewport viewPort = scrollPane.getViewport();
        Dimension scrollSize = viewPort.getPreferredSize();
        scrollSize.setSize(Math.min(scrollSize.width, screenWidth-75),
                           Math.min(scrollSize.height, screenHeight-75));
        viewPort.setPreferredSize(scrollSize);

        //
        // Create the content pane containing the tool bar and the scroll pane
        //
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setOpaque(true);
        contentPane.add(toolBar, BorderLayout.NORTH);
        contentPane.add(scrollPane, BorderLayout.CENTER);
        setContentPane(contentPane);

        //
        // Position the dialog window relative to the parent and ensure that
        // it will fit within the screen insets
        //
        pack();
        setLocationRelativeTo(parent);
        Rectangle bounds = getBounds();
        if (bounds.x-baseX+bounds.width > screenWidth)
            bounds.setLocation(baseX, bounds.y);

        if (bounds.y-baseY+bounds.height > screenHeight)
            bounds.setLocation(bounds.x, baseY);

        setBounds(bounds);

        //
        // Build the report
        //
        reportPage = new ReportPage(state);
        numPages = reportPage.paginate();

        //
        // Display the first page
        //
        if (numPages != 0) {
            displayPage();
            setTitle(state.getReportTitle()+" - Page 1 of "+numPages);
        }
    }

    /**
     * Action performed (ActionListener interface)
     *
     * @param       ae              Action event
     */
    public void actionPerformed(ActionEvent ae) {
        String action = ae.getActionCommand();
        boolean reshowPage = false;
        boolean resizeView = false;

        if (action.equals("back")) {

            //
            // Show the previous report page
            //
            if (pageIndex > 0) {
                pageIndex--;
                reshowPage = true;
            }

        } else if (action.equals("forward")) {

            //
            // Show the next report page
            //
            if (pageIndex < numPages) {
                pageIndex++;
                reshowPage = true;
            }

        } else if (action.equals("zoom in")) {

            //
            // Zoom in on the current report page
            //
            // The zoom factor will be incremented by 20% until it reaches 160%
            //
            if (zoomFactor < 160) {
                zoomFactor += 20;
                reshowPage = true;
                resizeView = true;
            }

        } else if (action.equals("zoom out")) {

            //
            // Zoom out on the current report page
            //
            // The zoom factor will be decreased by 20% until it reaches 60%
            //
            if (zoomFactor > 60) {
                zoomFactor -= 20;
                reshowPage = true;
                resizeView = true;
            }

        } else if (action.equals("print")) {

            //
            // Print the report
            //
            ReportPrint reportPrint = new ReportPrint(state);

            try {
                reportPrint.printReport();
            } catch (ReportException exc) {
                StringBuilder string = new StringBuilder(512);
                string.append("<html><b>Exception while printing report</b><br><br>");
                StackTraceElement[] trace = exc.getStackTrace();
                for (StackTraceElement elem : trace) {
                    string.append(elem.toString());
                    string.append("<br>");
                }

                string.append("</html>");
                JOptionPane.showMessageDialog(this, string, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        //
        // Reshow the report page if it has changed
        //
        if (numPages != 0 && reshowPage) {
            if (resizeView) {
                if (zoomFactor == 100)
                    zoomSize.setSize(viewSize);
                else
                    zoomSize.setSize((viewSize.width*zoomFactor)/100,
                                     (viewSize.height*zoomFactor)/100);

                reportView.setPreferredSize(zoomSize);
                reportView.setMinimumSize(zoomSize);
                reportView.revalidate();
            }

            displayPage();
            setTitle(String.format("%s - Page %d of %d", state.getReportTitle(),
                                   pageIndex+1, numPages));
        }
    }

    /**
     * Display the current page
     */
    private void displayPage() {

        //
        // Create the buffered image for the page
        //
        BufferedImage image = reportView.getPageBuffer();

        //
        // Create the graphics context for the page
        //
        Graphics2D g = image.createGraphics();

        //
        // Set the scale transform based on the current zoom factor and the
        // screen resolution
        //
        double scaleFactor = ((double)zoomFactor/100.0)*screenAdjustment;
        g.scale(scaleFactor, scaleFactor);

        //
        // Render the page
        //
        reportPage.print(g, state.getPageFormat(), pageIndex);

        //
        // Display the page
        //
        reportView.setPageBuffer(image);
        if (reportView.isVisible())
            reportView.repaint();
    }
}

