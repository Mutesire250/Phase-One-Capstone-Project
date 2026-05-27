package com.igirepay.lab3.service;

import com.igirepay.lab2.dao.TransactionDAO;
import com.igirepay.lab2.dao.AccountDAO;
import com.igirepay.lab1.model.Transaction;
import com.igirepay.lab1.model.Account;
import com.igirepay.lab3.exception.*;

import java.util.List;
import java.util.UUID;

public class TransactionService {

    private TransactionDAO transactionDAO;
    private AccountDAO accountDAO;

    // Exercise 1.3 compliance: Set of processed transaction reference IDs
    private static final java.util.Set<String> processedReferenceIdsCache = new java.util.concurrent.ConcurrentSkipListSet<>();

    // Exercise 1.3 compliance: List of failed transaction logs
    private static final java.util.List<String> failedTransactionLogs = new java.util.concurrent.CopyOnWriteArrayList<>();

    public TransactionService() {
        this.transactionDAO = new TransactionDAO();
        this.accountDAO = new AccountDAO();
    }

    private void logFailed(String reason) {
        String logEntry = String.format("[%s] Failed: %s", java.time.LocalDateTime.now().toString().substring(0, 19), reason);
        failedTransactionLogs.add(logEntry);
        System.out.println("❌ " + logEntry);
    }

    public List<String> getFailedTransactionLogs() {
        return failedTransactionLogs;
    }

    // Generate unique reference ID for idempotency
    public String generateReferenceId() {
        return "TXN_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // Process a deposit transaction
    // Demonstrates: Exception handling, idempotency check, and custom exceptions.
    public boolean processDeposit(int accountId, double amount, String referenceId) 
            throws InvalidAmountException, DuplicateRequestException, AccountNotFoundException {
        // Validate amount
        if (amount <= 0) {
            logFailed("Deposit on Account " + accountId + " failed: Invalid amount " + amount);
            throw new InvalidAmountException("Amount must be greater than zero!");
        }

        // Check for duplicate transaction
        if (isDuplicate(referenceId)) {
            logFailed("Deposit on Account " + accountId + " failed: Duplicate reference ID " + referenceId);
            throw new DuplicateRequestException("Duplicate transaction detected! Reference ID: " + referenceId);
        }

        // Get account and perform deposit
        Account account = accountDAO.getAccountById(accountId);
        if (account == null) {
            logFailed("Deposit on Account " + accountId + " failed: Account not found");
            throw new AccountNotFoundException("Account with ID " + accountId + " not found!");
        }

        boolean success = account.deposit(amount);
        if (success) {
            // Update balance in database
            accountDAO.updateBalance(accountId, account.getBalance());

            // Save transaction record
            Transaction transaction = new Transaction(0, accountId, referenceId, "DEPOSIT", amount);
            transactionDAO.addTransaction(transaction);

            // Add to in-memory cache
            processedReferenceIdsCache.add(referenceId);

            System.out.println(" Deposit processed successfully! Ref: " + referenceId);
            return true;
        }

        logFailed("Deposit on Account " + accountId + " failed: internal deposit execution failed");
        return false;
    }

    // Process a withdrawal transaction
    // Demonstrates: Exception handling and minimum balance checks.
    public boolean processWithdrawal(int accountId, double amount, String referenceId) 
            throws InvalidAmountException, DuplicateRequestException, AccountNotFoundException, InsufficientBalanceException {
        // Validate amount
        if (amount <= 0) {
            logFailed("Withdrawal on Account " + accountId + " failed: Invalid amount " + amount);
            throw new InvalidAmountException("Amount must be greater than zero!");
        }

        // Check for duplicate transaction
        if (isDuplicate(referenceId)) {
            logFailed("Withdrawal on Account " + accountId + " failed: Duplicate reference ID " + referenceId);
            throw new DuplicateRequestException("Duplicate transaction detected! Reference ID: " + referenceId);
        }

        // Get account and perform withdrawal
        Account account = accountDAO.getAccountById(accountId);
        if (account == null) {
            logFailed("Withdrawal on Account " + accountId + " failed: Account not found");
            throw new AccountNotFoundException("Account with ID " + accountId + " not found!");
        }

        // Check balance before attempting withdrawal (savings fee/minimum balance)
        double minRequired = account.getAccountType().equalsIgnoreCase("savings") ? 500.0 : 0.0;
        double charge = account.getAccountType().equalsIgnoreCase("savings") ? amount * 0.02 : 0.0;
        if (account.getBalance() < (amount + charge + minRequired)) {
            String errorMsg = String.format("Insufficient funds! Required: %.2f RWF (including fee/minimum), Available: %.2f RWF", 
                    (amount + charge + minRequired), account.getBalance());
            logFailed("Withdrawal on Account " + accountId + " failed: " + errorMsg);
            throw new InsufficientBalanceException(errorMsg);
        }

        boolean success = account.withdraw(amount);
        if (success) {
            // Update balance in database
            accountDAO.updateBalance(accountId, account.getBalance());

            // Save transaction record
            Transaction transaction = new Transaction(0, accountId, referenceId, "WITHDRAWAL", amount);
            transactionDAO.addTransaction(transaction);

            // Add to in-memory cache
            processedReferenceIdsCache.add(referenceId);

            System.out.println(" Withdrawal processed successfully! Ref: " + referenceId);
            return true;
        }

        logFailed("Withdrawal on Account " + accountId + " failed: internal withdraw execution failed");
        return false;
    }

    // Get transaction history for an account
    public List<Transaction> getTransactionHistory(int accountId) {
        return transactionDAO.getTransactionsByAccountId(accountId);
    }

    // Get all transactions
    public List<Transaction> getAllTransactions() {
        return transactionDAO.getAllTransactions();
    }

    // Check if transaction reference is duplicate (checks Set cache first)
    // Demonstrates: Java Collections (Set) for duplicate reference ID protection.
    public boolean isDuplicate(String referenceId) {
        if (processedReferenceIdsCache.contains(referenceId)) {
            return true;
        }
        boolean duplicate = transactionDAO.isDuplicate(referenceId);
        if (duplicate) {
            processedReferenceIdsCache.add(referenceId);
        }
        return duplicate;
    }

    // Process a transfer between accounts
    // Demonstrates: Atomic Transaction (JDBC Rollback) and custom exceptions.
    public boolean processTransfer(int fromAccountId, int toAccountId, double amount, String referenceId) 
            throws InvalidAmountException, DuplicateRequestException, AccountNotFoundException, InsufficientBalanceException {
        if (amount <= 0) {
            logFailed("Transfer from " + fromAccountId + " to " + toAccountId + " failed: Invalid amount " + amount);
            throw new InvalidAmountException("Amount must be greater than zero!");
        }

        if (isDuplicate(referenceId)) {
            logFailed("Transfer from " + fromAccountId + " to " + toAccountId + " failed: Duplicate reference ID " + referenceId);
            throw new DuplicateRequestException("Duplicate transaction detected! Reference ID: " + referenceId);
        }

        Account fromAcc = accountDAO.getAccountById(fromAccountId);
        if (fromAcc == null) {
            logFailed("Transfer from " + fromAccountId + " failed: Sender account not found");
            throw new AccountNotFoundException("Sender account with ID " + fromAccountId + " not found!");
        }

        Account toAcc = accountDAO.getAccountById(toAccountId);
        if (toAcc == null) {
            logFailed("Transfer to " + toAccountId + " failed: Receiver account not found");
            throw new AccountNotFoundException("Receiver account with ID " + toAccountId + " not found!");
        }

        // Check balance on sender account
        double minRequired = fromAcc.getAccountType().equalsIgnoreCase("savings") ? 500.0 : 0.0;
        double charge = fromAcc.getAccountType().equalsIgnoreCase("savings") ? amount * 0.02 : 0.0;
        if (fromAcc.getBalance() < (amount + charge + minRequired)) {
            String errorMsg = String.format("Insufficient funds in sender account! Required: %.2f RWF, Available: %.2f RWF", 
                    (amount + charge + minRequired), fromAcc.getBalance());
            logFailed("Transfer from " + fromAccountId + " failed: " + errorMsg);
            throw new InsufficientBalanceException(errorMsg);
        }

        // Use TransactionDAO.addTransfer which performs atomic DB update with rollback
        boolean success = transactionDAO.addTransfer(fromAccountId, toAccountId, referenceId, amount);

        if (success) {
            processedReferenceIdsCache.add(referenceId);
            System.out.println(" Transfer processed successfully! Ref: " + referenceId);
            return true;
        } else {
            logFailed("Transfer from " + fromAccountId + " to " + toAccountId + " failed: Database transaction rolled back.");
            throw new InsufficientBalanceException("Transfer failed: database rollback occurred.");
        }
    }

    // Export transaction history to CSV
    public void exportToCSV(int accountId, String filePath) {
        List<Transaction> transactions = getTransactionHistory(accountId);

        if (transactions.isEmpty()) {
            System.out.println(" No transactions found for this account!");
            return;
        }

        java.nio.file.Path outDir = java.nio.file.Paths.get("exports");
        try {
            if (!java.nio.file.Files.exists(outDir)) {
                java.nio.file.Files.createDirectories(outDir);
            }
        } catch (java.io.IOException e) {
            System.out.println("Could not create exports directory: " + e.getMessage());
        }

        java.nio.file.Path outFile = outDir.resolve(filePath);

        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (java.io.BufferedWriter writer = java.nio.file.Files.newBufferedWriter(outFile)) {
            writer.write("ID,Reference ID,Type,Amount,Created At\n");

            for (Transaction t : transactions) {
                String created = t.getCreatedAt() != null ? t.getCreatedAt().format(fmt) : "";
                writer.write(String.format("%d,%s,%s,%.2f,%s\n",
                        t.getId(), t.getReferenceId(), t.getTransactionType(), t.getAmount(), created));
            }

            System.out.println(" Transactions exported to: " + outFile.toString());

        } catch (java.io.IOException e) {
            System.out.println(" Error exporting transactions: " + e.getMessage());
        }
    }
}