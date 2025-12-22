package com.petedillo.api.service;

import com.petedillo.api.exception.MediaUploadException;
import com.petedillo.api.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FileStorageService Tests")
class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService(tempDir.toString());
    }

    @Nested
    @DisplayName("loadFile() method")
    class LoadFileTests {

        @Test
        @DisplayName("should return path for existing file")
        void loadFile_Success() throws IOException {
            // Given - create a test file
            String filename = "test-image.jpg";
            Path testFile = tempDir.resolve(filename);
            Files.writeString(testFile, "test image content");

            // When
            Path result = fileStorageService.loadFile(filename);

            // Then
            assertThat(result).exists();
            assertThat(result.getFileName().toString()).isEqualTo(filename);
        }

        @Test
        @DisplayName("should throw MediaUploadException for null filename")
        void loadFile_NullFilename() {
            // When/Then
            assertThatThrownBy(() -> fileStorageService.loadFile(null))
                    .isInstanceOf(MediaUploadException.class)
                    .hasMessageContaining("Invalid filename");
        }

        @Test
        @DisplayName("should throw MediaUploadException for empty filename")
        void loadFile_EmptyFilename() {
            // When/Then
            assertThatThrownBy(() -> fileStorageService.loadFile(""))
                    .isInstanceOf(MediaUploadException.class)
                    .hasMessageContaining("Invalid filename");
        }

        @Test
        @DisplayName("should throw MediaUploadException for path traversal attempt with ..")
        void loadFile_PathTraversal() {
            // When/Then - attempt to access parent directory
            assertThatThrownBy(() -> fileStorageService.loadFile("../../../etc/passwd"))
                    .isInstanceOf(MediaUploadException.class)
                    .hasMessageContaining("Invalid path sequence");
        }

        @Test
        @DisplayName("should throw MediaUploadException for directory escape attempt")
        void loadFile_DirectoryEscape() {
            // When/Then - attempt to escape storage directory
            assertThatThrownBy(() -> fileStorageService.loadFile("../../secret.txt"))
                    .isInstanceOf(MediaUploadException.class)
                    .hasMessageContaining("Invalid path sequence");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException for non-existent file")
        void loadFile_NonExistent() {
            // When/Then
            assertThatThrownBy(() -> fileStorageService.loadFile("nonexistent.jpg"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("File not found");
        }

        @Test
        @DisplayName("should reject filenames that resolve outside storage directory")
        void loadFile_ResolvedPathOutsideStorage() throws IOException {
            // Given - create a file outside storage directory
            Path outsideFile = tempDir.getParent().resolve("outside.txt");
            Files.writeString(outsideFile, "secret content");

            // When/Then - even if normalized, path outside storage should be rejected
            // This tests the second layer of security (startsWith check)
            assertThatThrownBy(() -> fileStorageService.loadFile("../outside.txt"))
                    .isInstanceOf(MediaUploadException.class);

            // Cleanup
            Files.deleteIfExists(outsideFile);
        }
    }
}
