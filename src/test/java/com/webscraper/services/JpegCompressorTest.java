package com.webscraper.services;

import com.webscraper.domain.entities.CompressionResult;
import com.webscraper.infrastructure.image.ImageCompressor;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class JpegCompressorTest {

    @Test
    public void testCompressAndSave_JpgFormat() throws Exception {
        // Завантажуємо тестове зображення з ресурсів
        BufferedImage image = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/imagesForTest/test-image2.jpg")));
        assertNotNull(image, "Зображення не завантажилося.");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        byte[] imageBytes = baos.toByteArray();

        assertTrue(imageBytes.length > 200 * 1024, "Розмір зображення має бути більше 200 КБ для тесту.");

        Path tempDir = Files.createTempDirectory("jpeg-compressor-test");

        ImageCompressor compressor = new ImageCompressor();

        CompressionResult result = compressor.compressAndSave(imageBytes, tempDir);
        assertNotNull(result, "Результат стиснення не повинен бути null.");
        assertTrue(Files.exists(Path.of(result.fileLink())), "Файл повинен існувати.");
        assertTrue(result.compressedSize() <= imageBytes.length / 2, "Розмір стисненого файлу не відповідає очікуванню.");
    }

    @Test
    public void testCompressAndSave_PngFormat() throws Exception {
        BufferedImage image = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/imagesForTest/test-image.png")));
        assertNotNull(image, "Зображення не завантажилося.");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();

        assertTrue(imageBytes.length > 200 * 1024, "Розмір зображення має бути більше 200 КБ для тесту.");

        Path tempDir = Files.createTempDirectory("jpeg-compressor-test");

        ImageCompressor compressor = new ImageCompressor();

        CompressionResult result = compressor.compressAndSave(imageBytes, tempDir);
        assertNotNull(result, "Результат стиснення не повинен бути null.");
        assertTrue(Files.exists(Path.of(result.fileLink())), "Файл повинен існувати.");
        assertTrue(result.compressedSize() <= imageBytes.length / 2, "Розмір стисненого файлу не відповідає очікуванню.");
    }
}