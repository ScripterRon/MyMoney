package MyMoney;

import java.io.*;
import javax.swing.*;

/**
 * This file chooser filter selects database files (*.database)
 */
public final class DatabaseFileFilter extends javax.swing.filechooser.FileFilter {
    
    /**
     * Create a new file filter
     */
    public DatabaseFileFilter() {
        super();
    }

    /**
     * Return the filter description
     *
     * @return                  String describing the file filter
     */
    public String getDescription() {
        String text = null;
        return "Database Files (*.database)";
    }
    
    /**
     * Accept or reject a file.
     *
     * @param       file            Current file
     * @return                      TRUE to accept the file
     */
    public boolean accept(File file) {
        boolean accept = false;
        if (file.isFile()) {
            String name = file.getName();
            int sep = name.lastIndexOf('.');
            if (sep > 0 && name.substring(sep).equalsIgnoreCase(".database"))
                accept = true;
        }
        
        return accept;
    }
}
