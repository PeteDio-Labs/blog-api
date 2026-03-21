import { Router } from 'express';
import type { Pool } from 'pg';
import { getMetrics } from '../../metrics/index.ts';

export function createHealthRouter(pool: Pool): Router {
  const router = Router();

  // GET /health — quick liveness
  router.get('/health', (_req, res) => {
    res.json({ status: 'UP' });
  });

  // GET /health/live — liveness probe
  router.get('/health/live', (_req, res) => {
    res.json({ status: 'UP' });
  });

  // GET /health/ready — readiness probe (checks DB)
  router.get('/health/ready', async (_req, res) => {
    try {
      await pool.query('SELECT 1');
      res.json({ status: 'UP', database: 'connected' });
    } catch {
      res.status(503).json({ status: 'DOWN', database: 'disconnected' });
    }
  });

  // GET /metrics — Prometheus
  router.get('/metrics', async (_req, res) => {
    const metrics = await getMetrics();
    res.set('Content-Type', 'text/plain; version=0.0.4; charset=utf-8');
    res.send(metrics);
  });

  return router;
}
