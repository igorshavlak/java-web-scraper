package com.webscraper.services.impl;

import com.google.common.util.concurrent.RateLimiter;
import com.webscraper.engines.ScraperEngine;
import com.webscraper.entities.ImageEntity;
import com.webscraper.entities.ProxyInfo;
import com.webscraper.entities.ScraperSession;
import com.webscraper.repositories.ImageRepository;
import com.webscraper.services.DocumentService;
import com.webscraper.services.ProxySelectorService;
import com.webscraper.services.RobotsTxtService;
import com.webscraper.services.ScraperService;
import com.webscraper.services.handlers.ContentHandler;
import com.webscraper.utils.ProxyCheckerService;
import crawlercommons.robots.BaseRobotRules;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final ProxySelectorService proxySelectorService;
    private final DocumentService documentService;
    private final List<ContentHandler> contentHandlers;
    private final ImageRepository imageRepository;

    private final Map<String, ScraperSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<Void>> activeSessions = new ConcurrentHashMap<>();


    public ScraperServiceImpl(@Qualifier("linkExecutor") ExecutorService linkExecutor,
                              RobotsTxtService robotsTxtService,
                              ProxySelectorService proxySelectorService,
                              DocumentService documentService,
                              List<ContentHandler> contentHandlers,
                              ImageRepository imageRepository) {
        this.linkExecutor = linkExecutor;
        this.robotsTxtService = robotsTxtService;
        this.proxySelectorService = proxySelectorService;
        this.documentService = documentService;
        this.contentHandlers = contentHandlers;
        this.imageRepository = imageRepository;
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
        BaseRobotRules rules = robotsTxtService.getRules(domain);
        if (rules != null) {
            log.info("Crawl-delay (from robots.txt): {}", rules.getCrawlDelay());
        }
        userProxies = ProxyCheckerService.filterWorkingProxies(userProxies);
        ScraperSession session = new ScraperSession(url, domain, maxDepth, rules, userDelay, userProxies);
        String sessionId = session.getSessionId();
        sessions.put(sessionId, session);

        long delay = determineDelay(session);
        if (delay > 0) {
            session.setRateLimiter(RateLimiter.create(delay));
            log.info("RateLimiter set with permitsPerSecond: {}", delay);
        }

        ScraperEngine engine = new ScraperEngine(linkExecutor, documentService, robotsTxtService, proxySelectorService, contentHandlers);
        CompletableFuture<Void> crawlingFuture = engine.crawl(session.getUrl(), session, 0);

        activeSessions.put(sessionId, crawlingFuture);

        CompletableFuture<Set<String>> resultFuture = crawlingFuture.thenApply(v -> Collections.unmodifiableSet(session.getVisitedLinksUrl()));

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

    /**
     * Determines the delay between requests based on robots.txt or user configuration.
     *
     * @param session the current scraper session
     * @return the delay in milliseconds
     */
    private long determineDelay(ScraperSession session) {
        if (session.getRobotsTxtRules() != null && session.getRobotsTxtRules().getCrawlDelay() > 0) {
            return session.getRobotsTxtRules().getCrawlDelay();
        } else if (session.getUserDelay() != null && session.getUserDelay() > 0) {
            return session.getUserDelay();
        }
        return 0;
    }
}
