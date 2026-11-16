package com.booksauthors.domain.model;

/** Критерії фільтрації книг (будь-яке поле може бути null). genre — один або кілька жанрів через кому. */
public record BookFilter(String genre, Integer year, String author) {}
