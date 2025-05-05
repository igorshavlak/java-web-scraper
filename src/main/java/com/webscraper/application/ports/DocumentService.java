package com.webscraper.application.ports;

import com.webscraper.domain.entities.ProxyInfo;
import com.webscraper.domain.entities.ScraperSession;
import org.jsoup.nodes.Document;
import java.io.IOException;

/**
 * Service interface for fetching HTML documents.
 */
public interface DocumentService {


     Document fetchDocument(String url, ScraperSession session) throws IOException;
}
