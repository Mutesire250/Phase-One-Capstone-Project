package com.igirepay.lab2.dao;

import com.igirepay.lab2.db.DBConnection;
import com.igirepay.lab1.model.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    // CHECK - Is this reference ID already processed (duplicate check)
    public boolean isDuplicate(String referenceId) {
        String sql = "SELECT id FROM processed_requests WHERE reference_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, referenceId);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // true means duplicate found

        } catch (SQLException e) {
            System.out.println("Error checking duplicate: " + e.getMessage());
            return false;
        }
    }

    // CREATE - Add new transaction + mark as processed (both happen together)
    public boolean addTransaction(Transaction transaction) {

        // First check for duplicate
        if (isDuplicate(transaction.getReferenceId())) {
            System.out.println("❌ Duplicate transaction detected! Reference ID already processed: "
                    + transaction.getReferenceId());
            return false;
        }

        String insertTransaction = "INSERT INTO transactions (account_id, reference_id, transaction_type, amount) " +
                "VALUES (?, ?, ?, ?)";
        String insertProcessed = "INSERT INTO processed_requests (reference_id) VALUES (?)";

        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // start transaction

            // Insert into transactions table
            PreparedStatement stmt1 = conn.prepareStatement(insertTransaction);
            stmt1.setInt(1, transaction.getAccountId());
            stmt1.setString(2, transaction.getReferenceId());
            stmt1.setString(3, transaction.getTransactionType());
            stmt1.setDouble(4, transaction.getAmount());
            stmt1.executeUpdate();

            // Insert into processed_requests table
            PreparedStatement stmt2 = conn.prepareStatement(insertProcessed);
            stmt2.setString(1, transaction.getReferenceId());
            stmt2.executeUpdate();

            conn.commit(); // save both together
            System.out.println("✅ Transaction saved successfully!");
            return true;

        } catch (SQLException e) {
            System.out.println("Error saving transaction: " + e.getMessage());
            try {
                if (conn != null) conn.rollback(); // undo if anything fails
                System.out.println("Transaction rolled back.");
            } catch (SQLException ex) {
                System.out.println("Rollback failed: " + ex.getMessage());
            }
            return false;

        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    // READ - Get all transactions for an account
    public List<Transaction> getTransactionsByAccountId(int accountId) {
        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY created_at DESC";
        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                java.sql.Timestamp ts = rs.getTimestamp("created_at");
                java.time.LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : java.time.LocalDateTime.now();
                transactions.add(new com.igirepay.lab1.model.Transaction(
                    rs.getInt("id"),
                    rs.getInt("account_id"),
                    rs.getString("reference_id"),
                    rs.getString("transaction_type"),
                    rs.getDouble("amount"),
                    createdAt
                ));
            }

        } catch (SQLException e) {
            System.out.println("Error getting transactions: " + e.getMessage());
        }
        return transactions;
    }

    // READ - Get all transactions
    public List<Transaction> getAllTransactions() {
        String sql = "SELECT * FROM transactions ORDER BY created_at DESC";
        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                java.sql.Timestamp ts = rs.getTimestamp("created_at");
                java.time.LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : java.time.LocalDateTime.now();
                transactions.add(new com.igirepay.lab1.model.Transaction(
                    rs.getInt("id"),
                    rs.getInt("account_id"),
                    rs.getString("reference_id"),
                    rs.getString("transaction_type"),
                    rs.getDouble("amount"),
                    createdAt
                ));
            }

        } catch (SQLException e) {
            System.out.println("Error getting transactions: " + e.getMessage());
        }
        return transactions;
    }

    // CREATE - Add a transfer (atomic): debit fromAccount, credit toAccount,
    // insert two transactions and mark reference as processed in one DB transaction
    public boolean addTransfer(int fromAccountId, int toAccountId, String referenceId, double amount) {
        // Duplicate check
        if (isDuplicate(referenceId)) {
            System.out.println("❌ Duplicate transfer detected! Reference ID already processed: " + referenceId);
            return false;
        }

        String selectBalance = "SELECT id, balance FROM accounts WHERE id = ? FOR UPDATE";
        String updateBalance = "UPDATE accounts SET balance = ? WHERE id = ?";
        String insertTransaction = "INSERT INTO transactions (account_id, reference_id, transaction_type, amount) VALUES (?, ?, ?, ?)";
        String insertProcessed = "INSERT INTO processed_requests (reference_id) VALUES (?)";

        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Lock and read sender balance
            PreparedStatement psFrom = conn.prepareStatement(selectBalance);
            psFrom.setInt(1, fromAccountId);
            ResultSet rsFrom = psFrom.executeQuery();
            if (!rsFrom.next()) {
                System.out.println("Sender account not found: " + fromAccountId);
                conn.rollback();
                return false;
            }
            double fromBalance = rsFrom.getDouble("balance");

            // Lock and read receiver balance
            PreparedStatement psTo = conn.prepareStatement(selectBalance);
            psTo.setInt(1, toAccountId);
            ResultSet rsTo = psTo.executeQuery();
            if (!rsTo.next()) {
                System.out.println("Receiver account not found: " + toAccountId);
                conn.rollback();
                return false;
            }
            double toBalance = rsTo.getDouble("balance");

            // Check sufficient funds
            if (fromBalance < amount) {
                System.out.println("Insufficient funds for transfer. Available: " + fromBalance + " Requested: " + amount);
                conn.rollback();
                return false;
            }

            // Update balances
            PreparedStatement upd = conn.prepareStatement(updateBalance);
            upd.setDouble(1, fromBalance - amount);
            upd.setInt(2, fromAccountId);
            upd.executeUpdate();

            PreparedStatement upd2 = conn.prepareStatement(updateBalance);
            upd2.setDouble(1, toBalance + amount);
            upd2.setInt(2, toAccountId);
            upd2.executeUpdate();

            // Insert transaction for sender (TRANSFER_OUT)
            PreparedStatement t1 = conn.prepareStatement(insertTransaction);
            t1.setInt(1, fromAccountId);
            t1.setString(2, referenceId);
            t1.setString(3, "TRANSFER_OUT");
            t1.setDouble(4, amount);
            t1.executeUpdate();

            // Insert transaction for receiver (TRANSFER_IN)
            PreparedStatement t2 = conn.prepareStatement(insertTransaction);
            t2.setInt(1, toAccountId);
            t2.setString(2, referenceId);
            t2.setString(3, "TRANSFER_IN");
            t2.setDouble(4, amount);
            t2.executeUpdate();

            // Mark processed
            PreparedStatement p = conn.prepareStatement(insertProcessed);
            p.setString(1, referenceId);
            p.executeUpdate();

            conn.commit();
            System.out.println("✅ Transfer completed and recorded. Ref: " + referenceId);
            return true;

        } catch (SQLException e) {
            System.out.println("Error performing transfer: " + e.getMessage());
            try {
                if (conn != null) conn.rollback();
                System.out.println("Transfer rolled back.");
            } catch (SQLException ex) {
                System.out.println("Rollback failed: " + ex.getMessage());
            }
            return false;
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}