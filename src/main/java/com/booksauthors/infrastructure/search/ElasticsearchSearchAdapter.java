package com.booksauthors.infrastructure.search;

import com.booksauthors.domain.model.Book;
import com.booksauthors.domain.port.out.SearchIndexPort;
import java.util.List;

import org.springframework.stereotype.Repository;

/** Вихідний адаптер: повнотекстовий пошук поверх Elasticsearch (індекс literature_search). */
@Repository
public class ElasticsearchSearchAdapter implements SearchIndexPort {

    @Override
    public void index(Book book) {
        // TODO: індексувати synopsis/author_name/biography
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public List<Book> fullTextSearch(String normalizedQuery) {
        throw new UnsupportedOperationException("TODO");
    }
}
