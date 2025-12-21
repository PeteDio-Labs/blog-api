package com.petedillo.api.controller;

import com.petedillo.api.dto.ApiInfoResponse;
import com.petedillo.api.model.BlogPost;
import com.petedillo.api.repository.BlogPostRepository;
import com.petedillo.api.repository.TagRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for API metadata and information.
 * All endpoints are publicly accessible (no authentication required).
 */
@RestController
@RequestMapping("/api/v1")
public class InfoController {

    private final BlogPostRepository blogPostRepository;
    private final TagRepository tagRepository;

    @Value("${app.version:0.6.1}")
    private String apiVersion;

    @Value("${app.environment:dev}")
    private String environment;

    public InfoController(BlogPostRepository blogPostRepository, TagRepository tagRepository) {
        this.blogPostRepository = blogPostRepository;
        this.tagRepository = tagRepository;
    }

    /**
     * Health check endpoint.
     *
     * @return Health status including database connectivity
     */
    @GetMapping("/health")
    public ResponseEntity<java.util.Map<String, Object>> getHealth() {
        java.util.Map<String, Object> health = new java.util.LinkedHashMap<>();
        health.put("status", "UP");
        health.put("version", apiVersion);
        health.put("environment", environment);
        
        // Check database connectivity
        try {
            Long postCount = blogPostRepository.count();
            health.put("database", "UP");
            health.put("postsCount", postCount);
        } catch (Exception e) {
            health.put("database", "DOWN");
            health.put("postsCount", 0);
        }
        
        health.put("timestamp", java.time.Instant.now().toString());
        return ResponseEntity.ok(health);
    }

    /**
     * Get API metadata and blog statistics.
     *
     * @return API information including version, environment, and blog stats
     */
    @GetMapping("/info")
    public ResponseEntity<ApiInfoResponse> getApiInfo() {
        // Count published posts
        Long totalPublishedPosts = blogPostRepository.countByStatus("PUBLISHED");

        // Get most recent published post date
        LocalDateTime recentPostDate = blogPostRepository.findByStatus(
                "PUBLISHED",
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "publishedAt"))
        ).stream()
                .findFirst()
                .map(BlogPost::getPublishedAt)
                .orElse(null);

        // Get all tag names
        List<String> availableTags = tagRepository.findAll().stream()
                .map(tag -> tag.getName())
                .collect(Collectors.toList());

        ApiInfoResponse response = new ApiInfoResponse(
                apiVersion,
                environment,
                totalPublishedPosts,
                recentPostDate,
                availableTags
        );

        return ResponseEntity.ok(response);
    }
}
