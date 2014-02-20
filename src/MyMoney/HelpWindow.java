package MyMoney;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.IOException;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.event.HyperlinkEvent.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Display the Help window as a separate frame.  The help text is contained
 * in the jar file as HTML files.
 */
public class HelpWindow extends JFrame implements ActionListener, 
                                                  HyperlinkListener,
                                                  PropertyChangeListener {
    
    /** Help contents */
    public static final String CONTENTS = "/help/Contents.html";
    
    /** Help for accounts */
    public static final String ACCOUNTS = "/help/AccountObject.html";
    
    /** Help for categories */
    public static final String CATEGORIES = "/help/CategoryObject.html";
    
    /** Help for securities */
    public static final String SECURITIES = "/help/SecurityObject.html";
    
    /** Help for schedules */
    public static final String SCHEDULES = "/help/ScheduleObject.html";
    
    /** Help for archiving */
    public static final String ARCHIVE_FILE = "/help/ArchiveFile.html";
    
    /** Help for asset accounts */
    public static final String ASSET_ACCOUNT = "/help/AssetAccount.html";
    
    /** Help for bank accounts */
    public static final String BANK_ACCOUNT = "/help/BankAccount.html";
    
    /** Help for credit card accounts */
    public static final String CREDIT_ACCOUNT = "/help/CreditAccount.html";
    
    /** Help for investment accounts */
    public static final String INVESTMENT_ACCOUNT = "/help/InvestmentAccount.html";
    
    /** Help for loan accounts */
    public static final String LOAN_ACCOUNT = "/help/LoanAccount.html";
    
    /** Help window is minimized */
    private boolean windowMinimized = false;
    
    /** Help text pane */
    private JEditorPane editorPane;
    
    /** Help text scroll pane */
    private JScrollPane scrollPane;
    
    /** History list */
    private List<URL> historyList;
    
    /** Current history index */
    private int historyIndex = -1;
    
    /**
     * Create the help window
     * 
     * @param       initialPage     Initial help page
     */
    public HelpWindow(String initialPage) {
        
        //
        // Create the help frame
        //
        super("MyMoney Help");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        //
        // Position the window using the saved position from the last time
        // the program was run.  Default position is the upper left corner
        // of the screen.
        //
        int frameX = 0;
        int frameY = 0;
        String propValue = Main.properties.getProperty("window.help.position");
        if (propValue != null) {
            int sep = propValue.indexOf(',');
            frameX = Integer.parseInt(propValue.substring(0, sep));
            frameY = Integer.parseInt(propValue.substring(sep+1));
        }

        setLocation(frameX, frameY);

        //
        // Size the window using the saved size from the last time
        // the program was run.  The minimum size is 600x600.
        //
        int frameWidth = 600;
        int frameHeight = 600;
        propValue = Main.properties.getProperty("window.help.size");
        if (propValue != null) {
            int sep = propValue.indexOf(',');
            frameWidth = Math.max(frameWidth, Integer.parseInt(propValue.substring(0, sep)));
            frameHeight = Math.max(frameHeight, Integer.parseInt(propValue.substring(sep+1)));
        }

        setPreferredSize(new Dimension(frameWidth, frameHeight));
        
        //
        // Create the history list
        //
        historyList = new ArrayList<>(25);
        
        //
        // Create the help text pane
        //
        editorPane = new JEditorPane();
        editorPane.setEditable(false);
        editorPane.addHyperlinkListener(this);
        editorPane.addPropertyChangeListener(this);
        
        //
        // Create the help text scroll pane
        //
        scrollPane = new JScrollPane(editorPane);

        //
        // Create the button pane (Back, Contents)
        //
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));

        JButton button = new JButton("Back");
        button.setActionCommand("back");
        button.addActionListener(this);
        buttonPane.add(button);
        
        buttonPane.add(Box.createHorizontalStrut(10));
        
        button = new JButton("Contents");
        button.setActionCommand("contents");
        button.addActionListener(this);
        buttonPane.add(button);
        getRootPane().setDefaultButton(button);
        
        //
        //  Set the initial help page
        //
        setPage(initialPage);
        
        //
        // Set up the content pane
        //
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.setOpaque(true);
        contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPane.add(scrollPane);
        contentPane.add(Box.createVerticalStrut(15));
        contentPane.add(buttonPane);
        setContentPane(contentPane);
        
        //
        // Receive WindowListener events
        //
        addWindowListener(new HelpWindowListener(this));
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
        // "back" - Display the previous page in the help history
        // "contents" - Display the help contents
        //
        switch (ae.getActionCommand()) {
            case "back":
                if (historyIndex > 0) {
                    URL url = historyList.get(historyIndex-1);
                    historyIndex -= 2;
                    setPage(url);
                }
                break;

            case "contents":
                setPage(CONTENTS);
                break;
        }
    }
    
    /**
     * Hyperlink update occurred (HyperlinkListener interface)
     * 
     * @param       he              Hyperlink event
     */
    public void hyperlinkUpdate(HyperlinkEvent he) {
        
        //
        // Change the help page if the user clicked on a document hyperlink
        //
        if (he.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
            setPage(he.getURL());
    }
    
    /**
     * Document property changed (PropertyChangeListener interface)
     * 
     * @param       pe              Property change event
     */
    public void propertyChange(PropertyChangeEvent pe) {
        
        //
        // Add the new URL to the history list if the page has changed
        //
        // We can't do this when we set the document page because JEditorPane
        // will asynchronously load the HTML document.  Thus we won't know if
        // the load is successful until we get the page change notification.
        // No notification is sent if the page fails to load (JEditorPane
        // sounds an error tone instead)
        //
        if (pe.getSource() == editorPane && pe.getPropertyName().equals("page")) {
            URL url = (URL)pe.getNewValue();
            if (historyIndex < 0 || !historyList.get(historyIndex).equals(url)) {
                historyList.add(url);
                historyIndex++;
            }
        }
    }
    
    /**
     * Set the help page using the help name
     * 
     * @param       page            Help page to display
     */
    public void setPage(String page) {
        URL url = HelpWindow.class.getResource(page);
        if (url != null)
            setPage(url);
        else
            JOptionPane.showMessageDialog(this, "Help page "+page+" not found",
                                          "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Set the help page using the help URL
     * 
     * @param       url             Help page to display
     */
    public void setPage(URL url) {
        
        //
        // Remove history list entries after the current entry and then
        // set the new help page.  The history list will be updated when
        // we receive the page changed notification.
        //
        try {
            trimHistory();
            editorPane.setPage(url);
        } catch (IOException exc) {
            Main.logException("Unable to display help page", exc);
        }
    }
    
    /**
     * Trim the history list following the current index
     */
    private void trimHistory() {
        int index = historyList.size()-1;
        while (index > historyIndex) {
            historyList.remove(index);
            index--;
        }
    }
    
    /**
     * Listen for window events
     */
    private class HelpWindowListener extends WindowAdapter {
        
        /** Help window */
        private JFrame window;
        
        /**
         * Create the window listener
         *
         * @param       window      The application window
         */
        public HelpWindowListener(JFrame window) {
            this.window = window;
        }

        /**
         * Window has been minimized (WindowListener interface)
         *
         * @param       we              Window event
         */
        public void windowIconified(WindowEvent we) {
            windowMinimized = true;
        }

        /**
         * Window has been restored (WindowListener interface)
         *
         * @param       we              Window event
         */
        public void windowDeiconified(WindowEvent we) {
            windowMinimized = false;
        }

        /**
         * Window is closing (WindowListener interface)
         *
         * @param       we              Window event
         */
        public void windowClosing(WindowEvent we) {
            
            //
            // Remember the current window position and size unless the window
            // is minimized
            //
            if (!windowMinimized) {
                Point p = window.getLocation();
                Dimension d = window.getSize();
                Main.properties.setProperty("window.help.position", p.x+","+p.y);
                Main.properties.setProperty("window.help.size", d.width+","+d.height);
            }
            
            //
            // The help window is closed
            //
            Main.helpWindow = null;
        }
    }
}
