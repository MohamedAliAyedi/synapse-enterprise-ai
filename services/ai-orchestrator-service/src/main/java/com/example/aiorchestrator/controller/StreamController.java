package com.example.aiorchestrator.controller;

import com.example.aiorchestrator.service.AiOrchestratorService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * REST controller that streams AI responses using Server-Sent Events (SSE).
 * <p>
 * The endpoint produces a continuous stream of {@link String} payloads.
 * Errors occurring during streaming are handled by {@link com.example.aiorchestrator.exception.StreamingExceptionHandler}
 * and emitted as SSE error events.
 * </p>
 */
@RestController
@RequestMapping("/api/stream")
public class StreamController {

    private final AiOrchestratorService orchestratorService;

    public StreamController(final AiOrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    /**
     * Starts a streaming response.
     *
     * @return a {@link Flux} emitting streamed data as {@code text/event-stream}
     */
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream() {
        return orchestratorService.streamResponses()
                .map(response -> "data: " + response + "\n\n")
                .doOnError(throwable -> {
                    // Error handling is delegated to the global exception handler.
                });
    }
}
