package com.booksauthors.application.dto;

import java.util.List;

/** Request DTO: дані однієї книги, що надходять від CsvDataIngestor на запис. */
public class IngestBookRequest {
    private String title;
    private String synopsis;
    private List<String> genres;
    private String publisherName;
    private int publishedYear;
    private double rating;
    private double price;
    private List<String> awards;
    private String authorName;

    // TODO: конструктори, гетери/сетери
}
