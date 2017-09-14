package models;

public class Account {

  private String id;
  private String name;
  private String ownerName;
  private float balance;

  public Account() { }

  public Account(String name, String ownerName, float balance) {
    this.name = name;
    this.ownerName = ownerName;
    this.balance = balance;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getOwnerName() {
    return ownerName;
  }

  public void setOwnerName(String ownerName) {
    this.ownerName = ownerName;
  }

  public float getBalance() {
    return balance;
  }

  public void setBalance(float balance) {
    this.balance = balance;
  }

  public synchronized float deposit(float amount) {
    this.balance += amount;
    return this.balance;
  }

  public synchronized float withdraw(float amount) throws InsufficientFundsException {
    if (this.balance - amount < 0)
      throw new InsufficientFundsException(id);
    this.balance -= amount;
    return this.balance;
  }

  public class InsufficientFundsException extends Exception{
    public InsufficientFundsException(String accountId) {
      super("Account with id " + accountId + " has insufficient funds for withdrawal.");
    }
  }
}
