package com.webscraper.domain.entities;

public record QueueItem<T>(T data, ScraperSession session, int depth) {
}
