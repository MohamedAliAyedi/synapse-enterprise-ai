package com.example.aiorchestrator.controller;

import com.example.aiorchestrator.service.AiOrchestratorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Flux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link StreamController} verifying the SSE stream behaviour.
 * <p>
 * The tests cover:
 * <ul>
 *   <li>Successful streaming when a valid {@code prompt} query parameter is supplied.</li>
 *   <li>Proper handling of missing {@code prompt} resulting in a {@code 400 Bad Request}.</li>
 *   <li>Ensuring the {@code SseEmitter} is completed after the Flux finishes.</li>
 * </ul>
 */
@WebMvcTest(StreamController.class)
class StreamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AiOrchestratorService aiOrchestratorService;

    @Test
    @DisplayName("GET /stream with valid prompt returns SSE events and completes")
    void whenPromptProvided_thenStreamEventsReturned() throws Exception {
        // Arrange: mock the service to emit two chunks and then complete
        Flux<String> mockFlux = Flux.just("chunk-1", "chunk-2");
        Mockito.when(aiOrchestratorService.streamResponse("hello world"))
                .thenReturn(mockFlux);

        // Act & Assert
        var result = mockMvc.perform(get("/stream")
                        .param("prompt", "hello world"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        // SSE format: "data:<payload>\n\n"
        assertThat(responseBody).contains("data:chunk-1");
        assertThat(responseBody).contains("data:chunk-2");
        // The emitter should have been closed – Spring adds a final "event:close" line when completed.
        assertThat(responseBody).contains("event:close");
    }

    @Test
    @DisplayName("GET /stream without prompt returns 400 Bad Request")
    void whenPromptMissing_thenBadRequest() throws Exception {
        mockMvc.perform(get("/stream"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Missing required parameter 'prompt'")));
    }
}
