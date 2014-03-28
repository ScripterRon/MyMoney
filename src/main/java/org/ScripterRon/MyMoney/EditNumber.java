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

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;

import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.*;
import java.awt.*;

/**
 * EditNumber is a formatter for a JFormattedTextField component.  It handles
 * numbers with a fixed number of decimal digits.  Addition, subtraction,
 * multiplication and division can be specified as part of the text string
 * using the '+', '-', '*' and '/' operators.  Operations are processed from
 * left to right as the operators are encountered in the text string.
 */
public final class EditNumber extends AbstractFormatter {

    /** Number formatter */
    private NumberFormat formatter;

    /** Number of decimal digits */
    private int decimalDigits;

    /**
     * Create a new number editor
     *
     * @param       decimalDigits       Number of decimal digits (0 for integer value)
     * @param       useGrouping         TRUE if grouping should be used
     */
    public EditNumber(int decimalDigits, boolean useGrouping) {
        super();
        this.decimalDigits = decimalDigits;
        formatter = NumberFormat.getNumberInstance();
        formatter.setGroupingUsed(useGrouping);
        if (decimalDigits == 0) {
            formatter.setParseIntegerOnly(true);
        } else {
            formatter.setParseIntegerOnly(false);
            formatter.setMinimumFractionDigits(decimalDigits);
            formatter.setMaximumFractionDigits(decimalDigits);
        }
    }

    /**
     * Convert a string to a Number.  The return value will be an Integer
     * if the number of decimal digits is zero.  Otherwise, the return value
     * will be a Double.
     *
     * @param       string          String to convert
     * @return                      Number object
     * @exception   ParseException  The string does not represent a valid number
     */
    public Object stringToValue(String string) throws ParseException {
        int length = string.length();

        //
        // Return zero for an empty string
        //
        if (length == 0) {
            setEditValid(false);
            if (decimalDigits == 0)
                return new Integer(0);

            return new Double(0.0);
        }

        //
        // Parse the string
        //
        ParsePosition pos = new ParsePosition(0);
        Number value = formatter.parse(string, pos);
        int index = pos.getIndex();

        //
        // Error if the string does not start with a valid number
        //
        if (value == null) {
            setEditValid(false);
            throw new ParseException("Unable to parse number", index);
        }

        //
        // We are done if the entire string has been processed
        //
        if (index == length) {
            setEditValid(true);
            if (decimalDigits == 0)
                value = new Integer(value.intValue());
            else if (!(value instanceof Double))
                value = new Double(value.doubleValue());

            return value;
        }

        //
        // Process arithmetic operations (+ - * /)
        //
        double number = value.doubleValue();
        int op = 0;
        while (index < length) {

            //
            // Get the next operation
            //
            char c = string.charAt(index);
            if (c == '+') {
                op = 0;
            } else if (c == '-') {
                op = 1;
            } else if (c == '*') {
                op = 2;
            } else if (c == '/') {
                op = 3;
            } else {
                setEditValid(false);
                throw new ParseException("Unrecognized operator", index);
            }

            //
            // Error if we have a trailing operator
            //
            if (++index == length) {
                setEditValid(false);
                throw new ParseException("Trailing operator", index);
            }

            //
            // Parse the next number
            //
            pos.setIndex(index);
            value = formatter.parse(string, pos);
            index = pos.getIndex();
            if (value == null) {
                setEditValid(false);
                throw new ParseException("Unable to parse number", index);
            }

            //
            // Perform the arithmetic operation
            //
            switch (op) {
                case 0:                         // Add
                    number += value.doubleValue();
                    break;

                case 1:                         // Subtract
                    number -= value.doubleValue();
                    break;

                case 2:                         // Multiply
                    number *= value.doubleValue();
                    break;

                case 3:                         // Divide
                    number /= value.doubleValue();
                    break;
            }
        }

        //
        // Return the final value
        //
        if (decimalDigits == 0)
            value = new Integer((int)number);
        else
            value = new Double(number);

        setEditValid(true);
        return value;
    }

    /**
     * Convert a Number value to a string
     *
     * @param       value           Number value to convert
     * @return                      Converted string
     * @exception   ParseException  Value is not a Number
     */
    public String valueToString(Object value) throws ParseException {
        if (value == null) {
            setEditValid(false);
            return new String();
        }

        if (!(value instanceof Number)) {
            setEditValid(false);
            throw new ParseException("Value is not a Number", 0);
        }

        setEditValid(true);
        return formatter.format(value);
    }
}

