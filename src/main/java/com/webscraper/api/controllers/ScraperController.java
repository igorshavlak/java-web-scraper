package com.webscraper.api.controllers;

import com.webscraper.api.dto.ScraperBody;
import com.webscraper.domain.entities.ProxyInfo;

import com.webscraper.domain.services.ScraperService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * REST controller for starting web scraping.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ScraperController {

    private final ScraperService scraperService;

    /**
     * Starts the scraping process with the provided parameters and returns a session ID.
     *
     * @param scraperBody the body containing URL, recursion depth, delay and proxies
     * @param bindingResult validation result for the scraper body
     * @return a CompletableFuture with the response entity containing the session ID or an error message
     * @throws URISyntaxException if the provided URL is invalid
     */
    @PostMapping("/start")
    public CompletableFuture<ResponseEntity<String>> startScraping(
            @Valid @RequestBody ScraperBody scraperBody, BindingResult bindingResult) throws URISyntaxException {

        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(errors));
        }

        List<ProxyInfo> proxyInfos = scraperBody.getProxies() != null
                ? scraperBody.getProxies().stream().map(str -> {
            String[] parts = str.split(":");
            try {
                int port = Integer.parseInt(parts[1]);
                return new ProxyInfo(parts[0], port);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid proxy port in: " + str, e);
            }
        }).collect(Collectors.toList())
                : List.of();

        return scraperService.startScraping(
                        scraperBody.getUrl(),
                        scraperBody.getRecursionDepth(),
                        scraperBody.getRequestDelay(),
                        proxyInfos)
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
    public ResponseEntity<String> stopScraping(@RequestParam String sessionId) {
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
