package com.booksauthors.application.dto;

/** Результат пакетного збереження: saved — збережено в MongoDB, indexFailed — не потрапило в Elasticsearch. */
public record IngestResult(int saved, int indexFailed) {}
