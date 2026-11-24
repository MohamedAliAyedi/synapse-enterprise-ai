package com.example.aiorchestrator.tool;

import com.example.aiorchestrator.repository.DocumentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

/**
 * Integration tests for {@link SearchDocumentTool} verifying behaviour for empty and valid queries.
 */
@SpringBootTest
class SearchDocumentToolIntegrationTest {

    @Autowired
    private SearchDocumentTool searchDocumentTool;

    @MockBean
    private DocumentRepository documentRepository;

    @Test
    @DisplayName("When query is null, tool returns empty list without invoking repository")
    void shouldReturnEmptyListForNullQuery() {
        Object result = searchDocumentTool.execute(null);
        assertThat(result).asList().isEmpty();
    }

    @Test
    @DisplayName("When query is blank, tool returns empty list without invoking repository")
    void shouldReturnEmptyListForBlankQuery() {
        Object result = searchDocumentTool.execute("   ");
        assertThat(result).asList().isEmpty();
    }

    @Test
    @DisplayName("When query is valid, tool delegates to repository and returns its results")
    void shouldDelegateToRepositoryForValidQuery() {
        String query = "spring boot";
        List<String> mockResults = List.of("doc1", "doc2");
        given(documentRepository.search(eq(query))).willReturn(mockResults);

        Object result = searchDocumentTool.execute(query);
        assertThat(result).isEqualTo(mockResults);
    }
}
