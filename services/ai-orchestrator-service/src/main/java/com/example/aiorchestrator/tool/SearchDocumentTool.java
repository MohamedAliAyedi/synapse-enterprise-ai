package com.example.aiorchestrator.tool;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Collections;

/**
 * Tool implementation that searches documents based on a query string.
 *
 * <p>Fix: Previously this class assumed the incoming query was always a non‑null, non‑blank
 * String. When a null or blank query was supplied a {@code NullPointerException} was thrown,
 * causing the AI orchestrator streaming endpoint to fail. The method now validates the input
 * and returns an empty result set for null or blank queries, preserving the contract of the
 * {@link Tool} interface and improving robustness.
 */
@Component
public class SearchDocumentTool implements Tool {

    private final DocumentRepository repository;

    public SearchDocumentTool(DocumentRepository repository) {
        this.repository = repository;
    }

    @Override
    public String getName() {
        return "search_document";
    }

    @Override
    public String getDescription() {
        return "Searches documents based on a query string.";
    }

    /**
     * Executes the search operation.
     *
     * @param input expected to be a {@link String} representing the search query
     * @return a {@link List} of matching {@link Document} objects, or an empty list if the
     *         query is null/blank or not a String
     */
    @Override
    public Object execute(Object input) {
        if (!(input instanceof String)) {
            throw new IllegalArgumentException("Input must be a query string");
        }
        String query = (String) input;
        if (query == null || query.isBlank()) {
            // Gracefully handle null or blank queries instead of throwing NPE
            return Collections.emptyList();
        }
        List<Document> results = repository.search(query);
        return results;
    }
}
