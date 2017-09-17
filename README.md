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
A singleton class _ApplicationStore_ is responsible for maintaining and provide interaction with both resources: account and transfers.

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
