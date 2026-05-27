package com.igirepay.lab3.exception;

/**
 * Exception thrown when a duplicate transaction request is detected based on reference ID.
 * Demonstrates: Idempotency protection & Custom Exception Handling.
 */
public class DuplicateRequestException extends Exception {
    public DuplicateRequestException(String message) {
        super(message);
    }
}
