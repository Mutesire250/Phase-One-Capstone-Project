package com.igirepay.lab1.model;

public class SavingsAccount extends Account {

    private static final double WITHDRAWAL_FEE = 0.02;
    private static final double MIN_BALANCE = 500.0;

    public SavingsAccount(int id, int customerId, double balance) {
        super(id, customerId, "SAVINGS", balance);
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
        double fee = amount * WITHDRAWAL_FEE;
        double totalDeducted = amount + fee;
        if ((getBalance() - totalDeducted) < MIN_BALANCE) return false;
        setBalance(getBalance() - totalDeducted);
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
        return "SavingsAccount{id=" + getId() + ", customerId=" + getCustomerId() + ", balance=" + getBalance() + ", minBalance=" + MIN_BALANCE + ", withdrawalFee=" + (WITHDRAWAL_FEE * 100) + "%, createdAt=" + getCreatedAt() + '}';
    }
}
