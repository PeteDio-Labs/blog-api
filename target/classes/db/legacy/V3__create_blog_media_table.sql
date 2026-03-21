-- ============================================================================
-- Migration: V3__create_blog_media_table.sql
-- Description: Create blog_media table for images, videos, and audio
-- Sprint: Sprint 1
-- Author: Pedro Delgadillo
-- Date: November 17, 2025
-- Documentation: 29 files, 368KB
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
    
    -- Ensure either file_path OR external_url is set, but not both
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

-- Add comments for documentation
COMMENT ON TABLE blog_media IS 'Media files and external links for blog posts';
COMMENT ON COLUMN blog_media.media_type IS 'Type of media: IMAGE, VIDEO, AUDIO, or EXTERNAL_IMAGE';
COMMENT ON COLUMN blog_media.file_path IS 'Relative path to uploaded file on server';
COMMENT ON COLUMN blog_media.external_url IS 'URL for externally hosted images';
COMMENT ON COLUMN blog_media.display_order IS 'Order in which media appears in post';
COMMENT ON COLUMN blog_media.file_size IS 'File size in bytes (null for external)';
-- ============================================================================
-- Solo Developer: Pedro Delgadillo
-- Documentation: 29 files, 368KB
-- Completion Date: November 17, 2025
-- ============================================================================
