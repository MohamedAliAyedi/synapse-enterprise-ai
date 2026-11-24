package com.example.aiorchestrator.util;

import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility class for creating and managing Server-Sent Events (SSE) emitters.
 * <p>
 * This class centralises the configuration of {@link SseEmitter} instances used across the
 * application, ensuring a consistent timeout, thread pool, and content type. It also provides
 * helper methods for sending data and completing the stream, reducing boilerplate in controllers.
 * </p>
 */
public final class StreamingResponseUtil {

    private static final long DEFAULT_TIMEOUT_MS = Duration.ofMinutes(30).toMillis();
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private StreamingResponseUtil() {
        // Prevent instantiation
    }

    /**
     * Creates a new {@link SseEmitter} with a sensible default timeout and content type.
     *
     * @return a configured {@link SseEmitter}
     */
    public static SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT_MS);
        // Ensure the response is sent as "text/event-stream"
        emitter.onCompletion(() -> {
            // No-op placeholder for potential cleanup logic
        });
        return emitter;
    }

    /**
     * Sends a data object to the client as an SSE event using the provided {@link SseEmitter}.
     * The operation is executed asynchronously to avoid blocking the request thread.
     *
     * @param emitter the {@link SseEmitter} to write to
     * @param data    the payload to send; it will be serialized by Spring's message converters
     */
    public static void sendEventAsync(SseEmitter emitter, Object data) {
        EXECUTOR.submit(() -> {
            try {
                emitter.send(data, MediaType.APPLICATION_JSON);
            } catch (IOException ex) {
                // If the client disconnects or an I/O error occurs, complete the emitter.
                emitter.completeWithError(ex);
            }
        });
    }

    /**
     * Completes the SSE stream gracefully.
     *
     * @param emitter the {@link SseEmitter} to complete
     */
    public static void complete(SseEmitter emitter) {
        emitter.complete();
    }

    /**
     * Completes the SSE stream with an error.
     *
     * @param emitter the {@link SseEmitter} to complete
     * @param error   the throwable that caused the termination
     */
    public static void completeWithError(SseEmitter emitter, Throwable error) {
        emitter.completeWithError(error);
    }
}
