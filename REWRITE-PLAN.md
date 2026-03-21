# Blog API Rewrite: Spring Boot → Bun/Express

**Branch**: `feature/bun-express-rewrite`
**Target**: Replace Spring Boot 3.5.7 / Java 21 with Bun + Express 5 + TypeScript
**Database**: Fresh Postgres 16 — clean schema, seeded from `seed-posts/` markdown files

---

## Why

Spring Boot is overkill for a CRUD content API. The rest of the homelab services (notification-service, web-search-service, blog-agent) already run Bun/Express. This rewrite:
- Unifies the stack (one runtime, one set of conventions)
- Drops JVM overhead (~200MB+ → ~50MB container)
- Matches existing CI/CD patterns (Bun build, same Dockerfile shape)
- Removes dead dependencies (OAuth2, Thymeleaf, JWT, Lombok)

---

## Scope

### Keep (port 1:1)
- All REST endpoints (same request/response shapes)
- Postgres connection to `petedillo_blog` database
- Slug generation logic
- Tag resolution (find-or-create, normalize to lowercase)
- View count increment on slug lookup
- Search (ILIKE on title, content, tag names)
- Environment headers (X-Environment, X-API-Version)
- Health endpoint with DB connectivity check
- CORS configuration

### Remove
- Spring Security / JWT / OAuth2 (no auth — network-level only)
- Thymeleaf templates
- Lombok
- H2 test database (use Postgres or bun:sqlite for tests)
- Maven build system (pom.xml, .mvn/)
- All legacy table code (admin_users, refresh_tokens, blog_media, blog_tags)

### Add
- Pino structured logging (match notification-service)
- prom-client Prometheus metrics at `/metrics`
- Zod v4 request validation
- `/health/live` and `/health/ready` K8s probes
- `UNLISTED` post status — accessible via direct slug URL but excluded from feeds/search/tags
- `source` field on posts — tracks who created the post (`seed`, `blog-agent`, `manual`)
- Blog-agent integration: auto-publish config flag for graduating from human review to autonomous
- Admin endpoints designed as the primary blog-agent write interface

---

## Tech Choices

| Concern | Choice | Reason |
|---------|--------|--------|
| **Runtime** | Bun | Matches all other services |
| **Framework** | Express 5 | Matches notification-service, blog-agent |
| **Validation** | Zod v4 | Matches all services |
| **Logging** | Pino + pino-pretty | Matches all services |
| **Metrics** | prom-client | Matches all services |
| **Database** | `pg` (node-postgres) | Raw SQL with parameterized queries — no ORM |
| **Migrations** | Raw SQL files + startup runner | Flyway-compatible numbering, runs on boot |
| **Testing** | Vitest + supertest | Matches notification-service |
| **Security** | helmet + cors | Matches all services |

### Why raw SQL over an ORM

The API has ~10 queries total. Prisma/Drizzle add build complexity and schema files for negligible gain. The existing JPQL queries translate directly to parameterized SQL. Keeping it raw means:
- No code generation step
- No ORM schema to keep in sync with Flyway
- Easier to debug (you see the actual query)
- Matches the `bun:sqlite` pattern used in notification-service

---

## Endpoint Map

### Public (no auth — exposed through Cloudflare tunnel)

| Method | Path | Notes |
|--------|------|-------|
| GET | `/api/v1/posts` | Paginated, PUBLISHED only (excludes DRAFT, UNLISTED, ARCHIVED) |
| GET | `/api/v1/posts/:slug` | Returns PUBLISHED or UNLISTED posts, increments view_count |
| GET | `/api/v1/search` | `?q=` required, ILIKE on title/content/tags, PUBLISHED only |
| GET | `/api/v1/health` | DB status, post count, version |
| GET | `/api/v1/info` | API metadata, tag list, recent post date |

### Admin (consumed by blog-agent + admin UI, network-gated at nginx layer)

| Method | Path | Notes |
|--------|------|-------|
| POST | `/api/v1/admin/posts` | Create post — blog-agent or manual. Accepts `source` field |
| GET | `/api/v1/admin/posts` | All statuses, `?status=` `?search=` `?source=` filters |
| GET | `/api/v1/admin/posts/:id` | Get by numeric ID (any status) |
| PUT | `/api/v1/admin/posts/:id` | Update post (status, content, tags — no slug regen) |
| DELETE | `/api/v1/admin/posts/:id` | 204 No Content, cascade via FK |
| POST | `/api/v1/admin/posts/:id/publish` | Shortcut: sets status → PUBLISHED, publishedAt → now |
| GET | `/api/v1/admin/tags` | Ordered by post_count DESC |

### Post Statuses

| Status | Visible in feeds/search | Accessible by slug | Who creates it |
|--------|------------------------|-------------------|---------------|
| `DRAFT` | No | No (admin only) | Blog-agent (default), manual |
| `UNLISTED` | No | Yes (direct link) | Manual — for posts you want shareable but not in feeds |
| `PUBLISHED` | Yes | Yes | Human review or blog-agent auto-publish |
| `ARCHIVED` | No | No | Manual — soft delete |

### Blog-Agent Integration

Blog-agent is the primary writer. The API is designed around this flow:

```
Blog-Agent              Blog API            Notif Service       Discord (DM)        Human
    │                       │                    │                   │                │
    ├─ POST /admin/posts ──►│ saves as DRAFT     │                   │                │
    │  { source:            │                    │                   │                │
    │    "blog-agent" }     │                    │                   │                │
    │                       │                    │                   │                │
    ├─ POST /events ────────┼───────────────────►│                   │                │
    │  { type: "draft",     │                    ├─ SSE broadcast ──►│                │
    │    message: "Draft:   │                    │                   │ Pete Bot DMs:  │
    │    'Post Title'" }    │                    │                   │ "Draft ready:  │
    │                       │                    │                   │  Post Title    │
    │                       │                    │                   │  Review at:    │
    │                       │                    │                   │  192.168.50.   │
    │                       │                    │                   │  241/admin/    │
    │                       │                    │                   │  drafts/42"  ──►│
    │                       │                    │                   │                │
    │                       │◄───────────────────┼───────────────────┼── reviews at   │
    │                       │  POST /admin/      │                   │   MetalLB IP   │
    │                       │  posts/:id/publish │                   │                │
    │                       │                    │                   │                │
    │ (OR if auto_publish)  │                    │                   │                │
    ├─ POST /admin/posts ──►│ saves as PUBLISHED │                   │                │
    │  { status:            │ (skips review)     │                   │                │
    │    "PUBLISHED" }      │                    │                   │                │
```

**Auto-publish graduation**: Blog-agent decides the status it sends based on its own config:
- `AUTO_PUBLISH=false` (default) → always sends `status: DRAFT` → human reviews
- `AUTO_PUBLISH=true` (graduated) → sends `status: PUBLISHED` → goes live immediately

The API doesn't enforce the review flow — it trusts the caller. The review gate lives in blog-agent's config, not the API. This keeps the API simple and lets blog-agent graduate independently.

### Infrastructure

| Method | Path | Notes |
|--------|------|-------|
| GET | `/health` | Quick liveness (matches K8s probe path) |
| GET | `/health/live` | Liveness probe |
| GET | `/health/ready` | Readiness probe (checks DB) |
| GET | `/metrics` | Prometheus metrics |

---

## Project Structure

```
src/
├── index.ts              # Entry point (createApp + listen)
├── app.ts                # Express app factory
├── config.ts             # Environment config (typed)
├── types.ts              # Zod schemas + TypeScript interfaces
├── db/
│   ├── pool.ts           # pg Pool factory
│   ├── migrate.ts        # SQL migration runner (reads migrations/ dir)
│   ├── seed.ts           # Parses seed-posts/*.md frontmatter, inserts posts + tags
│   └── migrations/       # Numbered .sql files (clean, no Flyway legacy)
│       ├── 001_create_blog_posts.sql
│       ├── 002_create_tags.sql
│       ├── 003_create_post_tags.sql
│       ├── 004_create_indexes.sql
│       └── 005_create_triggers.sql
├── api/routes/
│   ├── index.ts          # Route mounting
│   ├── posts.ts          # Public post endpoints
│   ├── search.ts         # Search endpoint
│   ├── admin.ts          # Admin CRUD endpoints
│   ├── info.ts           # /api/v1/info + /api/v1/health
│   └── health.ts         # /health, /health/live, /health/ready, /metrics
├── services/
│   └── posts.ts          # PostService (CRUD, slug gen, tag resolution, search)
├── metrics/
│   └── index.ts          # prom-client registry
└── utils/
    ├── logger.ts         # Pino logger
    └── pagination.ts     # Page response builder
```

---

## Database Strategy

Fresh database — no Flyway, no legacy tables. The old Spring Boot DB is thrown away.

**Approach:**
1. Clean SQL migrations in `src/db/migrations/` — only the tables we actually use
2. Simple migration runner tracks applied migrations in a `schema_migrations` table
3. On first boot, creates schema from scratch and seeds from `seed-posts/*.md`
4. Seed runner parses markdown frontmatter (title, slug, status, tags, excerpt) and inserts posts + tags

**Tables (3 total):**
- `blog_posts` — full CRUD
- `tags` — find-or-create, list
- `post_tags` — junction table (managed via post CRUD)

**Seed posts** (`seed-posts/` directory):
- `001-welcome-to-petedio-labs.md` — intro post
- `002-homelab-inventory.md` — infrastructure snapshot
- `003-how-the-blog-writes-itself.md` — blog-agent explainer
- New seed posts can be added as numbered markdown files with frontmatter

---

## Pagination Response Shape

Simple pagination — no Spring Data compatibility needed (UI is being rewritten too):

```json
{
  "data": [...],
  "pagination": {
    "page": 0,
    "size": 20,
    "totalElements": 42,
    "totalPages": 3
  }
}
```

---

## BlogPostResponse Shape

```json
{
  "id": 1,
  "title": "How I Set Up MetalLB",
  "slug": "how-i-set-up-metallb",
  "content": "## Markdown content here...",
  "excerpt": "A quick guide to MetalLB on MicroK8s",
  "status": "PUBLISHED",
  "source": "blog-agent",
  "isFeatured": false,
  "viewCount": 12,
  "createdAt": "2026-03-20T12:00:00Z",
  "updatedAt": "2026-03-20T12:05:00Z",
  "publishedAt": "2026-03-20T12:00:00Z",
  "tags": [
    { "id": 1, "name": "kubernetes", "slug": "kubernetes", "postCount": 5 },
    { "id": 2, "name": "metallb", "slug": "metallb", "postCount": 1 }
  ]
}
```

---

## Key Business Logic to Port

### 1. Slug Generation
```typescript
function generateSlug(title: string): string {
  return title
    .toLowerCase()
    .replace(/[^a-z0-9\s-]/g, '')
    .replace(/\s+/g, '-')
    .replace(/^-+|-+$/g, '');
}
```

### 2. Tag Resolution (find-or-create)
```
For each tag name in request:
  1. Normalize to lowercase, trim
  2. Generate slug (spaces → hyphens)
  3. SELECT id FROM tags WHERE name = $normalized
  4. If not found: INSERT INTO tags (name, slug, post_count) VALUES (...)
  5. Return tag ID for junction table insert
```

### 3. View Count Increment
```
On GET /api/v1/posts/:slug:
  1. Fetch post (PUBLISHED or UNLISTED)
  2. UPDATE blog_posts SET view_count = view_count + 1 WHERE id = $id
  3. Return post
```

### 4. Search Query (PUBLISHED only — excludes UNLISTED)
```sql
SELECT DISTINCT p.* FROM blog_posts p
LEFT JOIN post_tags pt ON p.id = pt.post_id
LEFT JOIN tags t ON pt.tag_id = t.id
WHERE p.status = 'PUBLISHED'
  AND (LOWER(p.title) LIKE $1 OR LOWER(p.content) LIKE $1 OR LOWER(t.name) LIKE $1)
ORDER BY p.published_at DESC NULLS LAST
LIMIT $2 OFFSET $3
```

### 5. Post Source Tracking
```
On POST /api/v1/admin/posts:
  - source field: 'seed' | 'blog-agent' | 'manual'
  - Blog-agent always sends source: 'blog-agent'
  - Seed runner sets source: 'seed'
  - Admin UI sets source: 'manual'
  - Admin list filterable by ?source= for review workflows
```

---

## Config / Environment Variables

```typescript
const config = {
  port: Number(process.env.PORT) || 8080,
  environment: process.env.APP_ENVIRONMENT || 'dev',
  version: process.env.APP_VERSION || '1.0.0',

  db: {
    host: process.env.DB_HOST || 'localhost',
    port: Number(process.env.DB_PORT) || 5432,
    database: process.env.DB_NAME || 'petedillo_blog',
    user: process.env.DB_USERNAME || 'blog_app',
    password: process.env.DB_PASSWORD || 'dev_app_password',
  },

  cors: {
    origins: (process.env.CORS_ORIGINS || '*').split(','),
  },

  log: {
    level: process.env.LOG_LEVEL || 'info',
  },

  // Used by blog-agent notification links — the internal URL for draft review
  internalUrl: process.env.INTERNAL_URL || 'http://192.168.50.241',
};
```

**Note:** Spring Boot used `SPRING_DATASOURCE_URL` (JDBC URL). The Bun rewrite uses separate `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` — the K8s ConfigMap/Secret will need updating to match.

---

## CI/CD Changes

### GitHub Actions (`build-deploy.yml`)
Replace Maven/JDK steps with:
1. `oven-sh/setup-bun@v2`
2. `bun install`
3. `bun run typecheck`
4. `bun test`
5. `bun run build`
6. Docker build + push (same registry, same tag strategy)

### Dockerfile
```dockerfile
FROM oven/bun:1-alpine AS builder
WORKDIR /app
COPY package.json bun.lock ./
RUN bun install --frozen-lockfile
COPY tsconfig.json ./
COPY src/ src/
RUN bun run build

FROM oven/bun:1-alpine
WORKDIR /app
COPY --from=builder /app/dist/ dist/
COPY --from=builder /app/node_modules/ node_modules/
COPY package.json ./
COPY seed-posts/ seed-posts/
EXPOSE 8080
CMD ["bun", "run", "dist/index.js"]
```

### K8s Changes (blog-gitops)
- Update ConfigMap: `SPRING_DATASOURCE_URL` → `DB_HOST`, `DB_PORT`, `DB_NAME`
- Update Secret: `DB_USERNAME`, `DB_PASSWORD` (same values, different env var names)
- Update readiness probe: `/actuator/health` → `/health/ready`
- Update liveness probe: → `/health/live`
- Port stays `8080`

---

## Implementation Order

### Phase 1: Scaffold + DB (day 1)
- [ ] Init Bun project (`package.json`, `tsconfig.json`, deps)
- [ ] `src/config.ts` — typed env config
- [ ] `src/utils/logger.ts` — Pino logger
- [ ] `src/metrics/index.ts` — prom-client registry
- [ ] `src/db/pool.ts` — pg Pool
- [ ] `src/db/migrations/` — clean SQL files (blog_posts, tags, post_tags, indexes, triggers)
- [ ] `src/db/migrate.ts` — migration runner
- [ ] `src/db/seed.ts` — parse `seed-posts/*.md` frontmatter, insert posts + tags
- [ ] `src/app.ts` — Express app factory with middleware
- [ ] `src/index.ts` — entry point (migrate → seed → listen)

### Phase 2: Services + Routes (day 1-2)
- [ ] `src/types.ts` — Zod schemas for request/response
- [ ] `src/utils/pagination.ts` — pagination response builder
- [ ] `src/services/posts.ts` — PostService (all business logic)
- [ ] `src/api/routes/posts.ts` — public GET endpoints
- [ ] `src/api/routes/search.ts` — search endpoint
- [ ] `src/api/routes/admin.ts` — admin CRUD
- [ ] `src/api/routes/info.ts` — /api/v1/info + /api/v1/health
- [ ] `src/api/routes/health.ts` — probes + metrics

### Phase 3: Tests (day 2)
- [ ] Test setup (Vitest + supertest)
- [ ] Post endpoint tests (list, get by slug, 404s)
- [ ] Search tests (query, case-insensitive, pagination)
- [ ] Admin tests (create, update, delete, tag resolution)
- [ ] Info/health tests

### Phase 4: CI/CD + Deploy (day 2-3)
- [ ] Dockerfile (multi-stage Bun)
- [ ] Update `build-deploy.yml` for Bun
- [ ] Update blog-gitops ConfigMap/Secret env vars
- [ ] Update K8s probes
- [ ] Push, verify ArgoCD picks up new image
- [ ] Smoke test against dev.petedillo.com

### Phase 5: Cleanup
- [ ] Delete Java source (`src/main/java/`, `src/test/java/`)
- [ ] Delete Maven files (`pom.xml`, `.mvn/`, `mvnw`, `mvnw.cmd`)
- [ ] Delete `Dockerfile.dev`, `compose.yaml`, `init.sql`
- [ ] Delete `.env.example` (replaced by config.ts defaults)
- [ ] Update blog-agent CLAUDE.md to reference new API shape
- [ ] Update docs (CLAUDE.md, STATUS.md, FUTURE-GOALS.md)

---

## Risk Mitigation

- **Same port (8080)**: K8s Service doesn't change, zero-downtime swap
- **Fresh DB**: Old Flyway-managed DB is discarded — new schema + seed posts on first boot
- **Rollback**: `develop` branch still has Spring Boot — revert ArgoCD image tag to roll back
- **Seed posts are the source of truth**: Initial content lives in `seed-posts/` as markdown — easy to review, edit, add more
- **Blog UI is being rewritten too**: No need for backwards-compatible response shapes
