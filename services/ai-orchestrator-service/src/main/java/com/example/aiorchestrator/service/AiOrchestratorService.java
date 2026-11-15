package com.example.aiorchestrator.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatResponse;
import org.springframework.ai.chat.client.DefaultChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * Service that orchestrates AI chat interactions using Spring AI and the Groq provider.
 * <p>
 * The {@code streamChat} method now validates the incoming {@code prompt} and returns a
 * {@link Flux} that either emits the streamed AI response or terminates with an
 * {@link IllegalArgumentException} when the prompt is {@code null} or blank. This guard
 * prevents downstream NPEs and ensures the SSE emitter in the controller always completes
 * with a deterministic error signal.
 */
@Service
public class AiOrchestratorService {

    private final ChatClient chatClient;

    public AiOrchestratorService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Streams the AI response for the given {@code prompt}.
     *
     * @param prompt the user prompt; must be non‑null and contain at least one non‑whitespace character
     * @return a {@link Flux} emitting chunks of the AI response
     */
    public Flux<String> streamChat(String prompt) {
        // ---- FIX: validate prompt to avoid NPEs and undefined behaviour ----
        if (!StringUtils.hasText(prompt)) {
            return Flux.error(new IllegalArgumentException("Prompt must not be null or empty"));
        }
        // -------------------------------------------------------------------

        // Build a reactive chat request that streams the response.
        return chatClient.prompt(new UserMessage(prompt))
                .stream()
                .map(ChatResponse::getResult)
                .flatMapIterable(result -> result.getOutput().split("\n"))
                .filter(chunk -> !chunk.isBlank());
    }
}
