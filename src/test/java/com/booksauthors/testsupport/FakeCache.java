package com.booksauthors.testsupport;

import com.booksauthors.domain.model.SearchResult;
import com.booksauthors.domain.port.out.CachePort;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FakeCache implements CachePort {
    public final Map<String, SearchResult> data = new HashMap<>();

    @Override public Optional<SearchResult> get(String key) { return Optional.ofNullable(data.get(key)); }
    @Override public void put(String key, SearchResult result, Duration ttl) { data.put(key, result); }
}
