package com.webscraper.domain.entities;

import com.google.common.util.concurrent.RateLimiter;
import crawlercommons.robots.BaseRobotRules;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Represents the state of a scraping session, including visited URLs, proxy settings, and crawling limits.
 */
@Getter
@Setter
public class ScraperSession {

    private final String sessionId;
    private String url;
    private String domain;

    private int maxDepth;

    private BaseRobotRules robotsTxtRules;
    private RateLimiter rateLimiter;

    private Set<String> visitedLinksUrl = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private Set<String> visitedImagesUrl = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final AtomicInteger proxyIndex = new AtomicInteger(0);

    private Long userDelay;
    private List<ProxyInfo> userProxies;

    private volatile boolean canceled;

    public ScraperSession(String id, String url, String domain) {
        this.sessionId = id;
        this.url = url;
        this.domain = domain;
    }
    public long determineEffectiveDelay() {
        if (userDelay != null && userDelay > 0) {
            return userDelay;
        } else if (robotsTxtRules != null && robotsTxtRules.getCrawlDelay() > 0) {
            return robotsTxtRules.getCrawlDelay();
        }
        return 0;
    }

    public ProxyInfo getNextProxy() {
        if (userProxies == null || userProxies.isEmpty()) {
            return null;
        }
        int pos = proxyIndex.getAndIncrement();
        return userProxies.get(pos % userProxies.size());
    }
}
