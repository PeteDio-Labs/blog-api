package com.petedillo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for API metadata endpoint.
 * Provides information about the API version, environment, and blog statistics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiInfoResponse {

    /**
     * API version number.
     */
    private String apiVersion;

    /**
     * Environment name (e.g., "dev", "test", "production").
     */
    private String environment;

    /**
     * Total count of published blog posts.
     */
    private Long totalPublishedPosts;

    /**
     * Publication date of the most recent published post.
     * Null if no published posts exist.
     */
    private LocalDateTime recentPostDate;

    /**
     * List of all available tag names.
     * Empty list if no tags exist.
     */
    private List<String> availableTags;
}
