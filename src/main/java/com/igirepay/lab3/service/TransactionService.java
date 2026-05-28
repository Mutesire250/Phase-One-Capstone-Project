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

    private static final java.util.Set<String> processedReferenceIdsCache = new java.util.concurrent.ConcurrentSkipListSet<>();
    private static final java.util.List<String> failedTransactionLogs = new java.util.concurrent.CopyOnWriteArrayList<>();

    public TransactionService() {
        this.transactionDAO = new TransactionDAO();
        this.accountDAO = new AccountDAO();
    }

    private void logFailed(String reason) {
        failedTransactionLogs.add(String.format("[%s] Failed: %s", java.time.LocalDateTime.now().toString().substring(0, 19), reason));
    }

    public List<String> getFailedTransactionLogs() {
        return failedTransactionLogs;
    }

    public String generateReferenceId() {
        return "TXN_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public boolean processDeposit(int accountId, double amount, String referenceId) throws InvalidAmountException, DuplicateRequestException, AccountNotFoundException {
        if (amount <= 0) { logFailed("Deposit on Account " + accountId + ": Invalid amount"); throw new InvalidAmountException("Amount must be greater than zero!"); }
        if (isDuplicate(referenceId)) { logFailed("Deposit on Account " + accountId + ": Duplicate ref " + referenceId); throw new DuplicateRequestException("Duplicate transaction detected! Reference ID: " + referenceId); }
        Account account = accountDAO.getAccountById(accountId);
        if (account == null) { logFailed("Deposit on Account " + accountId + ": Account not found"); throw new AccountNotFoundException("Account with ID " + accountId + " not found!"); }
        boolean success = account.deposit(amount);
        if (success) {
            accountDAO.updateBalance(accountId, account.getBalance());
            transactionDAO.addTransaction(new Transaction(0, accountId, referenceId, "DEPOSIT", amount));
            processedReferenceIdsCache.add(referenceId);
        }
        return success;
    }

    public boolean processWithdrawal(int accountId, double amount, String referenceId) throws InvalidAmountException, DuplicateRequestException, AccountNotFoundException, InsufficientBalanceException {
        if (amount <= 0) { logFailed("Withdrawal on Account " + accountId + ": Invalid amount"); throw new InvalidAmountException("Amount must be greater than zero!"); }
        if (isDuplicate(referenceId)) { logFailed("Withdrawal on Account " + accountId + ": Duplicate ref " + referenceId); throw new DuplicateRequestException("Duplicate transaction detected! Reference ID: " + referenceId); }
        Account account = accountDAO.getAccountById(accountId);
        if (account == null) { logFailed("Withdrawal on Account " + accountId + ": Account not found"); throw new AccountNotFoundException("Account with ID " + accountId + " not found!"); }
        double minRequired = account.getAccountType().equalsIgnoreCase("savings") ? 500.0 : 0.0;
        double charge = account.getAccountType().equalsIgnoreCase("savings") ? amount * 0.02 : 0.0;
        if (account.getBalance() < (amount + charge + minRequired)) {
            String msg = String.format("Insufficient funds! Required: %.2f RWF, Available: %.2f RWF", (amount + charge + minRequired), account.getBalance());
            logFailed("Withdrawal on Account " + accountId + ": " + msg);
            throw new InsufficientBalanceException(msg);
        }
        boolean success = account.withdraw(amount);
        if (success) {
            accountDAO.updateBalance(accountId, account.getBalance());
            transactionDAO.addTransaction(new Transaction(0, accountId, referenceId, "WITHDRAWAL", amount));
            processedReferenceIdsCache.add(referenceId);
        }
        return success;
    }

    public List<Transaction> getTransactionHistory(int accountId) {
        return transactionDAO.getTransactionsByAccountId(accountId);
    }

    public List<Transaction> getAllTransactions() {
        return transactionDAO.getAllTransactions();
    }

    public boolean isDuplicate(String referenceId) {
        if (processedReferenceIdsCache.contains(referenceId)) return true;
        boolean duplicate = transactionDAO.isDuplicate(referenceId);
        if (duplicate) processedReferenceIdsCache.add(referenceId);
        return duplicate;
    }

    public boolean processTransfer(int fromAccountId, int toAccountId, double amount, String referenceId) throws InvalidAmountException, DuplicateRequestException, AccountNotFoundException, InsufficientBalanceException {
        if (amount <= 0) throw new InvalidAmountException("Amount must be greater than zero!");
        if (isDuplicate(referenceId)) throw new DuplicateRequestException("Duplicate transaction detected! Reference ID: " + referenceId);
        Account fromAcc = accountDAO.getAccountById(fromAccountId);
        if (fromAcc == null) throw new AccountNotFoundException("Sender account with ID " + fromAccountId + " not found!");
        Account toAcc = accountDAO.getAccountById(toAccountId);
        if (toAcc == null) throw new AccountNotFoundException("Receiver account with ID " + toAccountId + " not found!");
        double minRequired = fromAcc.getAccountType().equalsIgnoreCase("savings") ? 500.0 : 0.0;
        double charge = fromAcc.getAccountType().equalsIgnoreCase("savings") ? amount * 0.02 : 0.0;
        if (fromAcc.getBalance() < (amount + charge + minRequired)) {
            throw new InsufficientBalanceException(String.format("Insufficient funds! Required: %.2f RWF, Available: %.2f RWF", (amount + charge + minRequired), fromAcc.getBalance()));
        }
        boolean success = transactionDAO.addTransfer(fromAccountId, toAccountId, referenceId, amount);
        if (success) { processedReferenceIdsCache.add(referenceId); return true; }
        throw new InsufficientBalanceException("Transfer failed: database rollback occurred.");
    }

    public void exportToCSV(int accountId, String filePath) {
        List<Transaction> transactions = getTransactionHistory(accountId);
        if (transactions.isEmpty()) return;
        java.nio.file.Path outDir = java.nio.file.Paths.get("exports");
        try {
            if (!java.nio.file.Files.exists(outDir)) java.nio.file.Files.createDirectories(outDir);
        } catch (java.io.IOException e) {
            System.out.println("Could not create exports directory: " + e.getMessage());
        }
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try (java.io.BufferedWriter writer = java.nio.file.Files.newBufferedWriter(outDir.resolve(filePath))) {
            writer.write("ID,Reference ID,Type,Amount,Created At\n");
            for (Transaction t : transactions) {
                writer.write(String.format("%d,%s,%s,%.2f,%s\n", t.getId(), t.getReferenceId(), t.getTransactionType(), t.getAmount(), t.getCreatedAt() != null ? t.getCreatedAt().format(fmt) : ""));
            }
        } catch (java.io.IOException e) {
            System.out.println("Error exporting transactions: " + e.getMessage());
        }
    }

    public void exportToFile(int accountId, String absolutePath) {
        List<Transaction> transactions = getTransactionHistory(accountId);
        if (transactions.isEmpty()) throw new RuntimeException("No transactions found for this account.");
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try (java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(absolutePath))) {
            writer.write("ID,Reference ID,Type,Amount,Created At\n");
            for (Transaction t : transactions) {
                writer.write(String.format("%d,%s,%s,%.2f,%s\n",
                    t.getId(), t.getReferenceId(), t.getTransactionType(), t.getAmount(),
                    t.getCreatedAt() != null ? t.getCreatedAt().format(fmt) : ""));
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to write file: " + e.getMessage());
        }
    }
}
