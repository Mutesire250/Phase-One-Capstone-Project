package com.igirepay.lab1.model;

import java.time.LocalDateTime;

public class Transaction {

    private int id;
    private int accountId;
    private String referenceId;
    private String transactionType;
    private double amount;
    private LocalDateTime createdAt;

    // Constructor
    public Transaction(int id, int accountId, String referenceId, String transactionType, double amount) {
        this.id = id;
        this.accountId = accountId;
        this.referenceId = referenceId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor with explicit createdAt (used when loading from DB)
    public Transaction(int id, int accountId, String referenceId, String transactionType, double amount, LocalDateTime createdAt) {
        this.id = id;
        this.accountId = accountId;
        this.referenceId = referenceId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", accountId=" + accountId +
                ", referenceId='" + referenceId + '\'' +
                ", transactionType='" + transactionType + '\'' +
                ", amount=" + amount +
                ", createdAt=" + createdAt +
                '}';
    }
}