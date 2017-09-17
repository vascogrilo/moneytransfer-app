package models;

import services.AccountStorage;
import services.TransferStorage;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

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
    public Account[] listAccounts(String name, String ownerName, Float balance, Float aboveBalance, Float belowBalance, String sort) {
        Stream<Account> stream = accounts.values().stream();

        //filtering
        if (name != null)
            stream = stream.filter((account) -> account.getName().equals(name));
        if (ownerName != null)
            stream = stream.filter((account) -> account.getOwnerName().equals(ownerName));
        boolean exactBalance = !balance.isNaN();
        if (exactBalance)
            stream = stream.filter((account -> account.getBalance() == balance));
        if (!exactBalance && !aboveBalance.isNaN())
            stream = stream.filter((account -> account.getBalance() > aboveBalance));
        if (!exactBalance && !belowBalance.isNaN())
            stream = stream.filter((account -> account.getBalance() < belowBalance));

        // ordering
        if (sort != null){
            final boolean desc = sort.startsWith("-");
            if (desc) sort = sort.substring(1);
            switch (sort){
                case "id":
                    stream = stream.sorted((a1, a2) -> desc ? a2.getId().compareTo(a1.getId()) : a1.getId().compareTo(a2.getId()));
                    break;
                case "name":
                    stream = stream.sorted((a1, a2) -> desc ? a2.getName().compareTo(a1.getName()) : a1.getName().compareTo(a2.getName()));
                    break;
                case "ownerName":
                    stream = stream.sorted((a1, a2) -> desc ? a2.getOwnerName().compareTo(a1.getOwnerName()) : a1.getOwnerName().compareTo(a2.getOwnerName()));
                    break;
                case "balance":
                    stream = stream.sorted((a1, a2) -> desc ? Float.compare(a2.getBalance(), a1.getBalance()) : Float.compare(a1.getBalance(), a2.getBalance()));
                    break;
            }
        }
        return (Account[]) stream.toArray();
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
    public Transfer[] listTransfers(String id, String originAccountId, String destinationAccountId, Float amount, Float aboveAmount, Float belowAmount, String sort) {
        Stream<Transfer> stream = transfers.values().stream();

        //filtering
        if (id != null)
            stream = stream.filter((transfer -> transfer.getId().equals(id)));
        if (originAccountId != null)
            stream = stream.filter((transfer -> transfer.getOriginAccountId().equals(originAccountId)));
        if (destinationAccountId != null)
            stream = stream.filter((transfer -> transfer.getDestinationAccountId().equals(destinationAccountId)));
        boolean exactAmount = !amount.isNaN();
        if (exactAmount)
            stream = stream.filter((transfer -> transfer.getAmount() == amount));
        if (!exactAmount && !aboveAmount.isNaN())
            stream = stream.filter((transfer -> transfer.getAmount() > aboveAmount));
        if (!exactAmount && !belowAmount.isNaN())
            stream = stream.filter((transfer -> transfer.getAmount() < belowAmount));

        // ordering
        if (sort != null){
            final boolean desc = sort.startsWith("-");
            if (desc) sort = sort.substring(1);
            switch (sort){
                case "id":
                    stream = stream.sorted((t1, t2) -> desc ? t2.getId().compareTo(t1.getId()) : t1.getId().compareTo(t2.getId()));
                    break;
                case "originAccountId":
                    stream = stream.sorted((t1, t2) -> desc ? t2.getOriginAccountId().compareTo(t1.getOriginAccountId()) : t1.getOriginAccountId().compareTo(t2.getOriginAccountId()));
                    break;
                case "destinationAccountId":
                    stream = stream.sorted((t1, t2) -> desc ? t2.getDestinationAccountId().compareTo(t1.getDestinationAccountId()) : t1.getDestinationAccountId().compareTo(t2.getDestinationAccountId()));
                    break;
                case "amount":
                    stream = stream.sorted((t1, t2) -> desc ? Float.compare(t2.getAmount(), t1.getAmount()) : Float.compare(t1.getAmount(), t2.getAmount()));
                    break;
                case "timestamp":
                    stream = stream.sorted((t1, t2) -> desc ? Instant.parse(t2.getTimestamp()).compareTo(Instant.parse(t1.getTimestamp())) : Instant.parse(t1.getTimestamp()).compareTo(Instant.parse(t2.getTimestamp())));
                    break;
            }
        }
        return (Transfer[]) stream.toArray();
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
