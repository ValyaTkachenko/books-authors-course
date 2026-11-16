package com.booksauthors.presentation;

import com.booksauthors.application.dto.IngestStatus;
import com.booksauthors.application.dto.StatsResponse;
import com.booksauthors.application.port.in.IngestionControlPort;
import com.booksauthors.application.port.in.StatsQueryPort;
import com.booksauthors.domain.model.GenreStat;
import com.booksauthors.domain.model.SearchLogEntry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** Керування імпортом (старт/стоп/швидкість) та статистика для панелі моніторингу. */
@RestController
@RequestMapping("/api")
public class AdminController {

    private final IngestionControlPort ingestion;
    private final StatsQueryPort stats;

    public AdminController(IngestionControlPort ingestion, StatsQueryPort stats) {
        this.ingestion = ingestion;
        this.stats = stats;
    }

    @PostMapping("/ingest/start")
    public IngestStatus start(@RequestParam(required = false) String path) {
        return ingestion.start(path);
    }

    @PostMapping("/ingest/stop")
    public IngestStatus stop() {
        return ingestion.stop();
    }

    @PostMapping("/ingest/rate")
    public IngestStatus rate(@RequestParam int rps) {
        return ingestion.setRate(rps);
    }

    @GetMapping("/ingest/status")
    public IngestStatus status() {
        return ingestion.status();
    }

    @GetMapping("/stats")
    public StatsResponse stats() {
        return stats.stats();
    }

    @GetMapping("/stats/by-genre")
    public List<GenreStat> byGenre(@RequestParam(defaultValue = "15") int limit) {
        return stats.byGenre(limit);
    }

    @GetMapping("/stats/shards")
    public Map<String, Long> shards() {
        return stats.shardDistribution();
    }

    @GetMapping("/stats/recent-searches")
    public List<SearchLogEntry> recentSearches(@RequestParam(defaultValue = "15") int limit) {
        return stats.recentSearches(limit);
    }
}
