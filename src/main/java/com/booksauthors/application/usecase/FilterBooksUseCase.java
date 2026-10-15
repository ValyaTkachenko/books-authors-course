package com.booksauthors.application.usecase;

import com.booksauthors.application.dto.BookResponse;
import com.booksauthors.application.port.in.FilterBooksQueryPort;
import com.booksauthors.domain.port.out.BookRepositoryPort;
import java.util.List;

import org.springframework.stereotype.Service;

/** Application Layer: сценарій фільтрації книг за жанром/роком (напряму через MongoDB). */
@Service
public class FilterBooksUseCase implements FilterBooksQueryPort {

    private final BookRepositoryPort bookRepositoryPort;

    public FilterBooksUseCase(BookRepositoryPort bookRepositoryPort) {
        this.bookRepositoryPort = bookRepositoryPort;
    }

    @Override
    public List<BookResponse> filter(String genre, Integer year) {
        // TODO: bookRepositoryPort.findByGenreAndYear(genre, year) -> змапити у BookResponse
        throw new UnsupportedOperationException("TODO");
    }
}
