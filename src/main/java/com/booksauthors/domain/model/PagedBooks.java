package com.booksauthors.domain.model;

import java.util.List;

public record PagedBooks(List<Book> items, long total, int page, int size) {}
