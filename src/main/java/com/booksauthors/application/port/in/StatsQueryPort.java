package com.booksauthors.application.port.in;

import com.booksauthors.application.dto.StatsResponse;
import com.booksauthors.domain.model.GenreStat;
import com.booksauthors.domain.model.SearchLogEntry;

import java.util.List;
import java.util.Map;

/** Вхідний порт (query): статистика та агрегати для панелі моніторингу. */
public interface StatsQueryPort {
    StatsResponse stats();

    List<GenreStat> byGenre(int limit);

    Map<String, Long> shardDistribution();

    List<SearchLogEntry> recentSearches(int limit);
}
