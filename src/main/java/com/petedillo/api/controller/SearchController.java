package com.petedillo.api.controller;

import com.petedillo.api.dto.BlogPostResponse;
import com.petedillo.api.model.BlogPost;
import com.petedillo.api.model.PostStatus;
import com.petedillo.api.service.BlogPostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for search functionality.
 * All endpoints are publicly accessible (no authentication required).
 */
@RestController
@RequestMapping("/api/v1")
public class SearchController {

    private final BlogPostService blogPostService;

    public SearchController(BlogPostService blogPostService) {
        this.blogPostService = blogPostService;
    }

    /**
     * Search published blog posts by query string.
     * Searches across title, content, and tags (case-insensitive).
     *
     * @param query the search query (required)
     * @param pageable pagination information (default: page=0, size=20, sort=publishedAt,desc)
     * @return paginated list of matching published posts
     */
    @GetMapping("/search")
    public ResponseEntity<Page<BlogPostResponse>> searchPosts(
            @RequestParam(required = true) String q,
            @PageableDefault(size = 20, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        // Search only published posts across title, content, and tags
        Page<BlogPost> posts = blogPostService.searchPostsByQuery(q, PostStatus.PUBLISHED.name(), pageable);
        Page<BlogPostResponse> response = posts.map(this::toResponse);

        return ResponseEntity.ok(response);
    }

    /**
     * Convert BlogPost entity to BlogPostResponse DTO.
     */
    private BlogPostResponse toResponse(BlogPost post) {
        BlogPostResponse response = new BlogPostResponse();
        response.setId(post.getId());
        response.setTitle(post.getTitle());
        response.setSlug(post.getSlug());
        response.setContent(post.getContent());
        response.setExcerpt(post.getExcerpt());
        response.setStatus(PostStatus.valueOf(post.getStatus()));
        response.setTags(post.getTagNames());
        response.setAuthorName("Admin");
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());
        response.setPublishedAt(post.getPublishedAt());
        return response;
    }
}
