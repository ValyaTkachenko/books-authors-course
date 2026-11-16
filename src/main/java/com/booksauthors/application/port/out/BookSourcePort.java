package com.booksauthors.application.port.out;

import com.booksauthors.application.dto.IngestBookRequest;

import java.util.Iterator;

/** Вихідний порт застосунку: джерело даних для імпорту (реалізація — читання CSV). */
public interface BookSourcePort {
    boolean exists(String path);

    BookStream open(String path);

    interface BookStream extends Iterator<IngestBookRequest>, AutoCloseable {
        @Override
        void close();
    }
}
