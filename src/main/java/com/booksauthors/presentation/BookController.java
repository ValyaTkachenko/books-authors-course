package com.booksauthors.presentation;

import com.booksauthors.application.dto.BookResponse;
import com.booksauthors.application.port.in.FilterBooksQueryPort;
import com.booksauthors.application.port.in.SearchQueryPort;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Presentation Layer: "тонкий" REST-контролер (Spring Web MVC).
 * Лише парсить/формує JSON, делегує сервісам, не містить бізнес-логіки.
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final SearchQueryPort searchQueryPort;
    private final FilterBooksQueryPort filterBooksQueryPort;

    public BookController(SearchQueryPort searchQueryPort, FilterBooksQueryPort filterBooksQueryPort) {
        this.searchQueryPort = searchQueryPort;
        this.filterBooksQueryPort = filterBooksQueryPort;
    }

    @GetMapping("/search")
    public List<BookResponse> search(@RequestParam String query) {
        return searchQueryPort.search(query);
    }

    @GetMapping
    public List<BookResponse> filter(@RequestParam(required = false) String genre,
                                      @RequestParam(required = false) Integer year) {
        return filterBooksQueryPort.filter(genre, year);
    }
}
