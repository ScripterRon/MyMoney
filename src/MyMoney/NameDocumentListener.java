package MyMoney;

import java.util.Iterator;
import java.util.SortedSet;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * The NameDocumentListener class provides type-ahead for the transaction name.
 * As the user enters characters in the name field, the text will be updated 
 * with the first matching selection from the supplied name list.  The type-ahead
 * characters will be updated as the user continues to enter data into the name field.
 */
public final class NameDocumentListener implements DocumentListener, Runnable {
    
    /** The text field */
    private JTextField nameField;
    
    /** The set of names */
    private SortedSet<String> nameList;

    /** The current text */
    private String currentText = "";
    
    /** Ignore document events */
    private boolean ignoreEvents = false;
    
    /** 
     * Create a new document listener for the name text field
     *
     * @param       field           The text field to be monitored
     * @param       names           The suggested names
     */
    public NameDocumentListener(JTextField field, SortedSet<String> names) {
        nameField = field;
        nameList = names;
    }
    
    /**
     * Create a new document listener instance and add it to the
     * document listeners for the supplied text field.
     * 
     * @param       field           The text field to be monitored
     * @param       names           The suggested names
     */
    public static void addInstance(JTextField field, SortedSet<String> names) {
        NameDocumentListener listener = new NameDocumentListener(field, names);
        field.getDocument().addDocumentListener(listener);
    }
    
    /**
     * Attribute changed (DocumentListener interface)
     * 
     * @param       event           Document event
     */
    public void changedUpdate(DocumentEvent e) {
    }
    
    /**
     * Text inserted into document (DocumentListener interface)
     * 
     * @param       event           Document event
     */
    public void insertUpdate(DocumentEvent e) {
        if (!ignoreEvents) {
            ignoreEvents = true;
            SwingUtilities.invokeLater(this);
        }
    }
    
    /**
     * Text removed from document (DocumentListener interface)
     * 
     * @param       event           Document event
     */
    public void removeUpdate(DocumentEvent e) {
        if (!ignoreEvents) {
            ignoreEvents = true;
            SwingUtilities.invokeLater(this);
        }
    }
    
    /**
     * Update the text based on what has been entered so far
     *
     * This method is called after all pending events have been processed
     * (the document cannot be changed while processing a document event)
     */
    public void run() {
            
        //
        // Get the updated text field
        //
        String text = nameField.getText();
        
        //
        // Suggest a name if the text has changed.  The new name will start
        // with the characters entered so far and the caret will be
        // positioned at the start of the additional characters (which will
        // be selected and highlighted).
        //
        // No suggestion will be made if the entered text matches a name in
        // the suggestion list or if there is no name starting with the
        // entered characters.
        //
        if (!text.equals(currentText)) {
            Iterator<String> nameIterator = nameList.iterator();
            while (nameIterator.hasNext()) {
                String name = nameIterator.next();
                if (name.equals(text))
                    break;
                
                if (name.startsWith(text)) {
                    nameField.setText(name);
                    nameField.setCaretPosition(name.length());
                    nameField.moveCaretPosition(text.length());
                    break;
                }
                
                if (name.compareTo(text) > 0)
                    break;
            }
            
            currentText = text;
        }
        
        //
        // Allow document events to be processed
        //
        ignoreEvents = false;
    }
}
