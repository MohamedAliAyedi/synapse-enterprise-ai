package com.example.aiorchestrator.dto;

import java.time.Instant;

/**
 * DTO representing health status of the service.
 */
public record HealthStatus(String status, Instant timestamp) {

    public HealthStatus(String status) {
        this(status, Instant.now());
    }
}
