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
package org.ScripterRon.Asn1;

/**
 * The Asn1Exception class defines the exceptions thrown by the ASN.1
 * encode/decode support when an error is detected while processing
 * an ASN.1 stream
 */
public final class Asn1Exception extends Exception {

    /**
     * Construct an exception with no message text or causing exception
     */
    public Asn1Exception() {
        super();
    }

    /**
     * Construct an exception with message text but no causing exception
     *
     * @param       exceptionMsg    The message text for the exception
     */
    public Asn1Exception(String exceptionMsg) {
        super(exceptionMsg);
    }
}

