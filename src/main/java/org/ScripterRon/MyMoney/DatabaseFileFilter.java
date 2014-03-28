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
