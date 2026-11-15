package com.example.aiorchestrator.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Spring Actuator health indicators.
 *
 * <p>This class registers a custom {@link HealthIndicator} that contributes
 * additional details to the standard {@code /actuator/health} endpoint.
 * In a real‑world scenario you would enrich the indicator with checks such as
 * database connectivity, external API reachability, or cache health.
 */
@Configuration
public class ActuatorConfig {

    /**
     * Custom health indicator that reports the status of the AI Orchestrator service.
     *
     * @return a {@link HealthIndicator} bean exposing service name and version.
     */
    @Bean
    public HealthIndicator aiOrchestratorHealthIndicator() {
        return () -> Health.up()
                .withDetail("service", "ai-orchestrator")
                .withDetail("version", "1.0.0")
                .build();
    }
}
