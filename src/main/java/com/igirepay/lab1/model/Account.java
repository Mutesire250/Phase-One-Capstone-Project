package com.igirepay.lab1.model;

import java.time.LocalDateTime;

public abstract class Account {

    private int id;
    private int customerId;
    private String accountType;
    private double balance;
    private LocalDateTime createdAt;

    public Account(int id, int customerId, String accountType, double balance) {
        this.id = id;
        this.customerId = customerId;
        this.accountType = accountType;
        this.balance = balance;
        this.createdAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public abstract boolean deposit(double amount);
    public abstract boolean withdraw(double amount);
    public abstract String processTransaction(String referenceId, double amount, String type);

    @Override
    public String toString() {
        return "Account{id=" + id + ", customerId=" + customerId + ", accountType='" + accountType + "', balance=" + balance + ", createdAt=" + createdAt + '}';
    }
}
