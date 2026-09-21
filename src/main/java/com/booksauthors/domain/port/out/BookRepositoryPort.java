package com.booksauthors.domain.port.out;

import com.booksauthors.domain.model.Book;
import java.util.List;
import java.util.Optional;

/** Вихідний порт: CRUD-доступ до основного сховища книг (реалізує MongoDBRepositoryAdapter). */
public interface BookRepositoryPort {
    Book save(Book book);
    Optional<Book> findById(String id);
    List<Book> findByGenreAndYear(String genre, Integer year);
    void deleteById(String id);
}
