package lab1;

public class Transaction {
    private int id;
    private String referenceId;
    private double amount;
    private String transactionType;
    private long timeStamp;

    public Transaction(int id, String referenceId, double amount, String transactionType, long timeStamp) {
        this.id = id;
        this.referenceId = referenceId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.timeStamp = timeStamp;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public long getTimeStamp() { return timeStamp; }
    public void setTimeStamp(long timeStamp) { this.timeStamp = timeStamp; }

    @Override
    public String toString() {
        return "Lab1.Transaction{" +
                "id=" + id +
                ", referenceId='" + referenceId + '\'' +
                ", amount=" + amount +
                ", transactionType='" + transactionType + '\'' +
                ", timeStamp=" + timeStamp +
                '}';
    }
}