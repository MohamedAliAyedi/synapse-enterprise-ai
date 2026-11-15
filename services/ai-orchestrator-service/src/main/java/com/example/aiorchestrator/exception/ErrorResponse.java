package com.example.aiorchestrator.exception;

import java.time.LocalDateTime;

/**
 * DTO representing a standardized error response sent to clients.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path) {}
