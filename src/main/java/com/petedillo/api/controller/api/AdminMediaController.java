package com.petedillo.api.controller.api;

import com.petedillo.api.dto.MediaDTO;
import com.petedillo.api.service.MediaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * REST controller for admin media management.
 * All endpoints require JWT authentication with ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/v1/admin/media")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMediaController {

    private final MediaService mediaService;

    public AdminMediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    /**
     * Upload media files for a blog post.
     *
     * @param files the files to upload
     * @param postId the blog post ID
     * @param altText optional alt text
     * @param caption optional caption
     * @return list of uploaded media
     */
    @PostMapping("/upload")
    public ResponseEntity<List<MediaDTO>> uploadMedia(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("postId") Long postId,
            @RequestParam(required = false) String altText,
            @RequestParam(required = false) String caption) {

        try {
            List<MediaDTO> uploadedMedia = new ArrayList<>();
            for (MultipartFile file : files) {
                MediaDTO media = mediaService.uploadMedia(file, postId, altText, caption);
                uploadedMedia.add(media);
            }

            return new ResponseEntity<>(uploadedMedia, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all media for a specific blog post.
     *
     * @param postId the blog post ID
     * @return list of media
     */
    @GetMapping("/posts/{postId}")
    public ResponseEntity<List<MediaDTO>> getMediaForPost(@PathVariable Long postId) {
        List<MediaDTO> media = mediaService.getMediaForPost(postId);
        return ResponseEntity.ok(media);
    }

    /**
     * Get all media across all posts (for media manager page).
     *
     * @return list of all media
     */
    @GetMapping
    public ResponseEntity<List<MediaDTO>> getAllMedia() {
        List<MediaDTO> media = mediaService.getAllMedia();
        return ResponseEntity.ok(media);
    }

    /**
     * Delete a media item.
     *
     * @param id the media ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedia(@PathVariable Long id) {
        try {
            mediaService.deleteMedia(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update media metadata (alt text and caption).
     *
     * @param id the media ID
     * @param altText new alt text
     * @param caption new caption
     * @return updated media
     */
    @PutMapping("/{id}/metadata")
    public ResponseEntity<MediaDTO> updateMetadata(
            @PathVariable Long id,
            @RequestParam(required = false) String altText,
            @RequestParam(required = false) String caption) {

        try {
            MediaDTO updated = mediaService.updateMetadata(id, altText, caption);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Reorder media items for a blog post.
     *
     * @param postId the blog post ID
     * @param mediaIds ordered list of media IDs
     * @return 204 No Content
     */
    @PutMapping("/posts/{postId}/reorder")
    public ResponseEntity<Void> reorderMedia(
            @PathVariable Long postId,
            @RequestBody List<Long> mediaIds) {

        try {
            mediaService.reorderMedia(postId, mediaIds);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
