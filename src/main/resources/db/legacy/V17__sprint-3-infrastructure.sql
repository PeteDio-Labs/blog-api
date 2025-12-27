-- ============================================================================
-- Migration: V17__sprint-3-infrastructure.sql
-- Description: Sprint 3 Infrastructure & GitOps Updates - Blog Post
-- Sprint: Sprint 3
-- Author: Pedro Delgadillo
-- Date: December 26, 2025
-- Purpose: Insert Sprint 3 infrastructure and GitOps update blog post covering
--          Argo CD apps, image updater tweaks, and observability baselines
-- ============================================================================

-- Insert Sprint 3 Infrastructure & GitOps Updates post
INSERT INTO blog_posts (
    title,
    slug,
    content,
    excerpt,
    status,
    is_featured,
    published_at
) VALUES (
    'Sprint 3 Part 3: Infrastructure & GitOps Updates',
    'sprint-3-infrastructure-updates',
    '## Overview

Sprint 3 infrastructure work strengthened GitOps workflows, refined Argo CD app structures, and prepared observability overlays. Image updater values were tuned for safer rollouts.

---

## GitOps & Argo CD

- Consolidated application manifests and project boundaries
- Stage/dev clusters aligned with namespace conventions
- Safer sync policies with manual gates where appropriate

## Image Updater

- Adjusted base and overlay values for tag policies
- Reduced noisy churn; batched updates on minor releases

## Observability Baselines

- Established overlay structure for metrics/logs/traces
- Set initial dashboards for API and UI components

**Status**: ✅ COMPLETE
**Environments**: dev, stage
',
    'GitOps alignment, Argo CD app consolidation, image updater tuning, and observability baselines.',
    'PUBLISHED',
    FALSE,
    CURRENT_TIMESTAMP
);

-- Get the inserted post ID and add tags
DO $$
DECLARE
    post_id BIGINT;
BEGIN
    SELECT id INTO post_id FROM blog_posts WHERE slug = 'sprint-3-infrastructure-updates';

    -- Insert tags into tags table if they don't exist, then link to post
    INSERT INTO tags (name, slug) VALUES 
        ('sprint-3', 'sprint-3'),
        ('infrastructure', 'infrastructure'),
        ('devops', 'devops'),
        ('gitops', 'gitops'),
        ('argocd', 'argocd'),
        ('kubernetes', 'kubernetes')
    ON CONFLICT (name) DO NOTHING;

    -- Link tags to post via post_tags junction table
    INSERT INTO post_tags (post_id, tag_id)
    SELECT post_id, t.id FROM tags t WHERE name IN ('sprint-3', 'infrastructure', 'devops', 'gitops', 'argocd', 'kubernetes')
    ON CONFLICT DO NOTHING;

    -- Update tag post counts
    UPDATE tags SET post_count = (SELECT COUNT(*) FROM post_tags WHERE tag_id = tags.id)
    WHERE name IN ('sprint-3', 'infrastructure', 'devops', 'gitops', 'argocd', 'kubernetes');
END $$
;

-- ============================================================================
-- Solo Developer: Pedro Delgadillo
-- Status: Sprint 3 Infrastructure Updates Complete
-- End
