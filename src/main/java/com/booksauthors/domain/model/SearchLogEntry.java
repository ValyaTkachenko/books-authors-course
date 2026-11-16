package com.booksauthors.domain.model;

/** Запис в журналі пошукових запитів (status: FOUND / EMPTY). */
public record SearchLogEntry(String query, String status, long total, boolean fromCache, long tookMs, long timeMillis) {}
