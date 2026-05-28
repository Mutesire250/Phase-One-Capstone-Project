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
    private TableView<String[]> table;
    private List<Customer> currentCustomers = new java.util.ArrayList<>();

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

        table = new TableView<>();

        TableColumn<String[], String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[0]));
        idCol.setPrefWidth(55);

        TableColumn<String[], String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[1]));
        nameCol.setPrefWidth(190);

        TableColumn<String[], String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[2]));
        emailCol.setPrefWidth(200);

        TableColumn<String[], String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[3]));
        phoneCol.setPrefWidth(130);

        TableColumn<String[], String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[4]));
        roleCol.setPrefWidth(75);

        TableColumn<String[], String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[5]));
        statusCol.setPrefWidth(75);

        table.getColumns().addAll(idCol, nameCol, emailCol, phoneCol, roleCol, statusCol);
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(420);

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
        view.setCenter(table);
    }

    private void loadCustomers() {
        currentCustomers = customerService.getAllCustomers();
        ObservableList<String[]> rows = FXCollections.observableArrayList();
        for (Customer c : currentCustomers) {
            rows.add(new String[]{
                String.valueOf(c.getId()),
                c.getFullName(),
                c.getEmail(),
                c.getPhoneNumber(),
                c.getRole() == null ? "user" : c.getRole(),
                c.isLocked() ? "LOCKED" : "ACTIVE"
            });
        }
        table.setItems(rows);
    }

    private Customer getSelectedCustomer() {
        int index = table.getSelectionModel().getSelectedIndex();
        if (index < 0 || index >= currentCustomers.size()) return null;
        return currentCustomers.get(index);
    }

    private void deleteSelected() {
        Customer sel = getSelectedCustomer();
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
        Customer sel = getSelectedCustomer();
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
        Customer sel = getSelectedCustomer();
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
