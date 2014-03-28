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

