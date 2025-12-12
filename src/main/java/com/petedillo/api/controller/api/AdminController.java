package com.petedillo.api.controller.api;

import com.petedillo.api.dto.BlogPostRequest;
import com.petedillo.api.dto.BlogPostResponse;
import com.petedillo.api.dto.TagResponse;
import com.petedillo.api.model.BlogPost;
import com.petedillo.api.model.PostStatus;
import com.petedillo.api.model.Tag;
import com.petedillo.api.repository.TagRepository;
import com.petedillo.api.service.BlogPostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST controller for admin blog post management.
 * All endpoints require JWT authentication with ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final BlogPostService blogPostService;
    private final TagRepository tagRepository;

    public AdminController(BlogPostService blogPostService, TagRepository tagRepository) {
        this.blogPostService = blogPostService;
        this.tagRepository = tagRepository;
    }

    /**
     * Create a new blog post.
     *
     * @param request the blog post creation request
     * @param authentication the authenticated user
     * @return the created blog post
     */
    @PostMapping("/posts")
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
    @GetMapping("/posts/{id}")
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
    @GetMapping("/posts")
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
    @PutMapping("/posts/{id}")
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
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        try {
            blogPostService.deletePost(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all tags for the tag picker in post editor.
     * Returns tags ordered by post count (most used first).
     *
     * @return list of all tags with their metadata
     */
    @GetMapping("/tags")
    public ResponseEntity<List<TagResponse>> getAllTags() {
        List<Tag> tags = tagRepository.findAllByOrderByPostCountDesc();
        List<TagResponse> response = tags.stream()
                .map(TagResponse::fromEntity)
                .collect(Collectors.toList());
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
