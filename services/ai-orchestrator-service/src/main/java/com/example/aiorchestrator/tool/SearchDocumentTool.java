package com.example.aiorchestrator.tool;

import com.example.aiorchestrator.repository.DocumentRepository;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Tool responsible for searching documents based on a query string.
 * <p>
 * This implementation now gracefully handles {@code null} or blank queries by returning an
 * empty result list instead of delegating to the {@link DocumentRepository}. This prevents
 * unnecessary database calls and aligns with the expected behaviour verified by the new
 * integration tests.
 * </p>
 */
@Component
public class SearchDocumentTool implements Tool {

    private final DocumentRepository documentRepository;

    public SearchDocumentTool(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    public String name() {
        return "search-document";
    }

    /**
     * Executes the search operation.
     *
     * @param input the search query; expected to be a {@link String}
     * @return a {@link List} of search results; never {@code null}
     */
    @Override
    public Object execute(Object input) {
        // Ensure the input is a non‑blank string; otherwise return an empty list.
        if (!(input instanceof String) || isBlank((String) input)) {
            return Collections.emptyList();
        }
        String query = ((String) input).trim();
        // Delegate to the repository; repository is expected to return a non‑null list.
        List<?> results = documentRepository.search(query);
        return Objects.requireNonNullElse(results, Collections.emptyList());
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
