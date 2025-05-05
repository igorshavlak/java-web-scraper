package com.webscraper.application.workers;

import com.webscraper.domain.entities.QueueItem;
import com.webscraper.application.services.QueueService;
import com.webscraper.application.handlers.ContentHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProcessorWorker {
    private final List<ContentHandler> contentHandlers;
    private final ExecutorService processorThreadPool;
    private final QueueService queueService;

    @PostConstruct
    public void startProcessing() {
        processorThreadPool.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    QueueItem<Document> docItem = queueService.getRawDocumentsQueue().take();
                    processItem(docItem);
                } catch (InterruptedException e) {
                    log.error("Processing thread interrupted", e);
                    Thread.currentThread().interrupt();
                }
            }
            log.info("ProcessorWorker thread shutting down.");
        });
    }

    private void processItem(QueueItem<Document> docItem) {
        if (docItem == null || docItem.data() == null || docItem.session() == null) {
            log.warn("Received invalid document item");
            return;
        }
        contentHandlers.forEach(handler ->
                handler.process(docItem.data(), docItem.session(), docItem.depth())
                        .exceptionally(ex -> {
                            log.error("Error processing content with handler: ", ex);
                            return null;
                        })
        );
    }
}