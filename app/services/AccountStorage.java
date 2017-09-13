package services;

import models.Account;

import java.util.Set;

public interface AccountStorage {
    Set<Account> getAllAccounts();
    Account getAccount(String id);
    Account createAccount(Account account);
    Account updateAccount(Account account);
    boolean deleteAccount(String id);
    void clearAccounts();
    boolean deposit(String id, float amount);
    boolean withdraw(String id, float amount);
}
