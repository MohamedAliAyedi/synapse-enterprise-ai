package com.example.aiorchestrator.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for streaming related errors.
 * <p>
 * Previously this handler returned {@link HttpStatus#INTERNAL_SERVER_ERROR} (500) for all
 * {@link StreamingException}s. From a client perspective a streaming failure is typically a
 * downstream service issue, so the more appropriate status is {@link HttpStatus#BAD_GATEWAY}
 * (502). This change improves observability and allows callers (including the frontend) to
 * react correctly to upstream failures.
 */
@RestControllerAdvice
public class StreamingExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(StreamingExceptionHandler.class);

    /**
     * Handles {@link StreamingException} thrown by the AI orchestrator streaming pipeline.
     *
     * @param ex the caught exception
     * @return a {@link ResponseEntity} containing an {@link ErrorResponse} with HTTP 502 status
     */
    @ExceptionHandler(StreamingException.class)
    public ResponseEntity<ErrorResponse> handleStreamingException(StreamingException ex) {
        // Log the exception with stack trace for debugging purposes
        logger.error("Streaming error occurred: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
                "STREAMING_ERROR",
                ex.getMessage() != null ? ex.getMessage() : "An unexpected streaming error occurred"
        );
        // Return 502 Bad Gateway to indicate a downstream service failure
        return new ResponseEntity<>(error, HttpStatus.BAD_GATEWAY);
    }
}
