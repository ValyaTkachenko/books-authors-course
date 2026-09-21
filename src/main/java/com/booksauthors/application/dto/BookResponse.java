package com.booksauthors.application.dto;

import java.util.List;

/** Response DTO: книга у форматі, що повертається на BookController / UI. */
public class BookResponse {
    private String id;
    private String title;
    private String synopsis;
    private List<String> genres;
    private String publisherName;
    private int publishedYear;
    private double rating;
    private List<String> authorNames;

    // TODO: конструктори, гетери/сетери
}
