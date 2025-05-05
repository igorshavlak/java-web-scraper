package com.webscraper.application.services;

import com.webscraper.domain.services.SessionService;
import com.webscraper.infrastructure.db.entities.ScraperSessionEntity;
import com.webscraper.infrastructure.db.repositories.ScraperSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor

public class SessionServiceImpl implements SessionService {
    private final ScraperSessionRepository sessionRepository;
    @Override
    public Optional<ScraperSessionEntity> findActiveSession(String domain) {
        return sessionRepository.findFirstByDomainAndIsCanceledFalse(domain);
    }

    @Override
    public ScraperSessionEntity saveSession(ScraperSessionEntity session) {
        return sessionRepository.save(session);
    }

    @Override
    public void updateSessionStatus(String sessionId, boolean isCanceled) {
        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.setCanceled(isCanceled);
            sessionRepository.save(session);
        });
    }

}
