package com.booksauthors.domain.port.out;

import com.booksauthors.domain.model.SearchCounters;
import com.booksauthors.domain.model.SearchLogEntry;

import java.util.List;

/** Вихідний порт: статистика пошуку для UI (реалізація — Redis-лічильники та список). */
public interface SearchStatsPort {
    void record(SearchLogEntry entry);

    List<SearchLogEntry> recent(int limit);

    SearchCounters counters();
}
