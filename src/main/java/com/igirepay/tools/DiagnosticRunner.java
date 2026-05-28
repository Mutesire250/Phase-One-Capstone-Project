package com.igirepay.tools;

import com.igirepay.lab2.db.DBConnection;
import java.sql.*;

public class DiagnosticRunner {

    public static void main(String[] args) throws Exception {
        Connection conn = DBConnection.getConnection();
        System.out.println("=== CUSTOMERS ===");
        ResultSet rs1 = conn.createStatement().executeQuery("SELECT id, full_name, email, role FROM customers");
        while (rs1.next()) {
            System.out.println(rs1.getInt("id") + " | " + rs1.getString("full_name") + " | " + rs1.getString("email") + " | " + rs1.getString("role"));
        }

        System.out.println("\n=== ACCOUNTS ===");
        ResultSet rs2 = conn.createStatement().executeQuery("SELECT id, customer_id, account_type, balance FROM accounts");
        while (rs2.next()) {
            System.out.println(rs2.getInt("id") + " | customer:" + rs2.getInt("customer_id") + " | " + rs2.getString("account_type") + " | " + rs2.getDouble("balance"));
        }

        System.out.println("\n=== TRANSACTIONS ===");
        ResultSet rs3 = conn.createStatement().executeQuery("SELECT id, account_id, reference_id, transaction_type, amount FROM transactions ORDER BY id DESC LIMIT 20");
        while (rs3.next()) {
            System.out.println(rs3.getInt("id") + " | acc:" + rs3.getInt("account_id") + " | " + rs3.getString("reference_id") + " | " + rs3.getString("transaction_type") + " | " + rs3.getDouble("amount"));
        }

        System.out.println("\n=== COLUMNS IN CUSTOMERS TABLE ===");
        ResultSet rs4 = conn.createStatement().executeQuery("SELECT column_name FROM information_schema.columns WHERE table_name='customers'");
        while (rs4.next()) {
            System.out.println("  col: " + rs4.getString("column_name"));
        }

        conn.close();
    }
}
