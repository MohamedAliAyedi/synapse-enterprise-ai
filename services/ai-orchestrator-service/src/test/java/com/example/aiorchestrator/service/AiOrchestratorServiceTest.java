package com.example.aiorchestrator.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatResponse;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AiOrchestratorServiceTest {

    private final ChatClient mockChatClient = Mockito.mock(ChatClient.class);
    private final AiOrchestratorService service = new AiOrchestratorService(mockChatClient);

    @Nested
    @DisplayName("Prompt validation")
    class PromptValidation {
        @Test
        @DisplayName("should error when prompt is null")
        void nullPrompt() {
            StepVerifier.create(service.streamChat(null))
                    .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                            throwable.getMessage().contains("Prompt must not be null or empty"))
                    .verify();
        }

        @Test
        @DisplayName("should error when prompt is blank")
        void blankPrompt() {
            StepVerifier.create(service.streamChat("   "))
                    .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                            throwable.getMessage().contains("Prompt must not be null or empty"))
                    .verify();
        }
    }

    @Nested
    @DisplayName("Successful streaming")
    class SuccessfulStreaming {
        @Test
        @DisplayName("should stream chunks from ChatClient response")
        void streamChunks() {
            // Mock a ChatResponse that contains a multi‑line assistant message
            AssistantMessage assistantMessage = new AssistantMessage("line1\nline2\nline3");
            ChatResponse mockResponse = ChatResponse.builder()
                    .result(assistantMessage)
                    .build();

            // Mock the reactive streaming API of ChatClient
            when(mockChatClient.prompt(any(UserMessage.class)))
                    .thenReturn(new ChatClient.ChatResponseSpec() {
                        @Override
                        public Flux<ChatResponse> stream() {
                            return Flux.just(mockResponse);
                        }
                    });

            Flux<String> result = service.streamChat("test prompt");

            StepVerifier.create(result)
                    .expectNext("line1")
                    .expectNext("line2")
                    .expectNext("line3")
                    .verifyComplete();
        }
    }
}
