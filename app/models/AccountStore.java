package models;

import services.AccountStorage;
import java.util.*;

public class AccountStore implements AccountStorage {
    private static AccountStore instance;
    private final Map<String, Account> accounts;

    public static AccountStore getInstance() {
        if (instance == null)
            instance = new AccountStore();
        return instance;
    }

    public static AccountStore newInstance() {
        return new AccountStore();
    }

    private AccountStore() {
        accounts = new HashMap<>();
    }

    @Override
    public Account createAccount(Account account) {
        String id = Integer.toString(accounts.size());
        account.setId(id);
        accounts.put(id, account);
        return account;
    }

    @Override
    public Account getAccount(String id) {
      return accounts.get(id);
    }

    @Override
    public Set<Account> getAllAccounts() {
      return new HashSet<>(accounts.values());
    }

    @Override
    public Account updateAccount(Account account){
        String id = account.getId();
        if (accounts.containsKey(id)){
          accounts.put(id, account);
          return account;
        }
        return null;
    }

    @Override
    public boolean deleteAccount(String id) {
      if (!accounts.containsKey(id))
          return false;

      accounts.remove(id);
      return true;
    }

    @Override
    public void clearAccounts() {
      accounts.clear();
    }

    @Override
    public boolean deposit(String id, float amount){
        if (!accounts.containsKey(id))
            return false;

        accounts.get(id).deposit(amount);
        return true;
    }

    @Override
    public boolean withdraw(String id, float amount){
        if (!accounts.containsKey(id))
            return false;

        accounts.get(id).withdraw(amount);
        return true;
    }
}
