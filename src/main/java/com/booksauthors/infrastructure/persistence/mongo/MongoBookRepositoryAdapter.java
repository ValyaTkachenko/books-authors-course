package com.booksauthors.infrastructure.persistence.mongo;

import com.booksauthors.domain.model.Book;
import com.booksauthors.domain.model.BookFilter;
import com.booksauthors.domain.model.GenreStat;
import com.booksauthors.domain.model.PagedBooks;
import com.booksauthors.domain.port.out.BookRepositoryPort;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Адаптер основного сховища. Підключення — через mongos, тому всі операції автоматично
 * маршрутизуються на шарди (ключ шардування — hashed _id).
 */
@Repository
public class MongoBookRepositoryAdapter implements BookRepositoryPort {

    static final String COLLECTION = "books";
    private static final Logger log = LoggerFactory.getLogger(MongoBookRepositoryAdapter.class);

    private final MongoTemplate mongo;

    public MongoBookRepositoryAdapter(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    @Override
    public Book save(Book book) {
        return mongo.save(book, COLLECTION);
    }

    @Override
    public void saveAll(List<Book> books) {
        if (books.isEmpty()) return;
        long t0 = System.nanoTime();
        BulkOperations ops = mongo.bulkOps(BulkOperations.BulkMode.UNORDERED, COLLECTION);
        for (Book b : books) {
            Update update = new Update()
                    .set("title", b.getTitle())
                    .set("synopsis", b.getSynopsis())
                    .set("genres", b.getGenres())
                    .set("publisherName", b.getPublisherName())
                    .set("publishedYear", b.getPublishedYear())
                    .set("rating", b.getRating())
                    .set("price", b.getPrice())
                    .set("awards", b.getAwards())
                    .set("authorName", b.getAuthorName())
                    .set("language", b.getLanguage())
                    .set("numRatings", b.getNumRatings());
            ops.upsert(Query.query(Criteria.where("_id").is(b.getId())), update);
        }
        ops.execute();
        log.debug("MongoDB bulk upsert: {} документів за {} мс", books.size(), (System.nanoTime() - t0) / 1_000_000);
    }

    @Override
    public Optional<Book> findById(String id) {
        return Optional.ofNullable(mongo.findById(id, Book.class, COLLECTION));
    }

    @Override
    public void deleteById(String id) {
        mongo.remove(Query.query(Criteria.where("_id").is(id)), COLLECTION);
    }

    @Override
    public PagedBooks find(BookFilter filter, int page, int size) {
        long t0 = System.nanoTime();
        List<Criteria> conditions = new ArrayList<>();

        if (filter.genre() != null) {
            List<Criteria> genreAny = new ArrayList<>();
            for (String g : filter.genre().split(",")) {
                String t = g.trim();
                if (!t.isEmpty()) {
                    genreAny.add(Criteria.where("genres").regex("^" + Pattern.quote(t) + "$", "i"));
                }
            }
            if (!genreAny.isEmpty()) {
                conditions.add(new Criteria().orOperator(genreAny.toArray(new Criteria[0])));
            }
        }
        if (filter.year() != null) {
            conditions.add(Criteria.where("publishedYear").is(filter.year()));
        }
        if (filter.author() != null) {
            conditions.add(Criteria.where("authorName").regex(Pattern.quote(filter.author()), "i"));
        }

        Query query = new Query();
        if (!conditions.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(conditions.toArray(new Criteria[0])));
        }
        long total = mongo.count(query, COLLECTION);
        query.with(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "numRatings")));
        List<Book> items = mongo.find(query, Book.class, COLLECTION);
        log.debug("MongoDB find {} -> {} з {} за {} мс", filter, items.size(), total, (System.nanoTime() - t0) / 1_000_000);
        return new PagedBooks(items, total, page, size);
    }

    @Override
    public long count() {
        return mongo.estimatedCount(COLLECTION);
    }

    @Override
    public List<GenreStat> aggregateByGenre(int limit) {
        long t0 = System.nanoTime();
        List<Document> pipeline = List.of(
                new Document("$unwind", "$genres"),
                new Document("$group", new Document("_id", "$genres")
                        .append("count", new Document("$sum", 1))
                        .append("avgRating", new Document("$avg", "$rating"))),
                new Document("$sort", new Document("count", -1)),
                new Document("$limit", limit));

        List<Document> docs = mongo.getCollection(COLLECTION).aggregate(pipeline).allowDiskUse(true).into(new ArrayList<>());
        List<GenreStat> result = new ArrayList<>();
        for (Document d : docs) {
            Number count = d.get("count", Number.class);
            Number avg = d.get("avgRating", Number.class);
            result.add(new GenreStat(
                    String.valueOf(d.get("_id")),
                    count == null ? 0 : count.longValue(),
                    avg == null ? 0 : Math.round(avg.doubleValue() * 100.0) / 100.0));
        }
        log.debug("MongoDB aggregate by genre: {} груп за {} мс", result.size(), (System.nanoTime() - t0) / 1_000_000);
        return result;
    }

    @Override
    public Map<String, Long> countByShard() {
        Map<String, Long> result = new LinkedHashMap<>();
        try {
            List<Document> stats = mongo.getCollection(COLLECTION)
                    .aggregate(List.of(new Document("$collStats", new Document("storageStats", new Document()))))
                    .into(new ArrayList<>());
            for (Document d : stats) {
                String shard = d.getString("shard");
                if (shard == null) shard = d.getString("host");
                Document storage = d.get("storageStats", Document.class);
                Number count = storage == null ? null : storage.get("count", Number.class);
                result.merge(shard == null ? "unknown" : shard, count == null ? 0L : count.longValue(), Long::sum);
            }
        } catch (Exception e) {
            log.warn("Не вдалося отримати розподіл по шардах: {}", e.getMessage());
        }
        return result;
    }
}
