package com.igirepay.lab3.exception;

/**
 * Exception thrown when a transaction amount is invalid (e.g. zero or negative).
 * Demonstrates: Custom Exception Handling & business logic validation.
 */
public class InvalidAmountException extends Exception {
    public InvalidAmountException(String message) {
        super(message);
    }
}
