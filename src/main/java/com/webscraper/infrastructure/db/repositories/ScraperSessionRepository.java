package com.webscraper.infrastructure.db.repositories;

import com.webscraper.infrastructure.db.entities.ScraperSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScraperSessionRepository extends JpaRepository<ScraperSessionEntity, String> {
    Optional<ScraperSessionEntity> findFirstByDomainAndIsCanceledFalse(String domain);
}