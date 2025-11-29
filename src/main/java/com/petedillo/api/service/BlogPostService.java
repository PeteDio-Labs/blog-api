package com.petedillo.api.service;

import com.petedillo.api.exception.ResourceNotFoundException;
import com.petedillo.api.model.BlogMedia;
import com.petedillo.api.model.BlogPost;
import com.petedillo.api.model.BlogTag;
import com.petedillo.api.repository.BlogMediaRepository;
import com.petedillo.api.repository.BlogPostRepository;
import com.petedillo.api.repository.BlogTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class BlogPostService {

    private final BlogPostRepository blogPostRepository;
    private final BlogTagRepository blogTagRepository;
    private final BlogMediaRepository blogMediaRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public BlogPostService(BlogPostRepository blogPostRepository,
                           BlogTagRepository blogTagRepository,
                           BlogMediaRepository blogMediaRepository,
                           FileStorageService fileStorageService) {
        this.blogPostRepository = blogPostRepository;
        this.blogTagRepository = blogTagRepository;
        this.blogMediaRepository = blogMediaRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<BlogPost> getAllPosts() {
        return blogPostRepository.findAll();
    }

    public BlogPost getPostById(Long id) {
        return blogPostRepository.findById(id).orElse(null);
    }

    public BlogPost getPostBySlug(String slug) {
        return blogPostRepository.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found with slug: " + slug));
    }

    public List<BlogPost> searchPosts(String searchTerm) {
        return blogPostRepository.searchByTitleOrSlug(searchTerm);
    }

    // === ADMIN METHODS FOR CRUD OPERATIONS ===

    /**
     * Create a new blog post with tags
     */
    @Transactional
    public BlogPost createPost(String title, String content, String excerpt, 
                               String status, Set<String> tagNames) {
        BlogPost post = new BlogPost();
        post.setTitle(title);
        post.setContent(content);
        post.setExcerpt(excerpt);
        post.setStatus(status != null ? status : "draft");
        post.setPublishedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        // Save post first to get ID
        BlogPost savedPost = blogPostRepository.save(post);

        // Process tags (normalized to lowercase, alphabetically ordered)
        if (tagNames != null && !tagNames.isEmpty()) {
            Set<BlogTag> tags = new HashSet<>();
            for (String tagName : tagNames) {
                String normalizedTag = tagName.trim().toLowerCase();
                
                BlogTag tag = blogTagRepository.findByTagNameIgnoreCase(normalizedTag)
                    .orElseGet(() -> {
                        BlogTag newTag = new BlogTag();
                        newTag.setTagName(normalizedTag);
                        newTag.setBlogPost(savedPost);
                        return blogTagRepository.save(newTag);
                    });
                
                // If existing tag, update its association
                if (tag.getId() != null && !tag.getBlogPost().getId().equals(savedPost.getId())) {
                    tag.setBlogPost(savedPost);
                    blogTagRepository.save(tag);
                }
                
                tags.add(tag);
            }
            savedPost.setBlogTags(tags);
        }

        return savedPost;
    }

    /**
     * Update an existing blog post
     */
    @Transactional
    public BlogPost updatePost(Long id, String title, String content, String excerpt,
                               String status, Set<String> tagNames) {
        BlogPost post = blogPostRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + id));

        post.setTitle(title);
        post.setContent(content);
        post.setExcerpt(excerpt);
        if (status != null) {
            post.setStatus(status);
        }
        post.setUpdatedAt(LocalDateTime.now());

        // Clear existing tags
        if (post.getBlogTags() != null) {
            post.getBlogTags().clear();
        }

        // Process new tags
        if (tagNames != null && !tagNames.isEmpty()) {
            Set<BlogTag> tags = new HashSet<>();
            for (String tagName : tagNames) {
                String normalizedTag = tagName.trim().toLowerCase();
                
                BlogTag tag = blogTagRepository.findByTagNameIgnoreCase(normalizedTag)
                    .orElseGet(() -> {
                        BlogTag newTag = new BlogTag();
                        newTag.setTagName(normalizedTag);
                        newTag.setBlogPost(post);
                        return blogTagRepository.save(newTag);
                    });
                
                // Update association if needed
                if (tag.getId() != null && !tag.getBlogPost().getId().equals(post.getId())) {
                    tag.setBlogPost(post);
                    blogTagRepository.save(tag);
                }
                
                tags.add(tag);
            }
            post.setBlogTags(tags);
        }

        return blogPostRepository.save(post);
    }

    /**
     * Delete a blog post and all associated media files
     */
    @Transactional
    public void deletePost(Long id) {
        BlogPost post = blogPostRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + id));

        // Delete physical files for local media
        List<BlogMedia> mediaList = blogMediaRepository.findByBlogPostIdOrderByDisplayOrderAsc(id);
        for (BlogMedia media : mediaList) {
            if (media.isLocalFile()) {
                String filename = media.getFilePath().substring(media.getFilePath().lastIndexOf('/') + 1);
                fileStorageService.deleteFile(filename);
            }
        }

        // Delete post (cascade will handle media and tags in database)
        blogPostRepository.delete(post);
    }

    /**
     * Set cover image for a post by updating media item with displayOrder = 0
     */
    @Transactional
    public BlogPost setCoverImage(Long postId, Long mediaId) {
        BlogPost post = blogPostRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));
        
        BlogMedia newCoverMedia = blogMediaRepository.findById(mediaId)
            .orElseThrow(() -> new ResourceNotFoundException("Media not found: " + mediaId));
        
        // Validate media belongs to this post
        if (!newCoverMedia.getBlogPost().getId().equals(postId)) {
            throw new IllegalArgumentException("Media does not belong to this post");
        }

        // Find current cover image and reset its display order
        List<BlogMedia> mediaList = blogMediaRepository.findByBlogPostIdOrderByDisplayOrderAsc(postId);
        for (BlogMedia media : mediaList) {
            if (media.getDisplayOrder() != null && media.getDisplayOrder() == 0 && !media.getId().equals(mediaId)) {
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
    public List<BlogPost> getPostsByTag(String tagName) {
        return blogPostRepository.findByBlogTags_TagNameIgnoreCase(tagName.trim().toLowerCase());
    }

    /**
     * Get all tags ordered alphabetically
     */
    public List<BlogTag> getAllTags() {
        return blogTagRepository.findAllByOrderByTagNameAsc();
    }

    /**
     * Get all posts ordered by published date descending
     */
    public List<BlogPost> getAllPostsSorted() {
        return blogPostRepository.findAllByOrderByPublishedAtDesc();
    }
}
