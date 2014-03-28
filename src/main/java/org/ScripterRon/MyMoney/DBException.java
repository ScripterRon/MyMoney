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
 * The DBException class defines the exceptions thrown by the application
 * database support.
 */
public final class DBException extends Exception {

    /**
     * Construct an exception with no message text or causing exception
     */
    public DBException() {
        super();
    }

    /**
     * Construct an exception with message text but no causing exception
     *
     * @param       exceptionMsg    The message text for the exception
     */
    public DBException(String exceptionMsg) {
        super(exceptionMsg);
    }

    /**
     * Construct an exception with message text and a causing exception
     *
     * @param       exceptionMsg    The message text for the exception
     * @param       cause           The causing exception
     */
    public DBException(String exceptionMsg, Throwable cause) {
        super(exceptionMsg, cause);
    }
}

