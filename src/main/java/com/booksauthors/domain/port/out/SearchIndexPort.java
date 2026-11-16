package com.booksauthors.domain.port.out;

import com.booksauthors.domain.model.Book;
import com.booksauthors.domain.model.SearchResult;

import java.util.List;

/** Вихідний порт: повнотекстовий пошук (реалізація — Elasticsearch). */
public interface SearchIndexPort {
    /** Індексує книги пакетом. Повертає кількість документів, які не вдалося проіндексувати. */
    int indexAll(List<Book> books);

    SearchResult search(String normalizedQuery, int size);

    long count();
}
