package com.webscraper.services;

import com.webscraper.domain.entities.ProxyInfo;
import com.webscraper.services.impl.RoundRobinProxySelectorService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoundRobinProxySelectorServiceTest {

    @Test
    void testSelectProxyReturnsNullForEmptyList() {
        RoundRobinProxySelectorService service = new RoundRobinProxySelectorService();
        assertNull(service.selectProxy(null));
        assertNull(service.selectProxy(List.of()));
    }

    @Test
    void testSelectProxyRoundRobin() {
        RoundRobinProxySelectorService service = new RoundRobinProxySelectorService();
        ProxyInfo proxy1 = new ProxyInfo("proxy1.example.com", 8080);
        ProxyInfo proxy2 = new ProxyInfo("proxy2.example.com", 8081);
        List<ProxyInfo> proxies = List.of(proxy1, proxy2);

        assertEquals(proxy1, service.selectProxy(proxies));
        assertEquals(proxy2, service.selectProxy(proxies));
        assertEquals(proxy1, service.selectProxy(proxies));
        assertEquals(proxy2, service.selectProxy(proxies));
    }
}