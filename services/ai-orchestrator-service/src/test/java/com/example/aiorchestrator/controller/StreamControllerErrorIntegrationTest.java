package com.example.aiorchestrator.controller;

import com.example.aiorchestrator.exception.StreamingException;
import com.example.aiorchestrator.service.AiOrchestratorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test that verifies the {@link StreamController} returns a 502 Bad Gateway
 * when a {@link StreamingException} is thrown by the service layer.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class StreamControllerErrorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AiOrchestratorService aiOrchestratorService;

    @Test
    @DisplayName("GET /api/stream returns 502 when StreamingException occurs")
    void whenStreamingException_thenReturnsBadGateway() throws Exception {
        // Simulate a streaming failure in the service layer
        doThrow(new StreamingException("Simulated streaming failure"))
                .when(aiOrchestratorService).streamResponse(anyString());

        // Perform the request – the controller should delegate to the service and the
        // GlobalExceptionHandler (via StreamingExceptionHandler) must translate it to 502.
        mockMvc.perform(get("/api/stream")
                        .param("prompt", "test prompt"))
                .andExpect(status().isBadGateway());
    }
}
