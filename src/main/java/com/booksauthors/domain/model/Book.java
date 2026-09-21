package com.booksauthors.domain.model;

import java.util.List;

/** Доменна сутність "Книга" (Domain Layer). Не залежить від БД чи фреймворка. */
public class Book {
    private String id;
    private String title;
    private String synopsis;
    private List<String> genres;
    private String publisherId;
    private int publishedYear;
    private double rating;
    private double price;
    private List<String> awards;

    // TODO: конструктори, гетери/сетери, доменна логіка
}
