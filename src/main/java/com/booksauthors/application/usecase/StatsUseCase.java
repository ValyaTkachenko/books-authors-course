package com.booksauthors.application.usecase;

import com.booksauthors.application.dto.StatsResponse;
import com.booksauthors.application.port.in.IngestionControlPort;
import com.booksauthors.application.port.in.StatsQueryPort;
import com.booksauthors.domain.model.GenreStat;
import com.booksauthors.domain.model.SearchLogEntry;
import com.booksauthors.domain.port.out.BookRepositoryPort;
import com.booksauthors.domain.port.out.SearchIndexPort;
import com.booksauthors.domain.port.out.SearchStatsPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.LongSupplier;

@Service
public class StatsUseCase implements StatsQueryPort {

    private static final Logger log = LoggerFactory.getLogger(StatsUseCase.class);

    private final BookRepositoryPort bookRepository;
    private final SearchIndexPort searchIndex;
    private final SearchStatsPort searchStats;
    private final IngestionControlPort ingestion;

    public StatsUseCase(BookRepositoryPort bookRepository,
                        SearchIndexPort searchIndex,
                        SearchStatsPort searchStats,
                        IngestionControlPort ingestion) {
        this.bookRepository = bookRepository;
        this.searchIndex = searchIndex;
        this.searchStats = searchStats;
        this.ingestion = ingestion;
    }

    @Override
    public StatsResponse stats() {
        return new StatsResponse(
                ingestion.status(),
                safe(bookRepository::count),
                safe(searchIndex::count),
                searchStats.counters(),
                System.currentTimeMillis());
    }

    @Override
    public List<GenreStat> byGenre(int limit) {
        return bookRepository.aggregateByGenre(Math.max(1, Math.min(limit, 100)));
    }

    @Override
    public Map<String, Long> shardDistribution() {
        return bookRepository.countByShard();
    }

    @Override
    public List<SearchLogEntry> recentSearches(int limit) {
        return searchStats.recent(Math.max(1, Math.min(limit, 50)));
    }

    private long safe(LongSupplier supplier) {
        try {
            return supplier.getAsLong();
        } catch (Exception e) {
            log.warn("Сховище недоступне для підрахунку: {}", e.getMessage());
            return -1;
        }
    }
}
