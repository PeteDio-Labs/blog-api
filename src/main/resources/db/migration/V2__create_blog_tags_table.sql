-- ============================================================================
-- Migration: V2__create_blog_tags_table.sql
-- Description: Create blog_tags table for post categorization
-- Sprint: Sprint 1
-- Author: Pedro Delgadillo
-- Date: November 17, 2025
-- Documentation: 29 files, 368KB
-- ============================================================================

CREATE TABLE blog_tags (
    id BIGSERIAL PRIMARY KEY,
    blog_post_id BIGINT NOT NULL,
    tag_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_blog_tags_post 
        FOREIGN KEY (blog_post_id) 
        REFERENCES blog_posts(id) 
        ON DELETE CASCADE,
    
    -- Prevent duplicate tags on same post
    CONSTRAINT uq_blog_tags_post_name 
        UNIQUE (blog_post_id, tag_name)
);

-- Add comments for documentation
COMMENT ON TABLE blog_tags IS 'Tags for categorizing and searching blog posts';
COMMENT ON COLUMN blog_tags.tag_name IS 'Tag name stored in lowercase for consistency';
COMMENT ON COLUMN blog_tags.blog_post_id IS 'Reference to parent blog post';

-- Add check constraint for lowercase tags
ALTER TABLE blog_tags ADD CONSTRAINT chk_tag_lowercase 
    CHECK (tag_name = LOWER(tag_name));
-- ============================================================================
-- Solo Developer: Pedro Delgadillo
-- Documentation: 29 files, 368KB
-- Completion Date: November 17, 2025
-- ============================================================================
