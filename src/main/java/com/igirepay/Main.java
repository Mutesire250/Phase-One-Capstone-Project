package com.igirepay;

import com.igirepay.lab2.db.DBConnection;
import com.igirepay.lab3.ui.LoginScreen;
import com.igirepay.lab3.ui.DashboardScreen;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;
import com.igirepay.lab2.dao.CustomerDAO;
import com.igirepay.lab1.model.Customer;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        // Test database connection
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println(" Database connected successfully!");
            // Ensure an admin user exists (email: admin@igirepay.com, PIN: 0000)
            try {
                // Ensure role column exists and set default 'user'
                try (java.sql.Statement s = conn.createStatement()) {
                    s.execute("ALTER TABLE customers ADD COLUMN IF NOT EXISTS role VARCHAR(20) DEFAULT 'user'");
                } catch (Exception ex) {
                    System.out.println("Could not ensure role column: " + ex.getMessage());
                }

                // Ensure pin column can hold hashed values (bcrypt)
                try (java.sql.Statement s2 = conn.createStatement()) {
                    s2.execute("ALTER TABLE customers ALTER COLUMN pin TYPE VARCHAR(128)");
                } catch (Exception ex) {
                    System.out.println("Could not alter pin column type: " + ex.getMessage());
                }

                // Ensure failed_attempts column exists and set default 0
                try (java.sql.Statement s3 = conn.createStatement()) {
                    s3.execute("ALTER TABLE customers ADD COLUMN IF NOT EXISTS failed_attempts INT DEFAULT 0");
                } catch (Exception ex) {
                    System.out.println("Could not ensure failed_attempts column: " + ex.getMessage());
                }

                // Ensure locked column exists and set default false
                try (java.sql.Statement s4 = conn.createStatement()) {
                    s4.execute("ALTER TABLE customers ADD COLUMN IF NOT EXISTS locked BOOLEAN DEFAULT FALSE");
                } catch (Exception ex) {
                    System.out.println("Could not ensure locked column: " + ex.getMessage());
                }

                CustomerDAO cdao = new CustomerDAO();
                if (cdao.getCustomerByEmail("admin@igirepay.com") == null) {
                    com.igirepay.lab1.model.Customer admin = new com.igirepay.lab1.model.Customer(0, "Admin User", "admin@igirepay.com", "0000000000", "0000", "admin");
                    cdao.addCustomer(admin);
                    System.out.println(" Admin user created: admin@igirepay.com (PIN: 0000)");
                } else {
                    // make sure existing admin has role set
                    try (java.sql.PreparedStatement ps = conn.prepareStatement("UPDATE customers SET role = 'admin' WHERE email = ?")) {
                        ps.setString(1, "admin@igirepay.com");
                        ps.executeUpdate();
                    } catch (Exception ex) {
                        // ignore
                    }
                }
                // Migrate any plaintext PINs to hashed values
                try {
                    com.igirepay.lab3.service.CustomerService cs = new com.igirepay.lab3.service.CustomerService();
                    cs.migratePlainPins();
                } catch (Exception ex) {
                    System.out.println("PIN migration failed: " + ex.getMessage());
                }
            } catch (Exception e) {
                System.out.println("Could not ensure admin user: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println(" Database connection failed: " + e.getMessage());
            showErrorAndExit("Database connection failed!\nPlease check PostgreSQL is running.");
            return;
        }

        // Show login screen
        showLoginScreen();

        stage.setTitle("IgirePay Payment Gateway");
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void showLoginScreen() {
        LoginScreen loginScreen = new LoginScreen();
        Scene scene = new Scene(loginScreen.getView(), 800, 600);

        // Try to load CSS, but don't crash if not found
        try {
            scene.getStylesheets().add(Main.class.getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("CSS file not found, using default styles");
        }

        primaryStage.setScene(scene);
    }

    public static void showDashboard() {
        DashboardScreen dashboard = new DashboardScreen();
        Scene scene = new Scene(dashboard.getView(), 800, 600);

        // Try to load CSS, but don't crash if not found
        try {
            scene.getStylesheets().add(Main.class.getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("CSS file not found, using default styles");
        }

        primaryStage.setScene(scene);
    }

    public static void showAdminScreen() {
        com.igirepay.lab3.ui.AdminScreen admin = new com.igirepay.lab3.ui.AdminScreen();
        Scene scene = new Scene(admin.getView(), 900, 600);

        try {
            scene.getStylesheets().add(Main.class.getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("CSS file not found, using default styles");
        }

        primaryStage.setScene(scene);
    }

    private void showErrorAndExit(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR
        );
        alert.setTitle("Fatal Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        System.exit(1);
    }

    public static void main(String[] args) {
        launch(args);
    }
}