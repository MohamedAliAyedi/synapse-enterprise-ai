package com.example.aiorchestrator.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.aiorchestrator.dto.HealthStatus;

/**
 * Simple health check endpoint.
 * Returns service status and current timestamp.
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<HealthStatus> health() {
        HealthStatus status = new HealthStatus("UP");
        return ResponseEntity.ok(status);
    }
}
