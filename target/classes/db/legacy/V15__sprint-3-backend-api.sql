-- ============================================================================
-- Migration: V15__sprint-3-backend-api.sql
-- Description: Sprint 3 Backend/API Consolidation - Blog Post
-- Sprint: Sprint 3
-- Author: Pedro Delgadillo
-- Date: December 26, 2025
-- Purpose: Insert Sprint 3 Backend/API consolidation blog post covering JWT auth,
--          admin controllers, services refactor, pagination fix, and test updates
-- ============================================================================

-- Insert Sprint 3 Backend/API Consolidation post
INSERT INTO blog_posts (
    title,
    slug,
    content,
    excerpt,
    status,
    is_featured,
    published_at
) VALUES (
    'Sprint 3 Part 1: Backend & API Consolidation',
    'sprint-3-backend-api-enhancements',
    '## Overview

Sprint 3 backend work consolidated authentication, admin features, and service-layer updates. The focus was on JWT infrastructure, secure controllers, repository/model alignment, and a small pagination fix.

---

## Authentication & Security

- Implemented JWT infrastructure for stateless API access
- Added refresh token persistence and rotation
- Hardened Spring Security configuration with role-based access
- Protected admin endpoints and standardized error responses

## Admin Controllers

- Auth controller for login and token issuance
- Admin post management endpoints for CRUD operations
- Media upload/reorder/delete endpoints with validation

## Services & Repositories

- Service methods aligned to UI needs (create, update, publish, delete)
- Repository queries optimized for slug, status, and pagination
- DTOs refined for consistent API responses

## Pagination Fix

- Addressed edge-case on UI pagination (page boundaries)
- Ensured stable ordering on published lists

## Testing

- Updated unit and integration tests to cover JWT flows
- Verified controller guards and error handling paths

**Status**: ✅ COMPLETE
**Environment**: dev/stage/prod profiles
',
    'JWT auth, admin controllers, service refactors, pagination fix, and broader test coverage.',
    'PUBLISHED',
    FALSE,
    CURRENT_TIMESTAMP
);

-- Get the inserted post ID and add tags
DO $$
DECLARE
    post_id BIGINT;
BEGIN
    SELECT id INTO post_id FROM blog_posts WHERE slug = 'sprint-3-backend-api-enhancements';

    -- Insert tags into tags table if they don't exist, then link to post
    INSERT INTO tags (name, slug) VALUES 
        ('sprint-3', 'sprint-3'),
        ('backend', 'backend'),
        ('api', 'api'),
        ('auth', 'auth'),
        ('jwt', 'jwt'),
        ('security', 'security'),
        ('testing', 'testing')
    ON CONFLICT (name) DO NOTHING;

    -- Link tags to post via post_tags junction table
    INSERT INTO post_tags (post_id, tag_id)
    SELECT post_id, t.id FROM tags t WHERE name IN ('sprint-3', 'backend', 'api', 'auth', 'jwt', 'security', 'testing')
    ON CONFLICT DO NOTHING;

    -- Update tag post counts
    UPDATE tags SET post_count = (SELECT COUNT(*) FROM post_tags WHERE tag_id = tags.id)
    WHERE name IN ('sprint-3', 'backend', 'api', 'auth', 'jwt', 'security', 'testing');
END $$
;

-- ============================================================================
-- Solo Developer: Pedro Delgadillo
-- Status: Sprint 3 Backend/API Consolidation Complete
-- Next: V16__sprint-3-frontend-ui.sql
