package com.example.aiorchestrator.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;

/**
 * Global exception handler for streaming endpoints.
 * <p>
 * When an exception occurs while producing a Server‑Sent Events (SSE) stream, the default
 * Spring behaviour is to close the connection without any payload, leaving the client
 * unaware of the failure. This handler captures exceptions and converts them into a
 * {@link ServerSentEvent} containing an error message, ensuring the client receives a
 * well‑formed SSE with the appropriate {@code event: error} type.
 * </p>
 */
@ControllerAdvice(assignableTypes = com.example.aiorchestrator.controller.StreamController.class)
public class StreamingExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(StreamingExceptionHandler.class);

    /**
     * Handles any uncaught exception thrown from streaming controller methods.
     *
     * @param ex the exception that was thrown
     * @return a Flux emitting a single {@link ServerSentEvent} with the error details
     */
    @ExceptionHandler(Exception.class)
    public Flux<ServerSentEvent<String>> handleStreamingException(Exception ex) {
        log.error("Streaming error occurred: {}", ex.getMessage(), ex);
        ServerSentEvent<String> errorEvent = ServerSentEvent.<String>builder()
                .event("error")
                .data("Streaming failed: " + ex.getMessage())
                .build();
        // Emit the error event and then complete the stream.
        return Flux.just(errorEvent);
    }

    /**
     * Specific handling for bad client input that results in a {@link ServerWebInputException}.
     * Returns a 400‑style error event.
     */
    @ExceptionHandler(ServerWebInputException.class)
    public Flux<ServerSentEvent<String>> handleBadInput(ServerWebInputException ex) {
        log.warn("Bad input for streaming endpoint: {}", ex.getMessage());
        ServerSentEvent<String> errorEvent = ServerSentEvent.<String>builder()
                .event("error")
                .data("Invalid request: " + ex.getReason())
                .build();
        return Flux.just(errorEvent);
    }
}
