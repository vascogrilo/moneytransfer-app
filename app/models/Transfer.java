package models;

public class Transfer {

    private String id;
    private String originAccountId;
    private String destinationAccountId;
    private float amount;
    private String timestamp;

    public Transfer() {

    }

    public Transfer(String originAccountId, String destinationAccountId, float amount){
        this.originAccountId = originAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getOriginAccountId() {
        return originAccountId;
    }

    public void setOriginAccountId(String id){
        this.originAccountId = id;
    }

    public String getDestinationAccountId() {
        return destinationAccountId;
    }

    public void setDestinationAccountId(String id) {
        this.destinationAccountId = id;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
