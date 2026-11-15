package com.example.aiorchestrator.config;

import org.springframework.boot.actuate.endpoint.web.EndpointRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures Spring Boot Actuator endpoints for the AI Orchestrator service.
 * Exposes JVM, HTTP, and health endpoints to support observability and e2e UI health checks.
 */
@Configuration
public class ActuatorConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/actuator/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
