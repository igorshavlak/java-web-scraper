package com.webscraper.domain.services;


import com.webscraper.infrastructure.db.entities.ScraperSessionEntity;

import java.util.Optional;

public interface SessionService {
    Optional<ScraperSessionEntity> findActiveSession(String domain);
    ScraperSessionEntity saveSession(ScraperSessionEntity session);
    void updateSessionStatus(String sessionId, boolean isCanceled);
}
