# WebScraper

Java-based web scraping application built with Spring Boot. It allows you to crawl websites, extract images, compress them, and store relevant data in a PostgreSQL database. The project is designed with modular components such as proxy handling, robots.txt parsing, and robust error handling with retry mechanisms.

---

## Table of Contents

- [Features](#features)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Endpoints](#api-endpoints)
- [Project Structure](#project-structure)
- [Building and Running](#building-and-running)
- [Docker Setup](#docker-setup)
- [Dependencies](#dependencies)
---

## Features

- **Asynchronous Scraping:** Start, monitor, and stop scraping sessions asynchronously.
- **Configurable Crawling:** Define recursion depth and request delays (from robots.txt or user-defined).
- **Proxy Support:** Utilize multiple proxies with a round-robin selector and working proxy filtering.
- **Image Processing:** Fetch and compress images using a custom JPEG compressor that dynamically adjusts quality.
- **Robust Error Handling:** Implements retries for network and HTTP errors, with non-retryable exception handling.
- **REST API:** Endpoints for starting/stopping scraping sessions and retrieving processed image data.
- **Database Integration:** Stores image details in PostgreSQL with schema management using Flyway.
- **Dockerized Deployment:** Includes Dockerfile and Docker Compose configuration for containerized setup.

---

## Getting Started

### Prerequisites

- **Java:** JDK 21 or later.
- **Build Tool:** Gradle 8.2.1 (or use the provided Gradle wrapper).
- **Database:** PostgreSQL (local instance or via Docker).
- **Docker:** (Optional) For containerized deployment.

### Installation

1. **Clone the Repository:**

   ```bash
   git clone https://github.com/yourusername/WebScraper.git
   cd WebScraper
   ```

2. **Database Setup:**

   The default configuration connects to PostgreSQL with:
   - **URL:** `jdbc:postgresql://localhost:5432/scraper`
   - **Username:** `postgres`
   - **Password:** `postgres`

   You can modify these settings in the `application.yml` file under `src/main/resources`.

3. **Configuration Adjustments:**

   Review and adjust crawler settings, image output directories, and other properties in `application.yml` as needed.

---

## Configuration

### Application Properties

Key configuration properties are found in `src/main/resources/application.yml`:

- **Crawler Settings:**
  ```yaml
  crawler:
    linkPoolSize: 10
    imagePoolSize: 10
  ```
- **Spring Datasource:**
  ```yaml
  spring:
    datasource:
      url: jdbc:postgresql://localhost:5432/scraper
      username: postgres
      password: postgres
  ```
- **Flyway Migration:**
  ```yaml
  spring:
    flyway:
      enabled: true
      locations: classpath:db/migration
      baseline-on-migrate: true
  ```

- **JPEG Compression:**
  ```yaml
  jpeg:
    compression:
      quality: 0.8
- Saved images path: WebScraper/compressed-images/**
---

## API Endpoints

The application exposes REST endpoints under `/api`:

### **Start Scraping**

- **Endpoint:** `POST /api/start`
- **Description:** Initiates a new scraping session. If requestDelay (in milliseconds) > 0 the scraper will work with a delay to avoid blockages, if requestDelay = 0 the scraper will work async. recursionDepth - max crawling depth. 
- **Request Body:**

  ```json
  {
    "url": "https://stocksnap.io/",
    "recursionDepth": 6,
    "requestDelay": 0,
    "proxies": []
  }
  ```

- **Response:** Returns a session ID confirming that the scraping has started.

### **Stop Scraping**

- **Endpoint:** `POST /api/stop`
- **Description:** Stops an ongoing scraping session.
- **Query Parameter:** `sessionId`
- **Response:** Success message if the session is stopped; error message if not found or already completed.

### **Retrieve Image Information**

- **Endpoint:** `GET /api/images`
- **Description:** Retrieves compressed image data for a given site.
- **Query Parameter:** `site`
- **Response:** A list of images with details such as original URL, local file path, original size, and compressed size.

---

## Project Structure

```
WebScraper/
├── src/main/java/com/webscraper
│   ├── config
│   │   ├── ExecutorConfig.java          // ExecutorService beans for concurrent tasks
│   │   ├── RestTemplateConfig.java      // Customized RestTemplate with connection pooling
│   │   ├── RetryConfig.java             // Retry settings configuration
│   │   └── ScraperConfig.java           // Beans related to the scraping process (e.g., LinkCrawler)
│   │
│   ├── controllers
│   │   └── ScraperController.java       // REST endpoints for scraping actions
│   │
│   ├── entities
│   │   ├── CompressionResult.java       // Record for image compression results
│   │   ├── ImageEntity.java             // JPA entity for image metadata
│   │   ├── ProxyInfo.java               // Record representing proxy server info
│   │   ├── RobotsTxtRules.java          // Record for robots.txt rules
│   │   ├── ScraperBody.java             // Request body for scraping tasks
│   │   └── ScraperSession.java          // Represents the state of a scraping session
│   │
│   ├── exceptions
│   │   ├── GlobalExceptionHandler.java  // Global exception handling for the REST API
│   │   └── NonRetryableException.java   // Custom exception for non-retryable errors
│   │
│   ├── providers
│   │   └── UserAgentProvider.java       // Provides random User-Agent strings
│   │
│   ├── repositories
│   │   └── ImageRepository.java         // Spring Data repository for ImageEntity
│   │
│   ├── services
│   │   ├── DocumentService.java         // Service interface for fetching HTML documents
│   │   ├── ImageProcessingService.java  // Interface for image processing (fetching/compression)
│   │   ├── ProxySelectorService.java    // Interface for selecting proxies
│   │   ├── RobotsTxtService.java        // Interface for retrieving robots.txt rules
│   │   ├── ScraperService.java          // Interface for managing scraping sessions
│   │   │
│   │   ├── impl
│   │   │   ├── DocumentServiceImpl.java         // Fetches documents with retry and proxy support
│   │   │   ├── ImageProcessingServiceImpl.java  // Processes images, including compression
│   │   │   ├── RobotsTxtServiceImpl.java        // Parses robots.txt files
│   │   │   ├── RoundRobinProxySelectorService.java // Selects proxies using round-robin strategy
│   │   │   └── ScraperServiceImpl.java          // Manages scraping sessions and engines
│   │   │
│   │   └── strategy
│   │       ├── DataUriImageFetchStrategy.java   // Handles images encoded as Data URIs
│   │       ├── ImageFetchStrategy.java          // Strategy interface for fetching images
│   │       ├── RegularImageFetchStrategy.java   // Fetches regular HTTP-based images
│   │       └── TemplateImageFetchStrategy.java  // Processes image URLs with template variables
│   │
│   ├── services/handlers
│   │   ├── ContentHandler.java          // Interface for processing HTML documents
│   │   ├── ImageHandler.java            // Extracts and processes images from pages
│   │   ├── LinkCrawler.java             // Functional interface for crawling a link
│   │   └── LinkHandler.java             // Extracts hyperlinks and delegates further crawling
│   │
│   ├── utils
│   │   ├── JpegCompressor.java          // Compresses JPEG images and saves files
│   │   ├── LinkExtractor.java           // Utility to extract links and image URLs from documents
│   │   ├── ProxyCheckerService.java     // Filters and validates proxy servers
│   │   ├── SSLUtil.java                 // Disables SSL verification for proxy connections
│   │   └── URLUtils.java                // Normalizes URLs and checks domain matching
│   │
│   └── engines
│       └── ScraperEngine.java           // Core engine that drives the crawling process
│
├── src/main/resources
│   ├── application.yml                  // Application configuration properties
│   └── db/migration                     // Flyway migration scripts (e.g., V1__create_image_entity.sql)
│
├── Dockerfile                           // Docker build instructions for containerizing the app
├── docker-compose.yml                   // Docker Compose configuration (app and PostgreSQL)
├── build.gradle                         // Gradle build configuration
└── README.md                            // This README file

```

---

## Building and Running

### Using Gradle

1. **Build the Project:**

   ```bash
   ./gradlew clean build
   ```

2. **Run the Application:**

   ```bash
   ./gradlew bootRun
   ```

3. **Run Tests:**

   ```bash
   ./gradlew test
   ```

---

## Docker Setup

### Building the Docker Image

Build the image using the provided Dockerfile:

```bash
docker build -t webscraper .
```

### Running with Docker Compose

Use the Docker Compose file to start both the PostgreSQL database and the WebScraper application:

```bash
docker-compose up
```

The application will be accessible at [http://localhost:8080](http://localhost:8080).

---

## Dependencies

Key dependencies used in this project:

- **Spring Boot:** For building the REST API and dependency management.
- **Spring Data JPA:** For database interactions with PostgreSQL.
- **JSoup:** For fetching and parsing HTML documents.
- **Flyway:** For managing database migrations.
- **Spring Retry:** For robust error handling with retries.
- **Guava:** For various utility functions.
- **Crawler-Commons:** For parsing and handling robots.txt.
- **TwelveMonkeys ImageIO:** To support WebP image format.
- **PostgreSQL Driver:** For connecting to the PostgreSQL database.


