package com.petedillo.api.service;

import com.petedillo.api.exception.MediaUploadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        String uploadDir = tempDir.toString() + "/uploads";
        fileStorageService = new FileStorageService(uploadDir);
    }

    @Test
    void testStoreFile_ValidFile_StoresSuccessfully() throws IOException {
        // Arrange
        MultipartFile file = new MockMultipartFile(
            "test.jpg",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );

        // Act
        String storedPath = fileStorageService.storeFile(file);

        // Assert
        assertNotNull(storedPath);
        assertTrue(storedPath.startsWith("images/"));
        assertTrue(storedPath.endsWith(".jpg"));
        assertTrue(storedPath.contains("-")); // UUID separator
    }

    @Test
    void testStoreFile_EmptyFile_ThrowsException() {
        // Arrange
        MultipartFile emptyFile = new MockMultipartFile(
            "empty.jpg",
            "empty.jpg",
            "image/jpeg",
            new byte[0]
        );

        // Act & Assert
        assertThrows(MediaUploadException.class, () -> {
            fileStorageService.storeFile(emptyFile);
        });
    }

    @Test
    void testStoreFile_FileWithPathTraversal_ThrowsException() {
        // Arrange
        MultipartFile maliciousFile = new MockMultipartFile(
            "../../../etc/passwd",
            "../../../etc/passwd",
            "text/plain",
            "malicious content".getBytes()
        );

        // Act & Assert
        assertThrows(MediaUploadException.class, () -> {
            fileStorageService.storeFile(maliciousFile);
        });
    }

    @Test
    void testStoreFile_InvalidFileType_ThrowsException() {
        // Arrange
        MultipartFile txtFile = new MockMultipartFile(
            "test.txt",
            "test.txt",
            "text/plain",
            "text content".getBytes()
        );

        // Act & Assert
        MediaUploadException exception = assertThrows(MediaUploadException.class, () -> {
            fileStorageService.storeFile(txtFile);
        });
        assertTrue(exception.getMessage().contains("File type not allowed"));
    }

    @Test
    void testStoreFile_ExecutableFile_ThrowsException() {
        // Arrange
        MultipartFile exeFile = new MockMultipartFile(
            "malware.exe",
            "malware.exe",
            "application/x-msdownload",
            "malicious".getBytes()
        );

        // Act & Assert
        assertThrows(MediaUploadException.class, () -> {
            fileStorageService.storeFile(exeFile);
        });
    }

    @Test
    void testStoreFile_AllowedTypes_StoreSuccessfully() throws IOException {
        // Test all allowed file types
        String[] allowedExtensions = {".jpg", ".jpeg", ".png", ".webp", ".gif"};
        
        for (String ext : allowedExtensions) {
            MultipartFile file = new MockMultipartFile(
                "test" + ext,
                "test" + ext,
                "image/" + ext.substring(1),
                "test content".getBytes()
            );

            String storedPath = fileStorageService.storeFile(file);
            
            assertNotNull(storedPath);
            assertTrue(storedPath.toLowerCase().endsWith(ext) || storedPath.toLowerCase().endsWith(".jpeg"));
        }
    }

    @Test
    void testStoreFile_CaseInsensitiveExtension_StoreSuccessfully() throws IOException {
        // Arrange
        MultipartFile file = new MockMultipartFile(
            "test.JPG",
            "test.JPG",
            "image/jpeg",
            "test content".getBytes()
        );

        // Act
        String storedPath = fileStorageService.storeFile(file);

        // Assert
        assertNotNull(storedPath);
        assertTrue(storedPath.startsWith("images/"));
    }

    @Test
    void testLoadFile_ExistingFile_ReturnsPath() throws IOException {
        // Arrange
        MultipartFile file = new MockMultipartFile(
            "test.jpg",
            "test.jpg",
            "image/jpeg",
            "test content".getBytes()
        );
        String storedPath = fileStorageService.storeFile(file);
        String filename = storedPath.substring(storedPath.lastIndexOf("/") + 1);

        // Act
        Path loadedPath = fileStorageService.loadFile(filename);

        // Assert
        assertNotNull(loadedPath);
        assertTrue(Files.exists(loadedPath));
    }

    @Test
    void testDeleteFile_ExistingFile_DeletesSuccessfully() throws IOException {
        // Arrange
        MultipartFile file = new MockMultipartFile(
            "test.jpg",
            "test.jpg",
            "image/jpeg",
            "test content".getBytes()
        );
        String storedPath = fileStorageService.storeFile(file);
        String filename = storedPath.substring(storedPath.lastIndexOf("/") + 1);

        // Act
        boolean deleted = fileStorageService.deleteFile(filename);

        // Assert
        assertTrue(deleted);
        Path filePath = fileStorageService.loadFile(filename);
        assertFalse(Files.exists(filePath));
    }

    @Test
    void testDeleteFile_NonExistentFile_ReturnsFalse() {
        // Act
        boolean deleted = fileStorageService.deleteFile("nonexistent.jpg");

        // Assert
        assertFalse(deleted);
    }

    @Test
    void testStoreFile_PreservesFileExtension() throws IOException {
        // Arrange
        MultipartFile pngFile = new MockMultipartFile(
            "test.png",
            "test.png",
            "image/png",
            "png content".getBytes()
        );

        // Act
        String storedPath = fileStorageService.storeFile(pngFile);

        // Assert
        assertTrue(storedPath.endsWith(".png"));
    }
}
