-- ============================================================================
-- Migration: V4__create_indexes.sql
-- Description: Create indexes for query optimization
-- Sprint: Sprint 1
-- Author: Pedro Delgadillo
-- Date: November 17, 2025
-- Documentation: 29 files, 368KB
-- ============================================================================

-- ============================================================================
-- Blog Posts Indexes
-- ============================================================================

-- Index for slug lookups (most common query)
CREATE INDEX idx_blog_posts_slug ON blog_posts(slug);

-- Index for filtering by status
CREATE INDEX idx_blog_posts_status ON blog_posts(status);

-- Index for sorting by published date (recent posts)
CREATE INDEX idx_blog_posts_published_at ON blog_posts(published_at DESC NULLS LAST);

-- Composite index for featured posts on homepage
CREATE INDEX idx_blog_posts_featured ON blog_posts(is_featured, published_at DESC) 
    WHERE is_featured = TRUE AND status = 'PUBLISHED';

-- Index for text search on title (case-insensitive)
CREATE INDEX idx_blog_posts_title ON blog_posts(LOWER(title));

-- ============================================================================
-- Blog Tags Indexes
-- ============================================================================

-- Index for post tag lookups
CREATE INDEX idx_blog_tags_post_id ON blog_tags(blog_post_id);

-- Index for tag name searches (case-insensitive already enforced)
CREATE INDEX idx_blog_tags_name ON blog_tags(tag_name);

-- Composite index for tag-based filtering
CREATE INDEX idx_blog_tags_name_post ON blog_tags(tag_name, blog_post_id);

-- ============================================================================
-- Blog Media Indexes
-- ============================================================================

-- Composite index for retrieving post media in order
CREATE INDEX idx_blog_media_post_order ON blog_media(blog_post_id, display_order);

-- Index for filtering by media type
CREATE INDEX idx_blog_media_type ON blog_media(media_type);

-- Index for finding media by file path (for cleanup operations)
CREATE INDEX idx_blog_media_file_path ON blog_media(file_path) 
    WHERE file_path IS NOT NULL;
-- ============================================================================
-- Solo Developer: Pedro Delgadillo
-- Documentation: 29 files, 368KB
-- Completion Date: November 17, 2025
-- ============================================================================
