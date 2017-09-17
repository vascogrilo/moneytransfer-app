package models;

import static util.Util.validateAmount;
import static util.Util.validateString;

/**
 * Simple model of a money transfer operation.
 * A transfer is represented by an Id, origin Account id, destination Account id, amount to be transferred and a timestamp.
 * Provides all methods for interacting with the internal fields.
 */
public class Transfer {

    private String id;
    private String originAccountId;
    private String destinationAccountId;
    private Float amount;
    private String timestamp;

    /**
     * Default constructor.
     * Returns an empty transfer with string fields null and an amount of 0f.
     */
    public Transfer() {
        this.amount = 0f;
    }

    /**
     * Creates a new instance of
     * @param originAccountId
     * @param destinationAccountId
     * @param amount
     */
    public Transfer(String originAccountId, String destinationAccountId, float amount){
        this.originAccountId = originAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
    }

    /**
     * Returns the transfer's id.
     * @return A string representing the transfer id.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the Id for this transfer.
     * @param id A string for the transfer's id.
     * @throws IllegalArgumentException if string is null or empty.
     */
    public void setId(String id){
        validateString(id);
        this.id = id;
    }

    /**
     * Returns the transfer's origin account id.
     * @return A string representing the transfer's origin account id.
     */
    public String getOriginAccountId() {
        return originAccountId;
    }

    /**
     * Sets the origin account id for this transfer.
     * @param id The origin account's id.
     * @throws IllegalArgumentException if string is null or empty.
     */
    public void setOriginAccountId(String id){
        validateString(id);
        this.originAccountId = id;
    }

    /**
     * Returns the transfer's destination account id.
     * @return A string representing the transfer's destination account id.
     */
    public String getDestinationAccountId() {
        return destinationAccountId;
    }

    /**
     * Sets the destination account id for this transfer.
     * @param id A string for the destination account id.
     * @throws IllegalArgumentException if string is null or empty.
     */
    public void setDestinationAccountId(String id) {
        validateString(id);
        this.destinationAccountId = id;
    }

    /**
     * Returns the transfer amount.
     * @return A Float representing the transfer amount.
     */
    public Float getAmount() {
        return amount;
    }

    /**
     * Sets the amount for this transfer.
     * @param amount A Float for the transfer amount.
     * @throws IllegalArgumentException if amount is less or equal than zero, NaN or infinite.
     */
    public void setAmount(Float amount) {
        validateAmount(amount);
        this.amount = amount;
    }

    /**
     * Returns the transfer's timestamp.
     * @return A string representing the transfer's timestamp.
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     *
     * @param timestamp
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}