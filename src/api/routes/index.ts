import { Router } from 'express';
import type { Pool } from 'pg';
import type { PostService } from '../../services/posts.ts';
import type { RagService } from '../../services/ragService.ts';
import { createPostsRouter } from './posts.ts';
import { createSearchRouter } from './search.ts';
import { createAdminRouter } from './admin.ts';
import { createInfoRouter } from './info.ts';
import { createHealthRouter } from './health.ts';
import { createRagRouter } from './rag.ts';
import { createAnalyticsRouter } from './analytics.ts';

export function createRoutes(pool: Pool, postService: PostService, ragService: RagService): Router {
  const routes = Router();

  // Infrastructure (no version prefix)
  routes.use(createHealthRouter(pool));

  // Versioned API
  const apiV1 = Router();
  apiV1.use('/posts', createPostsRouter(postService));
  apiV1.use('/search', createSearchRouter(postService));
  apiV1.use('/admin', createAdminRouter(postService, pool));
  apiV1.use('/rag', createRagRouter(ragService));
  apiV1.use('/analytics', createAnalyticsRouter(pool));
  apiV1.use(createInfoRouter(postService));

  routes.use('/api/v1', apiV1);

  return routes;
}

