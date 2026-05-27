package com.igirepay.tools;

import com.igirepay.lab2.db.DBConnection;

import java.sql.Connection;

public class DBTestRunner {
    public static void main(String[] args) {
        try (Connection c = DBConnection.getConnection()) {
            if (c != null && !c.isClosed()) {
                System.out.println("DB connected: " + c.getMetaData().getURL());
            } else {
                System.out.println("DB connection returned null or closed");
            }
        } catch (Exception e) {
            System.out.println("DB connection failed: " + e.getMessage());
            e.printStackTrace(System.out);
        }
    }
}
