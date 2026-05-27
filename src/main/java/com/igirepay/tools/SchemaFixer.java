package com.igirepay.tools;

import com.igirepay.lab2.db.DBConnection;
import java.sql.Connection;
import java.sql.Statement;

public class SchemaFixer {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Attempting to alter schema...");
            
            // Drop unique constraint on transactions(reference_id) if it exists
            try {
                stmt.execute("ALTER TABLE transactions DROP CONSTRAINT IF EXISTS transactions_reference_id_key");
                System.out.println("Successfully dropped transactions_reference_id_key constraint!");
            } catch (Exception e) {
                System.out.println("Could not drop constraint: " + e.getMessage());
            }

            // Also make sure processed_requests has reference_id UNIQUE
            try {
                stmt.execute("ALTER TABLE processed_requests ADD CONSTRAINT processed_requests_ref_unique UNIQUE (reference_id)");
                System.out.println("Successfully ensured processed_requests(reference_id) is UNIQUE!");
            } catch (Exception e) {
                System.out.println("Note on processed_requests constraint: " + e.getMessage());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
