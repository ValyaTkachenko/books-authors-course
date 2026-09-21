package com.booksauthors.application.usecase;

import com.booksauthors.application.dto.IngestBookRequest;
import com.booksauthors.application.port.in.IngestCommandPort;
import com.booksauthors.domain.port.out.BookRepositoryPort;
import com.booksauthors.domain.port.out.SearchIndexPort;
import com.booksauthors.domain.port.out.GraphQueryPort;

/**
 * Application Layer: сценарій запису книги.
 * Command Flow: CsvDataIngestor -> IngestCommandPort -> IngestBookUseCase
 *               -> BookRepositoryPort (MongoDB) -> синхронно SearchIndexPort + GraphQueryPort.
 */
public class IngestBookUseCase implements IngestCommandPort {

    private final BookRepositoryPort bookRepositoryPort;
    private final SearchIndexPort searchIndexPort;
    private final GraphQueryPort graphQueryPort;

    public IngestBookUseCase(BookRepositoryPort bookRepositoryPort,
                              SearchIndexPort searchIndexPort,
                              GraphQueryPort graphQueryPort) {
        this.bookRepositoryPort = bookRepositoryPort;
        this.searchIndexPort = searchIndexPort;
        this.graphQueryPort = graphQueryPort;
    }

    @Override
    public void ingest(IngestBookRequest request) {
        // TODO: 1) змапити request -> Book (доменна модель)
        // TODO: 2) bookRepositoryPort.save(book) - запис у MongoDB
        // TODO: 3) searchIndexPort.index(book) - оновлення Elasticsearch
        // TODO: 4) graphQueryPort.linkAuthorToBook(...) - оновлення Neo4j
        throw new UnsupportedOperationException("TODO");
    }
}
