package com.booksauthors.application.usecase;

import com.booksauthors.application.dto.IngestBookRequest;
import com.booksauthors.application.dto.IngestResult;
import com.booksauthors.application.port.in.IngestCommandPort;
import com.booksauthors.domain.model.Book;
import com.booksauthors.domain.port.out.BookRepositoryPort;
import com.booksauthors.domain.port.out.SearchIndexPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Командний потік (CQRS write side): MongoDB — єдине джерело істини, Elasticsearch оновлюється після нього.
 * Збій індексації не скасовує запис (eventual consistency), а лише логується.
 */
@Service
public class IngestBookUseCase implements IngestCommandPort {

    private static final Logger log = LoggerFactory.getLogger(IngestBookUseCase.class);

    private final BookRepositoryPort bookRepository;
    private final SearchIndexPort searchIndex;

    public IngestBookUseCase(BookRepositoryPort bookRepository, SearchIndexPort searchIndex) {
        this.bookRepository = bookRepository;
        this.searchIndex = searchIndex;
    }

    @Override
    public IngestResult ingestBatch(List<IngestBookRequest> requests) {
        Map<String, Book> unique = new LinkedHashMap<>();
        for (IngestBookRequest r : requests) {
            Book b = toBook(r);
            if (b != null) unique.put(b.getId(), b);
        }
        if (unique.isEmpty()) return new IngestResult(0, 0);

        List<Book> books = new ArrayList<>(unique.values());
        bookRepository.saveAll(books);

        int indexFailed;
        try {
            indexFailed = searchIndex.indexAll(books);
        } catch (Exception e) {
            log.warn("Індексація в Elasticsearch не вдалася для {} книг: {}", books.size(), e.getMessage());
            indexFailed = books.size();
        }
        log.debug("Збережено пакет: {} книг, не проіндексовано: {}", books.size(), indexFailed);
        return new IngestResult(books.size(), indexFailed);
    }

    private Book toBook(IngestBookRequest r) {
        if (r == null || r.title() == null || r.title().isBlank()) return null;
        Book b = new Book();
        b.setId(deriveId(r));
        b.setTitle(r.title().trim());
        b.setSynopsis(r.synopsis());
        b.setGenres(r.genres() == null ? new ArrayList<>() : new ArrayList<>(r.genres()));
        b.setPublisherName(r.publisherName());
        b.setPublishedYear(r.publishedYear());
        b.setAwards(r.awards() == null ? new ArrayList<>() : new ArrayList<>(r.awards()));
        b.setRating(r.rating());
        b.setPrice(r.price());
        b.setAuthorName(r.authorName());
        b.setLanguage(r.language());
        b.setNumRatings(r.numRatings());
        return b;
    }

    /** Стабільний ідентифікатор: bookId з датасету або хеш (назва + автор) — повторний імпорт не дублює книги. */
    static String deriveId(IngestBookRequest r) {
        if (r.sourceId() != null && !r.sourceId().isBlank()) return r.sourceId().trim();
        String basis = (r.title() + "|" + (r.authorName() == null ? "" : r.authorName())).toLowerCase(Locale.ROOT);
        return UUID.nameUUIDFromBytes(basis.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
