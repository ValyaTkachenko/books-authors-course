package com.booksauthors.application.dto;

import java.util.List;

/** Один запис, прочитаний із джерела (CSV) та переданий на збереження. */
public record IngestBookRequest(
        String sourceId,
        String title,
        String synopsis,
        List<String> genres,
        String publisherName,
        int publishedYear,
        List<String> awards,
        double rating,
        double price,
        String authorName,
        String language,
        long numRatings) {}
