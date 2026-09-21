package com.booksauthors.domain.port.out;

import com.booksauthors.domain.model.Book;
import java.util.List;
import java.util.Optional;

/** Вихідний порт: кешування результатів пошуку (реалізує RedisCacheAdapter). */
public interface CachePort {
    Optional<List<Book>> get(String normalizedQueryKey);
    void put(String normalizedQueryKey, List<Book> results);
}
