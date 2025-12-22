package com.petedillo.api.controller;

import com.petedillo.api.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("MediaController Tests")
class MediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${media.storage.path}")
    private String mediaStoragePath;

    private Path storageDir;

    @BeforeEach
    void setUp() throws Exception {
        storageDir = Paths.get(mediaStoragePath).toAbsolutePath().normalize();
        Files.createDirectories(storageDir);
    }

    @Test
    @DisplayName("GET /api/v1/media/images/{filename} - should serve existing image file")
    void serveFile_Success() throws Exception {
        // Given - create a test image file
        String filename = "test-abc123.jpg";
        Path testFile = storageDir.resolve(filename);
        byte[] imageContent = "fake image content".getBytes();
        Files.write(testFile, imageContent);

        try {
            // When/Then
            mockMvc.perform(get("/api/v1/media/images/{filename}", filename))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                    .andExpect(content().bytes(imageContent));
        } finally {
            // Cleanup
            Files.deleteIfExists(testFile);
        }
    }

    @Test
    @DisplayName("GET /api/v1/media/images/{filename} - should return 404 for non-existent file")
    void serveFile_NotFound() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/v1/media/images/{filename}", "nonexistent.jpg"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/media/images/{filename} - should include cache headers")
    void serveFile_CacheHeaders() throws Exception {
        // Given - create a test image file
        String filename = "cached-image.png";
        Path testFile = storageDir.resolve(filename);
        Files.write(testFile, "fake image content".getBytes());

        try {
            // When/Then
            mockMvc.perform(get("/api/v1/media/images/{filename}", filename))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000"));
        } finally {
            // Cleanup
            Files.deleteIfExists(testFile);
        }
    }

    @Test
    @DisplayName("GET /api/v1/media/images/{filename} - should return correct content type for PNG")
    void serveFile_ContentTypePng() throws Exception {
        // Given
        String filename = "test-image.png";
        Path testFile = storageDir.resolve(filename);
        Files.write(testFile, "fake png content".getBytes());

        try {
            // When/Then
            mockMvc.perform(get("/api/v1/media/images/{filename}", filename))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.IMAGE_PNG));
        } finally {
            Files.deleteIfExists(testFile);
        }
    }

    @Test
    @DisplayName("GET /api/v1/media/images/{filename} - should return correct content type for GIF")
    void serveFile_ContentTypeGif() throws Exception {
        // Given
        String filename = "test-image.gif";
        Path testFile = storageDir.resolve(filename);
        Files.write(testFile, "fake gif content".getBytes());

        try {
            // When/Then
            mockMvc.perform(get("/api/v1/media/images/{filename}", filename))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.IMAGE_GIF));
        } finally {
            Files.deleteIfExists(testFile);
        }
    }

    @Test
    @DisplayName("GET /api/v1/media/images/{filename} - should return correct content type for WebP")
    void serveFile_ContentTypeWebp() throws Exception {
        // Given
        String filename = "test-image.webp";
        Path testFile = storageDir.resolve(filename);
        Files.write(testFile, "fake webp content".getBytes());

        try {
            // When/Then
            mockMvc.perform(get("/api/v1/media/images/{filename}", filename))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("image/webp"));
        } finally {
            Files.deleteIfExists(testFile);
        }
    }

    @Test
    @DisplayName("GET /api/v1/media/images/{filename} - should reject path traversal attempts")
    void serveFile_PathTraversal() throws Exception {
        // When/Then - path traversal should be blocked (400 Bad Request or 404 Not Found)
        mockMvc.perform(get("/api/v1/media/images/{filename}", "../../../etc/passwd"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    // Accept 400 (Spring rejects malformed URL) or 404 (our security check)
                    if (status != 400 && status != 404) {
                        throw new AssertionError("Expected 400 or 404, but got " + status);
                    }
                });
    }

    @Test
    @DisplayName("GET /api/v1/media/images/{filename} - should handle filename with UUID pattern")
    void serveFile_UuidFilename() throws Exception {
        // Given - create file with UUID-based filename (as generated by storeFile)
        String filename = "abc12345-1234567890.jpg";
        Path testFile = storageDir.resolve(filename);
        Files.write(testFile, "uuid based image".getBytes());

        try {
            // When/Then
            mockMvc.perform(get("/api/v1/media/images/{filename}", filename))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.IMAGE_JPEG));
        } finally {
            Files.deleteIfExists(testFile);
        }
    }

    @Test
    @DisplayName("GET /api/v1/media/images/{filename} - should be publicly accessible without auth")
    void serveFile_NoAuthRequired() throws Exception {
        // Given
        String filename = "public-image.jpg";
        Path testFile = storageDir.resolve(filename);
        Files.write(testFile, "public content".getBytes());

        try {
            // When/Then - no authentication headers, should still work
            mockMvc.perform(get("/api/v1/media/images/{filename}", filename))
                    .andExpect(status().isOk());
        } finally {
            Files.deleteIfExists(testFile);
        }
    }
}
