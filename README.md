# IgirePay Payment Gateway

A secure desktop-based digital wallet system built with JavaFX, JDBC, and PostgreSQL — inspired by MTN Mobile Money.

---

## Project Overview

IgirePay allows users to register, log in with a PIN, manage wallet and savings accounts, send money, view transaction history, and export reports. An admin role can manage all customers from a dedicated screen.

---

## Technologies Used

- Java 17
- JavaFX 17 — Desktop UI
- JDBC — Database connectivity
- PostgreSQL — Persistent storage
- BCrypt — Secure PIN hashing
- Maven — Build tool

---

## Project Structure

```
src/main/java/com/igirepay/
│
├── lab1/model/               # Lab 1 - OOP Models
│   ├── Account.java          # Abstract base class
│   ├── WalletAccount.java    # Instant transfers, no fees
│   ├── SavingsAccount.java   # 2% withdrawal fee, 500 RWF minimum balance
│   ├── Customer.java         # User model with role and lock status
│   └── Transaction.java      # Transaction record with reference ID
│
├── lab2/dao/                 # Lab 2 - JDBC Database Layer
│   ├── AccountDAO.java       # CRUD for accounts
│   ├── CustomerDAO.java      # CRUD for customers
│   ├── TransactionDAO.java   # Transactions + atomic transfers
│   └── ProcessedRequestDAO.java  # Idempotency tracking
│
├── lab2/db/
│   └── DBConnection.java     # PostgreSQL connection
│
├── lab3/service/             # Lab 3 - Business Logic
│   ├── AccountService.java   # Account creation, deposit, withdraw
│   ├── CustomerService.java  # Registration, login, PIN hashing, caching
│   └── TransactionService.java  # Transfer, duplicate detection, CSV export
│
├── lab3/exception/           # Custom Exceptions
│   ├── AccountLockedException.java
│   ├── AccountNotFoundException.java
│   ├── DuplicateRequestException.java
│   ├── InsufficientBalanceException.java
│   └── InvalidAmountException.java
│
├── lab3/ui/                  # JavaFX Screens
│   ├── LoginScreen.java      # Login and registration
│   ├── DashboardScreen.java  # User dashboard with all features
│   ├── AdminScreen.java      # Admin customer management
│   └── Session.java          # Holds current logged-in user
│
└── Main.java                 # Application entry point
```

---

## Database Schema

```sql
CREATE TABLE customers (
    id SERIAL PRIMARY KEY,
    full_name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    phone_number VARCHAR(20),
    pin VARCHAR(128),
    role VARCHAR(20) DEFAULT 'user',
    failed_attempts INT DEFAULT 0,
    locked BOOLEAN DEFAULT FALSE
);

CREATE TABLE accounts (
    id SERIAL PRIMARY KEY,
    customer_id INT REFERENCES customers(id),
    account_type VARCHAR(20),
    balance DOUBLE PRECISION,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE transactions (
    id SERIAL PRIMARY KEY,
    account_id INT REFERENCES accounts(id),
    reference_id VARCHAR(50) UNIQUE,
    transaction_type VARCHAR(20),
    amount DOUBLE PRECISION,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE processed_requests (
    id SERIAL PRIMARY KEY,
    reference_id VARCHAR(50) UNIQUE,
    processed_at TIMESTAMP DEFAULT NOW()
);
```

---

## How to Run

**Prerequisites:**
- Java 17+
- PostgreSQL running locally
- Maven installed

**1. Create the database**
```sql
CREATE DATABASE igirepay_db;
```

**2. Update DB credentials** in `DBConnection.java`:
```java
private static final String URL = "jdbc:postgresql://localhost:5432/igirepay_db";
private static final String USER = "postgres";
private static final String PASSWORD = "your_password";
```

**3. Run the app**
```bash
mvn javafx:run
```

**4. Login as Admin**
- Email: `admin@igirepay.com`
- PIN: `0000`

---

## Features

### User
- Register and login with hashed PIN
- Create Wallet or Savings account
- Deposit and withdraw money
- Send money to another account by Account ID
- View transaction history
- Export transactions to CSV

### Admin
- View all customers
- Delete customers
- Reset customer PIN
- Unlock locked accounts

---

## Key Concepts Demonstrated

| Concept | Where |
|---|---|
| Inheritance & Polymorphism | `WalletAccount`, `SavingsAccount` extend `Account` |
| Encapsulation | All model fields private with getters/setters |
| Custom Exceptions | `lab3/exception/` package |
| JDBC + PreparedStatements | All DAO classes |
| Atomic Transactions + Rollback | `TransactionDAO.addTransfer()` |
| Idempotency | `processed_requests` table + in-memory Set |
| BCrypt PIN Hashing | `CustomerService.login()` |
| Account Locking | 3 failed PIN attempts locks the account |
| Role-Based Access | Admin vs User routing after login |
| Collections (Map, Set, List) | Customer cache, reference ID set, failed logs |
| CSV Export | `TransactionService.exportToCSV()` |

---

## Git Branches

| Branch | Description |
|---|---|
| `main` | Full project |
| `lab1/oop-models-collections` | OOP models and collections |
| `lab2/jdbc-dao-database` | JDBC integration and DAOs |
| `lab3/service-ui-exception-handling` | Services, UI, and exception handling |
| `feature/ui-fixes-admin-table-transaction-history` | UI bug fixes for admin table and transaction history |

---

## Author

Mutesire — IgirePay Technologies Ltd. Capstone Project
