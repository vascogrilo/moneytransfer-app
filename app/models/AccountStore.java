package models;

import java.util.*;

public class MoneyAccountStore {
  private Map<String, Account> accounts = new HashMap<String, Account>();

  public Account addAccount(Account account) {
    UUID uuid = UUID.randomUUID();
    String id = uuid.toString();
    account.setId(id);
    accounts.put(id, account);
    return account;
  }

  public Account getAccount(String id) {
      return accounts.get(id);
  }

  public Set<Account> getAllAccounts() {
      return new HashSet<Account>(accounts.values());
  }

  public Account updateAccount(Account account){
      String id = account.getId();
      if (accounts.containsKey(id)){
          accounts.put(id, account);
          return account;
      }
      return null;
  }
}
