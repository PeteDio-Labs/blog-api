-- ============================================================================
-- Migration: V1__schema_foundation.sql
-- Description: Consolidated schema foundation (Blog API v1.0)
-- Created: 2025-12-27
-- Replaces: V1-V6, V9-V11 from original migration history
-- Note: This is a SCHEMA-ONLY migration. All data seeding handled by Ansible.
-- ============================================================================

-- ============================================================================
-- TABLE: blog_posts
-- The main blog posts table with all content and metadata
-- ============================================================================

CREATE TABLE blog_posts (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    content TEXT NOT NULL,
    excerpt VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    view_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP,
    
    CONSTRAINT chk_status CHECK (status IN ('DRAFT', 'PUBLISHED')),
    CONSTRAINT chk_view_count CHECK (view_count >= 0)
);

COMMENT ON TABLE blog_posts IS 'Main table for blog post content';
COMMENT ON COLUMN blog_posts.id IS 'Unique identifier for blog post';
COMMENT ON COLUMN blog_posts.slug IS 'URL-friendly identifier generated from title';
COMMENT ON COLUMN blog_posts.content IS 'Full blog post content in Markdown format';
COMMENT ON COLUMN blog_posts.excerpt IS 'Short summary for list views';
COMMENT ON COLUMN blog_posts.status IS 'Publication status: DRAFT or PUBLISHED';
COMMENT ON COLUMN blog_posts.is_featured IS 'Whether post appears on homepage';
COMMENT ON COLUMN blog_posts.view_count IS 'Number of times post has been viewed';
COMMENT ON COLUMN blog_posts.published_at IS 'Timestamp when post was first published';

-- ============================================================================
-- TABLE: blog_media
-- Media files and external links for blog posts
-- ============================================================================

CREATE TABLE blog_media (
    id BIGSERIAL PRIMARY KEY,
    blog_post_id BIGINT NOT NULL,
    media_type VARCHAR(20) NOT NULL,
    file_path VARCHAR(500),
    external_url TEXT,
    display_order INTEGER NOT NULL DEFAULT 0,
    alt_text VARCHAR(255),
    caption VARCHAR(500),
    file_size BIGINT,
    mime_type VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_blog_media_post 
        FOREIGN KEY (blog_post_id) 
        REFERENCES blog_posts(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT chk_media_type 
        CHECK (media_type IN ('IMAGE', 'VIDEO', 'AUDIO', 'EXTERNAL_IMAGE')),
    
    CONSTRAINT chk_media_source 
        CHECK (
            (file_path IS NOT NULL AND external_url IS NULL) OR
            (file_path IS NULL AND external_url IS NOT NULL)
        ),
    
    CONSTRAINT chk_display_order 
        CHECK (display_order >= 0),
    
    CONSTRAINT chk_file_size 
        CHECK (file_size IS NULL OR file_size > 0)
);

COMMENT ON TABLE blog_media IS 'Media files and external links for blog posts';
COMMENT ON COLUMN blog_media.media_type IS 'Type of media: IMAGE, VIDEO, AUDIO, or EXTERNAL_IMAGE';
COMMENT ON COLUMN blog_media.file_path IS 'Relative path to uploaded file on server';
COMMENT ON COLUMN blog_media.external_url IS 'URL for externally hosted images';
COMMENT ON COLUMN blog_media.display_order IS 'Order in which media appears in post';
COMMENT ON COLUMN blog_media.file_size IS 'File size in bytes (null for external)';

-- ============================================================================
-- TABLE: tags (Normalized - from V11)
-- Normalized tags table with post counts
-- ============================================================================

CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    post_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE tags IS 'Normalized tags table with post counts';
COMMENT ON COLUMN tags.post_count IS 'Cached count of posts with this tag';

-- ============================================================================
-- TABLE: post_tags (Junction table - from V11)
-- Many-to-many relationship between posts and tags
-- ============================================================================

CREATE TABLE post_tags (
    post_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (post_id, tag_id),
    CONSTRAINT fk_post_tags_post FOREIGN KEY (post_id)
        REFERENCES blog_posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_post_tags_tag FOREIGN KEY (tag_id)
        REFERENCES tags(id) ON DELETE CASCADE
);

COMMENT ON TABLE post_tags IS 'Many-to-many relationship between posts and tags';

-- ============================================================================
-- TABLE: admin_users
-- Admin user accounts supporting multiple authentication providers
-- ============================================================================

CREATE TABLE admin_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255),
    auth_provider VARCHAR(50) NOT NULL DEFAULT 'LOCAL',
    provider_user_id VARCHAR(255),
    display_name VARCHAR(255),
    profile_picture_url TEXT,
    is_enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    CONSTRAINT unique_provider_user UNIQUE (auth_provider, provider_user_id)
);

COMMENT ON TABLE admin_users IS 'Admin user accounts supporting multiple authentication providers';
COMMENT ON COLUMN admin_users.password_hash IS 'BCrypt hash - nullable for OAuth-only accounts';
COMMENT ON COLUMN admin_users.auth_provider IS 'Authentication provider: LOCAL, GOOGLE, or APPLE';
COMMENT ON COLUMN admin_users.provider_user_id IS 'Unique user ID from OAuth provider';

-- ============================================================================
-- TABLE: refresh_tokens
-- JWT refresh tokens with rotation tracking
-- ============================================================================

CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR(255) UNIQUE NOT NULL,
    admin_user_id BIGINT NOT NULL,
    token_family_id UUID NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    is_revoked BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_agent TEXT,
    ip_address VARCHAR(45),
    CONSTRAINT fk_refresh_tokens_admin_user FOREIGN KEY (admin_user_id)
        REFERENCES admin_users(id) ON DELETE CASCADE
);

COMMENT ON TABLE refresh_tokens IS 'JWT refresh tokens with rotation tracking';
COMMENT ON COLUMN refresh_tokens.token_hash IS 'SHA-256 hash of refresh token';
COMMENT ON COLUMN refresh_tokens.token_family_id IS 'UUID linking rotated tokens for reuse detection';
COMMENT ON COLUMN refresh_tokens.is_revoked IS 'True if token or family has been revoked';

-- ============================================================================
-- INDEXES: blog_posts
-- ============================================================================

CREATE INDEX idx_blog_posts_slug ON blog_posts(slug);
CREATE INDEX idx_blog_posts_status ON blog_posts(status);
CREATE INDEX idx_blog_posts_published_at ON blog_posts(published_at DESC NULLS LAST);
CREATE INDEX idx_blog_posts_featured ON blog_posts(is_featured, published_at DESC) 
    WHERE is_featured = TRUE AND status = 'PUBLISHED';
CREATE INDEX idx_blog_posts_title ON blog_posts(LOWER(title));

-- ============================================================================
-- INDEXES: blog_media
-- ============================================================================

CREATE INDEX idx_blog_media_post_order ON blog_media(blog_post_id, display_order);
CREATE INDEX idx_blog_media_type ON blog_media(media_type);
CREATE INDEX idx_blog_media_file_path ON blog_media(file_path) 
    WHERE file_path IS NOT NULL;

-- ============================================================================
-- INDEXES: tags & post_tags
-- ============================================================================

CREATE INDEX idx_post_tags_post ON post_tags(post_id);
CREATE INDEX idx_post_tags_tag ON post_tags(tag_id);
CREATE INDEX idx_tags_name ON tags(name);
CREATE INDEX idx_tags_slug ON tags(slug);

-- ============================================================================
-- INDEXES: admin_users
-- ============================================================================

CREATE INDEX idx_admin_users_email ON admin_users(email);
CREATE INDEX idx_admin_users_username ON admin_users(username);
CREATE INDEX idx_admin_users_provider ON admin_users(auth_provider, provider_user_id);

-- ============================================================================
-- INDEXES: refresh_tokens
-- ============================================================================

CREATE INDEX idx_refresh_tokens_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(admin_user_id);
CREATE INDEX idx_refresh_tokens_family ON refresh_tokens(token_family_id);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at);

-- ============================================================================
-- FUNCTION: update_updated_at_column
-- Automatically updates updated_at timestamp on row update
-- ============================================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION update_updated_at_column() IS 'Automatically updates updated_at timestamp';

-- ============================================================================
-- TRIGGER: tr_blog_posts_updated_at
-- Auto-update updated_at on blog_posts
-- ============================================================================

CREATE TRIGGER tr_blog_posts_updated_at
    BEFORE UPDATE ON blog_posts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- TRIGGER: tr_tags_updated_at
-- Auto-update updated_at on tags
-- ============================================================================

CREATE TRIGGER tr_tags_updated_at
    BEFORE UPDATE ON tags
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- TRIGGER: tr_admin_users_updated_at
-- Auto-update updated_at on admin_users
-- ============================================================================

CREATE TRIGGER tr_admin_users_updated_at
    BEFORE UPDATE ON admin_users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- FUNCTION: set_published_at
-- Automatically set published_at when status changes to PUBLISHED
-- ============================================================================

CREATE OR REPLACE FUNCTION set_published_at()
RETURNS TRIGGER AS $$
BEGIN
    -- If status changed from DRAFT to PUBLISHED and published_at is null
    IF OLD.status = 'DRAFT' 
       AND NEW.status = 'PUBLISHED' 
       AND NEW.published_at IS NULL THEN
        NEW.published_at = CURRENT_TIMESTAMP;
    END IF;
    
    -- If status changed from PUBLISHED to DRAFT, clear published_at
    IF OLD.status = 'PUBLISHED' 
       AND NEW.status = 'DRAFT' THEN
        NEW.published_at = NULL;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION set_published_at() IS 'Sets published_at when post is published, clears when unpublished';

-- ============================================================================
-- TRIGGER: tr_blog_posts_published_at
-- Auto-set published_at on status change
-- ============================================================================

CREATE TRIGGER tr_blog_posts_published_at
    BEFORE UPDATE ON blog_posts
    FOR EACH ROW
    EXECUTE FUNCTION set_published_at();

-- ============================================================================
-- END OF CONSOLIDATED SCHEMA MIGRATION
-- Tables: blog_posts, blog_media, tags, post_tags, admin_users, refresh_tokens
-- ============================================================================
