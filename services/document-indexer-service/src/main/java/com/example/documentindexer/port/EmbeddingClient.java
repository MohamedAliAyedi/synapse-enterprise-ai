package com.example.documentindexer.port;

import java.util.List;

/**
 * Port abstraction for generating vector embeddings from raw text.
 * <p>
 * This interface allows the domain layer to remain agnostic of the concrete
 * embedding provider (e.g., Groq, OpenAI, local model). Implementations live in
 * the {@code adapter} package and are wired via Spring DI.
 */
public interface EmbeddingClient {

    /**
     * Generate a dense vector representation for the given text.
     *
     * @param text the raw text to embed
     * @return a list of double values representing the embedding vector
     */
    List<Double> embed(String text);
}
