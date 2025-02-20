package com.webscraper.repositories;

import com.webscraper.entities.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for managing ImageEntity instances.
 * Provides CRUD operations for storing, retrieving, and verifying the existence of images.
 */
@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, Long> {

    /**
     * Finds an image entity by its original URL.
     *
     * @param originalUrl the original URL of the image
     * @return an Optional containing the found ImageEntity, or empty if not found
     */
    Optional<ImageEntity> findByOriginalUrl(String originalUrl);

    /**
     * Checks if an image entity exists with the given original URL.
     *
     * @param originalUrl the original URL of the image
     * @return true if an image with the given URL exists, false otherwise
     */
    boolean existsByOriginalUrl(String originalUrl);
}