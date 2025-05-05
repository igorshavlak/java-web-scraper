package com.webscraper.infrastructure.db.configs;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {

    // Пулі для CrawlerWorker
    @Bean(name = "crawlerThreadPool")
    public ExecutorService crawlerThreadPool() {
        return new ThreadPoolExecutor(
                10, // corePoolSize
                50, // maximumPoolSize
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                new ThreadFactoryBuilder().setNameFormat("crawler-%d").build()
        );
    }

    // Пулі для ProcessorWorker
    @Bean(name = "processorThreadPool")
    public ExecutorService processorThreadPool() {
        return new ThreadPoolExecutor(
                5, // corePoolSize
                20, // maximumPoolSize
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(5000),
                new ThreadFactoryBuilder().setNameFormat("processor-%d").build()
        );
    }
}