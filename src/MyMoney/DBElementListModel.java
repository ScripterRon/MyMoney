package MyMoney;

import java.util.SortedSet;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * The DBElementListModel class provides a list model based on a sorted set of
 * database elements.  The list data is the name returned by the getName() method.
 * "[H]" will be appended to the name if the element is hidden.
 */
public final class DBElementListModel extends AbstractListModel {
    
    /** The database elements */
    private SortedSet<? extends DBElement> elementSet;
    
    /** The list data */
    private DBElement[] listData;
    
    /** 
     * Create a new list model.
     *
     * @param       elementSet      The database element set
     */
    public DBElementListModel(SortedSet<? extends DBElement> elementSet) {
        this.elementSet = elementSet;
        listData = new DBElement[elementSet.size()];
        if (listData.length != 0)
            listData = elementSet.toArray(listData);
    }

    /**
     * Get the number of elements in the list model
     *
     * @return                      The number of elements in the list model
     */
    public int getSize() {
        return listData.length;
    }

    /**
     * Get the list element at the specified index
     *
     * @param       index           List element index
     * @return                      The name associated with the list element
     */
    public Object getElementAt(int index) {
        DBElement element = listData[index];
        String name = element.getName();
        if (element.isHidden())
            name = name.concat(" [H]");
        
        return name;
    }

    /**
     * Get the database element at the specified index
     *
     * @param       index           List element index
     * @return                      DBElement associated with the list element
     */
    public DBElement getDBElementAt(int index) {
        return listData[index];
    }

    /**
     * A database element has been added to the set
     *
     * @param       element         New database element
     */
    public void addDBElement(DBElement element) {
        listData = new DBElement[elementSet.size()];
        listData = elementSet.toArray(listData);
        for (int index=0; index<listData.length; index++) {
            if (listData[index] == element) {
                fireIntervalAdded(this, index, index);
                break;
            }
        }
    }

    /**
     * A database element has been updated
     *
     * We need to rebuild the element list since the list is
     * sorted by the element name and the name can be modified, thus
     * affecting all of the index values
     */
    public void updateDBElement() {
        listData = elementSet.toArray(listData);
        fireContentsChanged(this, 0, listData.length-1);
    }

    /**
     * A database element has been removed
     *
     * @param       element         Deleted database element
     */
    public void removeDBElement(DBElement element) {
        int index;
        for (index=0; index<listData.length; index++)
            if (listData[index] == element)
                break;

        listData = new DBElement[elementSet.size()];
        if (listData.length != 0)
            listData = elementSet.toArray(listData);

        fireIntervalRemoved(this, index, index);
    }
}
