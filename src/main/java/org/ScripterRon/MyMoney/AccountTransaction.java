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
 * Associate a running balance with an account transaction.  This is a
 * transient class and the class instance variables are accessed directly
 * by the application.
 */
public final class AccountTransaction {

    /** Transaction record */
    public TransactionRecord transaction;

    /** Current account balance */
    public double balance;

    /**
     * Create a new account transaction
     *
     * @param       transaction     TransactionRecord
     * @param       balance         Current balance
     */
    public AccountTransaction(TransactionRecord transaction, double balance) {
        this.transaction = transaction;
        this.balance = balance;
    }
}
