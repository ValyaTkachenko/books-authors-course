package com.booksauthors.infrastructure;

import com.booksauthors.application.dto.IngestBookRequest;
import com.booksauthors.application.port.out.BookSourcePort;
import com.booksauthors.infrastructure.ingestion.CsvBookSourceAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvBookSourceAdapterTest {

    @TempDir
    Path dir;

    private List<IngestBookRequest> readAll(String csv) throws IOException {
        Path file = dir.resolve("books.csv");
        Files.writeString(file, csv, StandardCharsets.UTF_8);
        CsvBookSourceAdapter adapter = new CsvBookSourceAdapter(file.toString());
        assertTrue(adapter.exists(file.toString()));
        List<IngestBookRequest> out = new ArrayList<>();
        try (BookSourcePort.BookStream s = adapter.open(file.toString())) {
            while (s.hasNext()) out.add(s.next());
        }
        return out;
    }

    @Test
    void parsesMultilineDescriptionAndPythonLists() throws IOException {
        String csv = String.join("\n",
                "bookId,title,description,genres,author,publisher,publishDate,awards,rating,price,numRatings",
                "1-a,\"Multi, Line\",\"First line\nsecond line\",\"['Fantasy', 'Young Adult']\","
                        + "\"Jane Doe, John Roe (Illustrator)\",Pub,09/14/08,"
                        + "\"['Award (2012)', \"\"Children's Choice\"\"]\",4.5,5.9,\"1,200\"",
                "2-b,,No title row,\"['X']\",Nobody,Pub,2001,,3.0,1.0,5",
                "3-c,Second,Plain,\"['Drama']\",John Smith,Pub2,\"September 14th 2003\",\"[]\",3.8,,77");

        List<IngestBookRequest> books = readAll(csv);

        assertEquals(2, books.size(), "рядок без назви має бути пропущений");

        IngestBookRequest first = books.get(0);
        assertEquals("1-a", first.sourceId());
        assertEquals("Multi, Line", first.title());
        assertTrue(first.synopsis().contains("\n"), "багаторядковий опис не має розриватись");
        assertEquals(List.of("Fantasy", "Young Adult"), first.genres());
        assertEquals("Jane Doe", first.authorName());
        assertEquals(2008, first.publishedYear());
        assertEquals(List.of("Award (2012)", "Children's Choice"), first.awards());
        assertEquals(1200, first.numRatings());
        assertEquals(5.9, first.price());

        IngestBookRequest second = books.get(1);
        assertEquals("Second", second.title());
        assertEquals(2003, second.publishedYear());
        assertTrue(second.awards().isEmpty());
        assertEquals(0.0, second.price());
    }

    @Test
    void worksWithSampleFormatWithoutBookId() throws IOException {
        String csv = String.join("\n",
                "title,description,genres,publisher,publishDate,awards,rating,price,author",
                "\"1984\",\"Dystopia\",\"Fiction,Dystopian\",\"Secker & Warburg\",\"1949\",\"\",\"4.5\",\"14.50\",\"George Orwell\"");
        List<IngestBookRequest> books = readAll(csv);
        assertEquals(1, books.size());
        assertEquals(List.of("Fiction", "Dystopian"), books.get(0).genres());
        assertEquals(1949, books.get(0).publishedYear());
        assertEquals("", books.get(0).sourceId());
    }

    @Test
    void rejectsFilesOutsideDataDirectory() {
        CsvBookSourceAdapter adapter = new CsvBookSourceAdapter(dir.resolve("books.csv").toString());
        boolean threw = false;
        try {
            adapter.exists("/etc/passwd");
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        assertTrue(threw);
        assertFalse(Files.exists(dir.resolve("nothing.csv")));
    }
}
