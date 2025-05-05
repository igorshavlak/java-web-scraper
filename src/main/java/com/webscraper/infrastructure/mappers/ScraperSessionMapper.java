package com.webscraper.infrastructure.mappers;


import com.webscraper.domain.entities.ProxyInfo;
import com.webscraper.domain.entities.ScraperSession;
import com.webscraper.infrastructure.db.entities.ScraperSessionEntity;
import crawlercommons.robots.BaseRobotRules;

import java.util.List;

public class ScraperSessionMapper {

    public static ScraperSession toDomain(ScraperSessionEntity entity) {
        ScraperSession session = new ScraperSession(
                entity.getSessionId(),
                entity.getStartUrl(),
                entity.getDomain()
        );
        if (entity.getVisitedUrls() != null) {
            entity.getVisitedUrls().forEach(visitedUrl ->
                    session.getVisitedLinksUrl().add(visitedUrl.getUrl())
            );
        }

        return session;
    }
}