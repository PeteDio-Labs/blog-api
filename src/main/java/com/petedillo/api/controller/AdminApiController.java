package com.petedillo.api.controller;

import com.petedillo.api.dto.MediaDTO;
import com.petedillo.api.service.BlogPostService;
import com.petedillo.api.service.MediaService;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for admin AJAX operations (no CSRF protection needed)
 */
@RestController
@RequestMapping("/manage/api")
public class AdminApiController {

    private final MediaService mediaService;
    private final BlogPostService blogPostService;

    public AdminApiController(MediaService mediaService, BlogPostService blogPostService) {
        this.mediaService = mediaService;
        this.blogPostService = blogPostService;
    }

    // === Media Upload ===
    
    @PostMapping("/media/upload")
    public ResponseEntity<Map<String, Object>> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) @Nullable Long postId,
            @RequestParam(required = false) @Nullable String altText,
            @RequestParam(required = false) @Nullable String caption
    ) {
        try {
            if (postId == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Post ID is required");
                return ResponseEntity.badRequest().body(response);
            }

            MediaDTO mediaDTO = mediaService.uploadMedia(file, postId, altText, caption);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("media", mediaDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // === Delete Media ===
    
    @DeleteMapping("/media/{id}")
    public ResponseEntity<Map<String, Object>> deleteMedia(@PathVariable long id) {
        try {
            mediaService.deleteMedia((long) id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Media deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // === Reorder Media ===
    
    @PutMapping("/posts/{postId}/media/reorder")
    public ResponseEntity<Map<String, Object>> reorderMedia(
            @PathVariable long postId,
            @RequestBody Map<String, List<Long>> payload
    ) {
        try {
            List<Long> mediaIds = payload.get("mediaIds");
            if (mediaIds == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "mediaIds is required");
                return ResponseEntity.badRequest().body(response);
            }

            mediaService.reorderMedia(postId, mediaIds);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Media reordered successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // === Update Alt Text ===
    
    @PutMapping("/media/{id}/alt-text")
    public ResponseEntity<Map<String, Object>> updateAltText(
            @PathVariable long id,
            @RequestBody Map<String, String> payload
    ) {
        try {
            String altText = payload.get("altText");
            if (altText == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "altText is required");
                return ResponseEntity.badRequest().body(response);
            }

            MediaDTO updatedMedia = mediaService.updateMetadata((long) id, altText, null);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("media", updatedMedia);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // === Get Post Media ===
    
    @GetMapping("/posts/{postId}/media")
    public ResponseEntity<Map<String, Object>> getPostMedia(@PathVariable long postId) {
        try {
            List<MediaDTO> media = mediaService.getMediaForPost(postId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("media", media);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // === Set Cover Image ===
    
    @PostMapping("/posts/{postId}/cover-image")
    public ResponseEntity<Map<String, Object>> setCoverImage(
            @PathVariable long postId,
            @RequestBody Map<String, Long> payload
    ) {
        try {
            Long mediaId = payload.get("mediaId");
            blogPostService.setCoverImage(postId, mediaId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cover image set successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
