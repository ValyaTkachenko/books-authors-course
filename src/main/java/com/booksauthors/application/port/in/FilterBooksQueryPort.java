package com.booksauthors.application.port.in;

import com.booksauthors.application.dto.BookResponse;
import java.util.List;

/** Вхідний порт: фільтрація книг за жанром та роком видання. */
public interface FilterBooksQueryPort {
    List<BookResponse> filter(String genre, Integer year);
}
