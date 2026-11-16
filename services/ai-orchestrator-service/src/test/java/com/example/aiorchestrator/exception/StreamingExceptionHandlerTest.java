package com.example.aiorchestrator.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * Unit tests for {@link StreamingExceptionHandler}.
 *
 * <p>The handler is responsible for converting exceptions that occur during a streaming
 * operation into Server‑Sent Events (SSE) with an "error" event type. This test verifies that
 * the emitted {@link ServerSentEvent} contains the expected event name and includes the
 * exception message in its payload.</p>
 */
class StreamingExceptionHandlerTest {

    private final StreamingExceptionHandler handler = new StreamingExceptionHandler();

    @Test
    void handleExceptionEmitsErrorEvent() {
        // Given a runtime exception with a specific message
        RuntimeException ex = new RuntimeException("test error");

        // When the exception is handled
        Flux<ServerSentEvent<String>> flux = handler.handleException(ex);

        // Then the resulting flux should emit a single SSE with event "error"
        StepVerifier.create(flux)
                .assertNext(event -> {
                    assertEquals("error", event.event());
                    assertTrue(event.data().contains("test error"));
                })
                .verifyComplete();
    }
}
