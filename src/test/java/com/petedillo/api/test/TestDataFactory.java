package com.petedillo.api.test;

import com.petedillo.api.model.AdminUser;
import com.petedillo.api.model.AuthProvider;
import com.petedillo.api.model.BlogPost;
import com.petedillo.api.model.PostStatus;
import com.petedillo.api.model.RefreshToken;
import com.petedillo.api.model.Tag;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Factory for creating test data with consistent default values.
 * Allows fluent building of test entities with overridable properties.
 */
public class TestDataFactory {

    // ==================== AdminUser ====================

    public static AdminUserBuilder adminUserBuilder() {
        return new AdminUserBuilder();
    }

    public static AdminUser createAdminUser() {
        return adminUserBuilder().build();
    }

    public static class AdminUserBuilder {
        private String username = "testadmin";
        private String email = "testadmin@example.com";
        private String passwordHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhKu"; // admin123
        private AuthProvider authProvider = AuthProvider.LOCAL;
        private String providerUserId;
        private String displayName = "Test Admin";
        private String profilePictureUrl;
        private Boolean isEnabled = true;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();
        private LocalDateTime lastLoginAt;

        public AdminUserBuilder username(String username) {
            this.username = username;
            return this;
        }

        public AdminUserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public AdminUserBuilder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public AdminUserBuilder authProvider(AuthProvider authProvider) {
            this.authProvider = authProvider;
            return this;
        }

        public AdminUserBuilder providerUserId(String providerUserId) {
            this.providerUserId = providerUserId;
            return this;
        }

        public AdminUserBuilder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public AdminUserBuilder profilePictureUrl(String profilePictureUrl) {
            this.profilePictureUrl = profilePictureUrl;
            return this;
        }

        public AdminUserBuilder isEnabled(Boolean isEnabled) {
            this.isEnabled = isEnabled;
            return this;
        }

        public AdminUserBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public AdminUserBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public AdminUserBuilder lastLoginAt(LocalDateTime lastLoginAt) {
            this.lastLoginAt = lastLoginAt;
            return this;
        }

        public AdminUser build() {
            AdminUser adminUser = new AdminUser();
            adminUser.setUsername(username);
            adminUser.setEmail(email);
            adminUser.setPasswordHash(passwordHash);
            adminUser.setAuthProvider(authProvider);
            adminUser.setProviderUserId(providerUserId);
            adminUser.setDisplayName(displayName);
            adminUser.setProfilePictureUrl(profilePictureUrl);
            adminUser.setIsEnabled(isEnabled);
            adminUser.setCreatedAt(createdAt);
            adminUser.setUpdatedAt(updatedAt);
            adminUser.setLastLoginAt(lastLoginAt);
            return adminUser;
        }
    }

    // ==================== RefreshToken ====================

    public static RefreshTokenBuilder refreshTokenBuilder() {
        return new RefreshTokenBuilder();
    }

    public static RefreshToken createRefreshToken(AdminUser adminUser) {
        return refreshTokenBuilder().adminUser(adminUser).build();
    }

    public static class RefreshTokenBuilder {
        private String tokenHash = "hash_" + UUID.randomUUID().toString();
        private AdminUser adminUser;
        private UUID tokenFamilyId = UUID.randomUUID();
        private LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);
        private LocalDateTime revokedAt;
        private Boolean isRevoked = false;
        private LocalDateTime createdAt = LocalDateTime.now();
        private String userAgent = "test-agent";
        private String ipAddress = "127.0.0.1";

        public RefreshTokenBuilder tokenHash(String tokenHash) {
            this.tokenHash = tokenHash;
            return this;
        }

        public RefreshTokenBuilder adminUser(AdminUser adminUser) {
            this.adminUser = adminUser;
            return this;
        }

        public RefreshTokenBuilder tokenFamilyId(UUID tokenFamilyId) {
            this.tokenFamilyId = tokenFamilyId;
            return this;
        }

        public RefreshTokenBuilder expiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public RefreshTokenBuilder revokedAt(LocalDateTime revokedAt) {
            this.revokedAt = revokedAt;
            return this;
        }

        public RefreshTokenBuilder isRevoked(Boolean isRevoked) {
            this.isRevoked = isRevoked;
            return this;
        }

        public RefreshTokenBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public RefreshTokenBuilder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public RefreshTokenBuilder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public RefreshToken build() {
            RefreshToken token = new RefreshToken();
            token.setTokenHash(tokenHash);
            token.setAdminUser(adminUser);
            token.setTokenFamilyId(tokenFamilyId);
            token.setExpiresAt(expiresAt);
            token.setRevokedAt(revokedAt);
            token.setIsRevoked(isRevoked);
            token.setCreatedAt(createdAt);
            token.setUserAgent(userAgent);
            token.setIpAddress(ipAddress);
            return token;
        }
    }

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

        public TagBuilder name(String name) {
            this.name = name;
            return this;
        }

        public TagBuilder slug(String slug) {
            this.slug = slug;
            return this;
        }

        public TagBuilder postCount(Integer postCount) {
            this.postCount = postCount;
            return this;
        }

        public TagBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public TagBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

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
        private String status = "DRAFT"; // DRAFT, PUBLISHED, ARCHIVED
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();
        private LocalDateTime publishedAt;

        public BlogPostBuilder title(String title) {
            this.title = title;
            return this;
        }

        public BlogPostBuilder slug(String slug) {
            this.slug = slug;
            return this;
        }

        public BlogPostBuilder content(String content) {
            this.content = content;
            return this;
        }

        public BlogPostBuilder excerpt(String excerpt) {
            this.excerpt = excerpt;
            return this;
        }

        public BlogPostBuilder status(String status) {
            this.status = status;
            return this;
        }

        public BlogPostBuilder status(PostStatus status) {
            this.status = status.name();
            return this;
        }

        public BlogPostBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public BlogPostBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public BlogPostBuilder publishedAt(LocalDateTime publishedAt) {
            this.publishedAt = publishedAt;
            return this;
        }

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
