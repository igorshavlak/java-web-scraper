package com.webscraper.infrastructure.http;

import com.webscraper.domain.entities.ProxyInfo;
import com.webscraper.domain.entities.ScraperSession;
import com.webscraper.infrastructure.exceptions.NonRetryableException;
import com.webscraper.application.ports.DocumentService;
import com.webscraper.infrastructure.utils.SSLUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.net.SocketException;
import java.util.List;
import java.util.Random;


@Slf4j
@Service
public class DocumentServiceImpl implements DocumentService {


    private static final List<String> USER_AGENTS = List.of(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.1 Safari/605.1.15",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36 Edg/90.0.818.66",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.140 Safari/537.36 OPR/51.0.2830.55",
            "Mozilla/5.0 (Linux; Android 10; SM-G973F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.91 Mobile Safari/537.36",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:89.0) Gecko/20100101 Firefox/89.0",
            "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0",
            "Mozilla/5.0 (X11; CrOS x86_64 14588.83.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.85 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36 OPR/77.0.4054.172"
    );
    private static final Random RANDOM = new Random();


    /**
     * Fetches the document from the given URL using the provided proxy.
     * This method is retryable in case of IO, Socket, or HTTP status exceptions.
     *
     * @param url   the URL to fetch
     * @return the fetched JSoup Document
     * @throws IOException if fetching fails after retries
     */
    @Retryable(
            value = { IOException.class, SocketException.class, HttpStatusException.class },
            maxAttempts = 2,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    @Override
    public Document fetchDocument(String url, ScraperSession session) throws IOException {
        ProxyInfo proxy = session.getNextProxy();
        return tryFetch(url, proxy);
    }

    /**
     * Attempts to fetch the document and handles specific HTTP status exceptions.
     *
     * @param url   the URL to fetch
     * @param proxy the proxy information; may be null
     * @return the fetched JSoup Document
     * @throws IOException if an error occurs during fetching
     */
    private Document tryFetch(String url, ProxyInfo proxy) throws IOException {
        try {
            Connection connection = createConnection(url, proxy);
            return connection.get();
        } catch (HttpStatusException e) {
            if (e.getStatusCode() == 404) {
                log.warn("Non-retryable HTTP 404 for URL: {}", url);
                throw new NonRetryableException("404 Not Found: " + url, e);
            }
            if(e.getStatusCode() == 400) {
                log.warn("Non-retryable HTTP 400 for URL: {}", url);
                throw new NonRetryableException("400 Bad Request: " + url, e);
            }
            if (e.getStatusCode() == 429 || e.getStatusCode() == 502) {
                log.warn("Retryable HTTP error {} for URL: {}", e.getStatusCode(), url);
                throw e;
            }
            throw new NonRetryableException("HTTP error: " + e.getStatusCode(), e);
        } catch (SocketException e) {
            log.warn("SocketException for URL {}: {}", url, e.getMessage());
            throw e;
        } catch (IOException e) {
            log.warn("IOException for URL {}: {}", url, e.getMessage());
            throw e;
        }
    }

    /**
     * Creates a JSoup connection for the given URL with the appropriate headers and proxy settings.
     *
     * @param url   the URL to connect to
     * @param proxy the proxy information; may be null
     * @return the JSoup {@link Connection} instance
     */
    private Connection createConnection(String url, ProxyInfo proxy) {
        String userAgent = getRandomUserAgent();
        Connection connection = Jsoup.connect(url)
                .userAgent(userAgent)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
                .timeout(30000);
        if (proxy != null) {
            SSLUtil.disableSslVerification();
            connection.proxy(proxy.host(), proxy.port());
        }
        return connection;
    }

    private String getRandomUserAgent() {
        int index = RANDOM.nextInt(USER_AGENTS.size());
        return USER_AGENTS.get(index);
    }
    /**
     * Recovery method for fetchDocument after all retries have been exhausted.
     *
     * @param e     the exception encountered during fetching
     * @param url   the URL that was being fetched
     * @param proxy the proxy information used
     * @return null, indicating that the document could not be fetched
     */
    @Recover
    public Document recover(IOException e, String url, ProxyInfo proxy) {
        log.error("Failed to fetch document from URL {} after retries: {}", url, e.getMessage());
        return null;
    }
}