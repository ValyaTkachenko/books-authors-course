package com.booksauthors.infrastructure.persistence.mongo;

import com.booksauthors.domain.model.Book;
import com.booksauthors.domain.port.out.BookRepositoryPort;
import java.util.List;
import java.util.Optional;

/** Вихідний адаптер: CRUD-реалізація BookRepositoryPort поверх MongoDB (колекції books/authors/publishers). */
public class MongoDBRepositoryAdapter implements BookRepositoryPort {

    @Override
    public Book save(Book book) {
        // TODO: запис документа в колекцію books (з урахуванням genres[0] як shard key)
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Optional<Book> findById(String id) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public List<Book> findByGenreAndYear(String genre, Integer year) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void deleteById(String id) {
        throw new UnsupportedOperationException("TODO");
    }
}
