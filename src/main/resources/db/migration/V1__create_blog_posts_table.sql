-- ============================================================================
-- Migration: V1__create_blog_posts_table.sql
-- Description: Create the main blog_posts table
-- Sprint: Sprint 1
-- Author: Pedro Delgadillo
-- Date: November 17, 2025
-- Documentation: 29 files, 368KB
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

-- Add comments for documentation
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
-- Solo Developer: Pedro Delgadillo
-- Documentation: 29 files, 368KB
-- Completion Date: November 17, 2025
-- ============================================================================
