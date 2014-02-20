package MyMoney;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * The DBElementComboBoxModel class provides a combo box model based on a sorted set of
 * database elements.  The combo box data is the name returned by the getName() method.
 * 
 * Use the getDBElementAt() method to get the database element corresponding to
 * a specific selection index.
 */
public final class DBElementComboBoxModel extends AbstractListModel implements ComboBoxModel {
    
    /** The list data */
    private List<DBElement> elementList;
    
    /** Selected element index */
    private int elementIndex = -1;
    
    /**
     * Create a new list model.  Hidden elements will not be included in the list.
     *
     * @param       elementSet      The database element set
     */
    public DBElementComboBoxModel(SortedSet<? extends DBElement> elementSet) {
        elementList = new ArrayList<>(elementSet.size());
        for (DBElement element : elementSet)
            if (!element.isHidden())
                elementList.add(element);
    }

    /**
     * Create a new list model.  A hidden element will be included in the list
     * if it is the current element.
     *
     * @param       elementSet      The database element set
     * @param       currentElement  The current element
     */
    public DBElementComboBoxModel(SortedSet<? extends DBElement> elementSet, DBElement currentElement) {
        elementList = new ArrayList<>(elementSet.size());
        for (DBElement element : elementSet)
            if (!element.isHidden() || element == currentElement)
                elementList.add(element);        
    }
    
    /**
     * Create a new list model using a subclass of the supplied database
     * elements.  Hidden elements will not be included in the list.
     * 
     * @param       elementSet      The database element set
     * @param       elementType     The element type
     */
    public DBElementComboBoxModel(SortedSet<? extends DBElement> elementSet, int elementType) {
        elementList = new ArrayList<>(elementSet.size());
        for (DBElement element : elementSet)
            if (!element.isHidden() && element.getType() == elementType)
                elementList.add(element);
    }
    
    /**
     * Get the number of elements in the list
     *
     * @return                      The number of elements in the list
     */
    public int getSize() {
        return elementList.size();
    }

    /**
     * Get the element at the specified index
     *
     * @param       index           List element index
     * @return                      The element type string
     */
    public Object getElementAt(int index) {
        return (index>=0&&index<elementList.size() ? elementList.get(index).getName() : null);
    }
    
    /**
     * Get the database element corresponding to the specified index
     *
     * @param       index           List element index
     * @return                      The database element
     */
    public DBElement getDBElementAt(int index) {
        return (index>=0&&index<elementList.size() ? elementList.get(index) : null);
    }
    
    /**
     * Get the selected element
     *
     * @return                      The selected element or null if there is no selection
     */
    public Object getSelectedItem() {
        return getElementAt(elementIndex);
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
            int index = 0;
            for (DBElement e : elementList) {
                if (e.getName().equals((String)element)) {
                    if (elementIndex != index) {
                        elementIndex = index;
                        fireContentsChanged(this, -1, -1);
                    }
                    
                    break;
                }
                
                index++;
            }
        }
    }
}
