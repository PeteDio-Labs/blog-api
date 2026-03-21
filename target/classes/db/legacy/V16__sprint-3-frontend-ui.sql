-- ============================================================================
-- Migration: V16__sprint-3-frontend-ui.sql
-- Description: Sprint 3 Frontend/UI Consolidation - Blog Post
-- Sprint: Sprint 3
-- Author: Pedro Delgadillo
-- Date: December 26, 2025
-- Purpose: Insert Sprint 3 Frontend/UI consolidation blog post covering Auth Context,
--          protected routes, admin lists, editor, media components, and styling refresh
-- ============================================================================

-- Insert Sprint 3 Frontend/UI Consolidation post
INSERT INTO blog_posts (
    title,
    slug,
    content,
    excerpt,
    status,
    is_featured,
    published_at
) VALUES (
    'Sprint 3 Part 2: Frontend & UI Consolidation',
    'sprint-3-frontend-ui-consolidation',
    '## Overview

Sprint 3 frontend work unified authentication flows, hardened route protection, and consolidated admin pages. Significant UI components were refined for posts, media, and shared layouts.

---

## Authentication UX

- React Auth Context synchronized with JWT API
- Login page with error states and loading indicators
- Token persistence and logout handling

## Protected Routes

- Guarded admin pages for posts and media
- Redirect patterns for unauthorized sessions
- Consistent toast notifications for errors

## Admin Screens

- Posts list with filters, sorting, and pagination
- Post editor with markdown input and live preview
- Media components: upload, reorder, delete

## Shared & Styling

- Shared components for headers, footers, buttons
- Neon theme refinements; improved readability and contrast
- Homepage revamp: highlighted posts and environment badges

**Status**: ✅ COMPLETE
**Environment**: dev/stage
',
    'Auth Context, protected routes, admin lists/editor, media components, styling refresh, and homepage revamp.',
    'PUBLISHED',
    FALSE,
    CURRENT_TIMESTAMP
);

-- Get the inserted post ID and add tags
DO $$
DECLARE
    post_id BIGINT;
BEGIN
    SELECT id INTO post_id FROM blog_posts WHERE slug = 'sprint-3-frontend-ui-consolidation';

    -- Insert tags into tags table if they don't exist, then link to post
    INSERT INTO tags (name, slug) VALUES 
        ('sprint-3', 'sprint-3'),
        ('frontend', 'frontend'),
        ('ui', 'ui'),
        ('react', 'react'),
        ('admin', 'admin'),
        ('auth', 'auth'),
        ('media', 'media')
    ON CONFLICT (name) DO NOTHING;

    -- Link tags to post via post_tags junction table
    INSERT INTO post_tags (post_id, tag_id)
    SELECT post_id, t.id FROM tags t WHERE name IN ('sprint-3', 'frontend', 'ui', 'react', 'admin', 'auth', 'media')
    ON CONFLICT DO NOTHING;

    -- Update tag post counts
    UPDATE tags SET post_count = (SELECT COUNT(*) FROM post_tags WHERE tag_id = tags.id)
    WHERE name IN ('sprint-3', 'frontend', 'ui', 'react', 'admin', 'auth', 'media');
END $$
;

-- ============================================================================
-- Solo Developer: Pedro Delgadillo
-- Status: Sprint 3 Frontend/UI Consolidation Complete
-- Next: V17__sprint-3-infrastructure.sql
