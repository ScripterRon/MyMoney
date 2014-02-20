package MyMoney;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * The LinkAccountComboBoxModel class provides a combo box model based on the
 * available bank accounts.  The current link account will be the initial combo
 * box selection.  The getAccountAt() method should be called to get the link
 * account associated with a specific combo box list index.
 *
 * The combo box will include "--None--" as the first selection to indicate that
 * no link account is selected.
 */
public final class LinkAccountComboBoxModel extends AbstractListModel implements ComboBoxModel {
    
    /** The link accounts */
    private List<AccountRecord> linkAccounts;
    
    /** The current selection */
    private int selectionIndex = 0;
    
    /** Dummy list element */
    private final String noSelection = "--None--";
    
    /** 
     * Create a new combo box list model
     *
     * @param       account         The current account or null
     */
    public LinkAccountComboBoxModel(AccountRecord account) {
        linkAccounts = new ArrayList<AccountRecord>(AccountRecord.accounts.size()+1);
        linkAccounts.add(null);
        int index = 1;
        for (AccountRecord a : AccountRecord.accounts) {
            if (a != account && a.getType() == AccountRecord.BANK) {
                linkAccounts.add(a);
                if (account != null && account.getLinkedAccount() == a)
                    selectionIndex = index;
                
                index++;
            }
        }
    }

    /**
     * Get the number of elements in the list
     *
     * @return                      The number of elements in the list
     */
    public int getSize() {
        return linkAccounts.size();
    }

    /**
     * Get the element at the specified index
     *
     * @param       index           List element index
     * @return                      The account name or null if the index is invalid
     */
    public Object getElementAt(int index) {
        Object element = null;
        if (index >= 0 && index < linkAccounts.size()) {
            AccountRecord account = linkAccounts.get(index);
            if (account == null)
                element = noSelection;
            else
                element = account.getName();
        }
        
        return element;
    }
    
    /**
     * Get the link account corresponding to the specified index
     *
     * @param       index           List element index
     * @return                      The link account or null if the index is invalid
     */
    public AccountRecord getAccountAt(int index) {
        AccountRecord account = null;
        if (index >= 0 && index < linkAccounts.size())
            account = linkAccounts.get(index);
        
        return account;
    }
    
    /**
     * Get the selected item
     *
     * @return                      The selected item or null if there is no selection
     */
    public Object getSelectedItem() {
        return getElementAt(selectionIndex);
    }
    
    /**
     * Set the selected item
     *
     * @param       item            The selected item or null to clear the selection
     */
    public void setSelectedItem(Object item) {
        if (item == null) {
            if (selectionIndex != -1) {
                selectionIndex = -1;
                fireContentsChanged(this, -1, -1);
            }
        } else if (item instanceof String) {
            String element = (String)item;
            if (element.equals(noSelection)) {
                if (selectionIndex != 0) {
                    selectionIndex = 0;
                    fireContentsChanged(this, -1, -1);
                }
            } else {
                int index = 0;
                for (AccountRecord a : linkAccounts) {
                    if (a != null && a.getName().equals(element)) {
                        if (index != selectionIndex) {
                            selectionIndex = index;
                            fireContentsChanged(this, -1, -1);
                        }
                    
                        break;
                    }
                
                    index++;
                }
            }
        }
    }
}
