package com.booksauthors.application.dto;

import java.util.List;

public record SearchResponse(
        String query,
        String normalizedQuery,
        long total,
        boolean fromCache,
        long tookMs,
        List<BookResponse> results) {}
