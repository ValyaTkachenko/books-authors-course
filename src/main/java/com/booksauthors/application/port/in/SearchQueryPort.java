package com.booksauthors.application.port.in;

import com.booksauthors.application.dto.SearchResponse;

/** Вхідний порт (query): повнотекстовий пошук. */
public interface SearchQueryPort {
    SearchResponse search(String rawQuery, int size);
}
