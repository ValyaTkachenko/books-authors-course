package com.booksauthors.application.port.in;

import com.booksauthors.application.dto.BookResponse;
import java.util.List;

/** Вхідний порт: повнотекстовий пошук книг. */
public interface SearchQueryPort {
    List<BookResponse> search(String rawQuery);
}
