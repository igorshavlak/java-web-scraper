package com.webscraper.utils;

import com.webscraper.infrastructure.utils.URLUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class URLUtilTest {


    @Test
    void testNormalizeUrl_Null() {
        assertNull(URLUtil.normalizeUrl(null), "При передачі null має повертатися null");
    }

    @Test
    void testNormalizeUrl_EmptyString() {
        assertNull(URLUtil.normalizeUrl("    "), "При передачі пустого рядка має повертатися null");
    }

    @Test
    void testNormalizeUrl_TrimAndLowerCase() {
        String input = " HTTP://EXAMPLE.COM ";
        String expected = "http://example.com";
        assertEquals(expected, URLUtil.normalizeUrl(input));
    }

    @Test
    void testNormalizeUrl_RemoveFragment() {
        String input = "http://example.com/page#section";
        String expected = "http://example.com/page";
        assertEquals(expected, URLUtil.normalizeUrl(input));
    }

    @Test
    void testNormalizeUrl_RemoveDefaultPortForHttp() {
        String input = "http://example.com:80/page";
        String expected = "http://example.com/page";
        assertEquals(expected, URLUtil.normalizeUrl(input));
    }

    @Test
    void testNormalizeUrl_RemoveDefaultPortForHttps() {
        String input = "https://example.com:443/page";
        String expected = "https://example.com/page";
        assertEquals(expected, URLUtil.normalizeUrl(input));
    }

    @Test
    void testNormalizeUrl_KeepNonDefaultPort() {
        String input = "http://example.com:8080/page";
        String expected = "http://example.com:8080/page";
        assertEquals(expected, URLUtil.normalizeUrl(input));
    }

    @Test
    void testNormalizeUrl_InvalidUrl() {
        String input = "ht@tp://invalid-url";
        assertNull(URLUtil.normalizeUrl(input), "Некоректний URL має повертати null");
    }


    @Test
    void testIsSameDomain_DirectMatch() {
        assertTrue(URLUtil.isSameDomain("http://example.com", "example.com"),
                "Домен повинен співпадати");
    }

    @Test
    void testIsSameDomain_WwwMatch() {
        assertTrue(URLUtil.isSameDomain("http://www.example.com", "example.com"),
                "Домен www.example.com має вважатися співпадаючим з example.com");
    }

    @Test
    void testIsSameDomain_SubdomainMatch() {
        assertTrue(URLUtil.isSameDomain("http://sub.example.com", "example.com"),
                "Піддомен має вважатися співпадаючим, якщо закінчується на .example.com");
    }

    @Test
    void testIsSameDomain_NonMatching() {
        assertFalse(URLUtil.isSameDomain("http://example.org", "example.com"),
                "Різні домени не повинні співпадати");
    }

    @Test
    void testIsSameDomain_InvalidUrl() {
        assertFalse(URLUtil.isSameDomain("not a url", "example.com"),
                "Некоректний URL має повертати false");
    }

    @Test
    void testIsSameDomain_CaseInsensitive() {
        assertTrue(URLUtil.isSameDomain("http://EXAMPLE.COM", "example.com"),
                "Порівняння має бути без врахування регістру");
        assertTrue(URLUtil.isSameDomain("http://WWW.EXAMPLE.COM", "example.com"),
                "Порівняння має бути без врахування регістру");
        assertTrue(URLUtil.isSameDomain("http://Sub.Example.Com", "example.com"),
                "Порівняння має бути без врахування регістру");
    }
}
