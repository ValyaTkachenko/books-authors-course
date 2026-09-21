package com.booksauthors.application.port.in;

import com.booksauthors.application.dto.IngestBookRequest;

/** Вхідний порт: прийом потоку книг на запис (з CsvDataIngestor). */
public interface IngestCommandPort {
    void ingest(IngestBookRequest request);
}
