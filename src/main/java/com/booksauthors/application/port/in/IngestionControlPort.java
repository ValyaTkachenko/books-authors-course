package com.booksauthors.application.port.in;

import com.booksauthors.application.dto.IngestStatus;

/** Вхідний порт: керування потоком зчитування CSV (старт / стоп / швидкість). */
public interface IngestionControlPort {
    IngestStatus start(String path);

    IngestStatus stop();

    IngestStatus setRate(int recordsPerSecond);

    IngestStatus status();
}
