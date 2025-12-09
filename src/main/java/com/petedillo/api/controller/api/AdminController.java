package com.petedillo.api.controller.api;

import com.petedillo.api.dto.BlogPostRequest;
import com.petedillo.api.dto.BlogPostResponse;
import com.petedillo.api.model.BlogPost;
import com.petedillo.api.model.PostStatus;
import com.petedillo.api.service.BlogPostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

/**
 * REST controller for admin blog post management.
 * All endpoints require JWT authentication with ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/v1/admin/posts")
public class AdminController {

    private final BlogPostService blogPostService;

    public AdminController(BlogPostService blogPostService) {
        this.blogPostService = blogPostService;
    }

    /**
     * Create a new blog post.
     *
     * @param request the blog post creation request
     * @param authentication the authenticated user
     * @return the created blog post
     */
    @PostMapping
    public ResponseEntity<BlogPostResponse> createPost(@RequestBody BlogPostRequest request, 
                                                        Authentication authentication) {
        Set<String> tags = request.getTags() != null ? 
                new HashSet<>(request.getTags()) : new HashSet<>();
        
        BlogPost savedPost = blogPostService.createPost(
                request.getTitle(),
                request.getContent(),
                request.getExcerpt(),
                request.getStatus().name(),
                tags
        );
        
        return new ResponseEntity<>(toResponse(savedPost), HttpStatus.CREATED);
    }

    /**
     * Get a blog post by ID.
     *
     * @param id the blog post ID
     * @return the blog post
     */
    @GetMapping("/{id}")
    public ResponseEntity<BlogPostResponse> getPost(@PathVariable Long id) {
        BlogPost post = blogPostService.getPostById(id);
        if (post == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toResponse(post));
    }

    /**
     * List blog posts with pagination and filtering.
     *
     * @param pageable pagination information
     * @param status optional status filter
     * @param search optional search term
     * @return page of blog posts
     */
    @GetMapping
    public ResponseEntity<Page<BlogPostResponse>> listPosts(
            Pageable pageable,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        
        Page<BlogPost> posts = blogPostService.searchPosts(search, status, pageable);
        
        return ResponseEntity.ok(posts.map(this::toResponse));
    }

    /**
     * Update a blog post.
     *
     * @param id the blog post ID
     * @param request the update request
     * @return the updated blog post
     */
    @PutMapping("/{id}")
    public ResponseEntity<BlogPostResponse> updatePost(@PathVariable Long id, 
                                                        @RequestBody BlogPostRequest request) {
        try {
            Set<String> tags = request.getTags() != null ? 
                    new HashSet<>(request.getTags()) : new HashSet<>();
            
            BlogPost updatedPost = blogPostService.updatePost(
                    id,
                    request.getTitle(),
                    request.getContent(),
                    request.getExcerpt(),
                    request.getStatus().name(),
                    tags
            );
            return ResponseEntity.ok(toResponse(updatedPost));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete a blog post.
     *
     * @param id the blog post ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        try {
            blogPostService.deletePost(id);
            return ResponseEntity.noContent().build();
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
        response.setContent(post.getContent());
        response.setExcerpt(post.getExcerpt());
        response.setStatus(PostStatus.valueOf(post.getStatus()));
        response.setAuthorName("Admin");
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());
        response.setPublishedAt(post.getPublishedAt());
        return response;
    }
}
