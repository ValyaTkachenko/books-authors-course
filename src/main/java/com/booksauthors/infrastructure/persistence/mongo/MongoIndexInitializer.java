package com.booksauthors.infrastructure.persistence.mongo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.stereotype.Component;

/** Створює вторинні індекси для фільтрації (жанр, рік, автор) та сортування за популярністю. */
@Component
public class MongoIndexInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MongoIndexInitializer.class);
    private final MongoTemplate mongo;

    public MongoIndexInitializer(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            IndexOperations ops = mongo.indexOps(MongoBookRepositoryAdapter.COLLECTION);
            ops.ensureIndex(new Index().on("genres", Sort.Direction.ASC));
            ops.ensureIndex(new Index().on("publishedYear", Sort.Direction.ASC));
            ops.ensureIndex(new Index().on("authorName", Sort.Direction.ASC));
            ops.ensureIndex(new Index().on("numRatings", Sort.Direction.DESC));
            log.info("Індекси MongoDB (genres, publishedYear, authorName, numRatings) готові");
        } catch (Exception e) {
            log.warn("Не вдалося створити індекси MongoDB: {}", e.getMessage());
        }
    }
}
