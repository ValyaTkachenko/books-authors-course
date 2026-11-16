package com.booksauthors.application;

import com.booksauthors.application.dto.IngestBookRequest;
import com.booksauthors.application.dto.IngestResult;
import com.booksauthors.application.usecase.IngestBookUseCase;
import com.booksauthors.domain.model.Book;
import com.booksauthors.testsupport.FakeBookRepository;
import com.booksauthors.testsupport.FakeSearchIndex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Тест на запис: дані проходять через use case, зберігаються без втрат і ідемпотентно (upsert за id). */
class IngestBookUseCaseTest {

    private FakeBookRepository repo;
    private FakeSearchIndex index;
    private IngestBookUseCase useCase;

    @BeforeEach
    void setUp() {
        repo = new FakeBookRepository();
        index = new FakeSearchIndex();
        useCase = new IngestBookUseCase(repo, index);
    }

    private IngestBookRequest request(String title, String author) {
        return new IngestBookRequest(null, title, "Synopsis of " + title, List.of("Fantasy", "Adventure"),
                "Pub", 2008, List.of("Award"), 4.5, 9.99, author, "English", 1200);
    }

    @Test
    void savesAllFieldsWithoutLoss() {
        IngestResult r = useCase.ingestBatch(List.of(request("The Hunger Games", "Suzanne Collins")));

        assertEquals(1, r.saved());
        assertEquals(1, repo.store.size());
        Book b = repo.store.values().iterator().next();
        assertEquals("The Hunger Games", b.getTitle());
        assertEquals("Suzanne Collins", b.getAuthorName());
        assertEquals(List.of("Fantasy", "Adventure"), b.getGenres());
        assertEquals(2008, b.getPublishedYear());
        assertEquals(4.5, b.getRating());
        assertEquals(1200, b.getNumRatings());
        assertEquals(1, index.docs.size());
    }

    @Test
    void repeatedImportDoesNotCreateDuplicates() {
        useCase.ingestBatch(List.of(request("Dune", "Frank Herbert")));
        useCase.ingestBatch(List.of(request("Dune", "Frank Herbert")));
        assertEquals(1, repo.store.size());
    }

    @Test
    void skipsRecordsWithoutTitle() {
        IngestResult r = useCase.ingestBatch(List.of(request("  ", "Nobody"), request("Real", "Someone")));
        assertEquals(1, r.saved());
    }

    @Test
    void indexFailureDoesNotRollBackMainStorage() {
        index.failOnIndex = true;
        IngestResult r = useCase.ingestBatch(List.of(request("Emma", "Jane Austen")));
        assertEquals(1, r.saved());
        assertEquals(1, r.indexFailed());
        assertTrue(repo.store.size() == 1);
    }
}
