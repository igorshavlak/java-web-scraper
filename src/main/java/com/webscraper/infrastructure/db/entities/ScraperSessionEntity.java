package com.webscraper.infrastructure.db.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "scraper_sessions")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScraperSessionEntity {
    @Id
    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "start_url", nullable = false)
    private String startUrl;

    @Column(nullable = false)
    private String domain;

    @Column(name = "is_canceled")
    private boolean isCanceled = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "scraperSession", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<VisitedUrlEntity> visitedUrls = new HashSet<>();
}