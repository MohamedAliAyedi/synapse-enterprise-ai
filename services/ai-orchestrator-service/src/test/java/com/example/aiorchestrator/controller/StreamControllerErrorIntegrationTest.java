package com.example.aiorchestrator.controller;

import com.example.aiorchestrator.service.AiOrchestratorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link StreamController} focusing on error handling scenarios.
 * <p>
 * These tests verify that the controller correctly returns HTTP error codes when
 * the request is malformed or when the underlying service fails.
 */
@SpringBootTest
@AutoConfigureMockMvc
class StreamControllerErrorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AiOrchestratorService aiOrchestratorService;

    @Nested
    @DisplayName("Prompt validation errors")
    class PromptValidation {
        @Test
        @DisplayName("GET /stream without prompt returns 400 Bad Request")
        void whenPromptMissing_thenBadRequest() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/stream"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Missing required parameter: prompt"));
        }
    }

    @Nested
    @DisplayName("Service layer failures")
    class ServiceFailures {
        @Test
        @DisplayName("GET /stream with valid prompt but service throws exception returns 500")
        void whenServiceThrowsException_thenInternalServerError() throws Exception {
            // Simulate a runtime exception from the service layer
            Mockito.when(aiOrchestratorService.streamResponse(anyString()))
                    .thenThrow(new RuntimeException("Service error"));

            mockMvc.perform(MockMvcRequestBuilders.get("/stream")
                            .param("prompt", "test prompt"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string("Service error"));
        }
    }
}
