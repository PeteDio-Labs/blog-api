package com.petedillo.api.controller;

import com.petedillo.api.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MediaController.class)
class MediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileStorageService fileStorageService;

    @TempDir
    Path tempDir;

    @Test
    void testServeFile_ExistingImage_Returns200() throws Exception {
        // Arrange
        Path testFile = tempDir.resolve("test.jpg");
        Files.write(testFile, "test image content".getBytes());
        
        when(fileStorageService.loadFile("test.jpg")).thenReturn(testFile);

        // Act & Assert
        mockMvc.perform(get("/api/v1/media/images/test.jpg"))
            .andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", "public, max-age=604800"))
            .andExpect(header().exists("Content-Disposition"));

        verify(fileStorageService).loadFile("test.jpg");
    }

    @Test
    void testServeFile_NonExistentFile_Returns404() throws Exception {
        // Arrange
        Path nonExistentPath = tempDir.resolve("nonexistent.jpg");
        when(fileStorageService.loadFile("nonexistent.jpg")).thenReturn(nonExistentPath);

        // Act & Assert
        mockMvc.perform(get("/api/v1/media/images/nonexistent.jpg"))
            .andExpect(status().isNotFound());

        verify(fileStorageService).loadFile("nonexistent.jpg");
    }

    @Test
    void testServeFile_PngImage_ReturnsCorrectContentType() throws Exception {
        // Arrange
        Path pngFile = tempDir.resolve("test.png");
        Files.write(pngFile, "PNG content".getBytes());
        
        when(fileStorageService.loadFile("test.png")).thenReturn(pngFile);

        // Act & Assert
        mockMvc.perform(get("/api/v1/media/images/test.png"))
            .andExpect(status().isOk())
            .andExpect(header().exists("Content-Type"));

        verify(fileStorageService).loadFile("test.png");
    }

    @Test
    void testServeFile_FileNameWithUUID_ServesCorrectly() throws Exception {
        // Arrange
        String filename = "abc123-1234567890-image.jpg";
        Path testFile = tempDir.resolve(filename);
        Files.write(testFile, "content".getBytes());
        
        when(fileStorageService.loadFile(filename)).thenReturn(testFile);

        // Act & Assert
        mockMvc.perform(get("/api/v1/media/images/" + filename))
            .andExpect(status().isOk());

        verify(fileStorageService).loadFile(filename);
    }

    @Test
    void testServeFile_HasCacheHeaders() throws Exception {
        // Arrange
        Path testFile = tempDir.resolve("cached.jpg");
        Files.write(testFile, "cached content".getBytes());
        
        when(fileStorageService.loadFile("cached.jpg")).thenReturn(testFile);

        // Act & Assert
        mockMvc.perform(get("/api/v1/media/images/cached.jpg"))
            .andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", "public, max-age=604800")); // 7 days

        verify(fileStorageService).loadFile("cached.jpg");
    }
}
