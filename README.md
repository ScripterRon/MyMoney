MyMoney
=======

MyMoney is a money management application written in Java.  It supports checking, savings, credit card, investment, mortgage and asset accounts.  It is general use except for the estimated tax function, which is set up for filing taxes in New York (and even then it is a very rough estimate of what you will owe).  Printed reports can be generated for transactions, income/expense categories, accounts and investments.

The following account classes are supported:
  - Bank accounts
  - Credit cards
  - Investment accounts
  - Assets
  - Loans
  
The following security classes are supported for use with investment accounts:
  - CD
  - Corporate bond
  - Municipal bond
  - Bond mutual fund
  - Stock
  - Stock mutual fund
  - Treasury Note
  
Income and expense transactions are classified by category (Medical, Automobile, Utility, Computer Game, etc).  New categories can be created at any time.
 
Reports can be generated for capital gains, stock holdings, and transactions.  A net worth graph can be generated for any period of time.  Stock prices can be automatically updated through an internet connection (the program uses Yahoo! to retrieve the current stock quotes).
 
Help is provided in the program using html files.  You can also browse the help files outside of the program using a web browser.  The help files are stored in src/MyMoney/help.

There are no external dependencies.  I use the Netbeans IDE but you should be able to use anything that supports the Java compiler.  Documentation is generated from the source code using javadoc.

A compiled version of the program is available: https://sites.google.com/site/ronsworkspace/mymoney
