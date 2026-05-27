package com.igirepay.lab3.ui;

import com.igirepay.lab1.model.Customer;
import com.igirepay.lab1.model.Account;

public class Session {
    private static Customer currentCustomer;
    private static Account currentAccount;

    public static Customer getCurrentCustomer() {
        return currentCustomer;
    }

    public static void setCurrentCustomer(Customer customer) {
        currentCustomer = customer;
    }

    public static Account getCurrentAccount() {
        return currentAccount;
    }

    public static void setCurrentAccount(Account account) {
        currentAccount = account;
    }

    public static void clear() {
        currentCustomer = null;
        currentAccount = null;
    }
}