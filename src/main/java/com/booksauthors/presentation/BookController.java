package com.booksauthors.presentation;

import com.booksauthors.application.dto.BookResponse;
import com.booksauthors.application.dto.PagedResponse;
import com.booksauthors.application.dto.SearchResponse;
import com.booksauthors.application.port.in.FilterBooksQueryPort;
import com.booksauthors.application.port.in.SearchQueryPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Тонкий контролер: приймає HTTP-запити та делегує вхідним портам (без бізнес-логіки). */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final FilterBooksQueryPort filterPort;
    private final SearchQueryPort searchPort;

    public BookController(FilterBooksQueryPort filterPort, SearchQueryPort searchPort) {
        this.filterPort = filterPort;
        this.searchPort = searchPort;
    }

    @GetMapping
    public PagedResponse<BookResponse> filter(@RequestParam(required = false) String genre,
                                              @RequestParam(required = false) Integer year,
                                              @RequestParam(required = false) String author,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return filterPort.filter(genre, year, author, page, size);
    }

    @GetMapping("/search")
    public SearchResponse search(@RequestParam String query,
                                 @RequestParam(defaultValue = "20") int size) {
        return searchPort.search(query, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> byId(@PathVariable String id) {
        return filterPort.byId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
