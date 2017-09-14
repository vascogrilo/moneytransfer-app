package models;

import services.AccountStorage;
import services.TransferStorage;

import java.time.Instant;
import java.util.*;

public class ApplicationStore implements AccountStorage, TransferStorage {

    private static ApplicationStore instance;
    private final Map<String, Account> accounts;
    private final Map<String, Transfer> transfers;

    public static ApplicationStore getInstance() {
        if (instance == null)
            instance = new ApplicationStore();
        return instance;
    }

    public static ApplicationStore newInstance() {
        return new ApplicationStore();
    }

    private ApplicationStore() {
        accounts = new HashMap<>();
        transfers = new HashMap<>();
    }

    @Override
    public synchronized Account createAccount(Account account) {
        String id = Integer.toString(accounts.size());
        account.setId(id);
        accounts.put(id, account);
        return account;
    }

    @Override
    public synchronized Account getAccount(String id) {
      return accounts.get(id);
    }

    @Override
    public synchronized Set<Account> listAccounts() {
      return new HashSet<>(accounts.values());
    }

    @Override
    public synchronized Account updateAccount(Account account){
        String id = account.getId();
        if (accounts.containsKey(id)){
          accounts.put(id, account);
          return account;
        }
        return null;
    }

    @Override
    public synchronized boolean deleteAccount(String id) {
      if (!accounts.containsKey(id))
          return false;

      accounts.remove(id);
      return true;
    }

    @Override
    public synchronized void clearAccounts() {
      accounts.clear();
    }

    @Override
    public synchronized boolean deposit(String id, float amount){
        if (!accounts.containsKey(id))
            return false;

        accounts.get(id).deposit(amount);
        return true;
    }

    @Override
    public synchronized boolean withdraw(String id, float amount) throws Account.InsufficientFundsException {
        if (!accounts.containsKey(id))
            return false;

        accounts.get(id).withdraw(amount);
        return true;
    }

    @Override
    public synchronized Set<Transfer> listTransfers() {
        return new HashSet<>(transfers.values());
    }

    @Override
    public synchronized Transfer getTransfer(String id) {
        return transfers.get(id);
    }

    @Override
    public synchronized Transfer createTransfer(Transfer transfer) throws AccountNotFoundException, Account.InsufficientFundsException {
        if (transfer == null)
            return null;

        // check validity of origin account id
        String originId = transfer.getOriginAccountId();
        if (!accounts.containsKey(originId))
            throw new AccountNotFoundException(originId);

        String destinationId = transfer.getDestinationAccountId();
        // check validity of destination account id
        if (!accounts.containsKey(destinationId))
            throw new AccountNotFoundException(destinationId);

        Account origin = accounts.get(originId);
        Account destination = accounts.get(destinationId);

        // try to withdraw from origin
        float amount = transfer.getAmount();
        origin.withdraw(amount);
        destination.deposit(amount);

        UUID uuid = UUID.randomUUID();
        transfer.setId(uuid.toString());
        transfer.setTimestamp(Instant.now().toString());

        transfers.put(transfer.getId(), transfer);

        return transfer;
    }

    @Override
    public synchronized boolean deleteTransfer(String id) {
        if(!transfers.containsKey(id))
            return false;
        transfers.remove(id);
        return true;
    }

    @Override
    public synchronized void clearTransfers() {
        transfers.clear();
    }

    public class AccountNotFoundException extends Exception{
        public AccountNotFoundException(String accountId) {
            super("Account with id " + accountId + " was not found.");
        }
    }
}
