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

import java.text.ParseException;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

/**
 * AmountEditor is a cell editor for use with a JTable column.  It edits amounts
 * with a fixed number of decimal digits using a formatted text field
 * (JFormattedTextField).  The formatted text field uses EditNumber as the
 * formatter and EditInputVerifier as the input verifier.
 */
public final class AmountEditor extends DefaultCellEditor {

    /**
     * Create a new amount editor
     *
     * @param       decimalDigits       Number of decimal digits
     * @param       useGrouping         TRUE if grouping should be used
     */
    public AmountEditor(int decimalDigits, boolean useGrouping) {
        super(new JFormattedTextField(new EditNumber(decimalDigits, useGrouping)));
        JFormattedTextField textField = (JFormattedTextField)getComponent();
        textField.setHorizontalAlignment(JTextField.RIGHT);
        textField.setColumns(14);
        textField.setInputVerifier(new EditInputVerifier(true));
    }

    /**
     * Get the cell editor component (TableCellEditor interface).  The
     * formatted text field value will be set to the supplied value and
     * then the formatted text field component will be returned.
     *
     * @return                      The formatted text field component
     */
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected,
                                                 int row, int column) {
        JFormattedTextField textField = (JFormattedTextField)getComponent();
        textField.setValue(value);
        return textField;
    }

    /**
     * Get the cell editor value.  The current formatted text field value
     * will be returned as the cell editor value.
     *
     * @return                      The text field value
     */
    public Object getCellEditorValue() {
        JFormattedTextField textField = (JFormattedTextField)getComponent();
        return textField.getValue();
    }

    /**
     * Stop editing the cell.  The current formatted text field value will
     * be committed.  FALSE will be returned if the commit failed and editing
     * will not be stopped.  Otherwise, TRUE will be returned to indicate
     * editing is stopped.
     *
     * @return                      TRUE if editing is stopped
     */
    public boolean stopCellEditing() {
        boolean editingStopped = true;
        JFormattedTextField textField = (JFormattedTextField)getComponent();

        try {
            textField.commitEdit();
            fireEditingStopped();
        } catch (ParseException exc) {
            editingStopped = false;
            UIManager.getLookAndFeel().provideErrorFeedback(textField);
        }

        return editingStopped;
    }
}

