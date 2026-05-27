package com.igirepay.lab2.dao;

import com.igirepay.lab2.db.DBConnection;
import com.igirepay.lab1.model.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public boolean isDuplicate(String referenceId) {
        String sql = "SELECT id FROM processed_requests WHERE reference_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, referenceId);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            System.out.println("Error checking duplicate: " + e.getMessage());
            return false;
        }
    }

    public boolean addTransaction(Transaction transaction) {
        if (isDuplicate(transaction.getReferenceId())) return false;

        String insertTransaction = "INSERT INTO transactions (account_id, reference_id, transaction_type, amount) VALUES (?, ?, ?, ?)";
        String insertProcessed = "INSERT INTO processed_requests (reference_id) VALUES (?)";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement stmt1 = conn.prepareStatement(insertTransaction);
            stmt1.setInt(1, transaction.getAccountId());
            stmt1.setString(2, transaction.getReferenceId());
            stmt1.setString(3, transaction.getTransactionType());
            stmt1.setDouble(4, transaction.getAmount());
            stmt1.executeUpdate();
            PreparedStatement stmt2 = conn.prepareStatement(insertProcessed);
            stmt2.setString(1, transaction.getReferenceId());
            stmt2.executeUpdate();
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.out.println("Error saving transaction: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { System.out.println("Rollback failed: " + ex.getMessage()); }
            return false;
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { System.out.println("Error closing connection: " + e.getMessage()); }
        }
    }

    public List<Transaction> getTransactionsByAccountId(int accountId) {
        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY created_at DESC";
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("created_at");
                java.time.LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : java.time.LocalDateTime.now();
                transactions.add(new Transaction(rs.getInt("id"), rs.getInt("account_id"), rs.getString("reference_id"), rs.getString("transaction_type"), rs.getDouble("amount"), createdAt));
            }
        } catch (SQLException e) {
            System.out.println("Error getting transactions: " + e.getMessage());
        }
        return transactions;
    }

    public List<Transaction> getAllTransactions() {
        String sql = "SELECT * FROM transactions ORDER BY created_at DESC";
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("created_at");
                java.time.LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : java.time.LocalDateTime.now();
                transactions.add(new Transaction(rs.getInt("id"), rs.getInt("account_id"), rs.getString("reference_id"), rs.getString("transaction_type"), rs.getDouble("amount"), createdAt));
            }
        } catch (SQLException e) {
            System.out.println("Error getting transactions: " + e.getMessage());
        }
        return transactions;
    }

    public boolean addTransfer(int fromAccountId, int toAccountId, String referenceId, double amount) {
        if (isDuplicate(referenceId)) return false;

        String selectBalance = "SELECT id, balance FROM accounts WHERE id = ? FOR UPDATE";
        String updateBalance = "UPDATE accounts SET balance = ? WHERE id = ?";
        String insertTransaction = "INSERT INTO transactions (account_id, reference_id, transaction_type, amount) VALUES (?, ?, ?, ?)";
        String insertProcessed = "INSERT INTO processed_requests (reference_id) VALUES (?)";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement psFrom = conn.prepareStatement(selectBalance);
            psFrom.setInt(1, fromAccountId);
            ResultSet rsFrom = psFrom.executeQuery();
            if (!rsFrom.next()) { conn.rollback(); return false; }
            double fromBalance = rsFrom.getDouble("balance");

            PreparedStatement psTo = conn.prepareStatement(selectBalance);
            psTo.setInt(1, toAccountId);
            ResultSet rsTo = psTo.executeQuery();
            if (!rsTo.next()) { conn.rollback(); return false; }
            double toBalance = rsTo.getDouble("balance");

            if (fromBalance < amount) { conn.rollback(); return false; }

            PreparedStatement upd1 = conn.prepareStatement(updateBalance);
            upd1.setDouble(1, fromBalance - amount);
            upd1.setInt(2, fromAccountId);
            upd1.executeUpdate();

            PreparedStatement upd2 = conn.prepareStatement(updateBalance);
            upd2.setDouble(1, toBalance + amount);
            upd2.setInt(2, toAccountId);
            upd2.executeUpdate();

            PreparedStatement t1 = conn.prepareStatement(insertTransaction);
            t1.setInt(1, fromAccountId); t1.setString(2, referenceId); t1.setString(3, "TRANSFER_OUT"); t1.setDouble(4, amount);
            t1.executeUpdate();

            PreparedStatement t2 = conn.prepareStatement(insertTransaction);
            t2.setInt(1, toAccountId); t2.setString(2, referenceId); t2.setString(3, "TRANSFER_IN"); t2.setDouble(4, amount);
            t2.executeUpdate();

            PreparedStatement p = conn.prepareStatement(insertProcessed);
            p.setString(1, referenceId);
            p.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.out.println("Error performing transfer: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { System.out.println("Rollback failed: " + ex.getMessage()); }
            return false;
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { System.out.println("Error closing connection: " + e.getMessage()); }
        }
    }
}
