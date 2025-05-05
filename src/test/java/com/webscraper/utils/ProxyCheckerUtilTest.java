package com.webscraper.utils;

import com.webscraper.domain.entities.ProxyInfo;
import com.webscraper.infrastructure.utils.ProxyCheckerUtil;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProxyCheckerUtilTest {

    @Test
    void testFilterWorkingProxies_NullList() {
        List<ProxyInfo> result = ProxyCheckerUtil.filterWorkingProxies(null);
        assertNull(result, "При передачі null повинен повертатися null");
    }

    @Test
    void testFilterWorkingProxies_EmptyList() {

        List<ProxyInfo> proxies = new ArrayList<>();
        List<ProxyInfo> result = ProxyCheckerUtil.filterWorkingProxies(proxies);
        assertTrue(result.isEmpty(), "При передачі порожнього списку повинен повертатися порожній список");
    }

    @Test
    void testFilterWorkingProxies_WithInvalidProxy() {

        ProxyInfo invalidProxy = new ProxyInfo("localhost", 12345);
        List<ProxyInfo> proxies = new ArrayList<>();
        proxies.add(invalidProxy);

        List<ProxyInfo> result = ProxyCheckerUtil.filterWorkingProxies(proxies);
        assertTrue(result.isEmpty(), "Недоступний проксі повинен бути відфільтрований");
    }


    @Test
    void testFilterWorkingProxies_WithValidProxy() {
        ProxyInfo validProxy = new ProxyInfo("35.72.118.126", 80);
        List<ProxyInfo> proxies = new ArrayList<>();
        proxies.add(validProxy);

        List<ProxyInfo> result = ProxyCheckerUtil.filterWorkingProxies(proxies);
        assertFalse(result.isEmpty(), "Робочий проксі не повинен бути відфільтрований");
    }

}