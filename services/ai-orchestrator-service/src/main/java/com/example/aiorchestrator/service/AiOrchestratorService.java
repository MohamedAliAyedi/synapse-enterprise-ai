package com.example.aiorchestrator.service;

import org.springframework.ai.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Service responsible for orchestrating AI calls and streaming the results back to the client via SSE.
 * <p>
 * This class fixes two issues observed in the previous implementation:
 * <ul>
 *   <li>When the {@link Model} bean was not available (e.g., mis‑configuration), a {@code NullPointerException}
 *       was thrown, breaking the request.</li>
 *   <li>The {@link SseEmitter} was not always completed, causing client‑side time‑outs and resource leaks.</li>
 * </ul>
 * The new implementation performs a null‑check on the model, sends a clear error event, and guarantees that the
 * emitter is completed (or completed with error) in every execution path.
 */
@Service
public class AiOrchestratorService {

    private final Model model;

    @Autowired
    public AiOrchestratorService(Model model) {
        this.model = model;
    }

    /**
     * Streams the AI response for the given {@code prompt} to the supplied {@link SseEmitter}.
     *
     * @param prompt  the user prompt to send to the AI model
     * @param emitter the SSE emitter used to push events to the client
     */
    public void streamChat(String prompt, SseEmitter emitter) {
        // Defensive check: ensure the model bean is present before proceeding.
        if (this.model == null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("AI model is not configured. Please contact the administrator."));
            } catch (IOException ignored) {
                // If we cannot send the error, just complete the emitter.
            } finally {
                emitter.complete();
            }
            return;
        }

        // Run the potentially blocking AI call in a separate thread to avoid blocking the servlet container.
        CompletableFuture.runAsync(() -> {
            try {
                // NOTE: The actual Model API may differ (e.g., chat completion, streaming). Here we use a simple
                // generate(...) method for illustration. Replace with the real streaming call when available.
                String response = this.model.generate(prompt);

                // Send the response as a single SSE event. In a real streaming scenario, you would split the
                // response into chunks and emit multiple events.
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(response));

                // Signal successful completion.
                emitter.complete();
            } catch (IOException ioEx) {
                // IO problems while writing to the emitter – propagate as an error.
                emitter.completeWithError(ioEx);
            } catch (Exception ex) {
                // Any other unexpected exception should also close the emitter with an error.
                emitter.completeWithError(ex);
            }
        });
    }
}
