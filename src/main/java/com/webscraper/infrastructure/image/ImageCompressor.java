package com.webscraper.infrastructure.image;

import com.webscraper.domain.entities.CompressionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.UUID;

/**
 * Service for compressing JPEG images.
 */
@Service
@Slf4j
public class ImageCompressor {

    @Value("${jpeg.compression.quality:0.8}")
    private float initialQuality;


    private static final double SCALE_FACTOR = 0.5;
    private static final float MIN_QUALITY = 0.1f;

    /**
     * Compresses the provided image bytes and saves the result to the output directory.
     *
     * @param imageBytes      the original image bytes
     * @param outputDirectory the directory where the compressed image will be saved
     * @return a CompressionResult containing the compressed size and file link
     * @throws IOException if an I/O error occurs or the image cannot be processed
     */
    public CompressionResult compressAndSave(byte[] imageBytes, Path outputDirectory) throws IOException {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IOException("Received an empty byte array.");
        }
        log.debug("Original size: {} bytes", imageBytes.length);

        BufferedImage bufferedImage = decodeImage(imageBytes);
        if (bufferedImage == null) {
            throw new IOException("Failed to decode image");
        }

        // Конвертація в RGB
        BufferedImage rgbImage = convertToRgb(bufferedImage);

        // Масштабування
        BufferedImage scaledImage = scaleImage(rgbImage, SCALE_FACTOR);

        long targetSize = imageBytes.length / 2;
        log.debug("Target size: {} bytes", targetSize);

        // Перше стиснення
        float quality = initialQuality;
        byte[] compressedBytes = compressJpegToByteArray(scaledImage, quality);
        int iterations = 1;

        // Корекція якості
        if (compressedBytes.length > targetSize) {
            quality = Math.max((float) targetSize / compressedBytes.length * quality, MIN_QUALITY);
            compressedBytes = compressJpegToByteArray(scaledImage, quality);
            iterations++;
            log.debug("Quality adjusted to: {}", quality);
        }

        // Збереження
        String outputFileName = UUID.randomUUID() + ".jpg";
        Path outputPath = outputDirectory.resolve(outputFileName);
        Files.write(outputPath, compressedBytes);

        log.info("Compressed in {} passes. Final size: {} bytes", iterations, compressedBytes.length);
        return new CompressionResult(compressedBytes.length, outputPath.toString());
    }


    private BufferedImage convertToRgb(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_INT_RGB) return src;
        BufferedImage rgbImage = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgbImage.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return rgbImage;
    }

    private BufferedImage scaleImage(BufferedImage src, double factor) {
        int newWidth = (int) (src.getWidth() * factor);
        int newHeight = (int) (src.getHeight() * factor);
        BufferedImage scaled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, newWidth, newHeight, null);
        g.dispose();
        return scaled;
    }

    /**
     * Compresses the given image to a JPEG byte array with the specified quality.
     *
     * @param image   the image to compress
     * @param quality the JPEG quality (0.0 to 1.0)
     * @return the compressed image as a byte array
     * @throws IOException if an error occurs during compression
     */
    private byte[] compressJpegToByteArray(BufferedImage image, float quality) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        compressJpeg(image, baos, quality);
        return baos.toByteArray();
    }

    /**
     * Decodes the image from the provided byte array. Supports JPEG and attempts WebP if needed.
     *
     * @param imageBytes the raw image bytes
     * @return the decoded BufferedImage, or null if decoding fails
     * @throws IOException if an error occurs during reading
     */
    private BufferedImage decodeImage(byte[] imageBytes) throws IOException {
        BufferedImage img;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
            img = ImageIO.read(bais);
        }
        if (img != null) {
            return img;
        }
        if (isWebP(imageBytes)) {
            log.info("WebP format detected. Attempting to decode WebP image.");
            return decodeWebP(imageBytes);
        }
        return null;
    }

    /**
     * Checks if the provided bytes represent a WebP image.
     *
     * @param imageBytes the image bytes
     * @return true if the image is WebP; false otherwise
     */
    private boolean isWebP(byte[] imageBytes) {
        if (imageBytes.length < 12) return false;
        String header = new String(imageBytes, 0, 12, StandardCharsets.US_ASCII);
        return header.startsWith("RIFF") && header.substring(8, 12).equals("WEBP");
    }

    /**
     * Attempts to decode a WebP image.
     *
     * @param imageBytes the image bytes
     * @return the decoded BufferedImage, or null if decoding fails
     * @throws IOException if an error occurs during reading
     */
    private BufferedImage decodeWebP(byte[] imageBytes) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
            BufferedImage img = ImageIO.read(bais);
            if (img == null) {
                log.error("Failed to decode WebP image. Ensure the appropriate plugin is installed.");
            }
            return img;
        }
    }

    /**
     * Compresses the BufferedImage as a JPEG and writes it to the provided OutputStream.
     *
     * @param image   the image to compress
     * @param os      the output stream to write the compressed image
     * @param quality the JPEG quality (0.0 to 1.0)
     * @throws IOException if an error occurs during writing
     */
    private void compressJpeg(BufferedImage image, OutputStream os, float quality) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) throw new IllegalStateException("No JPEG writers found");

        ImageWriter writer = writers.next();
        ImageWriteParam params = writer.getDefaultWriteParam();
        params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        params.setCompressionQuality(quality);

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(os)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), params);
        } finally {
            writer.dispose();
        }
    }
}
