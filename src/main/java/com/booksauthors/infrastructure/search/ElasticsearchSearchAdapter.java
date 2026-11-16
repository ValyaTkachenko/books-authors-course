package com.booksauthors.infrastructure.search;

import com.booksauthors.domain.exception.SearchUnavailableException;
import com.booksauthors.domain.model.Book;
import com.booksauthors.domain.model.SearchHit;
import com.booksauthors.domain.model.SearchResult;
import com.booksauthors.domain.port.out.SearchIndexPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;

/** Повнотекстовий пошук через REST API Elasticsearch (інвертований індекс, підсвічування збігів). */
@Component
public class ElasticsearchSearchAdapter implements SearchIndexPort {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchSearchAdapter.class);
    private static final String INDEX = "books";
    private static final MediaType NDJSON = MediaType.parseMediaType("application/x-ndjson; charset=UTF-8");

    private static final String INDEX_DEFINITION = """
            {
              "settings": {"number_of_shards": 1, "number_of_replicas": 0},
              "mappings": {"properties": {
                "id":            {"type": "keyword"},
                "title":         {"type": "text", "analyzer": "english"},
                "authorName":    {"type": "text"},
                "synopsis":      {"type": "text", "analyzer": "english"},
                "genres":        {"type": "keyword"},
                "publisherName": {"type": "keyword"},
                "publishedYear": {"type": "integer"},
                "rating":        {"type": "float"},
                "numRatings":    {"type": "long"}
              }}
            }
            """;

    private final RestClient http;
    private final ObjectMapper mapper;
    private volatile boolean indexReady = false;

    public ElasticsearchSearchAdapter(@Value("${app.elasticsearch.url}") String url, ObjectMapper mapper) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(30000);
        this.http = RestClient.builder().baseUrl(url).requestFactory(factory).build();
        this.mapper = mapper;
    }

    private void ensureIndex() {
        if (indexReady) return;
        synchronized (this) {
            if (indexReady) return;
            try {
                http.head().uri("/{index}", INDEX).retrieve().toBodilessEntity();
            } catch (HttpClientErrorException.NotFound notFound) {
                http.put().uri("/{index}", INDEX)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(INDEX_DEFINITION)
                        .retrieve().toBodilessEntity();
                log.info("Створено індекс Elasticsearch '{}'", INDEX);
            }
            indexReady = true;
        }
    }

    @Override
    public int indexAll(List<Book> books) {
        if (books.isEmpty()) return 0;
        long t0 = System.nanoTime();
        try {
            ensureIndex();
            StringBuilder body = new StringBuilder();
            for (Book b : books) {
                body.append("{\"index\":{\"_index\":\"").append(INDEX).append("\",\"_id\":")
                        .append(mapper.writeValueAsString(b.getId())).append("}}\n")
                        .append(mapper.writeValueAsString(b)).append('\n');
            }
            JsonNode resp = http.post().uri("/_bulk")
                    .contentType(NDJSON)
                    .body(body.toString())
                    .retrieve().body(JsonNode.class);

            int failed = 0;
            if (resp != null && resp.path("errors").asBoolean(false)) {
                for (JsonNode item : resp.path("items")) {
                    JsonNode op = item.path("index");
                    if (op.has("error")) {
                        if (failed == 0) log.warn("Elasticsearch bulk error (перша): {}", op.path("error"));
                        failed++;
                    }
                }
            }
            log.debug("Elasticsearch bulk: {} документів, помилок {}, {} мс", books.size(), failed, (System.nanoTime() - t0) / 1_000_000);
            return failed;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Не вдалося серіалізувати книгу в JSON", e);
        } catch (RestClientException e) {
            throw new SearchUnavailableException("Elasticsearch недоступний: " + e.getMessage(), e);
        }
    }

    @Override
    public SearchResult search(String normalizedQuery, int size) {
        long t0 = System.nanoTime();
        try {
            ensureIndex();

            ObjectNode body = mapper.createObjectNode();
            body.put("size", size);
            body.put("track_total_hits", true);

            ObjectNode multiMatch = body.putObject("query").putObject("multi_match");
            multiMatch.put("query", normalizedQuery);
            multiMatch.put("minimum_should_match", "70%");
            multiMatch.putArray("fields").add("title^3").add("authorName^2").add("synopsis");

            ObjectNode highlight = body.putObject("highlight");
            highlight.putArray("pre_tags").add("[[h]]");
            highlight.putArray("post_tags").add("[[/h]]");
            ObjectNode fields = highlight.putObject("fields");
            ObjectNode synopsis = fields.putObject("synopsis");
            synopsis.put("fragment_size", 180);
            synopsis.put("number_of_fragments", 2);
            fields.putObject("title").put("number_of_fragments", 0);

            JsonNode resp = http.post().uri("/{index}/_search", INDEX)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(mapper.writeValueAsString(body))
                    .retrieve().body(JsonNode.class);

            long total = resp == null ? 0 : resp.path("hits").path("total").path("value").asLong(0);
            List<SearchHit> hits = new ArrayList<>();
            if (resp != null) {
                for (JsonNode h : resp.path("hits").path("hits")) {
                    Book book = mapper.treeToValue(h.path("_source"), Book.class);
                    JsonNode hl = h.path("highlight");
                    String titleHl = hl.path("title").path(0).asText(null);
                    List<String> snippets = new ArrayList<>();
                    for (JsonNode s : hl.path("synopsis")) snippets.add(s.asText());
                    hits.add(new SearchHit(book, titleHl, snippets));
                }
            }
            log.debug("Elasticsearch search '{}' -> {} (всього {}) за {} мс", normalizedQuery, hits.size(), total, (System.nanoTime() - t0) / 1_000_000);
            return new SearchResult(total, hits);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Не вдалося обробити відповідь Elasticsearch", e);
        } catch (RestClientException e) {
            throw new SearchUnavailableException("Elasticsearch недоступний: " + e.getMessage(), e);
        }
    }

    @Override
    public long count() {
        try {
            ensureIndex();
            JsonNode resp = http.get().uri("/{index}/_count", INDEX).retrieve().body(JsonNode.class);
            return resp == null ? 0 : resp.path("count").asLong(0);
        } catch (RestClientException e) {
            throw new SearchUnavailableException("Elasticsearch недоступний: " + e.getMessage(), e);
        }
    }
}

