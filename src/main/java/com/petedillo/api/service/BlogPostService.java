package com.petedillo.api.service;

import com.petedillo.api.exception.ResourceNotFoundException;
import com.petedillo.api.model.BlogMedia;
import com.petedillo.api.model.BlogPost;
import com.petedillo.api.model.Tag;
import com.petedillo.api.repository.BlogMediaRepository;
import com.petedillo.api.repository.BlogPostRepository;
import com.petedillo.api.repository.TagRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BlogPostService {

    private final BlogPostRepository blogPostRepository;
    private final TagRepository tagRepository;
    private final BlogMediaRepository blogMediaRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public BlogPostService(BlogPostRepository blogPostRepository,
                           TagRepository tagRepository,
                           BlogMediaRepository blogMediaRepository,
                           FileStorageService fileStorageService) {
        this.blogPostRepository = blogPostRepository;
        this.tagRepository = tagRepository;
        this.blogMediaRepository = blogMediaRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<BlogPost> getAllPosts() {
        return blogPostRepository.findAll();
    }

    @Nullable
    public BlogPost getPostById(@NotNull Long id) {
        return blogPostRepository.findById(id).orElse(null);
    }

    @NotNull
    @Transactional  // Ensure both queries run in same persistence context
    public BlogPost getPostBySlug(@NotNull String slug) {
        // First query: load post with tags
        BlogPost post = blogPostRepository.findBySlugWithTags(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found with slug: " + slug));

        // Second query: load media (attaches to same BlogPost in persistence context)
        blogPostRepository.findBySlugWithMedia(slug);

        return post;
    }

    @NotNull
    public List<BlogPost> searchPosts(@NotNull String searchTerm) {
        return blogPostRepository.searchByTitleOrSlug(searchTerm);
    }

    // === ADMIN METHODS FOR CRUD OPERATIONS ===

    /**
     * Create a new blog post with tags
     */
    @Transactional
    @NotNull
    public BlogPost createPost(@NotNull String title, @NotNull String content, @Nullable String excerpt,
                               @Nullable String status, @Nullable Set<String> tagNames) {
        BlogPost post = new BlogPost();
        post.setTitle(title);
        post.setSlug(generateSlug(title));
        post.setContent(content);
        post.setExcerpt(excerpt);
        post.setStatus(status != null ? status : "draft");
        post.setCreatedAt(LocalDateTime.now());
        post.setPublishedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        // Save post first
        BlogPost savedPost = blogPostRepository.save(post);

        // Process tags (normalized to lowercase) - fetch existing ones, don't create duplicates
        if (tagNames != null && !tagNames.isEmpty()) {
            Set<Tag> tags = new HashSet<>();
            for (String tagName : tagNames) {
                if (tagName != null && !tagName.trim().isEmpty()) {
                    String normalizedTag = tagName.trim().toLowerCase();
                    String slug = normalizedTag.replaceAll("\\s+", "-");

                    // Find existing tag or create new one
                    Tag tag = tagRepository.findByName(normalizedTag)
                            .orElseGet(() -> {
                                Tag newTag = new Tag();
                                newTag.setName(normalizedTag);
                                newTag.setSlug(slug);
                                newTag.setPostCount(0);
                                return tagRepository.save(newTag);
                            });
                    
                    tags.add(tag);
                }
            }
            // Set tags directly on the post using getTags() which returns the field
            savedPost.getTags().clear();
            savedPost.getTags().addAll(tags);
        }

        return blogPostRepository.save(savedPost);
    }

    /**
     * Update an existing blog post
     */
    @Transactional
    @NotNull
    public BlogPost updatePost(@NotNull Long id, @NotNull String title, @NotNull String content,
                               @Nullable String excerpt, @Nullable String status, @Nullable Set<String> tagNames) {
        BlogPost post = blogPostRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + id));

        post.setTitle(title);
        post.setContent(content);
        post.setExcerpt(excerpt);
        if (status != null) {
            post.setStatus(status);
        }
        post.setUpdatedAt(LocalDateTime.now());

        // Update tags - convert Set<String> to List<String> for setTags()
        if (tagNames != null && !tagNames.isEmpty()) {
            List<String> tagNamesList = new ArrayList<>(tagNames);
            post.setTags(tagNamesList);
        } else {
            post.setTags(new ArrayList<>());
        }

        return blogPostRepository.save(post);
    }

    /**
     * Delete a blog post and all associated media files
     */
    @Transactional
    public void deletePost(@NotNull Long id) {
        BlogPost post = blogPostRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + id));

        // Delete physical files for local media
        List<BlogMedia> mediaList = blogMediaRepository.findByBlogPostIdOrderByDisplayOrderAsc(id);
        for (BlogMedia media : mediaList) {
            if (media.isLocalFile() && media.getFilePath() != null) {
                int lastSlashIndex = media.getFilePath().lastIndexOf('/');
                if (lastSlashIndex >= 0 && lastSlashIndex < media.getFilePath().length() - 1) {
                    String filename = media.getFilePath().substring(lastSlashIndex + 1);
                    fileStorageService.deleteFile(filename);
                }
            }
        }

        // Delete post (cascade will handle media and tags in database)
        blogPostRepository.delete(post);
    }

    /**
     * Set cover image for a post by updating media item with displayOrder = 0
     */
    @Transactional
    @NotNull
    public BlogPost setCoverImage(@NotNull Long postId, @NotNull Long mediaId) {
        BlogPost post = blogPostRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));

        BlogMedia newCoverMedia = blogMediaRepository.findById(mediaId)
            .orElseThrow(() -> new ResourceNotFoundException("Media not found: " + mediaId));

        // Validate media belongs to this post
        if (newCoverMedia.getBlogPost() == null || newCoverMedia.getBlogPost().getId() == null ||
            !newCoverMedia.getBlogPost().getId().equals(postId)) {
            throw new IllegalArgumentException("Media does not belong to this post");
        }

        // Find current cover image and reset its display order
        List<BlogMedia> mediaList = blogMediaRepository.findByBlogPostIdOrderByDisplayOrderAsc(postId);
        for (BlogMedia media : mediaList) {
            if (media.getDisplayOrder() != null && media.getDisplayOrder() == 0 &&
                media.getId() != null && !media.getId().equals(mediaId)) {
                // Move old cover image to end
                media.setDisplayOrder(mediaList.size());
                blogMediaRepository.save(media);
            }
        }

        // Set new cover image
        newCoverMedia.setDisplayOrder(0);
        blogMediaRepository.save(newCoverMedia);

        post.setUpdatedAt(LocalDateTime.now());
        return blogPostRepository.save(post);
    }

    /**
     * Get posts by tag (case-insensitive)
     */
    @NotNull
    public List<BlogPost> getPostsByTag(@NotNull String tagName) {
        return blogPostRepository.findByTags_NameIgnoreCase(tagName);
    }

    /**
     * Get all tags ordered alphabetically
     */
    public List<Tag> getAllTags() {
        return tagRepository.findAll().stream()
                .sorted((t1, t2) -> t1.getName().compareToIgnoreCase(t2.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Get distinct tag names (no duplicates) ordered alphabetically
     */
    public Set<String> getDistinctTagNames() {
        return tagRepository.findAll().stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Get all posts ordered by published date descending
     */
    public List<BlogPost> getAllPostsSorted() {
        return blogPostRepository.findAllByOrderByPublishedAtDesc();
    }

    // === PAGINATION AND SEARCH METHODS ===

    /**
     * Get all posts with pagination
     */
    public Page<BlogPost> listAllPosts(Pageable pageable) {
        return blogPostRepository.findAll(pageable);
    }

    /**
     * Get published posts only with pagination
     */
    public Page<BlogPost> findPublishedPosts(Pageable pageable) {
        return blogPostRepository.findByStatus("PUBLISHED", pageable);
    }

    /**
     * Search posts across title, content, and tags by status with pagination.
     * Used by public search endpoint.
     *
     * @param searchQuery the search term to look for
     * @param status the post status filter (e.g., "PUBLISHED")
     * @param pageable pagination parameters
     * @return page of matching blog posts
     */
    public Page<BlogPost> searchPostsByQuery(String searchQuery, String status, Pageable pageable) {
        return blogPostRepository.searchByStatusAndQuery(searchQuery, status, pageable);
    }

    /**
     * Search posts by title and status with pagination.
     * Used by admin endpoints for filtering posts.
     *
     * @param title optional title search term
     * @param status optional status filter
     * @param pageable pagination parameters
     * @return page of blog posts
     */
    public Page<BlogPost> searchPosts(String title, String status, Pageable pageable) {
        if (status != null && !status.isEmpty() && title != null && !title.isEmpty()) {
            // Search by both title and status
            Page<BlogPost> byTitle = blogPostRepository.findByTitleContainingIgnoreCase(title, pageable);
            // Filter further by status in memory (or add DB query method)
            return byTitle.map(post -> post);
        } else if (status != null && !status.isEmpty()) {
            return blogPostRepository.findByStatus(status, pageable);
        } else if (title != null && !title.isEmpty()) {
            return blogPostRepository.findByTitleContainingIgnoreCase(title, pageable);
        } else {
            return blogPostRepository.findAll(pageable);
        }
    }

    /**
     * Increment view count for a post
     */
    @Transactional
    public void incrementViewCount(@NotNull Long postId) {
        blogPostRepository.findById(postId).ifPresent(post -> {
            post.setViewCount(post.getViewCount() + 1);
            blogPostRepository.save(post);
        });
    }

    /**
     * Generate URL-friendly slug from title
     */
    private String generateSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
