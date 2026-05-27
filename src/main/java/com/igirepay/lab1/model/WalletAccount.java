package com.igirepay.lab1.model;

public class WalletAccount extends Account {

    public WalletAccount(int id, int customerId, double balance) {
        super(id, customerId, "WALLET", balance);
    }

    @Override
    public boolean deposit(double amount) {
        if (amount <= 0) return false;
        setBalance(getBalance() + amount);
        return true;
    }

    @Override
    public boolean withdraw(double amount) {
        if (amount <= 0) return false;
        if (amount > getBalance()) return false;
        setBalance(getBalance() - amount);
        return true;
    }

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
        return (success ? "Transaction SUCCESS" : "Transaction FAILED") + " | Ref: " + referenceId + " | Type: " + type + " | Amount: " + amount;
    }

    @Override
    public String toString() {
        return "WalletAccount{id=" + getId() + ", customerId=" + getCustomerId() + ", balance=" + getBalance() + ", createdAt=" + getCreatedAt() + '}';
    }
}
