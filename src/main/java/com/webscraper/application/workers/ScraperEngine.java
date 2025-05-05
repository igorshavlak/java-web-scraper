package com.webscraper.application.workers;

import com.webscraper.domain.entities.ScraperSession;
import com.webscraper.domain.entities.QueueItem;
import com.webscraper.application.services.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScraperEngine {
    private final QueueService queueService;
    private final CrawlerWorker crawlerWorker;

    public CompletableFuture<Void> startCrawling(ScraperSession session) {
        return CompletableFuture.runAsync(() -> {
            try {
                while (!session.isCanceled()) {
                    QueueItem<String> item = queueService.getNextUrlItem();
                        crawlerWorker.crawl(item.data(), item.session(), item.depth());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        });
    }

}
