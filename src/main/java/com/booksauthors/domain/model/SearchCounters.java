package com.booksauthors.domain.model;

public record SearchCounters(long total, long cacheHits, long cacheMisses, long found, long empty) {}
