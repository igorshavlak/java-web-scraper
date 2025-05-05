package com.webscraper.infrastructure.db.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Configuration class for creating ExecutorService beans used for concurrent processing.
 */
@Configuration
public class ExecutorConfig {

    /**
     * Creates a fixed thread pool ExecutorService for handling link crawling tasks.
     *
     * @param poolSize the number of threads in the pool, injected from the property {@code crawler.linkPoolSize}
     * @return a fixed thread pool ExecutorService for links
     */
    @Bean
    public ExecutorService linkExecutor(@Value("${crawler.linkPoolSize}") int poolSize) {
        return Executors.newFixedThreadPool(poolSize);
    }

    /**
     * Creates a fixed thread pool ExecutorService for handling image crawling tasks.
     *
     * @param poolSize the number of threads in the pool, injected from the property {@code crawler.imagePoolSize}
     * @return a fixed thread pool ExecutorService for images
     */
    @Bean
    public ExecutorService imageExecutor(@Value("${crawler.imagePoolSize}") int poolSize) {
        return Executors.newFixedThreadPool(poolSize);
    }
}
