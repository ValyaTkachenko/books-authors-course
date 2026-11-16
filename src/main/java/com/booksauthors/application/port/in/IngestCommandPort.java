package com.booksauthors.application.port.in;

import com.booksauthors.application.dto.IngestBookRequest;
import com.booksauthors.application.dto.IngestResult;

import java.util.List;

/** Вхідний порт (command): збереження пакета книг. */
public interface IngestCommandPort {
    IngestResult ingestBatch(List<IngestBookRequest> requests);
}
