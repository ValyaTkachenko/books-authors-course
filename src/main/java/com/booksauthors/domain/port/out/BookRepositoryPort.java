package com.booksauthors.domain.port.out;

import com.booksauthors.domain.model.Book;
import com.booksauthors.domain.model.BookFilter;
import com.booksauthors.domain.model.GenreStat;
import com.booksauthors.domain.model.PagedBooks;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Вихідний порт: основне сховище книг (реалізація — MongoDB sharded cluster). */
public interface BookRepositoryPort {
    Book save(Book book);

    /** Пакетний upsert за _id (ідемпотентний: повторний імпорт не створює дублікатів). */
    void saveAll(List<Book> books);

    Optional<Book> findById(String id);

    void deleteById(String id);

    PagedBooks find(BookFilter filter, int page, int size);

    long count();

    /** Агрегація «жанр → кількість, середній рейтинг» (виконується на всіх шардах, результати зливаються на mongos). */
    List<GenreStat> aggregateByGenre(int limit);

    /** Кількість документів на кожному шарді. */
    Map<String, Long> countByShard();
}
