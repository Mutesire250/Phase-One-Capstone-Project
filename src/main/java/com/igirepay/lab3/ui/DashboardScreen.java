package com.igirepay.lab3.ui;

import com.igirepay.Main;
import com.igirepay.lab1.model.Transaction;
import com.igirepay.lab1.model.Account;
import com.igirepay.lab1.model.Customer;
import com.igirepay.lab3.service.AccountService;
import com.igirepay.lab3.service.TransactionService;
import com.igirepay.lab3.exception.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class DashboardScreen {

    private AccountService accountService = new AccountService();
    private TransactionService transactionService = new TransactionService();
    private BorderPane view;
    private Customer currentCustomer;
    private VBox contentArea;

    public DashboardScreen() {
        this.currentCustomer = Session.getCurrentCustomer();
        createView();
    }

    private void createView() {
        view = new BorderPane();

        // Top Bar
        HBox topBar = createTopBar();
        view.setTop(topBar);

        // Side Menu
        VBox sideMenu = createSideMenu();
        sideMenu.getStyleClass().add("side-menu");
        view.setLeft(sideMenu);

        // Content Area
        contentArea = new VBox(20);
        contentArea.setPadding(new Insets(30));
        contentArea.getStyleClass().add("content-area");
        view.setCenter(contentArea);

        // Show welcome message
        showWelcomeScreen();
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(15);
        topBar.setPadding(new Insets(15, 30, 15, 30));
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER_RIGHT);

        Label welcomeLabel = new Label("Welcome, " + currentCustomer.getFullName());
        welcomeLabel.setTextFill(Color.WHITE);
        welcomeLabel.setFont(Font.font("Arial", 14));

        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().add("danger-button");
        logoutBtn.setOnAction(e -> {
            Session.clear();
            Main.showLoginScreen();
        });

        topBar.getChildren().addAll(welcomeLabel, logoutBtn);
        return topBar;
    }

    private VBox createSideMenu() {
        VBox menu = new VBox(10);
        menu.setPadding(new Insets(30, 20, 30, 20));
        menu.getStyleClass().add("side-menu");

        String[] menuItems = {
            "🏠 Dashboard", "💰 My Accounts", "➕ Create Account",
            "💸 Deposit", "🏧 Withdraw", "💸 Send Money", "📊 Transaction History",
            "📁 Export CSV", "👤 My Profile"
        };

        for (String item : menuItems) {
            Button btn = new Button(item);
            btn.getStyleClass().add("menu-button");
            btn.setOnMouseEntered(e -> btn.setOpacity(0.9));
            btn.setOnMouseExited(e -> btn.setOpacity(1.0));

            btn.setOnAction(e -> handleMenuClick(item));
            menu.getChildren().add(btn);
        }

        return menu;
    }

    private void handleMenuClick(String menuItem) {
        switch (menuItem) {
            case "🏠 Dashboard":
                showWelcomeScreen();
                break;
            case "💰 My Accounts":
                showAccounts();
                break;
            case "➕ Create Account":
                showCreateAccount();
                break;
            case "💸 Deposit":
                showDeposit();
                break;
            case "🏧 Withdraw":
                showWithdraw();
                break;
            case "💸 Send Money":
                showSendMoney();
                break;
            case "📊 Transaction History":
                showTransactionHistory();
                break;
            case "📁 Export CSV":
                exportToCSV();
                break;
            case "👤 My Profile":
                showProfile();
                break;
        }
    }

    private void showWelcomeScreen() {
        contentArea.getChildren().clear();

        VBox welcomeBox = new VBox(20);
        welcomeBox.setAlignment(Pos.CENTER);
        welcomeBox.setPadding(new Insets(50));

        Label title = new Label("Welcome to IgirePay");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));

        Label subtitle = new Label("Your secure payment gateway");
        subtitle.setFont(Font.font("Arial", 18));
        subtitle.setTextFill(Color.GRAY);

        // Show account summary
        var accounts = accountService.getCustomerAccounts(currentCustomer.getId());
        Label summaryLabel = new Label("You have " + accounts.size() + " account(s)");
        summaryLabel.setFont(Font.font("Arial", 14));

        welcomeBox.getChildren().addAll(title, subtitle, summaryLabel);
        contentArea.getChildren().add(welcomeBox);
    }

    private void showAccounts() {
        contentArea.getChildren().clear();

        Label title = new Label("My Accounts");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        var accounts = accountService.getCustomerAccounts(currentCustomer.getId());

        if (accounts.isEmpty()) {
            Label noAccounts = new Label("No accounts found. Create an account first!");
            noAccounts.setTextFill(Color.RED);
            contentArea.getChildren().addAll(title, noAccounts);
            return;
        }

        VBox accountsList = new VBox(10);
        for (Account acc : accounts) {
            HBox accountBox = new HBox(20);
            accountBox.setPadding(new Insets(15));
            accountBox.getStyleClass().add("form-card");

            VBox info = new VBox(5);
            Label typeLabel = new Label(acc.getAccountType() + " Account");
            typeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            Label idLabel = new Label("Account ID: " + acc.getId());
            idLabel.setFont(Font.font("Arial", 13));
            idLabel.setStyle("-fx-text-fill: #555;");
            Label balanceLabel = new Label(String.format("Balance: %.2f RWF", acc.getBalance()));
            balanceLabel.setFont(Font.font("Arial", 14));
            info.getChildren().addAll(typeLabel, idLabel, balanceLabel);

            Button selectBtn = new Button("Select");
            selectBtn.getStyleClass().add("success-button");
            selectBtn.setOnAction(e -> {
                Session.setCurrentAccount(acc);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Account selected!");
            });

            accountBox.getChildren().addAll(info, selectBtn);
            HBox.setHgrow(info, Priority.ALWAYS);
            accountsList.getChildren().add(accountBox);
        }

        contentArea.getChildren().addAll(title, accountsList);
    }

    private void showCreateAccount() {
        contentArea.getChildren().clear();

        Label title = new Label("Create New Account");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.getStyleClass().add("card-title");

        VBox form = new VBox(15);
        form.setMaxWidth(400);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-card");

        ComboBox<String> accountType = new ComboBox<>();
        accountType.getItems().addAll("Wallet Account", "Savings Account");
        accountType.setPromptText("Select Account Type");
        accountType.setPrefHeight(36);

        TextField amountField = new TextField();
        amountField.setPromptText("Initial Deposit Amount");
        amountField.getStyleClass().add("input-field");

        Button createBtn = new Button("Create Account");
        createBtn.getStyleClass().add("primary-button");
        createBtn.setPrefWidth(200);

        Label message = new Label();

        createBtn.setOnAction(e -> {
            String type = accountType.getValue();
            double amount;
            try {
                amount = Double.parseDouble(amountField.getText());
            } catch (NumberFormatException ex) {
                message.setText("Invalid amount!");
                return;
            }

            boolean success = false;
            try {
                if ("Wallet Account".equals(type)) {
                    success = accountService.createWalletAccount(currentCustomer.getId(), amount);
                } else if ("Savings Account".equals(type)) {
                    success = accountService.createSavingsAccount(currentCustomer.getId(), amount);
                }
            } catch (AccountNotFoundException | InvalidAmountException ex) {
                message.setStyle("-fx-text-fill: red;");
                message.setText(ex.getMessage());
                return;
            }

            if (success) {
                message.setStyle("-fx-text-fill: green;");
                message.setText("Account created successfully!");
                amountField.clear();
            } else {
                message.setStyle("-fx-text-fill: red;");
                message.setText("Failed to create account!");
            }
        });

        form.getChildren().addAll(accountType, amountField, createBtn, message);
        contentArea.getChildren().addAll(title, form);
    }

    private void showDeposit() {
        contentArea.getChildren().clear();

        if (Session.getCurrentAccount() == null) {
            Label error = new Label("Please select an account first from 'My Accounts'");
            error.setTextFill(Color.RED);
            contentArea.getChildren().add(error);
            return;
        }

        Label title = new Label("Deposit Money");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        VBox form = new VBox(15);
        form.setMaxWidth(400);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-card");

        Label accountInfo = new Label("Account: " + Session.getCurrentAccount().getAccountType());
        accountInfo.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        TextField amountField = new TextField();
        amountField.setPromptText("Amount to Deposit");
        amountField.getStyleClass().add("input-field");

        Button depositBtn = new Button("Deposit");
        depositBtn.getStyleClass().add("success-button");

        Label message = new Label();

        depositBtn.setOnAction(e -> {
            double amount;
            try {
                amount = Double.parseDouble(amountField.getText());
            } catch (NumberFormatException ex) {
                message.setText("Invalid amount!");
                return;
            }

            String refId = transactionService.generateReferenceId();
            try {
                boolean success = transactionService.processDeposit(Session.getCurrentAccount().getId(), amount, refId);
                if (success) {
                    Session.setCurrentAccount(accountService.getAccountById(Session.getCurrentAccount().getId()));
                    message.setStyle("-fx-text-fill: green;");
                    message.setText("Deposit successful! New balance: " + Session.getCurrentAccount().getBalance());
                    amountField.clear();
                } else {
                    message.setStyle("-fx-text-fill: red;");
                    message.setText("Deposit failed!");
                }
            } catch (InvalidAmountException | DuplicateRequestException | AccountNotFoundException ex) {
                message.setStyle("-fx-text-fill: red;");
                message.setText(ex.getMessage());
            }
        });

        form.getChildren().addAll(accountInfo, amountField, depositBtn, message);
        contentArea.getChildren().addAll(title, form);
    }

    private void showWithdraw() {
        contentArea.getChildren().clear();

        if (Session.getCurrentAccount() == null) {
            Label error = new Label("Please select an account first from 'My Accounts'");
            error.setTextFill(Color.RED);
            contentArea.getChildren().add(error);
            return;
        }

        Label title = new Label("Withdraw Money");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        VBox form = new VBox(15);
        form.setMaxWidth(400);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-card");

        Label accountInfo = new Label("Account: " + Session.getCurrentAccount().getAccountType());
        accountInfo.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Label balanceInfo = new Label("Current Balance: " + Session.getCurrentAccount().getBalance() + " RWF");

        TextField amountField = new TextField();
        amountField.setPromptText("Amount to Withdraw");
        amountField.getStyleClass().add("input-field");

        Button withdrawBtn = new Button("Withdraw");
        withdrawBtn.getStyleClass().add("danger-button");

        Label message = new Label();

        withdrawBtn.setOnAction(e -> {
            double amount;
            try {
                amount = Double.parseDouble(amountField.getText());
            } catch (NumberFormatException ex) {
                message.setText("Invalid amount!");
                return;
            }

            String refId = transactionService.generateReferenceId();
            try {
                boolean success = transactionService.processWithdrawal(Session.getCurrentAccount().getId(), amount, refId);
                if (success) {
                    Session.setCurrentAccount(accountService.getAccountById(Session.getCurrentAccount().getId()));
                    message.setStyle("-fx-text-fill: green;");
                    message.setText("Withdrawal successful! New balance: " + Session.getCurrentAccount().getBalance());
                    amountField.clear();
                    balanceInfo.setText("Current Balance: " + Session.getCurrentAccount().getBalance() + " RWF");
                } else {
                    message.setStyle("-fx-text-fill: red;");
                    message.setText("Withdrawal failed!");
                }
            } catch (InvalidAmountException | DuplicateRequestException | AccountNotFoundException | com.igirepay.lab3.exception.InsufficientBalanceException ex) {
                message.setStyle("-fx-text-fill: red;");
                message.setText(ex.getMessage());
            }
        });

        form.getChildren().addAll(accountInfo, balanceInfo, amountField, withdrawBtn, message);
        contentArea.getChildren().addAll(title, form);
    }

    private void showTransactionHistory() {
        contentArea.getChildren().clear();

        if (Session.getCurrentAccount() == null) {
            Label error = new Label("Please select an account first from 'My Accounts'");
            error.setTextFill(Color.RED);
            contentArea.getChildren().add(error);
            return;
        }

        Label title = new Label("Transaction History");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        var transactions = transactionService.getTransactionHistory(Session.getCurrentAccount().getId());

        Label countLabel = new Label("Account ID: " + Session.getCurrentAccount().getId() + "  |  Total transactions: " + transactions.size());
        countLabel.setFont(Font.font("Arial", 13));
        countLabel.setStyle("-fx-text-fill: #555;");

        if (transactions.isEmpty()) {
            Label noTrans = new Label("No transactions found for this account. Make a deposit or transfer first.");
            noTrans.setStyle("-fx-text-fill: gray; -fx-font-size: 13;");
            contentArea.getChildren().addAll(title, countLabel, noTrans);
            return;
        }

        javafx.scene.control.TableView<Transaction> table = new javafx.scene.control.TableView<>();
        table.getStyleClass().add("table-view");

        javafx.scene.control.TableColumn<Transaction, String> refCol = new javafx.scene.control.TableColumn<>("Reference ID");
        refCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getReferenceId()));
        refCol.setPrefWidth(180);

        javafx.scene.control.TableColumn<Transaction, String> typeCol = new javafx.scene.control.TableColumn<>("Type");
        typeCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTransactionType()));
        typeCol.setPrefWidth(130);

        javafx.scene.control.TableColumn<Transaction, String> amountCol = new javafx.scene.control.TableColumn<>("Amount (RWF)");
        amountCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                String.format("%.2f", cellData.getValue().getAmount())));
        amountCol.setPrefWidth(130);

        javafx.scene.control.TableColumn<Transaction, String> dateCol = new javafx.scene.control.TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getCreatedAt() != null
                        ? cellData.getValue().getCreatedAt().toString().replace("T", " ").substring(0, 19)
                        : ""));
        dateCol.setPrefWidth(170);

        table.getColumns().addAll(refCol, typeCol, amountCol, dateCol);
        table.setColumnResizePolicy(javafx.scene.control.TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(400);

        javafx.application.Platform.runLater(() -> {
            table.getItems().addAll(transactions);
            table.refresh();
        });

        contentArea.getChildren().addAll(title, countLabel, table);
    }

    private void showSendMoney() {
        contentArea.getChildren().clear();

        if (Session.getCurrentAccount() == null) {
            Label error = new Label("Please select an account first from 'My Accounts'");
            error.setTextFill(Color.RED);
            contentArea.getChildren().add(error);
            return;
        }

        Label title = new Label("Send Money");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        VBox form = new VBox(15);
        form.setMaxWidth(500);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-card");

        TextField toAccountField = new TextField();
        toAccountField.setPromptText("Enter numbers only, e.g. 15");
        toAccountField.getStyleClass().add("input-field");

        TextField amountField = new TextField();
        amountField.setPromptText("Amount to Send");
        amountField.getStyleClass().add("input-field");

        Button sendBtn = new Button("Send");
        sendBtn.getStyleClass().add("primary-button");

        Label message = new Label();

        sendBtn.setOnAction(e -> {
            int toId;
            double amount;
            try {
                toId = Integer.parseInt(toAccountField.getText());
                amount = Double.parseDouble(amountField.getText());
            } catch (NumberFormatException ex) {
                message.setText("Invalid destination account or amount!");
                return;
            }

            String refId = transactionService.generateReferenceId();
            try {
                boolean success = transactionService.processTransfer(Session.getCurrentAccount().getId(), toId, amount, refId);
                if (success) {
                    // refresh selected account
                    Session.setCurrentAccount(accountService.getAccountById(Session.getCurrentAccount().getId()));
                    message.setStyle("-fx-text-fill: green;");
                    message.setText("Transfer successful! Ref: " + refId + " New balance: " + Session.getCurrentAccount().getBalance());
                    toAccountField.clear();
                    amountField.clear();
                } else {
                    message.setStyle("-fx-text-fill: red;");
                    message.setText("Transfer failed! Check balance and destination account.");
                }
            } catch (InvalidAmountException | DuplicateRequestException | AccountNotFoundException | com.igirepay.lab3.exception.InsufficientBalanceException ex) {
                message.setStyle("-fx-text-fill: red;");
                message.setText(ex.getMessage());
            }
        });

        form.getChildren().addAll(new Label("From: " + Session.getCurrentAccount().getAccountType() + " (ID: " + Session.getCurrentAccount().getId() + ")"),
                toAccountField, amountField, sendBtn, message);

        contentArea.getChildren().addAll(title, form);
    }

    private void exportToCSV() {
        contentArea.getChildren().clear();

        if (Session.getCurrentAccount() == null) {
            Label error = new Label("Please select an account first from 'My Accounts'");
            error.setTextFill(Color.RED);
            contentArea.getChildren().add(error);
            return;
        }

        Label title = new Label("Export Transactions");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        Label info = new Label("Click the button below to choose where to save your Excel/CSV file.");
        info.setFont(Font.font("Arial", 13));

        Button exportBtn = new Button("Export to Excel / CSV");
        exportBtn.getStyleClass().add("primary-button");

        Label message = new Label();

        exportBtn.setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Save Transactions");
            fileChooser.setInitialFileName("transactions_account_" + Session.getCurrentAccount().getId() + ".csv");
            fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Excel / CSV Files", "*.csv"),
                new javafx.stage.FileChooser.ExtensionFilter("All Files", "*.*")
            );

            java.io.File file = fileChooser.showSaveDialog(exportBtn.getScene().getWindow());
            if (file != null) {
                try {
                    transactionService.exportToFile(Session.getCurrentAccount().getId(), file.getAbsolutePath());
                    message.setStyle("-fx-text-fill: green;");
                    message.setText("Exported successfully to: " + file.getAbsolutePath());
                } catch (Exception ex) {
                    message.setStyle("-fx-text-fill: red;");
                    message.setText("Export failed: " + ex.getMessage());
                }
            }
        });

        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.getChildren().addAll(title, info, exportBtn, message);
        contentArea.getChildren().add(box);
    }

    private void showProfile() {
        contentArea.getChildren().clear();

        Label title = new Label("My Profile");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        Label name = new Label("Name: " + currentCustomer.getFullName());
        name.setFont(Font.font("Arial", 14));

        Label email = new Label("Email: " + (currentCustomer.getEmail() == null ? "(not set)" : currentCustomer.getEmail()));
        email.setFont(Font.font("Arial", 14));

        contentArea.getChildren().addAll(title, name, email);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public BorderPane getView() {
        return view;
    }
}