package com.igirepay.lab3.ui;

import com.igirepay.lab1.model.Customer;
import com.igirepay.lab3.service.CustomerService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class AdminScreen {

    private BorderPane view;
    private CustomerService customerService = new CustomerService();
    private TableView<Customer> table;

    public AdminScreen() {
        createView();
        loadCustomers();
    }

    private void createView() {
        view = new BorderPane();
        view.setPadding(new Insets(20));
        view.getStyleClass().add("content-area");

        Label title = new Label("IgirePay Admin - Customers");
        title.getStyleClass().add("admin-title");

        // Table
        table = new TableView<>();

        // Use String for all columns to avoid JavaFX property binding issues
        TableColumn<Customer, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getId())));
        idCol.setPrefWidth(60);

        TableColumn<Customer, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getFullName()));
        nameCol.setPrefWidth(200);

        TableColumn<Customer, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEmail()));
        emailCol.setPrefWidth(200);

        TableColumn<Customer, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPhoneNumber()));
        phoneCol.setPrefWidth(130);

        TableColumn<Customer, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getRole() == null ? "user" : c.getValue().getRole()));
        roleCol.setPrefWidth(80);

        TableColumn<Customer, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().isLocked() ? "LOCKED" : "ACTIVE"));
        statusCol.setPrefWidth(80);

        // Do NOT display PINs in admin UI for security
        table.getColumns().addAll(idCol, nameCol, emailCol, phoneCol, roleCol, statusCol);

        // Buttons
        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("primary-button");
        refreshBtn.setOnAction(e -> loadCustomers());

        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> deleteSelected());

        Button resetPinBtn = new Button("Reset PIN");
        resetPinBtn.getStyleClass().add("success-button");
        resetPinBtn.setOnAction(e -> resetSelectedPin());

        Button unlockBtn = new Button("Unlock Account");
        unlockBtn.getStyleClass().add("success-button");
        unlockBtn.setOnAction(e -> unlockSelectedCustomer());

        Button backBtn = new Button("Logout");
        backBtn.getStyleClass().add("danger-button");
        backBtn.setOnAction(e -> {
            Session.clear();
            com.igirepay.Main.showLoginScreen();
        });

        HBox buttons = new HBox(10, refreshBtn, deleteBtn, resetPinBtn, unlockBtn, backBtn);
        buttons.setAlignment(Pos.CENTER_LEFT);
        buttons.setPadding(new Insets(10, 0, 10, 0));

        VBox top = new VBox(10, title, buttons);
        top.setPadding(new Insets(0, 0, 10, 0));

        view.setTop(top);
        table.getStyleClass().add("table-view");
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(420);
        view.setCenter(table);
    }

    private void loadCustomers() {
        List<Customer> customers = customerService.getAllCustomers();
        System.out.println("DEBUG: Loaded " + customers.size() + " customers from DB");
        for (Customer c : customers) {
            System.out.println("  -> " + c.getId() + " | " + c.getFullName() + " | " + c.getEmail());
        }
        ObservableList<Customer> data = FXCollections.observableArrayList(customers);
        table.setItems(data);
        table.refresh();
    }

    private void deleteSelected() {
        Customer sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(Alert.AlertType.WARNING, "No selection", "Please select a customer to delete.");
            return;
        }

        boolean ok = customerService.deleteCustomer(sel.getId());
        if (ok) {
            showAlert(Alert.AlertType.INFORMATION, "Deleted", "Customer deleted successfully.");
            loadCustomers();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete customer.");
        }
    }

    private void resetSelectedPin() {
        Customer sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(Alert.AlertType.WARNING, "No selection", "Please select a customer to reset PIN.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reset PIN");
        dialog.setHeaderText("Reset PIN for " + sel.getFullName());
        dialog.setContentText("Enter new 4-digit PIN:");

        dialog.showAndWait().ifPresent(newPin -> {
            if (newPin.length() != 4) {
                showAlert(Alert.AlertType.ERROR, "Invalid PIN", "PIN must be exactly 4 digits.");
                return;
            }

            boolean ok = customerService.changePin(sel.getId(), newPin);
            if (ok) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "PIN updated successfully.");
                loadCustomers();
            } else {
                showAlert(Alert.AlertType.ERROR, "Failed", "Failed to update PIN.");
            }
        });
    }

    private void unlockSelectedCustomer() {
        Customer sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(Alert.AlertType.WARNING, "No selection", "Please select a customer to unlock.");
            return;
        }

        boolean ok = customerService.unlockCustomer(sel.getId());
        if (ok) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Customer account unlocked successfully.");
            loadCustomers();
        } else {
            showAlert(Alert.AlertType.ERROR, "Failed", "Failed to unlock customer account.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }

    public BorderPane getView() {
        return view;
    }
}
