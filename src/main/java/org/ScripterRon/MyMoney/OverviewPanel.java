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
import java.util.Date;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;

/**
 * Account overview panel
 */
public final class OverviewPanel extends JPanel {

    /**
     * Create the account overview panel
     */
    public OverviewPanel() {

        //
        // Create the overview pane
        //
        super();
        setOpaque(true);
        setBackground(Color.white);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        //
        // Create the account pane
        //
        JPanel accountPane = new JPanel(new GridLayout(0, 2));
        accountPane.setOpaque(true);
        accountPane.setBackground(Color.white);

        //
        // Create the account text strings
        //
        JLabel label;
        String labelText;

        StringBuilder bankNames = new StringBuilder(256);
        bankNames.append("<b>Bank Accounts</b><br>");
        StringBuilder bankAmounts = new StringBuilder(80);
        bankAmounts.append("<br>");
        double bankTotal = 0.00;

        StringBuilder creditNames = new StringBuilder(256);
        creditNames.append("<b>Credit Cards</b><br>");
        StringBuilder creditAmounts = new StringBuilder(80);
        creditAmounts.append("<br>");
        double creditTotal = 0.00;

        StringBuilder investmentNames = new StringBuilder(256);
        investmentNames.append("<b>Investment Accounts</b><br>");
        StringBuilder investmentAmounts = new StringBuilder(80);
        investmentAmounts.append("<br>");
        double investmentTotal = 0.00;

        StringBuilder assetNames = new StringBuilder(256);
        assetNames.append("<b>Assets</b><br>");
        StringBuilder assetAmounts = new StringBuilder(80);
        assetAmounts.append("<br>");
        double assetTotal = 0.00;

        StringBuilder loanNames = new StringBuilder(256);
        loanNames.append("<b>Loans</b><br>");
        StringBuilder loanAmounts = new StringBuilder(80);
        loanAmounts.append("<br>");
        double loanTotal = 0.00;

        //
        // Compute the account balances
        //
        // The balance for an investment account is computed using the
        // current share price of the securities in the account (the linked
        // bank account will track the actual money spent or received for
        // investment transactions)
        //
        List<SecurityHolding> holdings = new ArrayList<>(SecurityRecord.securities.size());
        for (AccountRecord a : AccountRecord.accounts)
            a.balance = 0.00;

        for (TransactionRecord t : TransactionRecord.transactions) {
            double amount = t.getAmount();
            AccountRecord a = t.getAccount();
            if (a.getType() == AccountRecord.INVESTMENT) {
                SecurityHolding.updateSecurityHolding(holdings, t);
                a = t.getTransferAccount();
                if (a != null)
                    a.balance -= amount;
            } else {
                a.balance += amount;
                a = t.getTransferAccount();
                if (a != null)
                    a.balance -= amount;
            }

            List<TransactionSplit> splits = t.getSplits();
            if (splits != null) {
                for (TransactionSplit split : splits) {
                    a = split.getAccount();
                    if (a != null)
                        a.balance -= split.getAmount();
                }
            }
        }
        
        //
        // Update the investment account balances
        //
        for (SecurityHolding h : holdings) {
            AccountRecord a = h.getAccount();
            SecurityRecord s = h.getSecurity();
            a.balance += h.getTotalShares()*s.getPrice();
        }

        //
        // Add the account name and current balance to the appropriate
        // text strings.  Hidden accounts will not be displayed
        //
        for (AccountRecord a : AccountRecord.accounts) {
            if (a.isHidden())
                continue;

            double total = (Math.abs(a.balance)>0.005 ? a.balance : 0.0);
            String name = a.getName().concat("<br>");
            String balance = String.format("$%,.2f", total).concat("<br>");

            switch (a.getType()) {
                case AccountRecord.BANK:
                    bankNames.append(name);
                    bankAmounts.append(balance);
                    bankTotal += total;
                    break;

                case AccountRecord.CREDIT:
                    creditNames.append(name);
                    creditAmounts.append(balance);
                    creditTotal += total;
                    break;

                case AccountRecord.INVESTMENT:
                    investmentNames.append(name);
                    investmentAmounts.append(balance);
                    investmentTotal += total;
                    break;

                case AccountRecord.ASSET:
                    assetNames.append(name);
                    assetAmounts.append(balance);
                    assetTotal += total;
                    break;

                case AccountRecord.LOAN:
                    loanNames.append(name);
                    loanAmounts.append(balance);
                    loanTotal += total;
                    break;
            }
        }

        bankNames.append("Total<br><br>");
        bankAmounts.append("<b>"+String.format("$%,.2f", bankTotal)+"</b><br><br>");

        creditNames.append("Total<br><br>");
        creditAmounts.append("<b>"+String.format("$%,.2f", creditTotal)+"</b><br><br>");

        investmentNames.append("Total<br><br>");
        investmentAmounts.append("<b>"+String.format("$%,.2f", investmentTotal)+"</b><br><br>");

        assetNames.append("Total<br><br>");
        assetAmounts.append("<b>"+String.format("$%,.2f", assetTotal)+"</b><br><br>");

        loanNames.append("Total<br><br>");
        loanAmounts.append("<b>"+String.format("$%,.2f", loanTotal)+"</b><br><br>");

        labelText = "<HTML><BODY align=left><h1>Account Overview</h1>"+
                    bankNames+creditNames+investmentNames+assetNames+loanNames+
                    "<b>Net Worth</b></HTML>";

        label = new JLabel(labelText, JLabel.LEADING);
        accountPane.add(label);

        double netWorth = bankTotal+creditTotal+investmentTotal+assetTotal+loanTotal;
        labelText = "<HTML><BODY align=right><h1> </h1>"+
                    bankAmounts+creditAmounts+investmentAmounts+assetAmounts+loanAmounts+
                    "<b>"+String.format("$%,.2f", netWorth)+"</b></HTML>";
        label = new JLabel(labelText, JLabel.TRAILING);
        accountPane.add(label);

        //
        // Add the account pane to the overview panel
        //
        add(accountPane);
    }
}
