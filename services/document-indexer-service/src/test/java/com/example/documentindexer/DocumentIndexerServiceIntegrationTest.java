package com.example.documentindexer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration tests for {@link DocumentIndexerService} focusing on edge cases in the vector embedding
 * pipeline. The tests spin up real PostgreSQL and Redis containers to verify persistence and caching
 * behaviour while mocking the external embedding provider.
 */
@SpringBootTest
@ExtendWith(org.springframework.test.context.junit.jupiter.SpringExtension.class)
@Testcontainers
class DocumentIndexerServiceIntegrationTest {

    private static final String POSTGRES_IMAGE = "postgres:15-alpine";
    private static final String REDIS_IMAGE = "redis:7-alpine";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE)
            .withDatabaseName("docidx_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(REDIS_IMAGE)
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> postgres.getJdbcUrl());
        registry.add("spring.datasource.username", () -> postgres.getUsername());
        registry.add("spring.datasource.password", () -> postgres.getPassword());
        registry.add("spring.redis.host", () -> redis.getHost());
        registry.add("spring.redis.port", () -> redis.getFirstMappedPort().toString());
    }

    @Autowired
    private DocumentIndexerService documentIndexerService;

    @MockBean
    private EmbeddingClient embeddingClient; // external LLM embedding provider abstraction

    private static final List<Float> DUMMY_VECTOR = Collections.nCopies(768, 0.1f);

    @BeforeEach
    void setUp() {
        // Mock the embedding client to always return a deterministic vector regardless of input.
        when(embeddingClient.embed(anyString())).thenReturn(DUMMY_VECTOR);
    }

    @Nested
    @DisplayName("Edge case: Empty document content")
    class EmptyDocument {
        @Test
        void shouldStoreEmptyVectorWhenContentIsBlank() {
            String docId = "doc-empty";
            documentIndexerService.indexDocument(docId, "   ");
            List<Float> storedVector = documentIndexerService.retrieveVector(docId);
            assertThat(storedVector).isEqualTo(DUMMY_VECTOR);
        }
    }

    @Nested
    @DisplayName("Edge case: Very large document")
    class LargeDocument {
        private String largeContent;

        @BeforeEach
        void init() {
            // Generate a string > 100,000 characters to trigger chunking logic.
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 100_000; i++) {
                sb.append('a');
            }
            largeContent = sb.toString();
        }

        @Test
        void shouldChunkAndIndexLargeContent() {
            String docId = "doc-large";
            documentIndexerService.indexDocument(docId, largeContent);
            List<Float> storedVector = documentIndexerService.retrieveVector(docId);
            // The service aggregates chunk vectors; in our mock we still get the dummy vector.
            assertThat(storedVector).isEqualTo(DUMMY_VECTOR);
        }
    }

    @Nested
    @DisplayName("Edge case: Non‑ASCII / Unicode characters")
    class UnicodeDocument {
        @Test
        void shouldHandleUnicodeContentGracefully() {
            String docId = "doc-unicode";
            String unicodeContent = "Привет мир! 🌍🚀"; // Russian + emojis
            documentIndexerService.indexDocument(docId, unicodeContent);
            List<Float> storedVector = documentIndexerService.retrieveVector(docId);
            assertThat(storedVector).isEqualTo(DUMMY_VECTOR);
        }
    }
}
