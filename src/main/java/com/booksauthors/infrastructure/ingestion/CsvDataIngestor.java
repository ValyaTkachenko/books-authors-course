package com.booksauthors.infrastructure.ingestion;

import com.booksauthors.application.dto.IngestBookRequest;
import com.booksauthors.application.port.in.IngestCommandPort;

/**
 * Вхідний (driving) адаптер: читає CSV-файл (датасет Goodreads) із заданою швидкістю
 * та передає кожен запис у IngestCommandPort. Аналог до BookController, але для файлового джерела.
 */
public class CsvDataIngestor {

    private final IngestCommandPort ingestCommandPort;

    public CsvDataIngestor(IngestCommandPort ingestCommandPort) {
        this.ingestCommandPort = ingestCommandPort;
    }

    public void startIngesting(String csvFilePath, int recordsPerSecond) {
        // TODO: читати CSV пострічково з конфігурованою швидкістю (10..5000 записів/сек)
        // TODO: для кожного рядка -> побудувати IngestBookRequest -> ingestCommandPort.ingest(request)
        throw new UnsupportedOperationException("TODO");
    }
}
