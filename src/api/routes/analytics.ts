import { Router } from 'express';
import type { Pool } from 'pg';
import { z } from 'zod';
import { logger } from '../../utils/logger.ts';
import { pageviewsTotal } from '../../metrics/index.ts';

const PageviewSchema = z.object({
  path: z.string().min(1).max(2048),
  referrer: z.string().max(2048).optional(),
  session_id: z.string().uuid(),
});

export function createAnalyticsRouter(pool: Pool): Router {
  const router = Router();

  // POST /api/v1/analytics/pageview — public, fires on every route change
  router.post('/pageview', async (req, res) => {
    const parsed = PageviewSchema.safeParse(req.body);
    if (!parsed.success) {
      res.status(400).json({ error: 'Validation failed', details: parsed.error.issues });
      return;
    }

    const { path, referrer, session_id } = parsed.data;

    try {
      await pool.query(
        `INSERT INTO analytics_events (path, referrer, session_id) VALUES ($1, $2, $3)`,
        [path, referrer || null, session_id],
      );

      pageviewsTotal.inc({ path });

      res.status(201).json({ ok: true });
    } catch (err) {
      logger.error('Failed to record pageview', { error: (err as Error).message });
      res.status(500).json({ error: 'Failed to record pageview' });
    }
  });

  return router;
}
