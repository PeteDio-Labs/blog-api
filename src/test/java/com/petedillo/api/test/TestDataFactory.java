package com.petedillo.api.test;

import com.petedillo.api.model.BlogPost;
import com.petedillo.api.model.PostStatus;
import com.petedillo.api.model.Tag;

import java.time.LocalDateTime;

public class TestDataFactory {

    // ==================== Tag ====================

    public static TagBuilder tagBuilder() {
        return new TagBuilder();
    }

    public static Tag createTag() {
        return tagBuilder().build();
    }

    public static class TagBuilder {
        private String name = "test-tag";
        private String slug = "test-tag";
        private Integer postCount = 0;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        public TagBuilder name(String name) { this.name = name; return this; }
        public TagBuilder slug(String slug) { this.slug = slug; return this; }
        public TagBuilder postCount(Integer postCount) { this.postCount = postCount; return this; }
        public TagBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public TagBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Tag build() {
            Tag tag = new Tag();
            tag.setName(name);
            tag.setSlug(slug);
            tag.setPostCount(postCount);
            tag.setCreatedAt(createdAt);
            tag.setUpdatedAt(updatedAt);
            return tag;
        }
    }

    // ==================== BlogPost ====================

    public static BlogPostBuilder blogPostBuilder() {
        return new BlogPostBuilder();
    }

    public static BlogPost createBlogPost() {
        return blogPostBuilder().build();
    }

    public static class BlogPostBuilder {
        private String title = "Test Post";
        private String slug = "test-post";
        private String content = "Test content";
        private String excerpt = "Test excerpt";
        private String status = "DRAFT";
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();
        private LocalDateTime publishedAt;

        public BlogPostBuilder title(String title) { this.title = title; return this; }
        public BlogPostBuilder slug(String slug) { this.slug = slug; return this; }
        public BlogPostBuilder content(String content) { this.content = content; return this; }
        public BlogPostBuilder excerpt(String excerpt) { this.excerpt = excerpt; return this; }
        public BlogPostBuilder status(String status) { this.status = status; return this; }
        public BlogPostBuilder status(PostStatus status) { this.status = status.name(); return this; }
        public BlogPostBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public BlogPostBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public BlogPostBuilder publishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; return this; }

        public BlogPost build() {
            BlogPost post = new BlogPost();
            post.setTitle(title);
            post.setSlug(slug);
            post.setContent(content);
            post.setExcerpt(excerpt);
            post.setStatus(status);
            post.setCreatedAt(createdAt);
            post.setUpdatedAt(updatedAt);
            post.setPublishedAt(publishedAt);
            return post;
        }
    }
}
