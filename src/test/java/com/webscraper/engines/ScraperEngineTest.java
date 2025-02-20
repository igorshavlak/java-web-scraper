package com.webscraper.engines;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.webscraper.entities.ScraperSession;
import com.webscraper.services.DocumentService;
import com.webscraper.services.ProxySelectorService;
import com.webscraper.services.RobotsTxtService;
import com.webscraper.services.handlers.ContentHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class ScraperEngineTest {

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @AfterEach
    public void tearDown() {
        executor.shutdownNow();
    }

    @Test
    public void testCrawlSuccessful() throws Exception {

        DocumentService docService = mock(DocumentService.class);
        RobotsTxtService robotsService = mock(RobotsTxtService.class);
        ProxySelectorService proxySelector = mock(ProxySelectorService.class);
        ContentHandler handler = mock(ContentHandler.class);
        List<ContentHandler> handlers = Collections.singletonList(handler);

        ScraperEngine scraperEngine = new ScraperEngine(executor, docService, robotsService, proxySelector, handlers);


        ScraperSession session = new ScraperSession("http://example.com", "example.com", 2, null, null, Collections.emptyList());
        when(robotsService.isAllowed(anyString(), any())).thenReturn(true);

        Document dummyDoc = Jsoup.parse("<html><body>Test</body></html>");
        when(proxySelector.selectProxy(anyList())).thenReturn(null);
        when(docService.fetchDocument(anyString(), any())).thenReturn(dummyDoc);
        when(handler.process(dummyDoc, session, 0)).thenReturn(CompletableFuture.completedFuture(null));

        CompletableFuture<Void> future = scraperEngine.crawl("http://example.com", session, 0);
        future.join();

        verify(docService, times(1)).fetchDocument(anyString(), any());
        verify(handler, times(1)).process(dummyDoc, session, 0);
    }

    @Test
    public void testCrawlSessionCanceled() throws IOException {
        DocumentService docService = mock(DocumentService.class);
        RobotsTxtService robotsService = mock(RobotsTxtService.class);
        ProxySelectorService proxySelector = mock(ProxySelectorService.class);
        ContentHandler handler = mock(ContentHandler.class);
        List<ContentHandler> handlers = Collections.singletonList(handler);

        ScraperEngine scraperEngine = new ScraperEngine(executor, docService, robotsService, proxySelector, handlers);

        ScraperSession session = new ScraperSession("http://example.com", "example.com", 2, null, null, Collections.emptyList());
        session.setCanceled(true);

        CompletableFuture<Void> future = scraperEngine.crawl("http://example.com", session, 0);
        future.join();

        verify(docService, never()).fetchDocument(anyString(), any());
    }

    @Test
    public void testCrawlExceedsMaxDepth() throws IOException {
        DocumentService docService = mock(DocumentService.class);
        RobotsTxtService robotsService = mock(RobotsTxtService.class);
        ProxySelectorService proxySelector = mock(ProxySelectorService.class);
        ContentHandler handler = mock(ContentHandler.class);
        List<ContentHandler> handlers = Collections.singletonList(handler);

        ScraperEngine scraperEngine = new ScraperEngine(executor, docService, robotsService, proxySelector, handlers);

        ScraperSession session = new ScraperSession("http://example.com", "example.com", 1, null, null, Collections.emptyList());

        CompletableFuture<Void> future = scraperEngine.crawl("http://example.com", session, 2);
        future.join();

        verify(docService, never()).fetchDocument(anyString(), any());
    }

    @Test
    public void testCrawlDifferentDomain() throws IOException {
        DocumentService docService = mock(DocumentService.class);
        RobotsTxtService robotsService = mock(RobotsTxtService.class);
        ProxySelectorService proxySelector = mock(ProxySelectorService.class);
        ContentHandler handler = mock(ContentHandler.class);
        List<ContentHandler> handlers = Collections.singletonList(handler);

        ScraperEngine scraperEngine = new ScraperEngine(executor, docService, robotsService, proxySelector, handlers);

        ScraperSession session = new ScraperSession("http://example.com", "example.com", 2, null, null, Collections.emptyList());

        CompletableFuture<Void> future = scraperEngine.crawl("http://other.com", session, 0);
        future.join();

        verify(docService, never()).fetchDocument(anyString(), any());
    }

    @Test
    public void testCrawlDisallowedByRobots() throws IOException {
        DocumentService docService = mock(DocumentService.class);
        RobotsTxtService robotsService = mock(RobotsTxtService.class);
        ProxySelectorService proxySelector = mock(ProxySelectorService.class);
        ContentHandler handler = mock(ContentHandler.class);
        List<ContentHandler> handlers = Collections.singletonList(handler);

        ScraperEngine scraperEngine = new ScraperEngine(executor, docService, robotsService, proxySelector, handlers);

        ScraperSession session = new ScraperSession("http://example.com", "example.com", 2, null, null, Collections.emptyList());
        when(robotsService.isAllowed(anyString(), any())).thenReturn(false);

        CompletableFuture<Void> future = scraperEngine.crawl("http://example.com", session, 0);
        future.join();

        verify(docService, never()).fetchDocument(anyString(), any());
    }

    @Test
    public void testCrawlAlreadyVisited() throws IOException {
        DocumentService docService = mock(DocumentService.class);
        RobotsTxtService robotsService = mock(RobotsTxtService.class);
        ProxySelectorService proxySelector = mock(ProxySelectorService.class);
        ContentHandler handler = mock(ContentHandler.class);
        List<ContentHandler> handlers = Collections.singletonList(handler);

        ScraperEngine scraperEngine = new ScraperEngine(executor, docService, robotsService, proxySelector, handlers);

        ScraperSession session = new ScraperSession("http://example.com", "example.com", 2, null, null, Collections.emptyList());
        when(robotsService.isAllowed(anyString(), any())).thenReturn(true);

        Document dummyDoc = Jsoup.parse("<html></html>");
        when(proxySelector.selectProxy(anyList())).thenReturn(null);
        when(docService.fetchDocument(anyString(), any())).thenReturn(dummyDoc);
        when(handler.process(dummyDoc, session, 0)).thenReturn(CompletableFuture.completedFuture(null));

        CompletableFuture<Void> firstCall = scraperEngine.crawl("http://example.com", session, 0);
        firstCall.join();

        CompletableFuture<Void> secondCall = scraperEngine.crawl("http://example.com", session, 0);
        secondCall.join();

        verify(docService, times(1)).fetchDocument(anyString(), any());
    }
}