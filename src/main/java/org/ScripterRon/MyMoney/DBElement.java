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

/**
 * The DBElement interface defines required interfaces for the base database elements
 * (accounts, categories and securities)
 */
public abstract class DBElement implements Comparable<DBElement> {

    /** Record identifier */
    protected int recordID;
    
    /** Element type */
    protected int elementType;
    
    /** Element name */
    protected String elementName;

    /** Element is hidden */
    protected boolean elementHidden = false;

    /**
     * Element reference count.  The reference count is not preserved across
     * application restarts and is recomputed each time the database is read.
     */
    protected int refCount;

    /**
     * Get the hash code
     *
     * The hash code for a database element is the hash code
     * for the account name
     *
     * @return                      The hash code for the database element
     */
    public int hashCode() {
        return elementName.hashCode();
    }

    /**
     * Compare this object to the supplied object
     *
     * Two database elements are equal if they have the are in the same class
     * and have the same element name
     *
     * @param       obj             Comparison object
     * @return                      TRUE if the objects are equal
     */
    public boolean equals(Object obj) {
        boolean retValue = false;
        if (this == obj) {
            retValue = true;
        } else if (this.getClass() == obj.getClass()) {
            DBElement comp = (DBElement)obj;
            if (elementName.equals(comp.elementName))
                retValue = true;
        }

        return retValue;
    }

    /**
     * Compare two database elements for the Comparable interface
     *
     * @param       object          Comparison object
     * @return                      Negative, zero or positive based on comparison
     */
    public int compareTo(DBElement object) {
        return elementName.compareTo(object.elementName);
    }

    /**
     * Get the record identifier for this database element.
     *
     * Each database element has a record identifier.  This allows the element
     * name to be changed without impacting existing transactions that reference
     * the element.
     *
     * @return                      The record identifier
     */
    public int getID() {
        return recordID;
    }

    /**
     * Add a reference to the database element.
     */
    public void addReference() {
        refCount++;
    }

    /**
     * Remove a reference to the database element.
     */
    public void removeReference() {
        if (refCount <= 0)
            throw new IllegalStateException("Reference count is zero");

        refCount--;
    }

    /**
     * Test if the database element is referenced.
     */
    public boolean isReferenced() {
        return (refCount>0 ? true : false);
    }

    /**
     * Set the element hidden state
     *
     * @param       hidden          TRUE if the element is hidden
     */
    public void setHide(boolean hidden) {
        elementHidden = hidden;
    }

    /**
     * Test if the element is hidden
     *
     * @return                      TRUE if the element is hidden
     */
    public boolean isHidden() {
        return elementHidden;
    }

    /**
     * Get the database element name.  The return value will never be null.
     *
     * @return                      The database element name
     */
    public String getName() {
        return elementName;
    }

    /**
     * Set the database element name
     *
     * Note that the element name is used to compare two database elements.
     * For this reason, a database element must not be contained in a set
     * when the name is changed.
     *
     * @param       name            The database element name
     */
    public void setName(String name) {
        if (name == null)
            throw new NullPointerException("No element name supplied");

        elementName = name;        
    }
    
    /**
     * Get the database element type.  This identifies a subclass of the
     * derived database element (for example, "investment account" is a
     * subclass of "account").
     * 
     * @return                      Database element type
     */
    public int getType() {
        return elementType;
    }
    
    /**
     * Set the database element type.  This identifies a subclass of the
     * derived database element (for example, "investment account" is a
     * subclass of "account").
     */
    public void setType(int type) {
        elementType = type;
    }
}
