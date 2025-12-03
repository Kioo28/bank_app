package models;

import java.sql.Timestamp;

public class Transaction {
    private int transactionId;
    private int accountId;
    private String transactionType;
    private double amount;
    private String description;
    private Timestamp transactionDate;
    private String status;

    public Transaction(int accountId, String transactionType, double amount, String description) {
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.description = description;
    }

    // ===========================
    // GETTERS AND SETTERS
    // ===========================
    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Timestamp getTransactionDate() { return transactionDate; }
    public void setTransactionDate(Timestamp transactionDate) { this.transactionDate = transactionDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
