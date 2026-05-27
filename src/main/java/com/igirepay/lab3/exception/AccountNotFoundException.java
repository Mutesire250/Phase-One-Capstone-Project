package com.igirepay.lab3.exception;

/**
 * Exception thrown when a requested account ID is not found in the database.
 * Demonstrates: Query validation & domain safety.
 */
public class AccountNotFoundException extends Exception {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
