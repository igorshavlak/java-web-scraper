package com.webscraper.configs;

import com.webscraper.engines.ScraperEngine;
import com.webscraper.services.handlers.LinkCrawler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Configuration class for creating beans related to the web scraper.
 */
@Configuration
public class ScraperConfig {

    /**
     * Creates a LinkCrawler bean which delegates its crawling operation to the ScraperEngine.
     * @param scraperEngine the ScraperEngine instance that performs the actual crawling, injected lazily
     * @return a LinkCrawler instance that delegates crawling to the provided ScraperEngine
     */
    @Bean
    public LinkCrawler linkCrawler(@Lazy ScraperEngine scraperEngine) {
        return (url, session, currentDepth) -> scraperEngine.crawl(url, session, currentDepth);
    }
}
