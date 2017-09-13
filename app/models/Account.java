package models;

import java.lang.IllegalStateException;

public class Account {

  private String id;
  private float balance;

  public Account() { }

  public Account(float initialBalance) {
    this.balance = initialBalance;
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

  public void setBalance(float balance) {
    this.balance = balance;
  }

  public float deposit(float amount) {
    this.balance += amount;
    return this.balance;
  }

  public float withdraw(float amount) {
    if (this.balance - amount < 0)
      throw new IllegalStateException();
    this.balance -= amount;
    return this.balance;
  }
}
