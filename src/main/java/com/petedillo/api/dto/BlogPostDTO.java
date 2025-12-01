package com.petedillo.api.dto;

import java.util.Set;

/**
 * Data Transfer Object for Blog Post form data from admin UI
 */
public class BlogPostDTO {
    private String title;
    private String slug;
    private String content;
    private String excerpt;
    private String status;
    private String tags; // Comma-separated tag names

    public BlogPostDTO() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    /**
     * Parse comma-separated tags into a Set
     */
    public Set<String> getTagsAsSet() {
        if (tags == null || tags.trim().isEmpty()) {
            return Set.of();
        }
        return Set.of(tags.split(","));
    }
}
