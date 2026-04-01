import { Router } from 'express';
import type { Pool } from 'pg';
import type { PostService } from '../../services/posts.ts';
import { CreatePostSchema, UpdatePostSchema } from '../../types.ts';
import { parsePagination, paginate } from '../../utils/pagination.ts';
import { logger } from '../../utils/logger.ts';

export function createAdminRouter(postService: PostService, pool: Pool): Router {
  const router = Router();

  // POST /api/v1/admin/posts — create
  router.post('/posts', async (req, res) => {
    const parsed = CreatePostSchema.safeParse(req.body);
    if (!parsed.success) {
      res.status(400).json({ error: 'Validation failed', details: parsed.error.issues });
      return;
    }
    const post = await postService.create(parsed.data);
    res.status(201).json(post);
  });

  // GET /api/v1/admin/posts — all statuses, filterable
  router.get('/posts', async (req, res) => {
    const { page, size, offset } = parsePagination(
      req.query as { page?: string; size?: string },
    );
    const { posts, total } = await postService.listAll({
      status: req.query.status as string | undefined,
      source: req.query.source as string | undefined,
      search: req.query.search as string | undefined,
      page,
      size,
      offset,
    });
    res.json(paginate(posts, page, size, total));
  });

  // GET /api/v1/admin/posts/:id — by numeric ID
  router.get('/posts/:id', async (req, res) => {
    const id = Number(req.params.id);
    if (isNaN(id)) {
      res.status(400).json({ error: 'Invalid post ID' });
      return;
    }
    const post = await postService.getById(id);
    if (!post) {
      res.status(404).json({ error: 'Post not found' });
      return;
    }
    res.json(post);
  });

  // PUT /api/v1/admin/posts/:id — update
  router.put('/posts/:id', async (req, res) => {
    const id = Number(req.params.id);
    if (isNaN(id)) {
      res.status(400).json({ error: 'Invalid post ID' });
      return;
    }
    const parsed = UpdatePostSchema.safeParse(req.body);
    if (!parsed.success) {
      res.status(400).json({ error: 'Validation failed', details: parsed.error.issues });
      return;
    }
    const post = await postService.update(id, parsed.data);
    if (!post) {
      res.status(404).json({ error: 'Post not found' });
      return;
    }
    res.json(post);
  });

  // DELETE /api/v1/admin/posts/:id
  router.delete('/posts/:id', async (req, res) => {
    const id = Number(req.params.id);
    if (isNaN(id)) {
      res.status(400).json({ error: 'Invalid post ID' });
      return;
    }
    const deleted = await postService.delete(id);
    if (!deleted) {
      res.status(404).json({ error: 'Post not found' });
      return;
    }
    res.status(204).end();
  });

  // POST /api/v1/admin/posts/:id/publish — shortcut
  router.post('/posts/:id/publish', async (req, res) => {
    const id = Number(req.params.id);
    if (isNaN(id)) {
      res.status(400).json({ error: 'Invalid post ID' });
      return;
    }
    const post = await postService.publish(id);
    if (!post) {
      res.status(404).json({ error: 'Post not found' });
      return;
    }
    res.json(post);
  });

  // GET /api/v1/admin/tags
  router.get('/tags', async (_req, res) => {
    const tags = await postService.listTags();
    res.json(tags);
  });

  // GET /api/v1/admin/analytics — aggregated analytics data
  router.get('/analytics', async (req, res) => {
    try {
      const days = Math.min(Number(req.query.days) || 30, 90);

      // Top pages — all time
      const topPagesAll = await pool.query(
        `SELECT path, COUNT(*) as views
         FROM analytics_events
         GROUP BY path
         ORDER BY views DESC
         LIMIT 10`,
      );

      // Top pages — last 7 days
      const topPages7d = await pool.query(
        `SELECT path, COUNT(*) as views
         FROM analytics_events
         WHERE created_at >= NOW() - INTERVAL '7 days'
         GROUP BY path
         ORDER BY views DESC
         LIMIT 10`,
      );

      // Top referrers
      const topReferrers = await pool.query(
        `SELECT referrer, COUNT(*) as views
         FROM analytics_events
         WHERE referrer IS NOT NULL AND referrer != ''
         GROUP BY referrer
         ORDER BY views DESC
         LIMIT 10`,
      );

      // Daily trend
      const dailyTrend = await pool.query(
        `SELECT DATE(created_at) as date, COUNT(*) as views
         FROM analytics_events
         WHERE created_at >= NOW() - INTERVAL '${days} days'
         GROUP BY DATE(created_at)
         ORDER BY date ASC`,
      );

      // Unique sessions count
      const uniqueSessions = await pool.query(
        `SELECT COUNT(DISTINCT session_id) as count FROM analytics_events`,
      );

      // Total pageviews
      const totalPageviews = await pool.query(
        `SELECT COUNT(*) as count FROM analytics_events`,
      );

      res.json({
        totalPageviews: Number(totalPageviews.rows[0]?.count || 0),
        uniqueSessions: Number(uniqueSessions.rows[0]?.count || 0),
        topPages: {
          allTime: topPagesAll.rows.map(r => ({ path: r.path, views: Number(r.views) })),
          last7Days: topPages7d.rows.map(r => ({ path: r.path, views: Number(r.views) })),
        },
        topReferrers: topReferrers.rows.map(r => ({ referrer: r.referrer, views: Number(r.views) })),
        dailyTrend: dailyTrend.rows.map(r => ({ date: r.date, views: Number(r.views) })),
      });
    } catch (err) {
      logger.error('Failed to fetch analytics', { error: (err as Error).message });
      res.status(500).json({ error: 'Failed to fetch analytics' });
    }
  });

  return router;
}
