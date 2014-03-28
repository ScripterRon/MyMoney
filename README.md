MyMoney
=======

MyMoney is a money management application written in Java.  It supports checking, savings, credit card, investment, mortgage and asset accounts.  Printed reports can be generated for transactions, income/expense categories, accounts and investments.

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
 
Help is provided in the program using html files.  You can also browse the help files outside of the program using a web browser.  The help files are stored in src/main/resources/help.

A compiled version is available: https://drive.google.com/folderview?id=0B1312_6UqRHPRXF0bUZOYzcwSkk&usp=sharing.  Download the desired archive file and extract the files to a directory of your choice.  The files are signed with the GPG key for Ronald.Hoffman6@gmail.com (D6190F05).


Build
=====

I use the Netbeans IDE but any build environment with Maven and the Java compiler available should work.  The documentation is generated from the source code using javadoc.

Here are the steps for a manual build.  You will need to install Maven 3 and Java SE Development Kit 7 if you don't already have them.

  - Create the executable: mvn clean install
  - [Optional] Create the documentation: mvn javadoc:javadoc
  - [Optional] Copy target/MyMoney-v.r.jar to wherever you want to store the executable.
  - Create a shortcut to start MyMoney using javaw.exe. 
  
		javaw -Xmx256m -jar MyMoney-v.r.jar

	
