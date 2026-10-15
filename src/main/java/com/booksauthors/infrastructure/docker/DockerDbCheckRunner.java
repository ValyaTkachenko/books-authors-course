package com.booksauthors.infrastructure.docker;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public class DockerDbCheckRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DockerDbCheckRunner.class);
    private static final String COLLECTION = "docker_check";

    private final MongoTemplate mongoTemplate;

    public DockerDbCheckRunner(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String... args) {
        Document doc = new Document("title", "Docker test book")
                .append("author", "Test Author")
                .append("createdAt", Instant.now().toString());

        Document saved = mongoTemplate.insert(doc, COLLECTION);
        Object id = saved.get("_id");
        log.info("[DOCKER-CHECK] Saved document to MongoDB, id={}", id);

        Document found = mongoTemplate.findById(id, Document.class, COLLECTION);
        if (found != null) {
            log.info("[DOCKER-CHECK] Read document back: {}", found.toJson());
        } else {
            log.error("[DOCKER-CHECK] Document with id={} was not found!", id);
        }
    }
}
