package com.example.aiorchestrator.controller;

import com.example.aiorchestrator.service.AiOrchestratorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import static org.mockito.BDDMockito.given;

/**
 * Integration tests for {@link StreamController} focusing on Server‑Sent Events (SSE) behaviour.
 *
 * <p>These tests verify that:
 * <ul>
 *   <li>The endpoint returns a response with the correct {@code text/event-stream} media type.</li>
 *   <li>Successful streaming yields a sequence of SSE "data" events.</li>
 *   <li>When the underlying service throws an exception, the global {@link StreamingExceptionHandler}
 *       converts it into a proper error payload and the client receives a 500 status.</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class StreamControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AiOrchestratorService aiOrchestratorService;

    @Test
    @DisplayName("GET /api/v1/stream returns SSE data events in order")
    void shouldStreamDataEvents() {
        // Arrange: mock the service to emit two chunks of text.
        given(aiOrchestratorService.streamChat(ArgumentMatchers.anyString()))
                .willReturn(Flux.just("Hello", "World"));

        // Act & Assert: request the streaming endpoint and verify SSE format.
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/stream")
                        .queryParam("prompt", "test prompt")
                        .build())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .returnResult(String.class)
                .getResponseBody()
                .collectList()
                .as(listMono -> {
                    return listMono.doOnNext(events -> {
                        // Each event is prefixed with "data:" according to SSE spec.
                        // The WebTestClient strips the "data:" prefix, so we compare raw payloads.
                        // The order must match the mocked Flux.
                        assert events.size() == 2 : "Expected 2 SSE events";
                        assert "Hello".equals(events.get(0)) : "First event payload mismatch";
                        assert "World".equals(events.get(1)) : "Second event payload mismatch";
                    });
                })
                .block();
    }

    @Test
    @DisplayName("GET /api/v1/stream propagates service errors as JSON error response")
    void shouldHandleStreamingErrorGracefully() {
        // Arrange: mock the service to emit an error.
        given(aiOrchestratorService.streamChat(ArgumentMatchers.anyString()))
                .willReturn(Flux.error(new RuntimeException("simulated failure")));

        // Act & Assert: the global StreamingExceptionHandler should translate the error.
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/stream")
                        .queryParam("prompt", "error case")
                        .build())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.timestamp").exists()
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.error").isEqualTo("Internal Server Error")
                .jsonPath("$.message").isEqualTo("simulated failure");
    }
}
