package com.booksauthors.testsupport;

import com.booksauthors.domain.model.SearchCounters;
import com.booksauthors.domain.model.SearchLogEntry;
import com.booksauthors.domain.port.out.SearchStatsPort;

import java.util.ArrayList;
import java.util.List;

public class FakeSearchStats implements SearchStatsPort {
    public final List<SearchLogEntry> entries = new ArrayList<>();

    @Override public void record(SearchLogEntry entry) { entries.add(entry); }
    @Override public List<SearchLogEntry> recent(int limit) { return entries; }
    @Override public SearchCounters counters() {
        long hits = entries.stream().filter(SearchLogEntry::fromCache).count();
        long found = entries.stream().filter(e -> "FOUND".equals(e.status())).count();
        return new SearchCounters(entries.size(), hits, entries.size() - hits, found, entries.size() - found);
    }
}
