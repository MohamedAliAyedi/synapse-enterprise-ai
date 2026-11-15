package com.example.aiorchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main entry point for the AI Orchestrator Service.
 *
 * <p>This class now includes {@code @EnableDiscoveryClient} to ensure the service
 * registers itself with the Eureka discovery server. The lack of this annotation
 * caused the service to start without being discoverable, leading to runtime
 * errors when other microservices attempted to call it via Feign or the API
 * Gateway.</p>
 */
@SpringBootApplication
@EnableDiscoveryClient // <-- Fixed: service now registers with Eureka
public class AiOrchestratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiOrchestratorApplication.class, args);
    }
}
