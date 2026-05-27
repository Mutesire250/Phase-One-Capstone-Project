package com.igirepay.lab1.model;

public class WalletAccount extends Account {

    // Constructor
    public WalletAccount(int id, int customerId, double balance) {
        super(id, customerId, "WALLET", balance);
    }

    // Wallet allows instant deposits - no restrictions
    @Override
    public boolean deposit(double amount) {
        if (amount <= 0) {
            System.out.println("Deposit amount must be greater than zero.");
            return false;
        }
        setBalance(getBalance() + amount);
        System.out.println("Deposit successful! New balance: " + getBalance());
        return true;
    }

    // Wallet allows withdrawal if enough balance
    @Override
    public boolean withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("Withdrawal amount must be greater than zero.");
            return false;
        }
        if (amount > getBalance()) {
            System.out.println("Insufficient balance!");
            return false;
        }
        setBalance(getBalance() - amount);
        System.out.println("Withdrawal successful! New balance: " + getBalance());
        return true;
    }

    // Process a transaction on this wallet
    @Override
    public String processTransaction(String referenceId, double amount, String type) {
        boolean success = false;

        if (type.equalsIgnoreCase("DEPOSIT")) {
            success = deposit(amount);
        } else if (type.equalsIgnoreCase("WITHDRAW")) {
            success = withdraw(amount);
        } else {
            return "Unknown transaction type: " + type;
        }

        if (success) {
            return "Transaction SUCCESS | Ref: " + referenceId + " | Type: " + type + " | Amount: " + amount;
        } else {
            return "Transaction FAILED | Ref: " + referenceId + " | Type: " + type + " | Amount: " + amount;
        }
    }

    @Override
    public String toString() {
        return "WalletAccount{" +
                "id=" + getId() +
                ", customerId=" + getCustomerId() +
                ", balance=" + getBalance() +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}