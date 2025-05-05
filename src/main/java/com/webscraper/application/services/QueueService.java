package com.webscraper.application.services;

import com.webscraper.domain.entities.QueueItem;
import com.webscraper.domain.entities.ScraperSession;
import lombok.Data;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
@Data
public class QueueService {

    private final BlockingQueue<QueueItem<String>> urlQueue = new LinkedBlockingQueue<>(10_000);

    private final BlockingQueue<QueueItem<Document>> rawDocumentsQueue = new LinkedBlockingQueue<>(10_000);

    public void addNewUrl(String url, ScraperSession session, int depth) {
        urlQueue.add(new QueueItem<>(url, session, depth));
    }

    public void addRawDocument(Document doc, ScraperSession session, int depth) {
        rawDocumentsQueue.add(new QueueItem<>(doc, session, depth));

    }
    public QueueItem<String> getNextUrlItem() throws InterruptedException {
        return urlQueue.take();
    }

}
