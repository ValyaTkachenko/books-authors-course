package com.booksauthors.domain.exception;

/** Пошуковий рушій недоступний. */
public class SearchUnavailableException extends RuntimeException {
    public SearchUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
