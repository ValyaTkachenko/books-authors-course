package com.booksauthors.infrastructure.cache;

import com.booksauthors.domain.model.SearchCounters;
import com.booksauthors.domain.model.SearchLogEntry;
import com.booksauthors.domain.port.out.SearchStatsPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Лічильники та журнал останніх пошукових запитів у Redis. */
@Component
public class RedisSearchStatsAdapter implements SearchStatsPort {

    private static final Logger log = LoggerFactory.getLogger(RedisSearchStatsAdapter.class);
    private static final String TOTAL = "stats:search:total";
    private static final String HITS = "stats:search:cache_hits";
    private static final String MISSES = "stats:search:cache_misses";
    private static final String FOUND = "stats:search:found";
    private static final String EMPTY = "stats:search:empty";
    private static final String RECENT = "stats:search:recent";

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;

    public RedisSearchStatsAdapter(StringRedisTemplate redis, ObjectMapper mapper) {
        this.redis = redis;
        this.mapper = mapper;
    }

    @Override
    public void record(SearchLogEntry e) {
        try {
            redis.opsForValue().increment(TOTAL);
            redis.opsForValue().increment(e.fromCache() ? HITS : MISSES);
            redis.opsForValue().increment("FOUND".equals(e.status()) ? FOUND : EMPTY);
            redis.opsForList().leftPush(RECENT, mapper.writeValueAsString(e));
            redis.opsForList().trim(RECENT, 0, 49);
        } catch (Exception ex) {
            log.warn("Не вдалося записати статистику пошуку в Redis: {}", ex.getMessage());
        }
    }

    @Override
    public List<SearchLogEntry> recent(int limit) {
        List<SearchLogEntry> result = new ArrayList<>();
        try {
            List<String> raw = redis.opsForList().range(RECENT, 0, limit - 1L);
            if (raw != null) {
                for (String json : raw) result.add(mapper.readValue(json, SearchLogEntry.class));
            }
        } catch (Exception ex) {
            log.warn("Не вдалося прочитати журнал пошуку з Redis: {}", ex.getMessage());
        }
        return result;
    }

    @Override
    public SearchCounters counters() {
        return new SearchCounters(get(TOTAL), get(HITS), get(MISSES), get(FOUND), get(EMPTY));
    }

    private long get(String key) {
        try {
            String v = redis.opsForValue().get(key);
            return v == null ? 0 : Long.parseLong(v);
        } catch (Exception ex) {
            return 0;
        }
    }
}
