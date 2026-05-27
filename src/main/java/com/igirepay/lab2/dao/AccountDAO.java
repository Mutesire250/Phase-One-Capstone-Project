package com.igirepay.lab2.dao;

import com.igirepay.lab2.db.DBConnection;
import com.igirepay.lab1.model.Account;
import com.igirepay.lab1.model.WalletAccount;
import com.igirepay.lab1.model.SavingsAccount;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {

    public boolean addAccount(Account account) {
        String sql = "INSERT INTO accounts (customer_id, account_type, balance) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, account.getCustomerId());
            stmt.setString(2, account.getAccountType());
            stmt.setDouble(3, account.getBalance());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error adding account: " + e.getMessage());
            return false;
        }
    }

    public Account getAccountById(int id) {
        String sql = "SELECT * FROM accounts WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapAccount(rs);
        } catch (SQLException e) {
            System.out.println("Error getting account: " + e.getMessage());
        }
        return null;
    }

    public List<Account> getAccountsByCustomerId(int customerId) {
        String sql = "SELECT * FROM accounts WHERE customer_id = ?";
        List<Account> accounts = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) accounts.add(mapAccount(rs));
        } catch (SQLException e) {
            System.out.println("Error getting accounts: " + e.getMessage());
        }
        return accounts;
    }

    public boolean updateBalance(int accountId, double newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, newBalance);
            stmt.setInt(2, accountId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error updating balance: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteAccount(int id) {
        String sql = "DELETE FROM accounts WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting account: " + e.getMessage());
            return false;
        }
    }

    private Account mapAccount(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int customerId = rs.getInt("customer_id");
        String type = rs.getString("account_type");
        double balance = rs.getDouble("balance");
        if (type.equalsIgnoreCase("WALLET")) {
            return new WalletAccount(id, customerId, balance);
        } else {
            return new SavingsAccount(id, customerId, balance);
        }
    }
}
