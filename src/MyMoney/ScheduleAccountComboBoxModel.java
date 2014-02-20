package MyMoney;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 * The ScheduleAccountComboBoxModel class provides a combo box model based on available
 * BANK and CREDIT accounts.  A hidden account will not be included in the list unless the 
 * account is referenced by the current transaction.
 */
public class ScheduleAccountComboBoxModel extends AbstractListModel implements ComboBoxModel {
    
    /** The transfer list */
    private List<AccountRecord> accountList;
    
    /** Selected element index */
    private int elementIndex = -1;
    
    /**
     * Create a new list model
     *
     * @param       account             Current account or null
     */
    public ScheduleAccountComboBoxModel(AccountRecord account) {
        accountList = new ArrayList<AccountRecord>(AccountRecord.accounts.size());
        
        //
        // Add the available BANK and CREDIT accounts.  A hidden account will not
        // be included unless it is the current account.
        //
        for (AccountRecord a : AccountRecord.accounts) {
            if (!a.isHidden() || a == account) {
                int accountType = a.getType();
                if (accountType == AccountRecord.BANK || accountType == AccountRecord.CREDIT)
                    accountList.add(a);
            }
        }
    }

    /**
     * Get the number of elements in the list
     *
     * @return                      The number of elements in the list
     */
    public int getSize() {
        return accountList.size();
    }

    /**
     * Get the element at the specified index
     *
     * @param       index           List element index
     * @return                      The element type string
     */
    public Object getElementAt(int index) {
        return (index>=0&&index<accountList.size() ? accountList.get(index).getName() : null);
    }
    
    /**
     * Get the account corresponding to the specified index
     *
     * @param       index           List element index
     * @return                      The associated account
     */
    public AccountRecord getAccountAt(int index) {
        return (index>=0&&index<accountList.size() ? accountList.get(index) : null);
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
            String name = (String)element;
            for (AccountRecord a : accountList) {
                if (a.getName().equals(name)) {
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
