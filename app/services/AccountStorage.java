package services;

import models.Account;

import java.util.Set;
import java.util.stream.Stream;

/**
 * Basic API for storing and maintaining a list of {@link Account}s.
 */
public interface AccountStorage {
    /**
     * Lists all {@link Account}s.
     * @return A set with all existing accounts.
     */
    Set<Account> listAccounts();
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
    Stream<Account> listAccounts(String name, String ownerName, Float balance, Float aboveBalance, Float belowBalance, String sort);
    /**
     * Retrieves an {@link Account} by its id.
     * @param id The account id.
     * @return The account, null if it doesn't exist.
     */
    Account getAccount(String id);
    /**
     * Creates a new {@link Account}.
     * Account's field values must be valid (non nulls or empty strings and at least 0 balance).
     * A new account id is to be generated and assigned to the account.
     * @param account The new account to be created.
     * @return The new account with its generated id.
     */
    Account createAccount(Account account);
    /**
     * Updates an {@link Account}, defined by its id.
     * Account's field values must be valid (non nulls or empty strings and at least 0 balance).
     * It is impossible to change an account's id.
     * @param account The new account.
     * @return The updated account.
     */
    Account updateAccount(Account account);
    /**
     * Deletes an {@link Account}.
     * @param id The account's id.
     * @return True if the account exists, false otherwise.
     */
    boolean deleteAccount(String id);
    /**
     * Clears the list of accounts.
     */
    void clearAccounts();
    /**
     * Deposits a certain amount into an {@link Account}.
     * @param id The account's id.
     * @param amount The amount to be deposited.
     * @return The updated account.
     */
    Account deposit(String id, Float amount);
    /**
     * Withdraws a certain amount from an {@link Account}
     * @param id The account's id.
     * @param amount The amount to be withdrawn.
     * @return The updated account, null if the account id does not exist.
     * @throws Account.InsufficientFundsException if account does not have sufficient balance for the withdrawal.
     */
    Account withdraw(String id, Float amount) throws Account.InsufficientFundsException;
}
