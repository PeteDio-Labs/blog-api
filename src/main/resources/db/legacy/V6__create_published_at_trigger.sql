-- ============================================================================
-- Migration: V6__create_published_at_trigger.sql
-- Description: Automatically set published_at when status changes to PUBLISHED
-- Sprint: Sprint 1
-- Author: Pedro Delgadillo
-- Date: November 17, 2025
-- Documentation: 29 files, 368KB
-- ============================================================================

-- Create function to set published_at timestamp
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

-- Create trigger on blog_posts table
CREATE TRIGGER tr_blog_posts_published_at
    BEFORE UPDATE ON blog_posts
    FOR EACH ROW
    EXECUTE FUNCTION set_published_at();

COMMENT ON FUNCTION set_published_at() IS 'Sets published_at when post is published, clears when unpublished';
-- ============================================================================
-- Solo Developer: Pedro Delgadillo
-- Documentation: 29 files, 368KB
-- Completion Date: November 17, 2025
-- ============================================================================
