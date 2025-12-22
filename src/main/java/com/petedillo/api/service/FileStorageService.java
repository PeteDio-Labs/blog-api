package com.petedillo.api.service;

import com.petedillo.api.exception.MediaUploadException;
import com.petedillo.api.exception.ResourceNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_FILE_TYPES = Set.of(
        ".jpg", ".jpeg", ".png", ".webp", ".gif"
    );

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${media.storage.path:/app/media/images}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    /**
     * Store uploaded file with UUID prefix to prevent name conflicts
     */
    @NotNull
    public String storeFile(@NotNull MultipartFile file) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new MediaUploadException("Cannot store empty file");
        }

        // Get original filename
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new MediaUploadException("Invalid filename");
        }

        originalFilename = StringUtils.cleanPath(originalFilename);

        // Validate original filename for path traversal attacks
        if (originalFilename.contains("..")) {
            throw new MediaUploadException("Filename contains invalid path sequence: " + originalFilename);
        }

        String fileExtension = getFileExtension(originalFilename).toLowerCase();
        
        // Validate file type
        if (!ALLOWED_FILE_TYPES.contains(fileExtension)) {
            throw new MediaUploadException("File type not allowed. Supported types: jpg, jpeg, png, webp, gif");
        }

        String filename = UUID.randomUUID().toString() + "-" + System.currentTimeMillis() + fileExtension;

        // Store file
        Path targetLocation = this.fileStorageLocation.resolve(filename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // Return relative path for database storage
        return "images/" + filename;
    }

    /**
     * Load file from storage for serving to clients.
     * Validates filename to prevent path traversal attacks.
     *
     * @param filename the filename to load (e.g., "abc-123.jpg")
     * @return Path to the file
     * @throws ResourceNotFoundException if file doesn't exist
     * @throws MediaUploadException if filename is invalid
     */
    @NotNull
    public Path loadFile(@NotNull String filename) {
        // Validate filename for security
        if (filename == null || filename.isEmpty()) {
            throw new MediaUploadException("Invalid filename");
        }

        // Prevent path traversal attacks
        String cleanFilename = StringUtils.cleanPath(filename);
        if (cleanFilename.contains("..")) {
            throw new MediaUploadException("Invalid path sequence in filename");
        }

        // Resolve file path
        Path filePath = this.fileStorageLocation.resolve(cleanFilename).normalize();

        // Ensure resolved path is still within storage directory (security check)
        if (!filePath.startsWith(this.fileStorageLocation)) {
            throw new MediaUploadException("Access denied to file outside storage directory");
        }

        // Check file exists
        if (!Files.exists(filePath)) {
            throw new ResourceNotFoundException("File not found: " + filename);
        }

        return filePath;
    }

    /**
     * Delete file from storage
     */
    public boolean deleteFile(@NotNull String filename) {
        try {
            Path filePath = loadFile(filename);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return (lastDot == -1) ? "" : filename.substring(lastDot);
    }
}
