package com.booksauthors.infrastructure.cache;

import com.booksauthors.domain.model.Book;
import com.booksauthors.domain.port.out.CachePort;
import java.util.List;
import java.util.Optional;

/** Вихідний адаптер: кешування результатів пошуку поверх Redis (ключ search:{normalized_query_hash}). */
public class RedisCacheAdapter implements CachePort {

    @Override
    public Optional<List<Book>> get(String normalizedQueryKey) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void put(String normalizedQueryKey, List<Book> results) {
        throw new UnsupportedOperationException("TODO");
    }
}
