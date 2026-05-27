package com.igirepay.lab2.dao;

import com.igirepay.lab2.db.DBConnection;
import com.igirepay.lab1.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public boolean addCustomer(Customer customer) {
        String sql = "INSERT INTO customers (full_name, email, phone_number, pin, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customer.getFullName());
            stmt.setString(2, customer.getEmail());
            stmt.setString(3, customer.getPhoneNumber());
            stmt.setString(4, customer.getPin());
            stmt.setString(5, customer.getRole() == null ? "user" : customer.getRole());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error adding customer: " + e.getMessage());
            return false;
        }
    }

    public Customer getCustomerById(int id) {
        String sql = "SELECT * FROM customers WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapCustomer(rs);
        } catch (SQLException e) {
            System.out.println("Error getting customer: " + e.getMessage());
        }
        return null;
    }

    public Customer getCustomerByEmail(String email) {
        String sql = "SELECT * FROM customers WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapCustomer(rs);
        } catch (SQLException e) {
            System.out.println("Error getting customer: " + e.getMessage());
        }
        return null;
    }

    public List<Customer> getAllCustomers() {
        String sql = "SELECT * FROM customers";
        List<Customer> customers = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            java.util.Set<String> cols = new java.util.HashSet<>();
            for (int i = 1; i <= meta.getColumnCount(); i++) cols.add(meta.getColumnName(i).toLowerCase());
            while (rs.next()) {
                String role = cols.contains("role") ? rs.getString("role") : "user";
                int failedAttempts = cols.contains("failed_attempts") ? rs.getInt("failed_attempts") : 0;
                boolean locked = cols.contains("locked") && rs.getBoolean("locked");
                customers.add(new Customer(rs.getInt("id"), rs.getString("full_name"), rs.getString("email"), rs.getString("phone_number"), rs.getString("pin"), role, failedAttempts, locked));
            }
        } catch (SQLException e) {
            System.out.println("Error getting customers: " + e.getMessage());
            e.printStackTrace();
        }
        return customers;
    }

    public boolean updateCustomer(Customer customer) {
        String sql = "UPDATE customers SET full_name = ?, email = ?, phone_number = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customer.getFullName());
            stmt.setString(2, customer.getEmail());
            stmt.setString(3, customer.getPhoneNumber());
            stmt.setInt(4, customer.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error updating customer: " + e.getMessage());
            return false;
        }
    }

    public boolean updatePin(int customerId, String newPin) {
        String sql = "UPDATE customers SET pin = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPin);
            stmt.setInt(2, customerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error updating PIN: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteCustomer(int id) {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting customer: " + e.getMessage());
            return false;
        }
    }

    public boolean validatePin(int customerId, String pin) {
        String sql = "SELECT pin FROM customers WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("pin").equals(pin);
        } catch (SQLException e) {
            System.out.println("Error validating PIN: " + e.getMessage());
        }
        return false;
    }

    public void incrementFailedAttempts(int customerId) {
        String sql = "UPDATE customers SET failed_attempts = failed_attempts + 1 WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error incrementing failed attempts: " + e.getMessage());
        }
    }

    public void resetFailedAttempts(int customerId) {
        String sql = "UPDATE customers SET failed_attempts = 0 WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error resetting failed attempts: " + e.getMessage());
        }
    }

    public void setLockedStatus(int customerId, boolean locked) {
        String sql = "UPDATE customers SET locked = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, locked);
            stmt.setInt(2, customerId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error setting locked status: " + e.getMessage());
        }
    }

    private Customer mapCustomer(ResultSet rs) throws SQLException {
        return new Customer(rs.getInt("id"), rs.getString("full_name"), rs.getString("email"), rs.getString("phone_number"), rs.getString("pin"), rs.getString("role"), rs.getInt("failed_attempts"), rs.getBoolean("locked"));
    }
}
