# Revolut Java/Scala Test

This test consisted on building a RESTful API for money transfers between accounts.
I chose to implement this in Java and using the Play framework (because I've used it before and sped up development process).

## 1. Data Models
One of the main requirements for this was to keep it simple and to the point, no need for overcomplicating stuff with uneeded functionality.

### 1.1. Account
An account represents something like a _digital wallet_.
A simple account model was designed that contains the following information:
* Id: **String**
* Name: **String**
* OwnerName: **String**
* Balance: **Float**

### 1.2. Transfer
Transfer will be a resource that represents a transfer operation of a certain amount between two accounts.
A simple transfer model was designed that contains the following information:
* Id: **String**
* OriginAccountId: **String**
* DestinationAccountId: **String**
* Amount: **Float**
* Timestamp: **String**

I chose the **timestamp** to be of type **String** for better reading it when Play automatically parses it to JSON.
It would make more sense to be a type like **Date** or **Instant** but for the sake of this test I chose otherwise.
(Transfers cannot be updated and the server defines the timestamp so having it as a string doesn't prove that much of an hassle)

## 2. Storage
As the test requested, everything is stored in-memory. No need for external storage.

### 2.1. Apis
I've defined two interfaces for a clear separation of what can be performed when managing the resources.

#### 2.1.1. Account storage
The following operations should be implemented for an account storage:
* Set\<Account\> _listAccounts()_
* Stream\<Account\> _listAccounts(String name, String ownerName, Float balance, Float aboveBalance, Float belowBalance, String sort)_
  * **name** is an optional value for filtering accounts with a specific name
  * **ownerName** is an optional value for filtering accounts with a specific owner
  * **balance** is an optional value for filtering accounts with a specific balance
  * **aboveBalance** is an optional value for filtering accounts with a balance greater than
  * **belowBalance** is an optional value for filtering accounts with a balance less than
  * **sort** is an optional value for sorting the results. the value should be a field name of Account and preprend it with a '-' for descending order. Example: _-balance_
* Account _getAccount(String id)_
* Account _createAccount(Account account)_
* Account _updateAccount(Account account)_
* boolean _deleteAccount(String id)_
* void _clearAccounts()_
* Account _deposit(String id, Float amount)_
* Account _withdraw(String id, Float amount) throws Account.InsufficientFundsException_

#### 2.1.2. Transfer storage
The following operations should be implemented for a transfer storage:
* Set\<Transfer\> _listTransfers()_
* Stream\<Transfer\> _listTransfers(String originAccountId, String destinationAccountId, Float amount, Float aboveAmount, Float belowAmount, String sort)_
  * **originAccountId** is an optional value for filtering transfers with a specific origin account id
  * **destinationAccountId** is an optional value for filtering transfers with a specific destination account id
  * **amount** is an optional value for filtering transfers with a specific amount
  * **aboveAmount** is an optional value for filtering transfers above that amount
  * **belowAmount** is an optional value for filtering transfers below that amount
  * **sort** is an optional value for sorting the results. the value should be a field name of Transfer and preprend it with a '-' for descending order. Example: _-timestamp_
* Transfer _getTransfer(String id)_
* Transfer _createTransfer(Transfer transfer)_
* boolean _deleteTransfer(String id)_
* void _clearTransfers()_

### 2.2. ApplicationStore
This class implements both _AccountStorage_ and _TransferStorage_ and is responsible for maintaining the list of both accounts and transfers.

Creating an account needs to perform some validation like the account's name and owner name can't be either null or an empty string. The initial balance must not be below zero, NaN or Infinity. An internal id must be generated and assigned to the account.
Updating an account needs to perform some validation like the _modified_ account's name and owner name can't be either null or empty string. The new balance must not be below zero, NaN or Infinity.

Creating a transfer involves several checks. First of all the origin account id and destination account id must identify _existing_ accounts and they should not be the same. The amount should be above 0 and not NaN or Infinity. If these requirements are validated the transfer is considered OK and an internal id should be generated and assigned to the transfer, along with a timestamp of the operation. Now the _amount_ must be withdrawn from the origin account and deposited into the destination account, if the origin account has sufficient funds.

## 3. HTTP REST API
As the test requested there is no authentication on the http layer. Also, for the sake of this test, I chose to leave out any SSL.

### 3.1. /accounts
The following operations are available to be performed on this resource:
* **GET /accounts**
  * Lists all accounts. It is possible to supply query params for filtering and sorting (see **Account storage** api).
* **GET /accounts/{id}**
  * Retrieves a specific Account, identified by _id_
* **POST /accounts**
  * Creates a new account
* **PUT /accounts/{id}**
  * Updates an existing account, identified by _id_
* **PUT /accounts/{id}/deposit/{amount}**
  * Deposits _amount_ into the target account, identified by _id_
* **PUT /accounts/{id}/withdraw/{amount}**
  * Tries to withdraw _amount_ from the target account, identified by _id_
* **DELETE /accounts/{id}**
  * Deletes an account, identified by _id_
* **OPTIONS /accounts**
  * Lists all possible operations on this resource

### 3.2. /transfers
The following operations are available to be performed on this resource:
* **GET /transfers**
  * Lists all transfers. It is possible to supply query params for filtering and sorting (see **Transfer storage** api)
* **GET /transfers/{id}**
  * Retrieves a specific Transfer, identified by _id_
* **POST /transfers**
  * Creates a new transfer
* **DELETE /transfers/{id}**
  * Deletes a transfer, identified by _id_
* **OPTIONS /transfers**
  * Lists all possible operations on this resource

## 4. Considerations
The account model is pretty basic in which it only defines a balance that cannot go below zero. I thought about adding a second balance which woulld be **allowed balance** and with that I could allow for transfers to succeed even if the origin account did not have enough **available balance** but still **allowed balance**.
I've also considered representing different types of currency. An account would have a specific currency. There would be a different resource (maybe _/exchanges_) which would define exchange rates between different types of currency. Then, a transfer between two accounts with different currencies would check these rates and convert the origin amount to the destination currency.
After some consideration and remembering to keep it simple and to the point I've decided to not implement these two functionalities.

It was requested to prove that the application works through tests. I have added unit testing for Account, Transfer and ApplicationStore.
The overall stack can be considered tested via _test/ServerTest.java_, in which an http server is ran and a batch of operations through the controllers are issued for testing the behavior and also some invalid inputs.
Since it was not specified and I'm a backend developer applying for a backend role I did not bother to implement a nice and functional frontend to interact with the application's api.
The application's api can be easily test via some http command line tools like _curl_ or _httpie_.

## 5. Running it locally
Even though it is not a good practice, under **_vascogrilo-moneytransfer.zip_** is the distributable of this application.
Everything was developed and tested on MacOS X.
The application can be started by running the bash script _/bin/play-java_ or _/bin/play-java.bat_ for Windows environments.
It assumes the JRE is discoverable in its usual location.

