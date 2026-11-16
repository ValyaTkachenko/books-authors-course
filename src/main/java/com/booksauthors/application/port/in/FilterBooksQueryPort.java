package com.booksauthors.application.port.in;

import com.booksauthors.application.dto.BookResponse;
import com.booksauthors.application.dto.PagedResponse;

import java.util.Optional;

/** Вхідний порт (query): фільтрація та перегляд книг (жанр, рік, автор). */
public interface FilterBooksQueryPort {
    PagedResponse<BookResponse> filter(String genre, Integer year, String author, int page, int size);

    Optional<BookResponse> byId(String id);
}
