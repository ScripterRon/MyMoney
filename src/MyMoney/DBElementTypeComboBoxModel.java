package MyMoney;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * The DBElementTypeComboBoxModel class provides a combo box model based on an
 * array of database element types.  The type strings will be sorted before being
 * displayed in the combo box.  The getTypeAt() method should be called to get the
 * element type associated with a specific combo box list index.
 */
public final class DBElementTypeComboBoxModel extends AbstractListModel implements ComboBoxModel {
    
    /** The element types */
    private int[] elementTypes;
    
    /** The element type strings */
    private String[] elementTypeStrings;
    
    /** Selected element index */
    private int elementIndex = -1;
    
    /**
     * Create a new combo box model
     *
     * @param       types           Array of element types
     * @param       typeStrings     Array of strings corresponding to the element types
     */
    public DBElementTypeComboBoxModel(int[] types, String[] typeStrings) {
        if (types.length != typeStrings.length)
            throw new IllegalArgumentException("Element types array length mismatch");
    
        //
        // Clone the database element types
        //
        elementTypes = new int[types.length];
        System.arraycopy(types, 0, elementTypes, 0, types.length);
        
        //
        // Clone the element type strings
        //
        elementTypeStrings = new String[typeStrings.length];
        System.arraycopy(typeStrings, 0, elementTypeStrings, 0, typeStrings.length);
        
        //
        // Sort the element type strings
        //
        for (int i=0; i<elementTypes.length; i++) {
            for (int j=0; j<i; j++) {
                if (elementTypeStrings[i].compareTo(elementTypeStrings[j]) < 0) {
                    String t = elementTypeStrings[i];
                    elementTypeStrings[i] = elementTypeStrings[j];
                    elementTypeStrings[j] = t;
                    int k = elementTypes[i];
                    elementTypes[i] = elementTypes[j];
                    elementTypes[j] = k;
                }
            }
        }
    }

    /**
     * Get the number of elements in the list
     *
     * @return                      The number of elements in the list
     */
    public int getSize() {
        return elementTypeStrings.length;
    }

    /**
     * Get the element at the specified index
     *
     * @param       index           List element index
     * @return                      The element type string
     */
    public Object getElementAt(int index) {
        return (index>=0 ? elementTypeStrings[index] : null);
    }
    
    /**
     * Get the element type corresponding to the specified index
     *
     * @param       index           List element index
     * @return                      The element type or -1
     */
    public int getTypeAt(int index) {
        return (index>=0 ? elementTypes[index] : -1);
    }
    
    /**
     * Get the selected element
     *
     * @return                      The selected element or null if there is no selection
     */
    public Object getSelectedItem() {
        return (elementIndex>=0 ? elementTypeStrings[elementIndex] : null);
    }
    
    /**
     * Set the selected element
     *
     * @param       element         The selected element or null to clear the selection
     */
    public void setSelectedItem(Object element) {
        if (element == null) {
            if (elementIndex != -1) {
                elementIndex = -1;
                fireContentsChanged(this, -1, -1);
            }
        } else if (element instanceof String) {
            for (int i=0; i<elementTypeStrings.length; i++) {
                if (elementTypeStrings[i].equals((String)element)) {
                    if (elementIndex != i) {
                        elementIndex = i;
                        fireContentsChanged(this, -1, -1);
                    }
                    
                    break;
                }
            }
        }
    }
}
