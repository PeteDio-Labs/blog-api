import { Router } from 'express';
import type { PostService } from '../../services/posts.ts';
import { config } from '../../config.ts';

export function createInfoRouter(postService: PostService): Router {
  const router = Router();

  // GET /api/v1/health — DB status, post count, version
  router.get('/health', async (_req, res) => {
    try {
      const postCount = await postService.getPostCount();
      res.json({
        status: 'UP',
        version: config.version,
        environment: config.environment,
        postCount,
      });
    } catch {
      res.status(503).json({ status: 'DOWN', version: config.version });
    }
  });

  // GET /api/v1/info — API metadata
  router.get('/info', async (_req, res) => {
    const tags = await postService.listTags();
    const recentDate = await postService.getMostRecentDate();
    res.json({
      name: 'blog-api',
      version: config.version,
      environment: config.environment,
      tags: tags,
      mostRecentPostDate: recentDate,
    });
  });

  return router;
}
