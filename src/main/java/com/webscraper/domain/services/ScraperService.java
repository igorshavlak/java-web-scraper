package com.webscraper.domain.services;

import com.webscraper.domain.entities.ProxyInfo;
import com.webscraper.infrastructure.db.entities.ImageEntity;

import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for performing web scraping operations.
 */
public interface ScraperService {

    /**
     * Starts the web scraping process for the given URL with specified parameters.
     *
     * @param url         the starting URL for scraping
     * @param maxDepth    the maximum depth for recursive scraping
     * @param userDelay   the delay between requests in milliseconds
     * @param userProxies a list of proxies to be used during scraping
     * @return a CompletableFuture containing the scraping result as a String
     * @throws URISyntaxException if the provided URL is in an invalid format
     */
    CompletableFuture<String> startScraping(String url, int maxDepth, Long userDelay, List<ProxyInfo> userProxies) throws URISyntaxException;

    /**
     * Stops the ongoing scraping session.
     *
     * @param sessionId the unique identifier of the scraping session to stop
     * @return true if the session was successfully stopped, false otherwise
     */
    boolean stopScraping(String sessionId);

    List<ImageEntity> getImageInfoBySite(String site);
}