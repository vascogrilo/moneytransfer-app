package models;

import java.lang.IllegalStateException;

public class MoneyAccount {

  private String id;
  private float balance;

  public MoneyAccount(String id, float initialBalance) {
    this.id = id;
    this.balance = initialBalance;
  }

  public MoneyAccount(String id) {
    this(id, 0);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public float getBalance() {
    return balance;
  }

  public float depositFunds(float amount) {
    this.balance += amount;
    return this.balance;
  }

  public float withdrawFunds(float amount) {
    if (this.balance - amount < 0)
      throw new IllegalStateException();
    this.balance -= amount;
    return this.balance;
  }
}
