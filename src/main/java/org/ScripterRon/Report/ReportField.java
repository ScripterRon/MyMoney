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
package org.ScripterRon.Report;

/**
 * The report field is a report element consisting of text obtained from
 * the data column, expression or function identified by the field name.
 */
public class ReportField extends AbstractElement {

    /** The field name */
    private String field;

    /** The field renderer */
    private ReportRenderer renderer;

    /**
     * Construct a new report field using the value of the specified field
     *
     * @param       field           The field name
     */
    public ReportField(String field) {
        super();

        if (field == null)
            throw new NullPointerException("No field name provided");

        this.field = field;
    }

    /**
     * Set the renderer used to format the field value
     *
     * @param       renderer        The field renderer
     */
    public void setRenderer(ReportRenderer renderer) {
        this.renderer = renderer;
    }

    /**
     * Return the string representation of the field value
     *
     * @return                      The string value
     */
    public String getText() {
        String text;

        //
        // Get the string representation of the field value
        //
        Object value = getState().getDataRow().getValue(field);
        if (value == null)
            text = new String();
        else if (renderer != null)
            text = renderer.format(this, value);
        else
            text = value.toString();

        return text;
    }
}

