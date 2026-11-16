package com.booksauthors.domain.model;

import java.util.ArrayList;
import java.util.List;

/** Доменна сутність «Книга». Зберігається в MongoDB (_id = id) та індексується в Elasticsearch. */
public class Book {
    private String id;
    private String title;
    private String synopsis;
    private List<String> genres = new ArrayList<>();
    private String publisherName;
    private int publishedYear;
    private double rating;
    private double price;
    private List<String> awards = new ArrayList<>();
    private String authorName;
    private String language;
    private long numRatings;

    public Book() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSynopsis() { return synopsis; }
    public void setSynopsis(String synopsis) { this.synopsis = synopsis; }
    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres != null ? genres : new ArrayList<>(); }
    public String getPublisherName() { return publisherName; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }
    public int getPublishedYear() { return publishedYear; }
    public void setPublishedYear(int publishedYear) { this.publishedYear = publishedYear; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public List<String> getAwards() { return awards; }
    public void setAwards(List<String> awards) { this.awards = awards != null ? awards : new ArrayList<>(); }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public long getNumRatings() { return numRatings; }
    public void setNumRatings(long numRatings) { this.numRatings = numRatings; }
}
