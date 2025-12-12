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
 * REST controller for public blog endpoints.
 * All endpoints are publicly accessible (no authentication required).
 */
@RestController
@RequestMapping("/api/v1/posts")
public class BlogController {

    private final BlogPostService blogPostService;

    public BlogController(BlogPostService blogPostService) {
        this.blogPostService = blogPostService;
    }

    /**
     * Get all published blog posts with pagination.
     *
     * @param pageable pagination information (default: page=0, size=20, sort=publishedAt,desc)
     * @return paginated list of published posts
     */
    @GetMapping
    public ResponseEntity<Page<BlogPostResponse>> getPublishedPosts(
            @PageableDefault(size = 20, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<BlogPost> posts = blogPostService.findPublishedPosts(pageable);
        Page<BlogPostResponse> response = posts.map(this::toResponse);

        return ResponseEntity.ok(response);
    }

    /**
     * Get a single published blog post by slug.
     * Increments the view count for the post.
     *
     * @param slug the post slug
     * @return the blog post if found and published, 404 otherwise
     */
    @GetMapping("/{slug}")
    public ResponseEntity<BlogPostResponse> getPostBySlug(@PathVariable String slug) {
        try {
            BlogPost post = blogPostService.getPostBySlug(slug);

            // Only return published posts to public API
            if (post == null || !PostStatus.PUBLISHED.name().equals(post.getStatus())) {
                return ResponseEntity.notFound().build();
            }

            // Increment view count
            blogPostService.incrementViewCount(post.getId());

            return ResponseEntity.ok(toResponse(post));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
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
