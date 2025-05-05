package com.webscraper.utils;

import com.webscraper.infrastructure.utils.LinkExtractorUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class LinkExtractorUtilTest {

    @Test
    public void testExtractLinksExcludesImageLinks() {
        String html = "<html>" +
                "  <body>" +
                "    <a href='http://example.com/page1'>Page 1</a>" +
                "    <a href='http://example.com/image.jpg'>Image Link</a>" +
                "    <a href='http://example.com/page2'>Page 2</a>" +
                "    <a href='http://example.com/photo.png'>Photo Link</a>" +
                "  </body>" +
                "</html>";

        Document doc = Jsoup.parse(html, "http://example.com");
        Set<String> links = LinkExtractorUtil.extractLinks(doc);

        assertTrue(links.contains("http://example.com/page1"));
        assertTrue(links.contains("http://example.com/page2"));
        assertFalse(links.contains("http://example.com/image.jpg"));
        assertFalse(links.contains("http://example.com/photo.png"));
        assertEquals(2, links.size());
    }

    @Test
    public void testExtractImagesFromImgTags() {
        String html = "<html>" +
                "  <body>" +
                "    <img src='/images/img1.png' />" +
                "    <img src='http://example.com/images/img2.jpg' />" +
                "  </body>" +
                "</html>";
        Document doc = Jsoup.parse(html, "http://example.com");
        Set<String> images = LinkExtractorUtil.extractImages(doc);

        assertTrue(images.contains("http://example.com/images/img1.png"));
        assertTrue(images.contains("http://example.com/images/img2.jpg"));
        assertEquals(2, images.size());
    }

    @Test
    public void testExtractCssImagesFromInlineStylesAndStyleTags() {
        String html = "<html>" +
                "  <head>" +
                "    <style>" +
                "      .bg { background-image: url('css/bg.jpg'); }" +
                "    </style>" +
                "  </head>" +
                "  <body>" +
                "    <div style=\"background: url('images/pattern.png');\"></div>" +
                "  </body>" +
                "</html>";
        Document doc = Jsoup.parse(html, "http://example.com");
        Set<String> cssImages = LinkExtractorUtil.extractCssImages(doc);

        assertTrue(cssImages.contains("http://example.com/css/bg.jpg"));
        assertTrue(cssImages.contains("http://example.com/images/pattern.png"));
        assertEquals(2, cssImages.size());
    }

    @Test
    public void testExtractAnchorImageLinks() {
        String html = "<html>" +
                "  <body>" +
                "    <a href='http://example.com/img/photo.jpeg'>Photo</a>" +
                "    <a href='http://example.com/page'>Page</a>" +
                "    <a href='http://example.com/assets/logo.gif'>Logo</a>" +
                "  </body>" +
                "</html>";
        Document doc = Jsoup.parse(html, "http://example.com");
        Set<String> anchorImages = LinkExtractorUtil.extractAnchorImageLinks(doc);

        assertTrue(anchorImages.contains("http://example.com/img/photo.jpeg"));
        assertTrue(anchorImages.contains("http://example.com/assets/logo.gif"));
        assertFalse(anchorImages.contains("http://example.com/page"));
        assertEquals(2, anchorImages.size());
    }

    @Test
    public void testExtractCssImagesHandlesInvalidUrlGracefully() {
        String html = "<html>" +
                "  <body>" +
                "    <div style=\"background-image: url('::invalid-url');\"></div>" +
                "  </body>" +
                "</html>";
        Document doc = Jsoup.parse(html, "http://example.com");
        Set<String> cssImages = LinkExtractorUtil.extractCssImages(doc);
        assertTrue(cssImages.isEmpty());
    }
}