package models;

import static util.Util.validateAmount;
import static util.Util.validateString;

/**
 * Simple model of a money Account.
 * An account is represented by an Id, a Name, Owner name and it's balance.
 * This class contains all necessary methods for interacting with all fields.
 * Also contains methods for depositing and withdrawing money amounts from this account.
 * Deposit and withdraw methods are thread-safe.
 */
public class Account {

  private String id;
  private String name;
  private String ownerName;
  private Float balance;

  /**
   * Default constructor.
   * Returns an empty account with string fields null and initial balance of 0.
   */
  public Account() {
    this.balance = 0f;
  }

  /**
   * Creates an account with the specified values.
   * @param name The account's name.
   * @param ownerName The account's owner name.
   * @param balance The initial balance.
   */
  public Account(String name, String ownerName, Float balance) {
    this.name = name;
    this.ownerName = ownerName;
    this.balance = balance;
  }

  /**
   * Returns the account's id.
   * @return A string representing the account's id.
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the account's id.
   * @param id A string for the account's id.
   * @throws IllegalArgumentException if string is null or empty.
   */
  public void setId(String id) {
    validateString(id);
    this.id = id;
  }

  /**
   * Returns the account's name.
   * @return A string representing the account's name.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the account's name.
   * @param name A string for the account's name.
   * @throws IllegalArgumentException if string is null or empty.
   */
  public void setName(String name) {
    validateString(name);
    this.name = name;
  }

  /**
   * Returns the account's owner name.
   * @return A string representing the account's id.
   */
  public String getOwnerName() {
    return ownerName;
  }

  /**
   * Sets the account's owner name.
   * @param ownerName A string for the account's owner name.
   * @throws IllegalArgumentException if string is null or empty.
   */
  public void setOwnerName(String ownerName) {
    validateString(ownerName);
    this.ownerName = ownerName;
  }

  /**
   * Returns the account's balance.
   * @return A Float representing the account's balance.
   */
  public float getBalance() {
    return balance;
  }

  /**
   * Sets the account's balance.
   * @param balance The balance.
   * @throws IllegalArgumentException if the new balance is either negative, NaN or infinite.
   */
  public void setBalance(Float balance) {
    if (balance < 0 || balance.isNaN() || balance.isInfinite())
      throw new IllegalArgumentException("New balance should be greater or equal than zero (and non-infinite).");
    this.balance = balance;
  }

  /**
   * Deposits a certain amount into the account.
   * This method is thread-safe.
   * @param amount The amount to be deposited.
   * @return The new account's balance.
   * @throws IllegalArgumentException if amount is either zero, negative, NaN or infinite.
   */
  public synchronized Account deposit(Float amount) {
    validateAmount(amount);
    this.balance += amount;
    return this;
  }

  /**
   * Withdraws a certain amount from the account.
   * @param amount The amount to be withdrawn.
   * @return The new account's balance.
   * @throws InsufficientFundsException if the amount is greater than the current balance.
   * @throws IllegalArgumentException if the amount is either negative, NaN or infinite.
   */
  public synchronized Account withdraw(Float amount) throws InsufficientFundsException {
    validateAmount(amount);
    if (this.balance - amount < 0)
      throw new InsufficientFundsException(id);
    this.balance -= amount;
    return this;
  }

  /**
   * Custom exception for insufficient funds when withdrawing from an Account.
   */
  public class InsufficientFundsException extends Exception{
    public InsufficientFundsException(String accountId) {
      super("Account with id " + accountId + " has insufficient funds for withdrawal.");
    }
  }
}
