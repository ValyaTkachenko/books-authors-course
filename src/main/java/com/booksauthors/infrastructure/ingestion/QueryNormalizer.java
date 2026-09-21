package com.booksauthors.infrastructure.ingestion;

/**
 * Нормалізує пошуковий запит перед зверненням до кешу:
 * приведення до нижнього регістру + сортування слів за алфавітом,
 * щоб "зелена енергетика" і "енергетика зелена" давали однаковий ключ кешу.
 */
public class QueryNormalizer {

    public String normalize(String rawQuery) {
        // TODO: rawQuery.toLowerCase() -> split -> sort -> join
        throw new UnsupportedOperationException("TODO");
    }
}
