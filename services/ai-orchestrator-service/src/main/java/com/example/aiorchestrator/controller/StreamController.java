package com.example.aiorchestrator.controller;

import com.example.aiorchestrator.service.AiOrchestratorService;
import com.example.aiorchestrator.util.StreamingResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Controller exposing a Server‑Sent Events (SSE) endpoint that streams AI generated content.
 * <p>
 * The previous implementation duplicated the boiler‑plate required to create an {@link SseEmitter},
 * register callbacks and handle exceptions.  This refactor delegates that responsibility to
 * {@link StreamingResponseUtil}, which centralises emitter creation, timeout handling and error
 * conversion.  Behaviour is unchanged – the endpoint still streams the response produced by
 * {@link AiOrchestratorService#stream(String)} – but the code is now concise, easier to test and
 * consistent across the code‑base.
 */
@RestController
@RequestMapping("/api/v1/stream")
public class StreamController {

    private static final Logger logger = LoggerFactory.getLogger(StreamController.class);

    private final AiOrchestratorService aiOrchestratorService;

    public StreamController(AiOrchestratorService aiOrchestratorService) {
        this.aiOrchestratorService = aiOrchestratorService;
    }

    /**
     * Starts an SSE stream for the supplied {@code prompt}. The heavy lifting is performed by
     * {@link AiOrchestratorService#stream(String)} which returns a {@link java.util.stream.Stream<String>}
     * of partial results.  The {@link StreamingResponseUtil} handles emitter lifecycle and error
     * propagation.
     *
     * @param prompt the user prompt that drives the AI generation
     * @return an {@link SseEmitter} that streams partial results to the client
     */
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam String prompt) {
        logger.info("Received streaming request for prompt: {}", prompt);

        // Delegate emitter creation and streaming logic to the utility class.
        // The utility expects a lambda that receives the SseEmitter and performs the actual
        // streaming.  Any exception thrown inside the lambda will be converted to a proper SSE
        // error event by StreamingResponseUtil.
        return StreamingResponseUtil.createSseEmitter(emitter -> {
            // The service returns a Stream<String> where each element represents a chunk of the
            // generated text.  We write each chunk to the emitter as an SSE "data" event.
            aiOrchestratorService.stream(prompt).forEach(chunk -> {
                try {
                    emitter.send(chunk);
                } catch (Exception e) {
                    // Propagate the exception so the utility can close the emitter with an error.
                    throw new RuntimeException(e);
                }
            });
            // Signal completion of the stream.
            emitter.complete();
        });
    }
}
