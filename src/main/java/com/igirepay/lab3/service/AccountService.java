package com.igirepay.lab3.service;

import com.igirepay.lab2.dao.AccountDAO;
import com.igirepay.lab2.dao.CustomerDAO;
import com.igirepay.lab1.model.Account;
import com.igirepay.lab1.model.WalletAccount;
import com.igirepay.lab1.model.SavingsAccount;
import com.igirepay.lab3.exception.*;

import java.util.List;

public class AccountService {

    private AccountDAO accountDAO;
    private CustomerDAO customerDAO;

    public AccountService() {
        this.accountDAO = new AccountDAO();
        this.customerDAO = new CustomerDAO();
    }

    // Create a wallet account for a customer
    // Demonstrates: Custom Exceptions & validation.
    public boolean createWalletAccount(int customerId, double initialDeposit) 
            throws AccountNotFoundException, InvalidAmountException {
        // Check if customer exists
        if (customerDAO.getCustomerById(customerId) == null) {
            throw new AccountNotFoundException("Customer with ID " + customerId + " not found!");
        }

        if (initialDeposit < 0) {
            throw new InvalidAmountException("Initial deposit cannot be negative!");
        }

        WalletAccount account = new WalletAccount(0, customerId, initialDeposit);
        return accountDAO.addAccount(account);
    }

    // Create a savings account for a customer
    // Demonstrates: Custom Exceptions & validation.
    public boolean createSavingsAccount(int customerId, double initialDeposit) 
            throws AccountNotFoundException, InvalidAmountException {
        // Check if customer exists
        if (customerDAO.getCustomerById(customerId) == null) {
            throw new AccountNotFoundException("Customer with ID " + customerId + " not found!");
        }

        if (initialDeposit < 500) {
            throw new InvalidAmountException("Savings account requires a minimum initial deposit of 500 RWF!");
        }

        SavingsAccount account = new SavingsAccount(0, customerId, initialDeposit);
        return accountDAO.addAccount(account);
    }

    // Get account by ID
    public Account getAccountById(int id) {
        return accountDAO.getAccountById(id);
    }

    // Get all accounts for a customer
    public List<Account> getCustomerAccounts(int customerId) {
        return accountDAO.getAccountsByCustomerId(customerId);
    }

    // Deposit money into an account
    // Demonstrates: Exception propagation.
    public boolean deposit(int accountId, double amount, String referenceId) 
            throws InvalidAmountException, AccountNotFoundException {
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be greater than zero!");
        }

        Account account = accountDAO.getAccountById(accountId);
        if (account == null) {
            throw new AccountNotFoundException("Account with ID " + accountId + " not found!");
        }

        boolean success = account.deposit(amount);
        if (success) {
            accountDAO.updateBalance(accountId, account.getBalance());
            System.out.println(" Deposit of " + amount + " RWF successful!");
        }
        return success;
    }

    // Withdraw money from an account
    // Demonstrates: InsufficientBalanceException checking.
    public boolean withdraw(int accountId, double amount, String referenceId) 
            throws InvalidAmountException, AccountNotFoundException, InsufficientBalanceException {
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be greater than zero!");
        }

        Account account = accountDAO.getAccountById(accountId);
        if (account == null) {
            throw new AccountNotFoundException("Account with ID " + accountId + " not found!");
        }

        // Validate balance before proceeding
        double minRequired = account.getAccountType().equalsIgnoreCase("savings") ? 500.0 : 0.0;
        double charge = account.getAccountType().equalsIgnoreCase("savings") ? amount * 0.02 : 0.0;
        if (account.getBalance() < (amount + charge + minRequired)) {
            throw new InsufficientBalanceException(String.format(
                    "Insufficient balance! Required: %.2f RWF (including fee/minimum), Available: %.2f RWF",
                    (amount + charge + minRequired), account.getBalance()));
        }

        boolean success = account.withdraw(amount);
        if (success) {
            accountDAO.updateBalance(accountId, account.getBalance());
            System.out.println(" Withdrawal of " + amount + " RWF successful!");
        }
        return success;
    }

    // Delete account
    public boolean deleteAccount(int id) {
        return accountDAO.deleteAccount(id);
    }

    // Check account balance
    public double getBalance(int accountId) {
        Account account = accountDAO.getAccountById(accountId);
        if (account == null) {
            System.out.println(" Account not found!");
            return -1;
        }
        return account.getBalance();
    }
}