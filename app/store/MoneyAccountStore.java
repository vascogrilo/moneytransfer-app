package store;

import models.MoneyAccount;

import java.util.*;

public class MoneyAccountStore {
  private Map<String, MoneyAccount> accounts = new HashMap<String, MoneyAccount>();

  public MoneyAccount addAccount(MoneyAccount account) {
    UUID uuid = UUID.randomUUID();
    String id = uuid.toString();
    account.setId(id);
    accounts.put(id, account);
    return account;
  }

  public MoneyAccount getAccount(String id) {
      return accounts.get(id);
  }

  public Set<MoneyAccount> getAllAccounts() {
      return new HashSet<MoneyAccount>(accounts.values());
  }

  public MoneyAccount updateAccount(MoneyAccount account){
      String id = account.getId();
      if (accounts.containsKey(id)){
          accounts.put(id, account);
          return account;
      }
      return null;
  }
}
