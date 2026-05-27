package com.igirepay.lab2.dao;

import com.igirepay.lab2.db.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;

public class ProcessedRequestDAO {

    public boolean exists(String referenceId) {
        String sql = "SELECT id FROM processed_requests WHERE reference_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, referenceId);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            System.out.println("Error checking processed request: " + e.getMessage());
            return false;
        }
    }

    public boolean markAsProcessed(String referenceId) {
        String sql = "INSERT INTO processed_requests (reference_id) VALUES (?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, referenceId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error marking as processed: " + e.getMessage());
            return false;
        }
    }

    public boolean removeProcessedRequest(String referenceId) {
        String sql = "DELETE FROM processed_requests WHERE reference_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, referenceId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error removing processed request: " + e.getMessage());
            return false;
        }
    }

    public LocalDateTime getProcessedTime(String referenceId) {
        String sql = "SELECT processed_at FROM processed_requests WHERE reference_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, referenceId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getTimestamp("processed_at").toLocalDateTime();
        } catch (SQLException e) {
            System.out.println("Error getting processed time: " + e.getMessage());
        }
        return null;
    }
}
