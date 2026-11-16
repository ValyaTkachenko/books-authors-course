package com.booksauthors.application.usecase;

import com.booksauthors.application.dto.BookResponse;
import com.booksauthors.application.dto.PagedResponse;
import com.booksauthors.application.port.in.FilterBooksQueryPort;
import com.booksauthors.domain.model.BookFilter;
import com.booksauthors.domain.model.PagedBooks;
import com.booksauthors.domain.port.out.BookRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FilterBooksUseCase implements FilterBooksQueryPort {

    private final BookRepositoryPort bookRepository;

    public FilterBooksUseCase(BookRepositoryPort bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public PagedResponse<BookResponse> filter(String genre, Integer year, String author, int page, int size) {
        int p = Math.max(0, page);
        int s = Math.max(1, Math.min(size, 100));
        PagedBooks result = bookRepository.find(new BookFilter(blankToNull(genre), year, blankToNull(author)), p, s);
        return new PagedResponse<>(
                result.items().stream().map(BookResponse::from).toList(),
                result.total(), result.page(), result.size());
    }

    @Override
    public Optional<BookResponse> byId(String id) {
        return bookRepository.findById(id).map(BookResponse::from);
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
