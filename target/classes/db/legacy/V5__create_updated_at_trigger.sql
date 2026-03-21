-- ============================================================================
-- Migration: V5__create_updated_at_trigger.sql
-- Description: Automatically update updated_at timestamp on blog_posts
-- Sprint: Sprint 1
-- Author: Pedro Delgadillo
-- Date: November 17, 2025
-- Documentation: 29 files, 368KB
-- ============================================================================

-- Create function to update timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger on blog_posts table
CREATE TRIGGER tr_blog_posts_updated_at
    BEFORE UPDATE ON blog_posts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

COMMENT ON FUNCTION update_updated_at_column() IS 'Automatically updates updated_at timestamp';
-- ============================================================================
-- Solo Developer: Pedro Delgadillo
-- Documentation: 29 files, 368KB
-- Completion Date: November 17, 2025
-- ============================================================================
