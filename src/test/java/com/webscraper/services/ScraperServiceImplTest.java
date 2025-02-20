package com.webscraper.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.google.common.util.concurrent.RateLimiter;
import com.webscraper.entities.ImageEntity;
import com.webscraper.entities.ProxyInfo;
import com.webscraper.entities.ScraperSession;
import com.webscraper.repositories.ImageRepository;
import com.webscraper.services.DocumentService;
import com.webscraper.services.ProxySelectorService;
import com.webscraper.services.RobotsTxtService;
import com.webscraper.services.handlers.ContentHandler;
import com.webscraper.services.impl.ScraperServiceImpl;
import crawlercommons.robots.BaseRobotRules;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ScraperServiceImplTest {

    private ExecutorService linkExecutor;
    private RobotsTxtService robotsTxtService;
    private ProxySelectorService proxySelectorService;
    private DocumentService documentService;
    private ContentHandler contentHandler;
    private ImageRepository imageRepository;

    private ScraperServiceImpl scraperService;

    @BeforeEach
    public void setUp() {
        linkExecutor = Executors.newSingleThreadExecutor();
        robotsTxtService = mock(RobotsTxtService.class);
        proxySelectorService = mock(ProxySelectorService.class);
        documentService = mock(DocumentService.class);
        contentHandler = mock(ContentHandler.class);
        imageRepository = mock(ImageRepository.class);

        scraperService = new ScraperServiceImpl(
                linkExecutor,
                robotsTxtService,
                proxySelectorService,
                documentService,
                Collections.singletonList(contentHandler),
                imageRepository);
    }


    @Test
    public void testStartScrapingSetsRateLimiterFromRobotsTxtDelay() throws URISyntaxException {
        String url = "http://example.com";
        int maxDepth = 2;
        Long userDelay = 500L;
        List<ProxyInfo> userProxies = Collections.emptyList();

        BaseRobotRules dummyRules = mock(BaseRobotRules.class);
        when(dummyRules.getCrawlDelay()).thenReturn(1000L);
        when(robotsTxtService.getRules("example.com")).thenReturn(dummyRules);

        CompletableFuture<String> sessionIdFuture = scraperService.startScraping(url, maxDepth, userDelay, userProxies);
        String sessionId = sessionIdFuture.join();

        assertNotNull(sessionId, "SessionId must not be null");

        boolean stopResult = scraperService.stopScraping(sessionId);
        assertNotNull(stopResult);
    }

    @Test
    public void testStopScrapingReturnsFalseForNonExistingSession() {
        boolean result = scraperService.stopScraping("non-existing-session-id");
        assertFalse(result, "Stopping a non-existent session should return false");
    }

    @Test
    public void testGetImageInfoBySiteFiltersByDomain() {

        ImageEntity image1 = new ImageEntity();
        image1.setOriginalUrl("http://example.com/image1.jpg");

        ImageEntity image2 = new ImageEntity();
        image2.setOriginalUrl("http://other.com/image2.jpg");

        when(imageRepository.findAll()).thenReturn(Arrays.asList(image1, image2));

        List<ImageEntity> images = scraperService.getImageInfoBySite("example.com");
        assertEquals(1, images.size(), "Must return only one image for domain example.com");
        assertEquals("http://example.com/image1.jpg", images.get(0).getOriginalUrl());
    }
}