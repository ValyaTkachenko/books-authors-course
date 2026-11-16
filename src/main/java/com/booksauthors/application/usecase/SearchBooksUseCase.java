package com.booksauthors.application.usecase;

import com.booksauthors.application.QueryNormalizer;
import com.booksauthors.application.dto.BookResponse;
import com.booksauthors.application.dto.SearchResponse;
import com.booksauthors.application.port.in.SearchQueryPort;
import com.booksauthors.domain.model.SearchLogEntry;
import com.booksauthors.domain.model.SearchResult;
import com.booksauthors.domain.port.out.CachePort;
import com.booksauthors.domain.port.out.SearchIndexPort;
import com.booksauthors.domain.port.out.SearchStatsPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/** Потік читання (CQRS query side): нормалізація запиту → Redis → (miss) Elasticsearch → запис у кеш. */
@Service
public class SearchBooksUseCase implements SearchQueryPort {

    private static final Logger log = LoggerFactory.getLogger(SearchBooksUseCase.class);

    private final CachePort cache;
    private final SearchIndexPort searchIndex;
    private final SearchStatsPort stats;
    private final QueryNormalizer normalizer;
    private final Duration ttl;

    public SearchBooksUseCase(CachePort cache,
                              SearchIndexPort searchIndex,
                              SearchStatsPort stats,
                              QueryNormalizer normalizer,
                              @Value("${app.cache.ttl-seconds:60}") long ttlSeconds) {
        this.cache = cache;
        this.searchIndex = searchIndex;
        this.stats = stats;
        this.normalizer = normalizer;
        this.ttl = Duration.ofSeconds(ttlSeconds);
    }

    @Override
    public SearchResponse search(String rawQuery, int size) {
        long t0 = System.nanoTime();
        int limit = Math.max(1, Math.min(size, 100));
        String key = normalizer.normalize(rawQuery);
        if (key.isEmpty()) {
            return new SearchResponse(rawQuery, key, 0, false, 0, List.of());
        }

        String cacheKey = key + "|" + limit;
        Optional<SearchResult> cached = cache.get(cacheKey);
        boolean fromCache = cached.isPresent();
        SearchResult result = cached.orElseGet(() -> {
            SearchResult r = searchIndex.search(key, limit);
            cache.put(cacheKey, r, ttl);
            return r;
        });

        long tookMs = (System.nanoTime() - t0) / 1_000_000;
        String status = result.total() > 0 ? "FOUND" : "EMPTY";
        stats.record(new SearchLogEntry(rawQuery, status, result.total(), fromCache, tookMs, System.currentTimeMillis()));
        log.info("Пошук '{}' (ключ кешу '{}') -> {} ({} результатів), cache={}, {} мс",
                rawQuery, key, status, result.total(), fromCache ? "HIT" : "MISS", tookMs);

        List<BookResponse> items = result.hits().stream().map(BookResponse::from).toList();
        return new SearchResponse(rawQuery, key, result.total(), fromCache, tookMs, items);
    }
}
