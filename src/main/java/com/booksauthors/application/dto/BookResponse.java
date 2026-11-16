package com.booksauthors.application.dto;

import com.booksauthors.domain.model.Book;
import com.booksauthors.domain.model.SearchHit;

import java.util.List;

public record BookResponse(
        String id,
        String title,
        String titleHighlight,
        String authorName,
        String synopsis,
        List<String> genres,
        int publishedYear,
        double rating,
        double price,
        String publisherName,
        List<String> awards,
        long numRatings,
        List<String> snippets) {

    public static BookResponse from(Book b) {
        return new BookResponse(b.getId(), b.getTitle(), null, b.getAuthorName(), b.getSynopsis(),
                b.getGenres(), b.getPublishedYear(), b.getRating(), b.getPrice(), b.getPublisherName(),
                b.getAwards(), b.getNumRatings(), List.of());
    }

    public static BookResponse from(SearchHit h) {
        Book b = h.book();
        return new BookResponse(b.getId(), b.getTitle(), h.titleHighlight(), b.getAuthorName(), b.getSynopsis(),
                b.getGenres(), b.getPublishedYear(), b.getRating(), b.getPrice(), b.getPublisherName(),
                b.getAwards(), b.getNumRatings(), h.snippets() == null ? List.of() : h.snippets());
    }
}
