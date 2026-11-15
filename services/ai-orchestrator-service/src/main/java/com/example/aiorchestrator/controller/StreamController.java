package com.example.aiorchestrator.controller;

import com.example.aiorchestrator.service.AiOrchestratorService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.Duration;

/**
 * REST controller exposing a Server‑Sent Events (SSE) endpoint for streaming AI responses.
 * <p>
 * The controller delegates the actual generation to {@link AiOrchestratorService}. It ensures that the
 * {@link SseEmitter} is always completed – either normally or with an error – to avoid hanging client
 * connections. It also validates the incoming {@code prompt} parameter and returns a {@code 400 Bad Request}
 * if the prompt is missing or empty.
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StreamController {

    private static final long SSE_TIMEOUT_MS = Duration.ofMinutes(30).toMillis();
    private final AiOrchestratorService aiOrchestratorService;

    /**
     * Streams AI generated tokens back to the client via SSE.
     *
     * @param prompt the user prompt – must not be blank
     * @return an {@link SseEmitter} that pushes each token as a separate SSE event
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam @NotBlank String prompt) {
        log.debug("Received SSE stream request for prompt: {}", prompt);
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        // Guard against null or empty model configuration inside the service – the service already
        // throws an IllegalStateException in that case, but we keep the check here for clarity.
        if (prompt == null || prompt.isBlank()) {
            emitter.completeWithError(new IllegalArgumentException("Prompt must not be empty"));
            return emitter;
        }

        try {
            Flux<String> tokenFlux = aiOrchestratorService.streamResponse(prompt);

            tokenFlux
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(token -> {
                    try {
                        emitter.send(token);
                    } catch (IOException e) {
                        log.error("Error sending token via SSE", e);
                        emitter.completeWithError(e);
                    }
                })
                .doOnError(throwable -> {
                    log.error("Streaming error for prompt [{}]", prompt, throwable);
                    emitter.completeWithError(throwable);
                })
                .doOnComplete(() -> {
                    log.debug("Streaming completed for prompt: {}", prompt);
                    emitter.complete();
                })
                .subscribe();
        } catch (Exception ex) {
            // Any unexpected exception while setting up the stream should also close the emitter.
            log.error("Failed to initiate streaming for prompt [{}]", prompt, ex);
            emitter.completeWithError(ex);
        }

        return emitter;
    }
}
