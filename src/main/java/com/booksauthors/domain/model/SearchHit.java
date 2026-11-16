package com.booksauthors.domain.model;

import java.util.List;

/** Один результат повнотекстового пошуку: книга + фрагменти з підсвіченими збігами ([[h]]...[[/h]]). */
public record SearchHit(Book book, String titleHighlight, List<String> snippets) {}
