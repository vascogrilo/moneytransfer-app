# Revolut Java/Scala Test

This test consisted on building a RESTful API for money transfers between accounts.
I chose to implement this in Java and using the Play Framework 2.5 (because I've used it before and sped up development process).

## 1. Data Models
One of the main requirements for this was to keep it simple and to the point, no need for over-complicating stuff with unnecessary functionality.

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
#### 3.1.1. GET /accounts
Lists all accounts. It is possible to supply query params for filtering and sorting (see **Account storage** api).
Example:
```http
http -v localhost:9000/accounts

GET /accounts HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Host: localhost:9000
User-Agent: HTTPie/0.9.9

HTTP/1.1 200 OK
Content-Length: 129
Content-Type: application/json; charset=UTF-8
Date: Mon, 18 Sep 2017 09:42:39 GMT

[
    {
        "balance": 100.0,
        "id": "0",
        "name": "savings",
        "ownerName": "vasco"
    },
    {
        "balance": 250.0,
        "id": "1",
        "name": "standard",
        "ownerName": "john"
    }
]
```
Example with filtering:
```http
http -v localhost:9000/accounts name==savings

GET /accounts?name=savings HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Host: localhost:9000
User-Agent: HTTPie/0.9.9

HTTP/1.1 200 OK
Content-Length: 65
Content-Type: application/json; charset=UTF-8
Date: Mon, 18 Sep 2017 09:46:07 GMT

[
    {
        "balance": 100.0,
        "id": "0",
        "name": "savings",
        "ownerName": "vasco"
    }
]
```
#### 3.1.2. GET /accounts/{id}
Retrieves a specific Account, identified by _id_.
Example:
```http
http -v localhost:9000/accounts/1

GET /accounts/1 HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Host: localhost:9000
User-Agent: HTTPie/0.9.9

HTTP/1.1 200 OK
Content-Length: 63
Content-Type: application/json; charset=UTF-8
Date: Mon, 18 Sep 2017 09:47:18 GMT

{
    "balance": 250.0,
    "id": "1",
    "name": "standard",
    "ownerName": "john"
}
```
#### 3.1.3. POST /accounts
Creates a new account. **name** and **ownerName** are mandatory.
Example:
```http
http -v POST localhost:9000 name=revolut ownerName=james

POST /accounts HTTP/1.1
Accept: application/json, */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Content-Length: 41
Content-Type: application/json
Host: localhost:9000
User-Agent: HTTPie/0.9.9

{
    "name": "revolut",
    "ownerName": "james"
}

HTTP/1.1 201 Created
Content-Length: 61
Content-Type: application/json; charset=UTF-8
Date: Mon, 18 Sep 2017 09:48:10 GMT
Location: /accounts/2

{
    "balance": 0.0,
    "id": "2",
    "name": "revolut",
    "ownerName": "james"
}
```
#### 3.1.4. PUT /accounts/{id}
Updates an existing account, identified by _id_.
Example:
```http
http -v PUT localhost:9000/accounts/2 name=revolut ownerName="james harden" id=2

PUT /accounts/2 HTTP/1.1
Accept: application/json, */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Content-Length: 59
Content-Type: application/json
Host: localhost:9000
User-Agent: HTTPie/0.9.9

{
    "id": "2",
    "name": "revolut",
    "ownerName": "james harden"
}

HTTP/1.1 200 OK
Content-Length: 68
Content-Type: application/json; charset=UTF-8
Date: Mon, 18 Sep 2017 09:51:06 GMT

{
    "balance": 0.0,
    "id": "2",
    "name": "revolut",
    "ownerName": "james harden"
}
```
#### 3.1.5. PUT /accounts/{id}/deposit/{amount}
Deposits _amount_ into the target account, identified by _id_.
Example:
```http
http -v PUT localhost:9000/accounts/2/deposit/5000

PUT /accounts/2/deposit/5000 HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Content-Length: 0
Host: localhost:9000
User-Agent: HTTPie/0.9.9

HTTP/1.1 200 OK
Content-Length: 71
Content-Type: application/json; charset=UTF-8
Date: Mon, 18 Sep 2017 09:52:31 GMT

{
    "balance": 5000.0,
    "id": "2",
    "name": "revolut",
    "ownerName": "james harden"
}
```
#### 3.1.6. PUT /accounts/{id}/withdraw/{amount}
Tries to withdraw _amount_ from the target account, identified by _id_.
Example:
```http
http -v PUT localhost:9000/accounts/2/withdraw/4500

PUT /accounts/2/withdraw/4500 HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Content-Length: 0
Host: localhost:9000
User-Agent: HTTPie/0.9.9

HTTP/1.1 200 OK
Content-Length: 70
Content-Type: application/json; charset=UTF-8
Date: Mon, 18 Sep 2017 09:53:54 GMT

{
    "balance": 500.0,
    "id": "2",
    "name": "revolut",
    "ownerName": "james harden"
}
```
#### 3.1.7. DELETE /accounts/{id}
Deletes an account, identified by _id_.
Example:
```http
http -v DELETE localhost:9000/accounts/1

DELETE /accounts/1 HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Content-Length: 0
Host: localhost:9000
User-Agent: HTTPie/0.9.9

HTTP/1.1 204 No Content
Date: Mon, 18 Sep 2017 09:54:59 GMT
```
#### 3.1.8. OPTIONS /accounts
Lists all possible operations on this resource.
Example:
```http
http -v OPTIONS localhost:9000/accounts
OPTIONS /accounts HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Content-Length: 0
Host: localhost:9000
User-Agent: HTTPie/0.9.9

HTTP/1.1 200 OK
Allow: GET,POST,PUT,DELETE,OPTIONS
Content-Length: 0
Date: Mon, 18 Sep 2017 09:55:37 GMT
```
### 3.2. /transfers
The following operations are available to be performed on this resource:
#### 3.2.1. POST /transfers
Creates a new transfer.
Example:
```http
http -v POST localhost:9000/transfers originAccountId=0 destinationAccountId=2 amount=22.4

POST /transfers HTTP/1.1
Accept: application/json, */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Content-Length: 71
Content-Type: application/json
Host: localhost:9000
User-Agent: HTTPie/0.9.9

{
    "amount": "22.4",
    "destinationAccountId": "2",
    "originAccountId": "0"
}

HTTP/1.1 201 Created
Content-Length: 147
Content-Type: application/json; charset=UTF-8
Date: Mon, 18 Sep 2017 10:00:28 GMT
Location: /transfers/f644b928-080d-4e50-bec3-1e314c067a69

{
    "amount": 22.4,
    "destinationAccountId": "2",
    "id": "f644b928-080d-4e50-bec3-1e314c067a69",
    "originAccountId": "0",
    "timestamp": "2017-09-18T10:00:28.661Z"
}
```
#### 3.2.2. GET /transfers
Lists all transfers. It is possible to supply query params for filtering and sorting (see **Transfer storage** api).
Example:
```http
http -v localhost:9000/transfers

GET /transfers HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Host: localhost:9000
User-Agent: HTTPie/0.9.9

HTTP/1.1 200 OK
Content-Length: 296
Content-Type: application/json; charset=UTF-8
Date: Mon, 18 Sep 2017 10:02:10 GMT

[
    {
        "amount": 22.4,
        "destinationAccountId": "2",
        "id": "f644b928-080d-4e50-bec3-1e314c067a69",
        "originAccountId": "0",
        "timestamp": "2017-09-18T10:00:28.661Z"
    },
    {
        "amount": 0.4,
        "destinationAccountId": "0",
        "id": "d5b41d32-ee9e-4432-9627-7ff8b0f11e41",
        "originAccountId": "2",
        "timestamp": "2017-09-18T10:01:56.998Z"
    }
]
```
#### 3.2.3. GET /transfers/{id}
Retrieves a specific Transfer, identified by _id_.
```http
http -v localhost:9000/transfers/d5b41d32-ee9e-4432-9627-7ff8b0f11e41

GET /transfers/d5b41d32-ee9e-4432-9627-7ff8b0f11e41 HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Host: localhost:9000
User-Agent: HTTPie/0.9.9

HTTP/1.1 200 OK
Content-Length: 146
Content-Type: application/json; charset=UTF-8
Date: Mon, 18 Sep 2017 10:03:03 GMT

{
    "amount": 0.4,
    "destinationAccountId": "0",
    "id": "d5b41d32-ee9e-4432-9627-7ff8b0f11e41",
    "originAccountId": "2",
    "timestamp": "2017-09-18T10:01:56.998Z"
}
```
#### 3.2.4. DELETE /transfers/{id}
Deletes a transfer, identified by _id_.
Example:
```http
http -v DELETE localhost:9000/transfers/d5b41d32-ee9e-4432-9627-7ff8b0f11e41

DELETE /transfers/d5b41d32-ee9e-4432-9627-7ff8b0f11e41 HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Content-Length: 0
Host: localhost:9000
User-Agent: HTTPie/0.9.9

HTTP/1.1 204 No Content
Date: Mon, 18 Sep 2017 10:03:59 GMT
```
Deleting a transfer doesn't undo it. It just deletes the record of it.
#### 3.2.5. OPTIONS /transfers
Lists all possible operations on this resource.
Example:
```http
http -v OPTIONS localhost:9000/transfers

OPTIONS /transfers HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Content-Length: 0
Host: localhost:9000
User-Agent: HTTPie/0.9.9

HTTP/1.1 200 OK
Allow: GET,POST,DELETE,OPTIONS
Content-Length: 0
Date: Mon, 18 Sep 2017 10:04:38 GMT
```
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
The application can be started by running:
```
sh /path/to/bin/vgrilo-moneytransfer
```
or the _/bin/vgrilo-moneytransfer.bat_ for Windows environments.
It assumes the JRE is discoverable in its usual location (JAVA_HOME).

For Unix users, zip files do not retain Unix file permissions so when the file is expanded the start script will be required to be set as an executable:

```bash
chmod +x /path/to/bin/<project-name>
```

If you do not want it to run on 0.0.0.0 and port 9000 (defaults) you can do it by running it the following way:
```bash
sh /path/to/bin/vgrilo-moneytransfer -Dhttp.port=DESIRED_PORT -Dhttp.address=DESIRED_ADDRESS
```