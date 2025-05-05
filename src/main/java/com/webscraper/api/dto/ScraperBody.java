package com.webscraper.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;


/**
 * POJO representing the request body for a scraping task.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScraperBody {

    @NotBlank(message = "URL cannot be empty")
    private String url;

    @Min(value = 0, message = "The depth of recursion cannot be negative")
    private int recursionDepth;

    @Min(value = 0, message = "Request delay cannot be negative")
    private long requestDelay;

    private List<String> proxies;
}
