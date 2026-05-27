package com.igirepay.tools;

import com.igirepay.lab2.db.DBConnection;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

public class SchemaPrinter {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String[] types = {"TABLE"};
            try (ResultSet tables = metaData.getTables(null, null, "%", types)) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    System.out.println("Table: " + tableName);
                    try (ResultSet columns = metaData.getColumns(null, null, tableName, "%")) {
                        while (columns.next()) {
                            String colName = columns.getString("COLUMN_NAME");
                            String colType = columns.getString("TYPE_NAME");
                            int colSize = columns.getInt("COLUMN_SIZE");
                            System.out.println("  - " + colName + " (" + colType + ", size=" + colSize + ")");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
