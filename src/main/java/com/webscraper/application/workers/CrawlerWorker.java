package com.webscraper.application.workers;

import com.webscraper.domain.entities.ScraperSession;
import com.webscraper.application.ports.DocumentService;
import com.webscraper.application.services.QueueService;
import com.webscraper.application.ports.RobotsTxtService;
import com.webscraper.infrastructure.utils.URLUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Component
@Slf4j
@RequiredArgsConstructor
public class CrawlerWorker {
    private final DocumentService documentService;
    private final RobotsTxtService robotsTxtService;
    private final ExecutorService crawlerThreadPool;
    private final QueueService queueService;

    public CompletableFuture<Void> crawl(String url, ScraperSession session, int depth) {
        if (session.isCanceled() || Thread.currentThread().isInterrupted()) {
            return CompletableFuture.completedFuture(null);
        }
        String normalizedUrl = URLUtil.normalizeUrl(url);
        if (!shouldProcess(normalizedUrl, session, depth)) {
            return CompletableFuture.completedFuture(null);
        }

        log.info("Crawling URL: {} at depth {}", normalizedUrl, depth);
        return CompletableFuture.runAsync(() -> {
                    try {
                        Document doc = documentService.fetchDocument(url, session);
                        if (doc != null) {
                            queueService.addRawDocument(doc, session, depth);
                        }
                    } catch (IOException e) {
                        log.error("Crawling error: {}", e.getMessage());
                    }
                }, crawlerThreadPool)
                .exceptionally(ex -> {
                    log.error("Error processing URL: {}. Error: {}", normalizedUrl, ex.getMessage());
                    return null;
                });
    }

    private boolean shouldProcess(String url, ScraperSession session, int currentDepth) {
        if (currentDepth > session.getMaxDepth()) {
            return false;
        }
        if (!URLUtil.isSameDomain(url, session.getDomain())) {
            return false;
        }
        if (!robotsTxtService.isAllowed(url, session.getRobotsTxtRules())) {
            return false;
        }
        return session.getVisitedLinksUrl().add(url);
    }
}