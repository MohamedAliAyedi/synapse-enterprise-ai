package com.example.aiorchestrator.tool;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Arrays;

import com.example.aiorchestrator.model.Document;
import com.example.aiorchestrator.repository.DocumentRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link SearchDocumentTool}.
 *
 * These tests verify that the tool correctly delegates to the {@link DocumentRepository}
 * and returns the expected results for various query scenarios.
 */
@ExtendWith(MockitoExtension.class)
class SearchDocumentToolTest {

    @Mock
    private DocumentRepository documentRepository;

    @InjectMocks
    private SearchDocumentTool searchDocumentTool;

    @Test
    void shouldReturnDocumentsWhenQueryMatches() {
        // Arrange
        String query = "AI";
        Document doc1 = new Document(1L, "AI Overview", "Content about AI");
        Document doc2 = new Document(2L, "Machine Learning", "Content about ML");
        List<Document> expected = Arrays.asList(doc1, doc2);
        when(documentRepository.searchByKeyword(query)).thenReturn(expected);

        // Act
        List<Document> result = searchDocumentTool.search(query);

        // Assert
        assertEquals(expected, result);
        verify(documentRepository).searchByKeyword(query);
    }

    @Test
    void shouldReturnEmptyListWhenNoMatch() {
        // Arrange
        String query = "nonexistent";
        when(documentRepository.searchByKeyword(query)).thenReturn(List.of());

        // Act
        List<Document> result = searchDocumentTool.search(query);

        // Assert
        assertTrue(result.isEmpty());
        verify(documentRepository).searchByKeyword(query);
    }
}
