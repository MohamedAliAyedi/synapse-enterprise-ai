package com.example.aiorchestrator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.model.Model;
import org.springframework.ai.groq.GroqChatModel;
import org.springframework.ai.groq.api.GroqApi;

/**
 * Configuration class for the AI Orchestrator service.
 * It registers the Eureka discovery client and the Groq AI model beans.
 */
@Configuration
@EnableDiscoveryClient
public class AiOrchestratorConfig {

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Bean
    public GroqApi groqApi() {
        return new GroqApi(groqApiKey);
    }

    @Bean
    public Model groqChatModel(GroqApi groqApi) {
        return new GroqChatModel(groqApi);
    }
}
