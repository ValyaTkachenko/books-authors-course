package com.booksauthors.infrastructure.ingestion;

import com.booksauthors.application.dto.IngestBookRequest;
import com.booksauthors.application.port.out.BookSourcePort;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Потокове читання CSV (датасет Goodreads «Best Books Ever») бібліотекою Apache Commons CSV.
 * Коректно обробляє багаторядкові описи в лапках та списки у форматі Python (['A', 'B']).
 * Файли можна читати лише з директорії, де лежить файл за замовчуванням.
 */
@Component
public class CsvBookSourceAdapter implements BookSourcePort {

    private static final Logger log = LoggerFactory.getLogger(CsvBookSourceAdapter.class);

    private static final Pattern PY_ITEM = Pattern.compile("'([^']*)'|\"([^\"]*)\"");
    private static final Pattern YEAR4 = Pattern.compile("\\b(1\\d{3}|20\\d{2})\\b");
    private static final Pattern DATE_SHORT = Pattern.compile("(\\d{1,2})/(\\d{1,2})/(\\d{2})\\b");

    private final Path baseDir;

    public CsvBookSourceAdapter(@Value("${app.ingest.default-path}") String defaultPath) {
        Path p = Paths.get(defaultPath).toAbsolutePath().normalize();
        this.baseDir = p.getParent() != null ? p.getParent() : p;
    }

    private Path resolve(String path) {
        Path p = Paths.get(path).toAbsolutePath().normalize();
        if (!p.startsWith(baseDir)) {
            throw new IllegalArgumentException("Файл має лежати в директорії даних: " + baseDir);
        }
        return p;
    }

    @Override
    public boolean exists(String path) {
        return Files.isRegularFile(resolve(path));
    }

    @Override
    public BookStream open(String path) {
        Path p = resolve(path);
        try {
            Reader reader = new InputStreamReader(Files.newInputStream(p), StandardCharsets.UTF_8);
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setAllowMissingColumnNames(true)
                    .setIgnoreEmptyLines(true)
                    .build();
            CSVParser parser = format.parse(reader);
            log.info("Відкрито CSV {} (колонки: {})", p, parser.getHeaderNames());
            return new CsvStream(parser);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private final class CsvStream implements BookStream {
        private final CSVParser parser;
        private final Iterator<CSVRecord> it;
        private final Map<String, Integer> columns = new HashMap<>();
        private IngestBookRequest nextRequest;

        CsvStream(CSVParser parser) {
            this.parser = parser;
            this.it = parser.iterator();
            List<String> names = parser.getHeaderNames();
            for (int i = 0; i < names.size(); i++) {
                String n = names.get(i) == null ? "" : names.get(i).replace("\uFEFF", "").trim().toLowerCase(Locale.ROOT);
                columns.putIfAbsent(n, i);
            }
        }

        @Override
        public boolean hasNext() {
            while (nextRequest == null) {
                try {
                    if (!it.hasNext()) return false;
                    nextRequest = map(it.next());
                } catch (RuntimeException e) {
                    log.warn("Пошкоджений запис CSV, зчитування файлу завершено: {}", e.getMessage());
                    return false;
                }
            }
            return true;
        }

        @Override
        public IngestBookRequest next() {
            if (!hasNext()) throw new NoSuchElementException();
            IngestBookRequest r = nextRequest;
            nextRequest = null;
            return r;
        }

        @Override
        public void close() {
            try {
                parser.close();
            } catch (IOException e) {
                log.debug("Помилка закриття CSV: {}", e.getMessage());
            }
        }

        private String field(CSVRecord rec, String... names) {
            for (String name : names) {
                Integer idx = columns.get(name);
                if (idx != null && idx < rec.size()) {
                    String v = rec.get(idx);
                    if (v != null && !v.isBlank()) return v.trim();
                }
            }
            return "";
        }

        /** Повертає null, якщо запис без назви (пропускається). */
        private IngestBookRequest map(CSVRecord rec) {
            String title = field(rec, "title", "book_title");
            if (title.isEmpty()) return null;

            String author = field(rec, "author", "book_authors", "author_name");
            int comma = author.indexOf(',');
            if (comma > 0) author = author.substring(0, comma);
            author = author.replaceAll("\\s*\\(.*?\\)\\s*", " ").trim();

            String date = field(rec, "publishdate", "publish_date", "published_year", "year");
            if (date.isEmpty()) date = field(rec, "firstpublishdate");

            return new IngestBookRequest(
                    field(rec, "bookid", "book_id"),
                    title,
                    field(rec, "description", "synopsis", "book_desc"),
                    parseList(field(rec, "genres", "genre")),
                    field(rec, "publisher", "publisher_name"),
                    extractYear(date),
                    parseList(field(rec, "awards")),
                    parseDouble(field(rec, "rating", "avg_rating")),
                    parseDouble(field(rec, "price")),
                    author,
                    field(rec, "language"),
                    parseLong(field(rec, "numratings")));
        }
    }

    // ---------- розбір значень ----------

    /** Розбирає "['A', 'B']" (формат Python) або "A, B" / "A|B" / "A;B". */
    static List<String> parseList(String raw) {
        List<String> out = new ArrayList<>();
        if (raw == null) return out;
        String s = raw.trim();
        if (s.isEmpty() || s.equals("[]")) return out;
        if (s.startsWith("[")) {
            Matcher m = PY_ITEM.matcher(s);
            while (m.find()) {
                String v = m.group(1) != null ? m.group(1) : m.group(2);
                v = v.trim();
                if (!v.isEmpty()) out.add(v);
            }
            if (!out.isEmpty()) return out;
            s = s.replaceAll("[\\[\\]]", "");
        }
        for (String part : s.split("[,|;]")) {
            String t = part.trim();
            if (!t.isEmpty() && !t.equalsIgnoreCase("null")) out.add(t);
        }
        return out;
    }

    static int extractYear(String s) {
        if (s == null || s.isBlank()) return 0;
        Matcher m = YEAR4.matcher(s);
        if (m.find()) return Integer.parseInt(m.group(1));
        Matcher m2 = DATE_SHORT.matcher(s);
        if (m2.find()) {
            int yy = Integer.parseInt(m2.group(3));
            return yy <= 30 ? 2000 + yy : 1900 + yy;
        }
        return 0;
    }

    static double parseDouble(String s) {
        if (s == null || s.isBlank()) return 0;
        try {
            return Double.parseDouble(s.replaceAll("[^0-9.,-]", "").replace(',', '.'));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    static long parseLong(String s) {
        if (s == null) return 0;
        String digits = s.replaceAll("[^0-9]", "");
        if (digits.isEmpty() || digits.length() > 18) return 0;
        return Long.parseLong(digits);
    }
}
