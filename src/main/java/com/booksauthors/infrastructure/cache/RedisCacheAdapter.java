package com.booksauthors.infrastructure.cache;

import com.booksauthors.domain.model.SearchResult;
import com.booksauthors.domain.port.out.CachePort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/** Кеш результатів пошуку в Redis (JSON + TTL). Якщо Redis недоступний — працюємо без кешу. */
@Component
public class RedisCacheAdapter implements CachePort {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheAdapter.class);
    private static final String PREFIX = "search:";

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;

    public RedisCacheAdapter(StringRedisTemplate redis, ObjectMapper mapper) {
        this.redis = redis;
        this.mapper = mapper;
    }

    @Override
    public Optional<SearchResult> get(String normalizedKey) {
        try {
            String json = redis.opsForValue().get(PREFIX + normalizedKey);
            if (json == null) return Optional.empty();
            return Optional.of(mapper.readValue(json, SearchResult.class));
        } catch (Exception e) {
            log.warn("Redis get не вдався, працюємо без кешу: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void put(String normalizedKey, SearchResult result, Duration ttl) {
        try {
            redis.opsForValue().set(PREFIX + normalizedKey, mapper.writeValueAsString(result), ttl);
        } catch (Exception e) {
            log.warn("Redis put не вдався: {}", e.getMessage());
        }
    }
}
