package com.booksauthors.application.dto;

import com.booksauthors.domain.model.SearchCounters;

/** Знімок статистики для UI. booksInDb / indexedInEs = -1, якщо відповідне сховище недоступне. */
public record StatsResponse(
        IngestStatus ingest,
        long booksInDb,
        long indexedInEs,
        SearchCounters search,
        long serverTime) {}
