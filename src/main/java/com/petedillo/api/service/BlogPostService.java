package com.petedillo.api.service;

import com.petedillo.api.exception.ResourceNotFoundException;
import com.petedillo.api.model.BlogPost;
import com.petedillo.api.model.Tag;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BlogPostService {

    private final BlogPostRepository blogPostRepository;
    private final TagRepository tagRepository;

    @Autowired
    public BlogPostService(BlogPostRepository blogPostRepository,
                           TagRepository tagRepository) {
        this.blogPostRepository = blogPostRepository;
        this.tagRepository = tagRepository;
    }

    public List<BlogPost> getAllPosts() {
        return blogPostRepository.findAll();
    }

    @Nullable
    public BlogPost getPostById(@NotNull Long id) {
        return blogPostRepository.findById(id).orElse(null);
    }

    @NotNull
    @Transactional
    public BlogPost getPostBySlug(@NotNull String slug) {
        return blogPostRepository.findBySlugWithTags(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found with slug: " + slug));
    }

    @NotNull
    public List<BlogPost> searchPosts(@NotNull String searchTerm) {
        return blogPostRepository.searchByTitleOrSlug(searchTerm);
    }

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

        BlogPost savedPost = blogPostRepository.save(post);

        if (tagNames != null && !tagNames.isEmpty()) {
            Set<Tag> tags = resolveTags(tagNames);
            savedPost.getTags().clear();
            savedPost.getTags().addAll(tags);
        }

        return blogPostRepository.save(savedPost);
    }

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

        post.getTags().clear();
        if (tagNames != null && !tagNames.isEmpty()) {
            post.getTags().addAll(resolveTags(tagNames));
        }

        return blogPostRepository.save(post);
    }

    @Transactional
    public void deletePost(@NotNull Long id) {
        BlogPost post = blogPostRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + id));
        blogPostRepository.delete(post);
    }

    @NotNull
    public List<BlogPost> getPostsByTag(@NotNull String tagName) {
        return blogPostRepository.findByTags_NameIgnoreCase(tagName);
    }

    public List<Tag> getAllTags() {
        return tagRepository.findAll().stream()
                .sorted((t1, t2) -> t1.getName().compareToIgnoreCase(t2.getName()))
                .collect(Collectors.toList());
    }

    public Set<String> getDistinctTagNames() {
        return tagRepository.findAll().stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());
    }

    public List<BlogPost> getAllPostsSorted() {
        return blogPostRepository.findAllByOrderByPublishedAtDesc();
    }

    public Page<BlogPost> listAllPosts(Pageable pageable) {
        return blogPostRepository.findAll(pageable);
    }

    public Page<BlogPost> findPublishedPosts(Pageable pageable) {
        return blogPostRepository.findByStatus("PUBLISHED", pageable);
    }

    public Page<BlogPost> searchPostsByQuery(String searchQuery, String status, Pageable pageable) {
        return blogPostRepository.searchByStatusAndQuery(searchQuery, status, pageable);
    }

    public Page<BlogPost> searchPosts(String title, String status, Pageable pageable) {
        if (status != null && !status.isEmpty() && title != null && !title.isEmpty()) {
            return blogPostRepository.findByTitleContainingIgnoreCase(title, pageable);
        } else if (status != null && !status.isEmpty()) {
            return blogPostRepository.findByStatus(status, pageable);
        } else if (title != null && !title.isEmpty()) {
            return blogPostRepository.findByTitleContainingIgnoreCase(title, pageable);
        } else {
            return blogPostRepository.findAll(pageable);
        }
    }

    @Transactional
    public void incrementViewCount(@NotNull Long postId) {
        blogPostRepository.findById(postId).ifPresent(post -> {
            post.setViewCount(post.getViewCount() + 1);
            blogPostRepository.save(post);
        });
    }

    private String generateSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    private Set<Tag> resolveTags(Set<String> tagNames) {
        Set<Tag> tags = new HashSet<>();
        for (String tagName : tagNames) {
            if (tagName != null && !tagName.trim().isEmpty()) {
                String normalizedTag = tagName.trim().toLowerCase();
                String slug = normalizedTag.replaceAll("\\s+", "-");
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
        return tags;
    }
}
