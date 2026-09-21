package com.booksauthors.application.usecase;

import com.booksauthors.application.dto.BookResponse;
import com.booksauthors.application.port.in.SearchQueryPort;
import com.booksauthors.domain.port.out.CachePort;
import com.booksauthors.domain.port.out.SearchIndexPort;
import com.booksauthors.infrastructure.ingestion.QueryNormalizer;
import java.util.List;

/**
 * Application Layer: сценарій повнотекстового пошуку.
 * Query Flow: SearchQueryPort -> QueryNormalizer -> CachePort (Hit/Miss)
 *             -> при Miss: SearchIndexPort (Elasticsearch) -> запис у кеш.
 */
public class SearchBooksUseCase implements SearchQueryPort {

    private final CachePort cachePort;
    private final SearchIndexPort searchIndexPort;
    private final QueryNormalizer queryNormalizer;

    public SearchBooksUseCase(CachePort cachePort, SearchIndexPort searchIndexPort,
                               QueryNormalizer queryNormalizer) {
        this.cachePort = cachePort;
        this.searchIndexPort = searchIndexPort;
        this.queryNormalizer = queryNormalizer;
    }

    @Override
    public List<BookResponse> search(String rawQuery) {
        // TODO: 1) normalizedKey = queryNormalizer.normalize(rawQuery)
        // TODO: 2) cachePort.get(normalizedKey) - якщо є, повернути (Hit)
        // TODO: 3) інакше searchIndexPort.fullTextSearch(...) (Miss), потім cachePort.put(...)
        throw new UnsupportedOperationException("TODO");
    }
}
