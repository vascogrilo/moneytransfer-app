package models;

import services.AccountStorage;
import services.TransferStorage;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

/**
 * Singleton class for providing in memory storage for {@link Account}s and {@link Transfer}s.
 * Implements both {@link AccountStorage} and {@link TransferStorage} apis.
 * Thread-safe methods for managing and changing internal data.
 */
public class ApplicationStore implements AccountStorage, TransferStorage {

    private static ApplicationStore instance;
    private final Map<String, Account> accounts;
    private final Map<String, Transfer> transfers;

    /**
     * Gets the current singleton instance of this class.
     * @return The singleton application store.
     */
    public static ApplicationStore getInstance() {
        if (instance == null)
            instance = new ApplicationStore();
        return instance;
    }

    /**
     * Always creates a new instance of this class.
     * @return A new instance of an application store.
     */
    public static ApplicationStore newInstance() {
        return new ApplicationStore();
    }

    private ApplicationStore() {
        accounts = new HashMap<>();
        transfers = new HashMap<>();
    }

    /**
     * Creates a new {@link Account}.
     * Account's field values must be valid (non nulls or empty strings and at least 0 balance).
     * A new account is generated and assigned to the account.
     * Thread-safe method for concurrency.
     * @param account The new account to be created.
     * @return The new account with its generated id.
     */
    @Override
    public synchronized Account createAccount(Account account) {
        String id = Integer.toString(accounts.size());
        account.setId(id);
        accounts.put(id, account);
        return account;
    }

    /**
     * Retrieves an {@link Account} by its id.
     * @param id The account id.
     * @return The account, null if it doesn't exist.
     */
    @Override
    public synchronized Account getAccount(String id) {
      return accounts.get(id);
    }

    /**
     * Lists all {@link Account}s.
     * @return A set with all existing accounts.
     */
    @Override
    public synchronized Set<Account> listAccounts() {
      return new HashSet<>(accounts.values());
    }

    /**
     * Lists all {@link Account}s that match possible field values and sorted by a certain field as well.
     * @param name Optional name to filter accounts by.
     * @param ownerName Optional owner name to filter accounts by.
     * @param balance Optional balance to filter accounts by.
     * @param aboveBalance Optional balance to filter accounts richer than that.
     * @param belowBalance Optional balance to filter accounts poorer than that.
     * @param sort Optional field name to sort for. Prepend with '-' for descending order.
     * @return An array of accounts possibly sorted by 'sort' param and that match provided field values.
     */
    @Override
    public Stream<Account> listAccounts(String name, String ownerName, Float balance, Float aboveBalance, Float belowBalance, String sort) {
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
        return stream;
    }

    /**
     * Updates an {@link Account}, identified by its id.
     * Account's field values must be valid (non nulls or empty strings and at least 0 balance).
     * It is impossible to change an account's id.
     * @param account The new account.
     * @return The updated account.
     */
    @Override
    public synchronized Account updateAccount(Account account){
        String id = account.getId();
        if (accounts.containsKey(id)){
          accounts.put(id, account);
          return account;
        }
        return null;
    }

    /**
     * Deletes an {@link Account}.
     * @param id The account's id.
     * @return True if the account exists, false otherwise.
     */
    @Override
    public synchronized boolean deleteAccount(String id) {
      if (!accounts.containsKey(id))
          return false;

      accounts.remove(id);
      return true;
    }

    /**
     * Clears the list of accounts.
     */
    @Override
    public synchronized void clearAccounts() {
      accounts.clear();
    }

    /**
     * Deposits a certain amount into an {@link Account}.
     * @param id The account's id.
     * @param amount The amount to be deposited.
     * @return The updated account.
     */
    @Override
    public synchronized Account deposit(String id, Float amount){
        if (!accounts.containsKey(id))
            return null;

        return accounts.get(id).deposit(amount);
    }

    /**
     * Withdraws a certain amount from an {@link Account}
     * @param id The account's id.
     * @param amount The amount to be withdrawn.
     * @return The updated account, null if the account id does not exist.
     * @throws Account.InsufficientFundsException if account does not have sufficient balance for the withdrawal.
     */
    @Override
    public synchronized Account withdraw(String id, Float amount) throws Account.InsufficientFundsException {
        if (!accounts.containsKey(id))
            return null;

        return accounts.get(id).withdraw(amount);
    }

    /**
     * Lists all {@link Transfer}s.
     * @return A set with all existing transfers.
     */
    @Override
    public synchronized Set<Transfer> listTransfers() {
        return new HashSet<>(transfers.values());
    }

    /**
     * Lists all {@link Transfer}s that match possible field values and sorted by a certain field as well.
     * @param originAccountId Optional origin account id to filter transfers by.
     * @param destinationAccountId Optional destination account id to filter transfers by.
     * @param amount Optional amount to filter transfers by.
     * @param aboveAmount Optional amount to filter transfers bigger than that.
     * @param belowAmount Optional amount to filter transfers smaller than that.
     * @param sort Optional field name to sort for. Prepend with '-' for descending order.
     * @return An array of transfers possibly sorted by 'sort' param and that match provided field values.
     */
    @Override
    public Stream<Transfer> listTransfers(String originAccountId, String destinationAccountId, Float amount, Float aboveAmount, Float belowAmount, String sort) {
        Stream<Transfer> stream = transfers.values().stream();

        //filtering
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
        return stream;
    }

    /**
     * Retrieves a certain {@link Transfer}.
     * @param id The transfer's id.
     * @return The transfer.
     */
    @Override
    public synchronized Transfer getTransfer(String id) {
        return transfers.get(id);
    }

    /**
     * Creates a new transfer.
     * Transfer's field values must be valid (non nulls or empty strings and positive amount).
     * Verifies if both accounts exists and are not the same and tries to withdraw from one and deposit into the other.
     * Id and timestamp are generated and assigned to the transfer.
     * @param transfer The transfer to be created.
     * @return The transfer with its new id and timestamp.
     * @throws ApplicationStore.AccountNotFoundException If any of the account ids do not exist.
     * @throws Account.InsufficientFundsException If the origin account does not have sufficient funds for the transfer.
     */
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

    /**
     * Deletes a certain {@link Transfer}.
     * @param id The transfer's id.
     * @return True if the transfer exists, false otherwise.
     */
    @Override
    public synchronized boolean deleteTransfer(String id) {
        if(!transfers.containsKey(id))
            return false;
        transfers.remove(id);
        return true;
    }

    /**
     * Deletes all transfers.
     */
    @Override
    public synchronized void clearTransfers() {
        transfers.clear();
    }

    /**
     * Custom exception for when an account does not exist.
     */
    public class AccountNotFoundException extends Exception{
        public AccountNotFoundException(String accountId) {
            super("Account with id " + accountId + " was not found.");
        }
    }
}
