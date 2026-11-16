package com.booksauthors.application.usecase;

import com.booksauthors.application.dto.IngestBookRequest;
import com.booksauthors.application.dto.IngestResult;
import com.booksauthors.application.dto.IngestStatus;
import com.booksauthors.application.port.in.IngestCommandPort;
import com.booksauthors.application.port.in.IngestionControlPort;
import com.booksauthors.application.port.out.BookSourcePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Генератор потоку даних із файлу. Швидкість (10…5000 записів/с) змінюється «на льоту»:
 * кожні 100 мс береться rate/10 записів і зберігається одним пакетом.
 */
@Service
public class IngestionService implements IngestionControlPort {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);
    private static final int TICK_MS = 100;
    static final int MIN_RATE = 10;
    static final int MAX_RATE = 5000;

    private final BookSourcePort source;
    private final IngestCommandPort ingest;
    private final String defaultPath;

    private final AtomicInteger rate = new AtomicInteger(50);
    private final AtomicLong processed = new AtomicLong();
    private final AtomicLong saved = new AtomicLong();
    private final AtomicLong failed = new AtomicLong();
    private final AtomicLong indexFailed = new AtomicLong();
    private volatile boolean running = false;
    private volatile String currentPath;
    private volatile long startedAt;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "csv-ingest");
        t.setDaemon(true);
        return t;
    });

    public IngestionService(BookSourcePort source,
                            IngestCommandPort ingest,
                            @Value("${app.ingest.default-path}") String defaultPath) {
        this.source = source;
        this.ingest = ingest;
        this.defaultPath = defaultPath;
        this.currentPath = defaultPath;
    }

    @Override
    public synchronized IngestStatus start(String path) {
        if (running) throw new IllegalStateException("Зчитування вже запущено");
        String p = (path == null || path.isBlank()) ? defaultPath : path.trim();
        if (!source.exists(p)) {
            throw new IllegalArgumentException("CSV-файл не знайдено: " + p);
        }
        processed.set(0);
        saved.set(0);
        failed.set(0);
        indexFailed.set(0);
        currentPath = p;
        startedAt = System.currentTimeMillis();
        running = true;
        executor.submit(() -> run(p));
        return status();
    }

    @Override
    public IngestStatus stop() {
        running = false;
        log.info("Зчитування зупинено користувачем");
        return status();
    }

    @Override
    public IngestStatus setRate(int recordsPerSecond) {
        rate.set(Math.max(MIN_RATE, Math.min(MAX_RATE, recordsPerSecond)));
        log.info("Швидкість зчитування змінено на {} записів/с", rate.get());
        return status();
    }

    @Override
    public IngestStatus status() {
        return new IngestStatus(running, processed.get(), saved.get(), failed.get(), indexFailed.get(),
                rate.get(), currentPath, startedAt);
    }

    private void run(String path) {
        log.info("Початок зчитування {} зі швидкістю {} записів/с", path, rate.get());
        long lastProgressLog = System.currentTimeMillis();
        double budget = 0;
        try (BookSourcePort.BookStream stream = source.open(path)) {
            Iterator<IngestBookRequest> it = stream;
            while (running && it.hasNext()) {
                long tickStart = System.nanoTime();

                budget += rate.get() * (TICK_MS / 1000.0);
                int n = (int) budget;
                budget -= n;

                List<IngestBookRequest> batch = new ArrayList<>(n);
                while (batch.size() < n && it.hasNext()) {
                    batch.add(it.next());
                }
                if (!batch.isEmpty()) {
                    processed.addAndGet(batch.size());
                    try {
                        IngestResult r = ingest.ingestBatch(batch);
                        saved.addAndGet(r.saved());
                        indexFailed.addAndGet(r.indexFailed());
                        failed.addAndGet(batch.size() - r.saved());
                    } catch (Exception e) {
                        failed.addAndGet(batch.size());
                        log.error("Не вдалося зберегти пакет з {} записів: {}", batch.size(), e.getMessage());
                        Thread.sleep(1000);
                    }
                }

                long now = System.currentTimeMillis();
                if (now - lastProgressLog >= 5000) {
                    log.info("Прогрес: прочитано={}, збережено={}, помилок={}, не проіндексовано={}, швидкість={}/с",
                            processed.get(), saved.get(), failed.get(), indexFailed.get(), rate.get());
                    lastProgressLog = now;
                }

                long elapsedMs = (System.nanoTime() - tickStart) / 1_000_000;
                if (elapsedMs < TICK_MS) Thread.sleep(TICK_MS - elapsedMs);
            }
            log.info("Зчитування завершено. Прочитано={}, збережено={}, помилок={}, не проіндексовано={}",
                    processed.get(), saved.get(), failed.get(), indexFailed.get());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Помилка під час зчитування {}", path, e);
        } finally {
            running = false;
        }
    }
}
