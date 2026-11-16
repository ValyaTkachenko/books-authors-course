package com.booksauthors.domain.model;

import java.util.List;

public record SearchResult(long total, List<SearchHit> hits) {}
