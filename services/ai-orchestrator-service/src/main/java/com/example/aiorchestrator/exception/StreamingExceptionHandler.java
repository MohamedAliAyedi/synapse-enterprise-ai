package com.example.aiorchestrator.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;

/**
 * Global handler for exceptions that occur during Server‑Sent Events (SSE) streaming.
 * <p>
 * When a {@link StreamingException} is thrown from the streaming pipeline we want the
 * client to receive a structured error event instead of the connection silently closing.
 * This handler builds an {@link ErrorResponse} payload and emits it as an SSE event
 * named "error". The client can listen for this event and react accordingly (e.g.
 * display a toast, retry, etc.).
 * </p>
 */
@ControllerAdvice
public class StreamingExceptionHandler {

    /**
     * Handles {@link StreamingException} by sending an SSE error event to the client.
     *
     * @param ex the streaming exception that was raised
     * @param emitter the {@link SseEmitter} associated with the current request
     * @throws IOException if writing to the emitter fails
     */
    @ExceptionHandler(StreamingException.class)
    public void handleStreamingException(StreamingException ex, SseEmitter emitter) throws IOException {
        // Build a generic error response that matches the API contract
        ErrorResponse errorResponse = new ErrorResponse(
                "STREAMING_ERROR",
                ex.getMessage()
        );

        // Emit an SSE event named "error" with the error payload.
        // A reconnection time is provided so the client can attempt to reconnect if desired.
        emitter.send(SseEmitter.event()
                .name("error")
                .data(errorResponse)
                .reconnectTime(3000));

        // Complete the emitter to close the SSE stream cleanly.
        emitter.complete();
    }
}
