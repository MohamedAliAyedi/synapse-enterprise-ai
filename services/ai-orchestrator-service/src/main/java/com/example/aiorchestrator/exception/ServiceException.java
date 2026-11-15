package com.example.aiorchestrator.exception;

/**
 * Base exception for service‑level errors. Carries an HTTP status code so the
 * {@link GlobalExceptionHandler} can translate it into an appropriate response.
 */
public class ServiceException extends RuntimeException {
    private final int statusCode;

    public ServiceException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
