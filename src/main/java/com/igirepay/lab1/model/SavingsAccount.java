package com.igirepay.lab1.model;

public class SavingsAccount extends Account {

    private static final double WITHDRAWAL_FEE = 0.02; // 2% fee
    private static final double MIN_BALANCE = 500.0;   // minimum balance

    // Constructor
    public SavingsAccount(int id, int customerId, double balance) {
        super(id, customerId, "SAVINGS", balance);
    }

    // Savings allows deposits - no restrictions
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

    // Savings has a 2% fee and minimum balance restriction
    @Override
    public boolean withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("Withdrawal amount must be greater than zero.");
            return false;
        }

        double fee = amount * WITHDRAWAL_FEE;
        double totalDeducted = amount + fee;

        if ((getBalance() - totalDeducted) < MIN_BALANCE) {
            System.out.println("Withdrawal denied! Must maintain minimum balance of " + MIN_BALANCE);
            System.out.println("Fee would be: " + fee + " | Total deducted: " + totalDeducted);
            return false;
        }

        setBalance(getBalance() - totalDeducted);
        System.out.println("Withdrawal successful! Fee charged: " + fee + " | New balance: " + getBalance());
        return true;
    }

    // Process a transaction on this savings account
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
        return "SavingsAccount{" +
                "id=" + getId() +
                ", customerId=" + getCustomerId() +
                ", balance=" + getBalance() +
                ", minBalance=" + MIN_BALANCE +
                ", withdrawalFee=" + (WITHDRAWAL_FEE * 100) + "%" +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}