package services;

import models.Account;

import java.util.Set;

public interface AccountStorage {
    Set<Account> getAllAccounts();
    Account getAccount(int id);
    Account createAccount(Account account);
    Account updateAccount(Account account);
    boolean deleteAccount(int id);
    void clearAccounts();
}
