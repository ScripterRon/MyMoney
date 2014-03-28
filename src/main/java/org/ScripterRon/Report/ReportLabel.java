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
 * The report label is a report element consisting of static text.
 */
public class ReportLabel extends AbstractElement {

    /** The label text */
    private String text;

    /**
     * Construct a new report label
     *
     * @param       text            The text for the report label
     */
    public ReportLabel(String text) {
        super();

        if (text == null)
            throw new NullPointerException("No label text provided");

        this.text = text;
    }

    /**
     * Return the text for the label
     *
     * @return                      The text string
     */
    public String getText() {
        return text;
    }
}

