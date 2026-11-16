package com.booksauthors.application.dto;

public record IngestStatus(
        boolean running,
        long processed,
        long saved,
        long failed,
        long indexFailed,
        int rate,
        String path,
        long startedAt) {}
