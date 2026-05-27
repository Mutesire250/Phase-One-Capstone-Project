package com.igirepay.lab2.dao;

import com.igirepay.lab2.db.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;

public class ProcessedRequestDAO {

    // CHECK - Is this reference ID already processed
    public boolean exists(String referenceId) {
        String sql = "SELECT id FROM processed_requests WHERE reference_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, referenceId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.out.println("Error checking processed request: " + e.getMessage());
            return false;
        }
    }

    // CREATE - Mark a reference ID as processed
    public boolean markAsProcessed(String referenceId) {
        String sql = "INSERT INTO processed_requests (reference_id) VALUES (?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, referenceId);
            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Error marking as processed: " + e.getMessage());
            return false;
        }
    }

    // DELETE - Remove processed request (for testing/reset)
    public boolean removeProcessedRequest(String referenceId) {
        String sql = "DELETE FROM processed_requests WHERE reference_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, referenceId);
            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Error removing processed request: " + e.getMessage());
            return false;
        }
    }

    // GET - When was this reference ID processed?
    public LocalDateTime getProcessedTime(String referenceId) {
        String sql = "SELECT processed_at FROM processed_requests WHERE reference_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, referenceId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getTimestamp("processed_at").toLocalDateTime();
            }

        } catch (SQLException e) {
            System.out.println("Error getting processed time: " + e.getMessage());
        }
        return null;
    }
}