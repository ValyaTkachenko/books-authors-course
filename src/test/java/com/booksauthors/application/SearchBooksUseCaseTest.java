package com.booksauthors.application;

import com.booksauthors.application.dto.SearchResponse;
import com.booksauthors.application.usecase.SearchBooksUseCase;
import com.booksauthors.domain.model.Book;
import com.booksauthors.testsupport.FakeCache;
import com.booksauthors.testsupport.FakeSearchIndex;
import com.booksauthors.testsupport.FakeSearchStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchBooksUseCaseTest {

    private FakeSearchIndex index;
    private FakeCache cache;
    private FakeSearchStats stats;
    private SearchBooksUseCase useCase;

    @BeforeEach
    void setUp() {
        index = new FakeSearchIndex();
        cache = new FakeCache();
        stats = new FakeSearchStats();
        useCase = new SearchBooksUseCase(cache, index, stats, new QueryNormalizer(), 60);

        Book b = new Book();
        b.setId("1");
        b.setTitle("Test book");
        b.setSynopsis("A story with the unique marker zzmarker123 inside");
        index.docs.add(b);
    }

    @Test
    void positiveSearchFindsDocumentByUniqueMarker() {
        SearchResponse r = useCase.search("ZZMARKER123", 20);
        assertEquals(1, r.total());
        assertEquals("Test book", r.results().get(0).title());
        assertFalse(r.fromCache());
    }

    @Test
    void negativeSearchReturnsEmptyResultNotError() {
        SearchResponse r = useCase.search("no-such-marker-999", 20);
        assertEquals(0, r.total());
        assertTrue(r.results().isEmpty());
        assertEquals("EMPTY", stats.entries.get(0).status());
    }

    @Test
    void repeatedQueryWithReorderedWordsIsServedFromCache() {
        useCase.search("unique marker", 20);
        SearchResponse second = useCase.search("Marker, UNIQUE", 20);
        assertTrue(second.fromCache());
        assertEquals(1, index.searchCalls);
    }

    @Test
    void blankQueryReturnsEmptyWithoutTouchingIndex() {
        SearchResponse r = useCase.search("   ", 20);
        assertEquals(0, r.total());
        assertEquals(0, index.searchCalls);
    }
}
