package com.booksauthors.domain.port.out;

import com.booksauthors.domain.model.SearchResult;

import java.time.Duration;
import java.util.Optional;

/** Вихідний порт: кеш результатів пошуку (реалізація — Redis). Недоступний кеш = cache miss. */
public interface CachePort {
    Optional<SearchResult> get(String normalizedKey);

    void put(String normalizedKey, SearchResult result, Duration ttl);
}
