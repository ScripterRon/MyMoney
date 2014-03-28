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

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * The TransferComboBoxModel class provides a combo box model based on available
 * transfer accounts and categories.  Hidden accounts and categories will not be
 * included in the list unless they are referenced by the current transaction.
 *
 * Account names will be enclosed in brackets to differentiate them from category names.
 * The first element will always be "--None--" to indicate there is no transfer account or
 * category.
 */
public final class TransferComboBoxModel extends AbstractListModel implements ComboBoxModel {
    
    /** The transfer list */
    private List<DBElement> transferList;
    
    /** Selected element index */
    private int elementIndex = -1;
    
    /** No selection element */
    private final String noSelection = "--None--";
    
    /**
     * Create a new list model containing both accounts and categories
     *
     * @param       account             Transaction account or null.
     * @param       transferAccount     Transaction transfer account or null.
     * @param       category            Transaction category or null.
     */
    public TransferComboBoxModel(AccountRecord account, AccountRecord transferAccount, CategoryRecord category) {
        transferList = new ArrayList<DBElement>(AccountRecord.accounts.size()+CategoryRecord.categories.size()+1);
        transferList.add(null);
        
        //
        // Add the available accounts.  The current transaction account will not be
        // included nor will ASSET and INVESTMENT accounts.  A hidden account will not be
        // included unless it is the current transaction transfer account.
        //
        for (AccountRecord a : AccountRecord.accounts) {
            if (a != account && (!a.isHidden() || a == transferAccount)) {
                int accountType = a.getType();
                if (accountType != AccountRecord.ASSET && accountType != AccountRecord.INVESTMENT)
                    transferList.add(a);
            }
        }
        
        //
        // Add the category names.  A hidden category will not be included unless it
        // is the current transaction category.
        //
        for (CategoryRecord c : CategoryRecord.categories) {
            if (!c.isHidden() || c == category)
                transferList.add(c);
        }
    }
    
    /**
     * Create a new list model containing just accounts
     *
     * @param       account             Transaction account or null.
     * @param       transferAccount     Transaction transfer account or null.
     */
    public TransferComboBoxModel(AccountRecord account, AccountRecord transferAccount) {
        transferList = new ArrayList<DBElement>(AccountRecord.accounts.size()+1);
        transferList.add(null);
        
        //
        // Add the available accounts.  The current transaction account will not be
        // included nor will ASSET and INVESTMENT accounts.  A hidden account will not be
        // included unless it is the current transaction transfer account.
        //
        for (AccountRecord a : AccountRecord.accounts) {
            if (a != account && (!a.isHidden() || a == transferAccount)) {
                int accountType = a.getType();
                if (accountType != AccountRecord.ASSET && accountType != AccountRecord.INVESTMENT)
                    transferList.add(a);
            }
        }
    }
    
    /**
     * Create a new list model containing just categories
     *
     * @param       category            Transaction category or null.
     */
    public TransferComboBoxModel(CategoryRecord category) {
        transferList = new ArrayList<DBElement>(CategoryRecord.categories.size()+1);
        transferList.add(null);
        
        //
        // Add the category names.  A hidden category will not be included unless it
        // is the current transaction category.
        //
        for (CategoryRecord c : CategoryRecord.categories) {
            if (!c.isHidden() || c == category)
                transferList.add(c);
        }        
    }

    /**
     * Get the number of elements in the list
     *
     * @return                      The number of elements in the list
     */
    public int getSize() {
        return transferList.size();
    }

    /**
     * Get the element at the specified index
     *
     * @param       index           List element index
     * @return                      The element type string
     */
    public Object getElementAt(int index) {
        String name = null;
        if (index >= 0 && index < transferList.size()) {
            DBElement element = transferList.get(index);
            if (element == null)
                name = noSelection;
            else if (element instanceof AccountRecord)
                name = "["+element.getName()+"]";
            else
                name = element.getName();
        }
        
        return name;
    }
    
    /**
     * Get the database element corresponding to the specified index
     *
     * @param       index           List element index
     * @return                      The database element or null
     */
    public DBElement getDBElementAt(int index) {
        return (index>=0&&index<transferList.size() ? transferList.get(index) : null);
    }
    
    /**
     * Get the index of the specified element
     *
     * @param       element         The combo box element
     * @return                      Element index or -1 if not found
     */
    public int getElementIndex(Object element) {
        int index;
        if (element != null && element instanceof String) {
            String name = (String)element;
            if (name.equals(noSelection)) {
                index = 0;
            } else {
                boolean accountName = false;
                if (name.charAt(0) == '[') {
                    accountName = true;
                    name = name.substring(1, name.length()-1);
                }
        
                index = 0;
                for (DBElement e : transferList) {
                    if (e != null && ((e instanceof AccountRecord && accountName) || 
                                      (e instanceof CategoryRecord && !accountName)) && e.getName().equals(name))
                        break;
                    
                    index++;
                }
                
                if (index == transferList.size())
                    index = -1;
            }
        } else {
            index = -1;
        }
        
        return index;
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
        int index = getElementIndex(element);
        if (elementIndex != index) {
            elementIndex = index;
            fireContentsChanged(this, -1, -1);
        }
    }
}
