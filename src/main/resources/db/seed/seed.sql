-- =============================================================================
-- Blog Post Seed Data - Local Development
-- =============================================================================
-- Generated from blog-posts.json
-- Uses ON CONFLICT for idempotent seeding (safe to run multiple times)
-- =============================================================================

BEGIN;

-- ============================================================================
-- TAGS
-- ============================================================================
-- Insert all unique tags with ON CONFLICT handling

INSERT INTO tags (name, slug, post_count) VALUES
    ('homelab', 'homelab', 0),
    ('kubernetes', 'kubernetes', 0),
    ('infrastructure', 'infrastructure', 0),
    ('sprint-1', 'sprint-1', 0),
    ('planning', 'planning', 0),
    ('network-design', 'network-design', 0),
    ('proxmox', 'proxmox', 0),
    ('sprint-2', 'sprint-2', 0),
    ('backend', 'backend', 0),
    ('api', 'api', 0),
    ('testing', 'testing', 0),
    ('spring-boot', 'spring-boot', 0),
    ('frontend', 'frontend', 0),
    ('ui', 'ui', 0),
    ('react', 'react', 0),
    ('tailwind', 'tailwind', 0),
    ('devops', 'devops', 0),
    ('sprint-3', 'sprint-3', 0),
    ('auth', 'auth', 0),
    ('jwt', 'jwt', 0),
    ('security', 'security', 0),
    ('admin', 'admin', 0),
    ('media', 'media', 0),
    ('gitops', 'gitops', 0),
    ('argocd', 'argocd', 0)
ON CONFLICT (name) DO NOTHING;

-- ============================================================================
-- BLOG POSTS
-- ============================================================================

-- Post 1: Sprint 1 Complete: Infrastructure Foundation
INSERT INTO blog_posts (slug, title, content, excerpt, status, is_featured, published_at, created_at, updated_at)
VALUES (
    'sprint-1-infrastructure-foundation',
    'Sprint 1 Complete: Infrastructure Foundation',
    E'# Sprint 1 Summary - Infrastructure Foundation\n\n**Sprint Duration**: November 14-17, 2025  \n**Status**: ✅ 100% COMPLETE  \n**Goal**: Build complete homelab infrastructure from zero to production-ready Kubernetes deployment\n\n---\n\n## Overview\n\nSprint 1 encompassed the complete foundation setup of the PeteDillo.com blog infrastructure, covering Phases 0-6. This sprint established all core infrastructure components needed for modern cloud-native application deployment.\n\n## Phases Completed\n\n### Phase 0: Planning ✅\n**Duration**: Nov 14, 2025  \n**Objective**: Network architecture and infrastructure planning\n\n**Key Deliverables**:\n- Network topology designed (192.168.50.0/24)\n- IP address scheme planned and documented\n- Service placement strategy defined\n- Hardware allocation plan (pedro: 40 cores/125GB RAM, pete: 4 cores/16GB RAM)\n\n---\n\n### Phase 1: Proxmox & Network Setup ✅\n**Duration**: Nov 14, 2025  \n**Objective**: Establish virtualization platform and network infrastructure\n\n**Key Achievements**:\n- Proxmox VE installed on 2-node cluster\n- Static IP configuration for all infrastructure nodes\n- SSH access configured\n- Storage configured for LXC containers and VMs\n- Ubuntu 22.04 LXC template downloaded\n\n---\n\n### Phase 2: Nexus Registry Setup ✅\n**Duration**: Nov 14, 2025  \n**Objective**: Deploy private container and Maven artifact registry\n\n---\n\n### Phase 3: Cloudflare Tunnel Setup ✅\n**Duration**: Nov 14, 2025  \n**Objective**: Enable secure external access to internal services\n\n---\n\n### Phase 4: MicroK8s Cluster Setup ✅\n**Duration**: Nov 15, 2025  \n**Objective**: Deploy production-grade Kubernetes cluster\n\n---\n\n### Phase 5: ArgoCD & GitOps Setup ✅\n**Duration**: Nov 15-17, 2025  \n**Objective**: Implement GitOps workflow with CI/CD pipelines\n\n---\n\n### Phase 6: GitOps Deployment with API Gateway ✅\n**Duration**: Nov 17, 2025  \n**Objective**: Deploy blog applications using GitOps with unified domain architecture\n\n---\n\n## Sprint 1 Metrics\n\n### Timeline\n- **Start Date**: November 14, 2025\n- **End Date**: November 17, 2025\n- **Duration**: 4 days\n- **Status**: 100% Complete\n\n### Deliverables\n- **Phases Completed**: 6 (Phase 0-6)\n- **Services Deployed**: 7\n- **Infrastructure Components**: 11\n- **Documentation Files**: 29 markdown files\n- **GitHub Repositories**: 3 (blog-api, blog-ui, blog-gitops)\n\n---\n\n## Conclusion\n\nSprint 1 successfully delivered a complete, modern cloud-native infrastructure from scratch in just 4 days. The foundation is production-ready, secure, and fully automated with GitOps workflows.\n\n**Key Achievement**: Zero-downtime deployments with GitOps, from code commit to production in minutes, fully automated.\n\n---\n\n**Documentation**: 29 files, 368KB  \n**Completion Date**: November 17, 2025  \n**Status**: ✅ COMPLETE',
    'Building a complete homelab infrastructure from zero to production-ready Kubernetes deployment in 4 days.',
    'PUBLISHED',
    true,
    '2025-11-17 00:00:00',
    NOW(),
    NOW()
) ON CONFLICT (slug) DO NOTHING;

-- Post 2: Phase 0: Planning the Infrastructure
INSERT INTO blog_posts (slug, title, content, excerpt, status, is_featured, published_at, created_at, updated_at)
VALUES (
    'phase-0-planning-infrastructure',
    'Phase 0: Planning the Infrastructure',
    E'# Phase 0: Planning the Infrastructure\n\n**Duration**: November 14, 2025  \n**Objective**: Network architecture and infrastructure planning  \n**Status**: ✅ Complete\n\n---\n\n## Overview\n\nBefore building any infrastructure, proper planning is essential. Phase 0 focused on designing the network topology, IP addressing scheme, service placement strategy, and hardware allocation for the entire homelab infrastructure.\n\n---\n\n## Network Design\n\n### Network Topology\n\nThe homelab network operates on a single subnet with clear IP allocation for different service types:\n\n```\nSubnet: 192.168.50.0/24\nGateway: 192.168.50.1\nUsable IPs: 192.168.50.1 - 192.168.50.254\n```\n\n### IP Address Scheme\n\n**Infrastructure Nodes (10-19)**:\n- `192.168.50.10` - pedro (Proxmox host 1)\n- `192.168.50.11` - pete (Proxmox host 2)\n\n**Kubernetes Cluster (60-69)**:\n- `192.168.50.60` - k8s-node1 (control plane)\n- `192.168.50.61` - k8s-node2 (worker node)\n\n**Services (100-119)**:\n- `192.168.50.111` - Nexus Registry (LXC container)\n\n---\n\n## Hardware Allocation\n\n### Proxmox Host: pedro\n```\nCPU: 40 cores\nRAM: 125 GB\nRole: Primary compute node\n```\n\n### Proxmox Host: pete\n```\nCPU: 4 cores\nRAM: 16 GB\nRole: Secondary compute node\n```\n\n---\n\n## Conclusion\n\nPhase 0 successfully established a comprehensive infrastructure plan with clear technical decisions, resource allocation, and implementation phases.\n\n**Next Phase**: [Phase 1 - Proxmox & Network Setup]\n\n---\n\n**Planning Date**: November 14, 2025  \n**Status**: ✅ Complete',
    'Network architecture and infrastructure planning for the PeteDillo.com blog homelab.',
    'DRAFT',
    false,
    NULL,
    NOW(),
    NOW()
) ON CONFLICT (slug) DO NOTHING;

-- Post 3: Sprint 2 Part 1: Backend & API Enhancements
INSERT INTO blog_posts (slug, title, content, excerpt, status, is_featured, published_at, created_at, updated_at)
VALUES (
    'sprint-2-backend-api-enhancements',
    'Sprint 2 Part 1: Backend & API Enhancements',
    E'**Sprint Duration**: November 19-29, 2025\n**Status**: ✅ 100% COMPLETE\n**Focus**: API implementation, comprehensive testing, admin UI foundation, security hardening\n\n---\n\n## Overview\n\nSprint 2 Backend Phase focused on completing critical API functionality, implementing comprehensive unit test coverage (52 tests, 80%+ coverage), building a production-ready admin UI with Spring Security, and establishing security foundations for OAuth2 integration.\n\n---\n\n## Phase 1: API Critical Features ✅ COMPLETE\n\n### Core API Enhancements\n\n- GET /api/v1/posts/{slug} Endpoint\n- Environment Awareness Endpoint\n- Database Query Optimization\n\n### Service Layer Enhancements\n\n- BlogPostService Implementation\n- MediaService Implementation\n\n### Admin Controllers - MVC + REST\n\n- Admin MVC Controllers (Thymeleaf)\n- Admin REST Controllers (AJAX)\n\n### Spring Security Implementation\n\n- Form-based login\n- BCrypt password hashing\n- Role-based access control (RBAC)\n\n### Unit Tests - 52 Tests Passing\n\n**Test Coverage: 80%+**\n\n---\n\n## Success Metrics\n\n| Metric | Target | Achieved |\n|--------|--------|----------|\n| Unit Test Coverage | 80%+ | ✅ 80%+ (52 tests) |\n| API Endpoints Functional | All core endpoints | ✅ 7/7 |\n| Admin UI Operational | All CRUD operations | ✅ Complete |\n\n---\n\n## Conclusion\n\nSprint 2 Backend Phase delivered a production-ready API with comprehensive test coverage, security-first architecture, and a fully functional admin UI.\n\n**Key Achievement**: From zero tests to 80%+ coverage with 52 tests.\n\n---\n\n**Completion Date**: November 21, 2025\n**Status**: ✅ COMPLETE',
    'Building production-ready backend: 52 unit tests (80%+ coverage), Spring Security integration, admin UI with Thymeleaf, comprehensive service layer.',
    'PUBLISHED',
    false,
    '2025-12-21 00:00:00',
    NOW(),
    NOW()
) ON CONFLICT (slug) DO NOTHING;

-- Post 4: Sprint 2 Part 2: Frontend & UI Enhancements
INSERT INTO blog_posts (slug, title, content, excerpt, status, is_featured, published_at, created_at, updated_at)
VALUES (
    'sprint-2-frontend-ui-enhancements',
    'Sprint 2 Part 2: Frontend & UI Enhancements',
    E'**Sprint Duration**: November 19-29, 2025\n**Status**: ✅ 100% COMPLETE\n**Focus**: Cyberpunk neon theme, environment awareness UI, media display optimization, markdown table rendering\n\n---\n\n## Overview\n\nSprint 2 Frontend Phase focused on implementing a cohesive cyberpunk neon aesthetic across all pages, adding environment indicators for development/staging/production visibility, optimizing media display from multiple sources, and ensuring proper markdown table rendering with consistent styling.\n\n---\n\n## Phase 2: UI Enhancements ✅ COMPLETE\n\n### 1. Neon Theme Implementation\n\n**Design System: Cyberpunk Neon Aesthetic**\n\n### 2. Environment Indicator Component\n\n### 3. Blog Media Frontend Display\n\n### 4. Markdown Table Display\n\n### 5. Theme Consistency Refactor\n\n---\n\n## Conclusion\n\nSprint 2 Frontend Phase delivered a cohesive, visually striking cyberpunk neon aesthetic that transforms the blog UI into a memorable experience.\n\n**Key Achievement**: From generic styling to cohesive neon cyberpunk aesthetic across all pages.\n\n---\n\n**Completion Date**: November 29, 2025\n**Status**: ✅ COMPLETE',
    'Modern cyberpunk neon UI: Tailwind theme system, environment indicators, multi-source image display, GFM markdown tables, responsive design.',
    'PUBLISHED',
    false,
    '2025-12-21 00:00:00',
    NOW(),
    NOW()
) ON CONFLICT (slug) DO NOTHING;

-- Post 5: Sprint 2 Part 3: Infrastructure Analysis & Deferral
INSERT INTO blog_posts (slug, title, content, excerpt, status, is_featured, published_at, created_at, updated_at)
VALUES (
    'sprint-2-infrastructure-defer',
    'Sprint 2 Part 3: Infrastructure Analysis & Deferral',
    E'**Sprint Duration**: November 19-29, 2025\n**Status**: 📝 PLANNING COMPLETE (Execution Deferred)\n**Focus**: Multi-environment setup planning, infrastructure optimization, sprint scope management\n\n---\n\n## Overview\n\nSprint 2 Infrastructure Phase involved comprehensive planning for multi-environment deployment, network policies, cluster expansion, and monitoring setup. However, after assessing scope and dependencies, infrastructure tasks were strategically deferred to Sprint 3 to prioritize application layer completion (API + UI).\n\n---\n\n## Deferral Decision & Rationale\n\n### Why Defer Infrastructure Work?\n\n**Priority Alignment**\n1. **Application Layer First**: Admin UI and comprehensive testing took priority\n2. **Feature Completeness**: Users need functional blog before multi-env deployment\n3. **Dependency Chain**: Infrastructure depends on application stability\n4. **Testing Requirements**: Multi-env setup requires dedicated testing cycle\n\n---\n\n## Conclusion\n\nSprint 2 Infrastructure Phase represents strategic planning that enables confident Sprint 3 execution.\n\n**Key Achievement**: Complete infrastructure roadmap with detailed planning documents—ready for immediate execution in Sprint 3.\n\n---\n\n**Planning Completion Date**: November 29, 2025\n**Documentation Status**: ✅ COMPLETE\n**Execution Status**: ⏳ DEFERRED TO SPRINT 3',
    'Strategic infrastructure planning deferred to Sprint 3: Multi-env setup, network policies, cluster expansion—all planned with execution saved for dedicated infrastructure sprint.',
    'PUBLISHED',
    false,
    '2025-12-21 00:00:00',
    NOW(),
    NOW()
) ON CONFLICT (slug) DO NOTHING;

-- Post 6: Sprint 3 Part 1: Backend & API Consolidation
INSERT INTO blog_posts (slug, title, content, excerpt, status, is_featured, published_at, created_at, updated_at)
VALUES (
    'sprint-3-backend-api-enhancements',
    'Sprint 3 Part 1: Backend & API Consolidation',
    E'## Overview\n\nSprint 3 backend work consolidated authentication, admin features, and service-layer updates. The focus was on JWT infrastructure, secure controllers, repository/model alignment, and a small pagination fix.\n\n---\n\n## Authentication & Security\n\n- Implemented JWT infrastructure for stateless API access\n- Added refresh token persistence and rotation\n- Hardened Spring Security configuration with role-based access\n- Protected admin endpoints and standardized error responses\n\n## Admin Controllers\n\n- Auth controller for login and token issuance\n- Admin post management endpoints for CRUD operations\n- Media upload/reorder/delete endpoints with validation\n\n## Services & Repositories\n\n- Service methods aligned to UI needs (create, update, publish, delete)\n- Repository queries optimized for slug, status, and pagination\n- DTOs refined for consistent API responses\n\n## Pagination Fix\n\n- Addressed edge-case on UI pagination (page boundaries)\n- Ensured stable ordering on published lists\n\n## Testing\n\n- Updated unit and integration tests to cover JWT flows\n- Verified controller guards and error handling paths\n\n**Status**: ✅ COMPLETE\n**Environment**: dev/stage/prod profiles',
    'JWT auth, admin controllers, service refactors, pagination fix, and broader test coverage.',
    'PUBLISHED',
    false,
    '2025-12-26 00:00:00',
    NOW(),
    NOW()
) ON CONFLICT (slug) DO NOTHING;

-- Post 7: Sprint 3 Part 2: Frontend & UI Consolidation
INSERT INTO blog_posts (slug, title, content, excerpt, status, is_featured, published_at, created_at, updated_at)
VALUES (
    'sprint-3-frontend-ui-consolidation',
    'Sprint 3 Part 2: Frontend & UI Consolidation',
    E'## Overview\n\nSprint 3 frontend work unified authentication flows, hardened route protection, and consolidated admin pages. Significant UI components were refined for posts, media, and shared layouts.\n\n---\n\n## Authentication UX\n\n- React Auth Context synchronized with JWT API\n- Login page with error states and loading indicators\n- Token persistence and logout handling\n\n## Protected Routes\n\n- Guarded admin pages for posts and media\n- Redirect patterns for unauthorized sessions\n- Consistent toast notifications for errors\n\n## Admin Screens\n\n- Posts list with filters, sorting, and pagination\n- Post editor with markdown input and live preview\n- Media components: upload, reorder, delete\n\n## Shared & Styling\n\n- Shared components for headers, footers, buttons\n- Neon theme refinements; improved readability and contrast\n- Homepage revamp: highlighted posts and environment badges\n\n**Status**: ✅ COMPLETE\n**Environment**: dev/stage',
    'Auth Context, protected routes, admin lists/editor, media components, styling refresh, and homepage revamp.',
    'PUBLISHED',
    false,
    '2025-12-26 00:00:00',
    NOW(),
    NOW()
) ON CONFLICT (slug) DO NOTHING;

-- Post 8: Sprint 3 Part 3: Infrastructure & GitOps Updates
INSERT INTO blog_posts (slug, title, content, excerpt, status, is_featured, published_at, created_at, updated_at)
VALUES (
    'sprint-3-infrastructure-updates',
    'Sprint 3 Part 3: Infrastructure & GitOps Updates',
    E'## Overview\n\nSprint 3 infrastructure work strengthened GitOps workflows, refined Argo CD app structures, and prepared observability overlays. Image updater values were tuned for safer rollouts.\n\n---\n\n## GitOps & Argo CD\n\n- Consolidated application manifests and project boundaries\n- Stage/dev clusters aligned with namespace conventions\n- Safer sync policies with manual gates where appropriate\n\n## Image Updater\n\n- Adjusted base and overlay values for tag policies\n- Reduced noisy churn; batched updates on minor releases\n\n## Observability Baselines\n\n- Established overlay structure for metrics/logs/traces\n- Set initial dashboards for API and UI components\n\n**Status**: ✅ COMPLETE\n**Environments**: dev, stage',
    'GitOps alignment, Argo CD app consolidation, image updater tuning, and observability baselines.',
    'PUBLISHED',
    false,
    '2025-12-26 00:00:00',
    NOW(),
    NOW()
) ON CONFLICT (slug) DO NOTHING;

-- ============================================================================
-- POST-TAG ASSOCIATIONS
-- ============================================================================

-- Post 1: sprint-1-infrastructure-foundation
INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-1-infrastructure-foundation' AND t.name = 'homelab'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-1-infrastructure-foundation' AND t.name = 'kubernetes'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-1-infrastructure-foundation' AND t.name = 'infrastructure'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-1-infrastructure-foundation' AND t.name = 'sprint-1'
ON CONFLICT (post_id, tag_id) DO NOTHING;

-- Post 2: phase-0-planning-infrastructure
INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'phase-0-planning-infrastructure' AND t.name = 'planning'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'phase-0-planning-infrastructure' AND t.name = 'network-design'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'phase-0-planning-infrastructure' AND t.name = 'homelab'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'phase-0-planning-infrastructure' AND t.name = 'proxmox'
ON CONFLICT (post_id, tag_id) DO NOTHING;

-- Post 3: sprint-2-backend-api-enhancements
INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-2-backend-api-enhancements' AND t.name = 'sprint-2'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-2-backend-api-enhancements' AND t.name = 'backend'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-2-backend-api-enhancements' AND t.name = 'api'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-2-backend-api-enhancements' AND t.name = 'testing'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-2-backend-api-enhancements' AND t.name = 'spring-boot'
ON CONFLICT (post_id, tag_id) DO NOTHING;

-- Post 4: sprint-2-frontend-ui-enhancements
INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-2-frontend-ui-enhancements' AND t.name = 'sprint-2'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-2-frontend-ui-enhancements' AND t.name = 'frontend'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-2-frontend-ui-enhancements' AND t.name = 'ui'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-2-frontend-ui-enhancements' AND t.name = 'react'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-2-frontend-ui-enhancements' AND t.name = 'tailwind'
ON CONFLICT (post_id, tag_id) DO NOTHING;

-- Post 5: sprint-2-infrastructure-defer
INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-2-infrastructure-defer' AND t.name = 'sprint-2'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-2-infrastructure-defer' AND t.name = 'infrastructure'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-2-infrastructure-defer' AND t.name = 'kubernetes'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-2-infrastructure-defer' AND t.name = 'devops'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-2-infrastructure-defer' AND t.name = 'planning'
ON CONFLICT (post_id, tag_id) DO NOTHING;

-- Post 6: sprint-3-backend-api-enhancements
INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-3-backend-api-enhancements' AND t.name = 'sprint-3'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-3-backend-api-enhancements' AND t.name = 'backend'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-3-backend-api-enhancements' AND t.name = 'api'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-3-backend-api-enhancements' AND t.name = 'auth'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-3-backend-api-enhancements' AND t.name = 'jwt'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-3-backend-api-enhancements' AND t.name = 'security'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-3-backend-api-enhancements' AND t.name = 'testing'
ON CONFLICT (post_id, tag_id) DO NOTHING;

-- Post 7: sprint-3-frontend-ui-consolidation
INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-3-frontend-ui-consolidation' AND t.name = 'sprint-3'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-3-frontend-ui-consolidation' AND t.name = 'frontend'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-3-frontend-ui-consolidation' AND t.name = 'ui'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-3-frontend-ui-consolidation' AND t.name = 'react'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-3-frontend-ui-consolidation' AND t.name = 'admin'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-3-frontend-ui-consolidation' AND t.name = 'auth'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-3-frontend-ui-consolidation' AND t.name = 'media'
ON CONFLICT (post_id, tag_id) DO NOTHING;

-- Post 8: sprint-3-infrastructure-updates
INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-3-infrastructure-updates' AND t.name = 'sprint-3'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-3-infrastructure-updates' AND t.name = 'infrastructure'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-3-infrastructure-updates' AND t.name = 'devops'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-3-infrastructure-updates' AND t.name = 'gitops'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-3-infrastructure-updates' AND t.name = 'argocd'
ON CONFLICT (post_id, tag_id) DO NOTHING;

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id FROM blog_posts p, tags t
WHERE p.slug = 'sprint-3-infrastructure-updates' AND t.name = 'kubernetes'
ON CONFLICT (post_id, tag_id) DO NOTHING;

-- ============================================================================
-- BLOG MEDIA (Cover images for posts that have them)
-- ============================================================================

-- Post 1: sprint-1-infrastructure-foundation
INSERT INTO blog_media (blog_post_id, media_type, external_url, display_order, alt_text, caption, created_at)
SELECT p.id, 'EXTERNAL_IMAGE', 'https://images.unsplash.com/photo-1499750310107-5fef28a66643?w=1200&h=600&fit=crop', 0, 'Blog writing workspace with laptop and coffee', 'Cover image for Sprint 1 Complete: Infrastructure Foundation', NOW()
FROM blog_posts p WHERE p.slug = 'sprint-1-infrastructure-foundation'
ON CONFLICT DO NOTHING;

INSERT INTO blog_media (blog_post_id, media_type, external_url, display_order, alt_text, caption, created_at)
SELECT p.id, 'EXTERNAL_IMAGE', 'https://images.unsplash.com/photo-1667372393119-3d4c48d07fc9?w=800&h=600&fit=crop', 1, 'Kubernetes cluster architecture diagram', 'Kubernetes deployment overview', NOW()
FROM blog_posts p WHERE p.slug = 'sprint-1-infrastructure-foundation'
ON CONFLICT DO NOTHING;

-- Post 2: phase-0-planning-infrastructure
INSERT INTO blog_media (blog_post_id, media_type, external_url, display_order, alt_text, caption, created_at)
SELECT p.id, 'EXTERNAL_IMAGE', 'https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=1200&h=600&fit=crop', 0, 'Server infrastructure and networking cables', 'Cover image for Phase 0: Planning the Infrastructure', NOW()
FROM blog_posts p WHERE p.slug = 'phase-0-planning-infrastructure'
ON CONFLICT DO NOTHING;

-- ============================================================================
-- UPDATE TAG POST COUNTS
-- ============================================================================

UPDATE tags
SET post_count = (
    SELECT COUNT(*) FROM post_tags WHERE tag_id = tags.id
);

COMMIT;

-- ============================================================================
-- SUMMARY
-- ============================================================================
SELECT 'Seeding complete!' AS status;

SELECT 'Posts' as entity, COUNT(*) as count FROM blog_posts
UNION ALL
SELECT 'Tags', COUNT(*) FROM tags
UNION ALL
SELECT 'Media', COUNT(*) FROM blog_media
UNION ALL
SELECT 'Post-Tags', COUNT(*) FROM post_tags;
