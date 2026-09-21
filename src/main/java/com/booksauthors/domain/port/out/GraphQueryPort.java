package com.booksauthors.domain.port.out;

import com.booksauthors.domain.model.Book;
import java.util.List;

/** Вихідний порт: зв'язки та рекомендації (реалізує Neo4jGraphAdapter). Використовується і на запис, і на читання. */
public interface GraphQueryPort {
    void linkAuthorToBook(String authorId, String bookId);
    void linkBookToAward(String bookId, String awardId);
    List<Book> findBooksByAuthor(String authorId);
    List<Book> recommendSimilarBooks(String bookId);
}
