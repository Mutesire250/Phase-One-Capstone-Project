package com.igirepay.lab3.ui;

import com.igirepay.Main;
import com.igirepay.lab3.service.CustomerService;
import com.igirepay.lab1.model.Customer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class LoginScreen {

    private CustomerService customerService = new CustomerService();
    private VBox view;

    public LoginScreen() {
        createView();
    }

    private void createView() {
        view = new VBox(20);
        view.setAlignment(Pos.CENTER);
        view.setPadding(new Insets(40));
        view.getStyleClass().add("login-root");

        // Title
        Label titleLabel = new Label("IGIREPAY");
        titleLabel.getStyleClass().add("login-title");

        Label subtitleLabel = new Label("Secure Payment Gateway");
        subtitleLabel.getStyleClass().add("login-subtitle");

        // Login Card
        VBox card = new VBox(15);
        card.setMaxWidth(400);
        card.setPadding(new Insets(30));
        card.getStyleClass().add("login-card");

        Label loginLabel = new Label("Login to Your Account");
        loginLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");
        emailField.setPrefHeight(40);
        emailField.setStyle("-fx-font-size: 14;");

        PasswordField pinField = new PasswordField();
        pinField.setPromptText("PIN (4 digits)");
        pinField.setPrefHeight(40);
        pinField.setStyle("-fx-font-size: 14;");

        Button loginButton = new Button("LOGIN");
        loginButton.getStyleClass().addAll("primary-button");
        loginButton.setPrefHeight(40);
        loginButton.setStyle(loginButton.getStyle() + " -fx-font-size: 14; -fx-font-weight: bold;");
        loginButton.setOnMouseEntered(e -> loginButton.setOpacity(0.9));
        loginButton.setOnMouseExited(e -> loginButton.setOpacity(1.0));

        Button registerButton = new Button("Create New Account");
        registerButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #1a2980; -fx-font-size: 14;");

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12;");

        // Login action
        loginButton.setOnAction(e -> {
            String email = emailField.getText();
            String pin = pinField.getText();

            if (email.isEmpty() || pin.isEmpty()) {
                messageLabel.setText("Please enter both email and PIN!");
                return;
            }

            try {
                Customer customer = customerService.login(email, pin);
                if (customer != null) {
                    Session.setCurrentCustomer(customer);
                    // Route by role if available
                    if (customer.getRole() != null && customer.getRole().equalsIgnoreCase("admin")) {
                        com.igirepay.Main.showAdminScreen();
                    } else {
                        com.igirepay.Main.showDashboard();
                    }
                } else {
                    messageLabel.setText("Invalid email or PIN!");
                }
            } catch (com.igirepay.lab3.exception.AccountLockedException ex) {
                messageLabel.setText(ex.getMessage());
                showAlert(Alert.AlertType.ERROR, "Account Locked", ex.getMessage());
            }
        });

        registerButton.setOnAction(e -> showRegistrationDialog());

        card.getChildren().addAll(loginLabel, emailField, pinField, loginButton, registerButton, messageLabel);
        view.getChildren().addAll(titleLabel, subtitleLabel, card);
    }

    private void showRegistrationDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Register New Customer");
        dialog.setHeaderText("Create IgirePay Account");

        GridPane grid = new GridPane();
        grid.getStyleClass().add("dialog-grid");
        grid.setPadding(new Insets(20));

        dialog.getDialogPane().getStyleClass().add("dialog-pane");

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");
        PasswordField pinField = new PasswordField();
        pinField.setPromptText("PIN (4 digits)");

        grid.add(new Label("Full Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("PIN:"), 0, 3);
        grid.add(pinField, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Style dialog buttons
        dialog.setOnShown(win -> {
            try {
                Button ok = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
                Button cancel = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
                if (ok != null) ok.getStyleClass().addAll("primary-button", "dialog-button");
                if (cancel != null) cancel.getStyleClass().addAll("danger-button", "dialog-button");
            } catch (Exception ignored) {}
        });

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                String name = nameField.getText();
                String email = emailField.getText();
                String phone = phoneField.getText();
                String pin = pinField.getText();

                if (customerService.registerCustomer(name, email, phone, pin)) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Registration successful! Please login.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Registration failed! Try again.");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public VBox getView() {
        return view;
    }
}