package com.igirepay.lab3.exception;

/**
 * Exception thrown when an account has insufficient funds to process a withdrawal or transfer.
 * Demonstrates: Transaction safety & custom error messaging.
 */
public class InsufficientBalanceException extends Exception {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
