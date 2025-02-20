package com.webscraper.controllers;

import com.webscraper.entities.ProxyInfo;
import com.webscraper.entities.ScraperBody;
import com.webscraper.services.impl.ScraperServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


/**
 * REST controller for starting web scraping.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ScraperController {

    private final ScraperServiceImpl scraperService;

    /**
     * Starts the scraping process with the provided parameters and returns a session ID.
     *
     * @param scraperBody the body containing URL, recursion depth, delay and proxies
     * @return a CompletableFuture with the response entity containing the session ID or an error message
     * @throws URISyntaxException if the provided URL is invalid
     */
    @PostMapping("/start")
    public CompletableFuture<ResponseEntity<String>> startScraping(@RequestBody ScraperBody scraperBody) throws URISyntaxException {
        if (scraperBody == null) {
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body("No scraper body found"));
        }
        List<ProxyInfo> proxyInfos = new ArrayList<>();
        if(scraperBody.getProxies() != null && !scraperBody.getProxies().isEmpty()) {
            for(String str : scraperBody.getProxies()) {
                String[] arr = str.split(":");
                proxyInfos.add(new ProxyInfo(arr[0], Integer.parseInt(arr[1])));
            }
        }
        return scraperService.startScraping(
                        scraperBody.getUrl(),
                        scraperBody.getRecursionDepth(),
                        scraperBody.getRequestDelay(),
                        proxyInfos
                )
                .thenApply(sessionId -> ResponseEntity.ok("Scraping started. Session ID: " + sessionId))
                .exceptionally(ex -> ResponseEntity.internalServerError().body("Processing error: " + ex.getMessage()));
    }
    /**
     * Stops the scraping process for the given session ID.
     *
     * @param sessionId the id of the session to stop
     * @return a ResponseEntity with a message indicating success or failure
     */
    @PostMapping("/stop")
    public ResponseEntity<?> stopScraping(@RequestParam String sessionId) {
        boolean stopped = scraperService.stopScraping(sessionId);
        if (stopped) {
            return ResponseEntity.ok("Scraping stopped for session " + sessionId);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found or already completed");
        }
    }

    /**
     * Returns information about images for the specified site.
     *
     * @param site the domain name to retrieve images for
     * @return a ResponseEntity with image information or a message if none found
     */
    @GetMapping("/images")
    public ResponseEntity<?> getImagesInfo(@RequestParam String site) {
        var images = scraperService.getImageInfoBySite(site);
        if (images == null || images.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No images found for the site " + site);
        }
        return ResponseEntity.ok(images);
    }
}




