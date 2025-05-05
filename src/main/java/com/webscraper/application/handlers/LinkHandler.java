package com.webscraper.application.handlers;

import com.webscraper.domain.entities.ScraperSession;
import com.webscraper.application.services.QueueService;
import com.webscraper.infrastructure.utils.LinkExtractorUtil;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Handles processing of links found in a web page.
 * Extracts hyperlinks from the document and delegates crawling to the provided {@link LinkCrawler}.
 */
@RequiredArgsConstructor
@Component
public class LinkHandler implements ContentHandler {

    private final QueueService queueService;

    /**
     * Processes the document to extract links and initiates crawling for each extracted link.
     *
     * @param document     the JSoup Document to process
     * @param session      the current scraping session
     * @param currentDepth the current recursion depth
     * @return a CompletableFuture that completes when all link crawling tasks are finished
     */
    @Override
    public CompletableFuture<Void> process(Document document, ScraperSession session, int currentDepth) {
        Set<String> links = LinkExtractorUtil.extractLinks(document);
        for (String link : links) {
            queueService.addNewUrl(link, session,currentDepth + 1);
        }
        return CompletableFuture.completedFuture(null);
    }
}
