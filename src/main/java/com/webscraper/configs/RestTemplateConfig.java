package com.webscraper.configs;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for creating a customized RestTemplate bean.
 * <p>
 * This configuration sets up a connection pool and custom timeouts for HTTP requests.
 * </p>
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Creates a RestTemplate bean with a custom HTTP client configuration.
     *
     * @param builder the RestTemplateBuilder to help create the RestTemplate
     * @return a RestTemplate instance with a connection pool and timeouts configured
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100);
        connectionManager.setDefaultMaxPerRoute(20);

        HttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(5000);

        return builder
                .requestFactory(() -> requestFactory)
                .build();
    }
}
