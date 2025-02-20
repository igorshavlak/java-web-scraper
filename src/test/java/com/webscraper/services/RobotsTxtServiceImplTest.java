package com.webscraper.services;


import com.webscraper.services.impl.RobotsTxtServiceImpl;
import crawlercommons.robots.BaseRobotRules;
import crawlercommons.robots.SimpleRobotRules;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RobotsTxtServiceImplTest {

    @Test
    void testGetRulesReturnsValidRules() throws Exception {
        String domain = "example.com";
        String robotsUrl = "https://" + domain + "/robots.txt";
        String robotsContent = "User-agent: *\nDisallow: /private";
        byte[] contentBytes = robotsContent.getBytes(StandardCharsets.UTF_8);

        Connection.Response mockResponse = Mockito.mock(Connection.Response.class);
        Mockito.when(mockResponse.statusCode()).thenReturn(200);
        Mockito.when(mockResponse.body()).thenReturn(robotsContent);

        Connection mockConnection = Mockito.mock(Connection.class);
        Mockito.when(mockConnection.userAgent(Mockito.anyString())).thenReturn(mockConnection);
        Mockito.when(mockConnection.ignoreContentType(true)).thenReturn(mockConnection);
        Mockito.when(mockConnection.timeout(Mockito.anyInt())).thenReturn(mockConnection);
        Mockito.when(mockConnection.execute()).thenReturn(mockResponse);

        try (MockedStatic<Jsoup> jsoupMock = Mockito.mockStatic(Jsoup.class)) {
            jsoupMock.when(() -> Jsoup.connect(robotsUrl)).thenReturn(mockConnection);

            RobotsTxtServiceImpl service = new RobotsTxtServiceImpl();
            BaseRobotRules rules = service.getRules(domain);

            assertNotNull(rules);
            assertFalse(rules.isAllowed("https://example.com/private"));
            assertTrue(rules.isAllowed("https://example.com/public"));
        }
    }

    @Test
    void testGetRulesReturnsNullFor404() throws Exception {
        // Arrange
        String domain = "example.com";
        String robotsUrl = "https://" + domain + "/robots.txt";

        HttpStatusException exception = new HttpStatusException("Not Found", 404, robotsUrl);

        Connection mockConnection = Mockito.mock(Connection.class);
        Mockito.when(mockConnection.userAgent(Mockito.anyString())).thenReturn(mockConnection);
        Mockito.when(mockConnection.ignoreContentType(true)).thenReturn(mockConnection);
        Mockito.when(mockConnection.timeout(Mockito.anyInt())).thenReturn(mockConnection);
        Mockito.when(mockConnection.execute()).thenThrow(exception);

        try (MockedStatic<Jsoup> jsoupMock = Mockito.mockStatic(Jsoup.class)) {
            jsoupMock.when(() -> Jsoup.connect(robotsUrl)).thenReturn(mockConnection);

            RobotsTxtServiceImpl service = new RobotsTxtServiceImpl();
            BaseRobotRules rules = service.getRules(domain);

            assertNull(rules);
        }
    }

    @Test
    void testIsAllowedReturnsTrueWhenRulesNull() {
        RobotsTxtServiceImpl service = new RobotsTxtServiceImpl();
        assertTrue(service.isAllowed("https://example.com/somepage", null));
    }


}