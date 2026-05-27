package com.igirepay.lab3.exception;

/**
 * Exception thrown when a user attempts to log in to an account that is locked due to too many failed PIN attempts.
 * Demonstrates: Basic Authentication security & brute force protection.
 */
public class AccountLockedException extends Exception {
    public AccountLockedException(String message) {
        super(message);
    }
}
