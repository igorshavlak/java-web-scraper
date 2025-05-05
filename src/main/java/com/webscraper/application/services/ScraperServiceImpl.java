package com.webscraper.application.services;

import com.google.common.util.concurrent.RateLimiter;
import com.webscraper.application.ports.RobotsTxtService;
import com.webscraper.application.workers.ScraperEngine;
import com.webscraper.domain.services.SessionService;
import com.webscraper.infrastructure.db.entities.ImageEntity;
import com.webscraper.domain.entities.ProxyInfo;
import com.webscraper.domain.entities.ScraperSession;
import com.webscraper.infrastructure.db.entities.ScraperSessionEntity;
import com.webscraper.infrastructure.db.repositories.ImageRepository;
import com.webscraper.domain.services.ScraperService;
import com.webscraper.infrastructure.mappers.ScraperSessionMapper;
import com.webscraper.infrastructure.utils.ProxyCheckerUtil;
import crawlercommons.robots.BaseRobotRules;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * Service facade for starting and managing the scraping process.
 */
@Slf4j
@Service
public class ScraperServiceImpl implements ScraperService {

    private final ExecutorService linkExecutor;
    private final RobotsTxtService robotsTxtService;
    private final ImageRepository imageRepository;
    private final ScraperEngine scraperEngine;
    private final QueueService queueService;
    private final SessionService sessionService;

    private final Map<String, ScraperSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<Void>> activeSessions = new ConcurrentHashMap<>();


    public ScraperServiceImpl(@Qualifier("linkExecutor") ExecutorService linkExecutor,
                              RobotsTxtService robotsTxtService,
                              ImageRepository imageRepository,
                              ScraperEngine scraperEngine,
                              QueueService queueService,
                              SessionService sessionService) {
        this.linkExecutor = linkExecutor;
        this.robotsTxtService = robotsTxtService;
        this.imageRepository = imageRepository;
        this.scraperEngine = scraperEngine;
        this.queueService = queueService;
        this.sessionService = sessionService;
    }

    /**
     * Starts the scraping process.
     *
     * @param url         the starting URL
     * @param maxDepth    the maximum recursion depth
     * @param userDelay   a delay (in milliseconds) between requests (if provided)
     * @param userProxies a list of proxies to use
     * @return a CompletableFuture containing the set of visited links
     * @throws URISyntaxException if the URL is invalid
     */
    @Override
    public CompletableFuture<String> startScraping(String url, int maxDepth, Long userDelay, List<ProxyInfo> userProxies) throws URISyntaxException {
        long startTime = System.currentTimeMillis();

        String domain = new URI(url).getHost();
        Optional<ScraperSessionEntity> existingSessionOpt = sessionService.findActiveSession(domain);
        BaseRobotRules rules = robotsTxtService.getRules(domain);
        userProxies = ProxyCheckerUtil.filterWorkingProxies(userProxies);
        ScraperSession session;
        if (existingSessionOpt.isPresent()) {
            ScraperSessionEntity sessionEntity = existingSessionOpt.get();
            log.info("Restoring an existing session");
            session = ScraperSessionMapper.toDomain(sessionEntity);
        } else {
            session = new ScraperSession(UUID.randomUUID().toString(), url, domain);
        }
        session.setRobotsTxtRules(rules);
        session.setUserProxies(userProxies);
        session.setMaxDepth(maxDepth);

        if (rules != null) {
            log.info("Crawl-delay (from robots.txt): {}", rules.getCrawlDelay());
        }

        String sessionId = session.getSessionId();
        sessions.put(sessionId, session);

       setRateLimiterIfNeeded(session);

        CompletableFuture<Void> crawlingFuture = scraperEngine.startCrawling(session);
        activeSessions.put(sessionId, crawlingFuture);
        queueService.addNewUrl(url, session, 0);
        
        CompletableFuture<Set<String>> resultFuture = crawlingFuture.thenApplyAsync(
                v -> Collections.unmodifiableSet(session.getVisitedLinksUrl()),
                linkExecutor
        );

        resultFuture.whenComplete((result, throwable) -> {
            activeSessions.remove(sessionId);
            log.info("Scraping completed in {} ms", System.currentTimeMillis() - startTime);
        });
        return CompletableFuture.completedFuture(sessionId);
    }

    /**
     * Stops the scraping process for the given sessionId.
     *
     * @param sessionId the id of the session to stop
     * @return true if the session was found and cancelled, false otherwise
     */
    @Override
    public boolean stopScraping(String sessionId) {
        ScraperSession session = sessions.get(sessionId);
        if (session != null) {
            session.setCanceled(true);
        }
        CompletableFuture<Void> future = activeSessions.get(sessionId);
        if (future != null && !future.isDone()) {
            boolean cancelledFuture = future.cancel(true);
            activeSessions.remove(sessionId);
            log.info("Session {} cancelled: {}", sessionId, cancelledFuture);
            return cancelledFuture;
        }
        return false;
    }


    /**
     * Retrieves image information for the given domain.
     *
     * @param site the domain to filter images by (matching the host in originalUrl)
     * @return a list of ImageEntity for the given domain
     */
    public List<ImageEntity> getImageInfoBySite(String site) {
        List<ImageEntity> allImages = imageRepository.findAll();

        return allImages.stream().filter(image -> {
            try {
                String[] path = image.getPath().split("\\\\");
                URI uri = new URI(image.getOriginalUrl());
                String domain = new URI(site).getHost();
                return uri.getHost().equals(domain) || path[1].equals(domain);
            } catch (URISyntaxException e) {
                try {
                    return site.equals(new URI(image.getOriginalUrl()).getHost());
                } catch (URISyntaxException ex) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }).collect(Collectors.toList());
    }


    private void setRateLimiterIfNeeded(ScraperSession session) {
        long delay = session.determineEffectiveDelay();
        if (delay > 0) {
            double permitsPerSecond = 1000.0 / delay;
            session.setRateLimiter(RateLimiter.create(permitsPerSecond));
            log.info("RateLimiter set with {} ms delay => permits/s: {}", delay, permitsPerSecond);
        }
    }
}
