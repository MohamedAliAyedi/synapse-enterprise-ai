package com.example.aiorchestrator.controller;

import com.example.aiorchestrator.service.AiOrchestratorService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * Controller responsible for handling streaming AI responses via Server-Sent Events (SSE).
 */
@RestController
public class StreamController {

    private final AiOrchestratorService aiOrchestratorService;

    public StreamController(AiOrchestratorService aiOrchestratorService) {
        this.aiOrchestratorService = aiOrchestratorService;
    }

    /**
     * Starts an SSE stream for the given user prompt.
     *
     * @param prompt the user input to be processed by the AI orchestrator
     * @return an {@link SseEmitter} that streams AI generated events
     */
    @GetMapping(value = "/api/v1/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam String prompt) {
        SseEmitter emitter = new SseEmitter();
        try {
            aiOrchestratorService.processPrompt(prompt, emitter);
        } catch (IOException ex) {
            emitter.completeWithError(ex);
        }
        return emitter;
    }
}
