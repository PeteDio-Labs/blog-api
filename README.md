# Blog API

REST API backend for [blog.petedillo.com](https://blog.petedillo.com). Manages posts, tags, analytics, semantic search (RAG), and admin operations. Deployed on Kubernetes with ArgoCD.

## Quick Start

```bash
bun install
cp .env.example .env  # configure DATABASE_URL, OLLAMA_HOST, etc.
bun dev               # http://localhost:3000
```

## Scripts

```bash
bun dev          # dev server (port 3000, hot reload)
bun build        # production build
bun start        # run production build
bun test         # run tests
bun run lint
bun run typecheck
```

## Stack

- **Runtime:** Bun
- **Framework:** Express 5, TypeScript
- **Validation:** Zod v4
- **Database:** PostgreSQL (migrations + seeding built-in)
- **Logging:** Pino
- **Metrics:** prom-client (Prometheus)

## API

### Posts
- `GET /api/v1/posts` — List posts (pagination, tag filter)
- `GET /api/v1/posts/:slug` — Get post by slug
- `POST /api/v1/posts` — Create post
- `PUT /api/v1/posts/:slug` — Update post
- `DELETE /api/v1/posts/:slug` — Delete post

### Search & RAG
- `GET /api/v1/search?q=...` — Full-text post search
- `POST /api/v1/rag` — Semantic search via embeddings

### Analytics
- `POST /api/v1/analytics` — Record page view / event
- `GET /api/v1/analytics` — Query analytics data

### Admin
- `GET /api/v1/admin/posts` — Admin post list (drafts included)
- `POST /api/v1/admin/posts` — Admin create/update

### Info & Health
- `GET /api/v1/info` — API info and version
- `GET /health` — Health check
- `GET /health/live` — Liveness probe
- `GET /health/ready` — Readiness probe
- `GET /metrics` — Prometheus metrics

## Deployment

Pushed to `docker.toastedbytes.com/blog-api` via GitHub Actions on push to `main`. ArgoCD Image Updater handles digest pinning. K8s manifests live in `infrastructure/kubernetes/blog`. Deployed in `blog-dev` (dev) and `blog-prod` (prod) namespaces.
