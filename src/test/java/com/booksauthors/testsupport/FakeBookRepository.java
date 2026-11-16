package com.booksauthors.testsupport;

import com.booksauthors.domain.model.Book;
import com.booksauthors.domain.model.BookFilter;
import com.booksauthors.domain.model.GenreStat;
import com.booksauthors.domain.model.PagedBooks;
import com.booksauthors.domain.port.out.BookRepositoryPort;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** In-memory реалізація порту: upsert за id, як у MongoDB. */
public class FakeBookRepository implements BookRepositoryPort {

    public final Map<String, Book> store = new LinkedHashMap<>();

    @Override public Book save(Book book) { store.put(book.getId(), book); return book; }
    @Override public void saveAll(List<Book> books) { books.forEach(this::save); }
    @Override public Optional<Book> findById(String id) { return Optional.ofNullable(store.get(id)); }
    @Override public void deleteById(String id) { store.remove(id); }
    @Override public PagedBooks find(BookFilter filter, int page, int size) {
        return new PagedBooks(List.copyOf(store.values()), store.size(), page, size);
    }
    @Override public long count() { return store.size(); }
    @Override public List<GenreStat> aggregateByGenre(int limit) { return List.of(); }
    @Override public Map<String, Long> countByShard() { return Map.of(); }
}
