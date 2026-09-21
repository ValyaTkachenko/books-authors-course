package com.booksauthors.domain.port.out;

import com.booksauthors.domain.model.Book;
import java.util.List;

/** Вихідний порт: повнотекстовий пошук (реалізує ElasticsearchSearchAdapter). */
public interface SearchIndexPort {
    void index(Book book);
    List<Book> fullTextSearch(String normalizedQuery);
}
