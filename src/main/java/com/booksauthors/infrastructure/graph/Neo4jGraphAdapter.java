package com.booksauthors.infrastructure.graph;

import com.booksauthors.domain.model.Book;
import com.booksauthors.domain.port.out.GraphQueryPort;
import java.util.List;

import org.springframework.stereotype.Repository;

/** Вихідний адаптер: зв'язки та рекомендації поверх Neo4j: (Author)-[:WRITTEN]->(Book)-[:WON]->(Award). */
@Repository
public class Neo4jGraphAdapter implements GraphQueryPort {

    @Override
    public void linkAuthorToBook(String authorId, String bookId) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void linkBookToAward(String bookId, String awardId) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public List<Book> findBooksByAuthor(String authorId) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public List<Book> recommendSimilarBooks(String bookId) {
        throw new UnsupportedOperationException("TODO");
    }
}
