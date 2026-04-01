import { describe, it, expect } from 'vitest';
import request from 'supertest';
import { createApp } from '../../app.ts';
import { createMockPool, defaultQueryHandler } from '../../test/helpers.ts';
import type { QueryResultRow } from 'pg';

function analyticsQueryHandler(text: string, params?: unknown[]): QueryResultRow[] {
  // Health check
  if (text.trim() === 'SELECT 1') return [{ '?column?': 1 }];

  // INSERT analytics_events
  if (text.includes('INSERT INTO analytics_events')) {
    return [{ id: 1 }];
  }

  // Total pageviews (must match before generic COUNT)
  if (text.includes('COUNT(*)') && text.includes('analytics_events') && !text.includes('GROUP BY')) {
    return [{ count: '60' }];
  }

  // Unique sessions
  if (text.includes('COUNT(DISTINCT session_id)')) {
    return [{ count: '15' }];
  }

  // Top pages all time (GROUP BY path, no INTERVAL)
  if (text.includes('FROM analytics_events') && text.includes('GROUP BY path') && !text.includes('INTERVAL')) {
    return [
      { path: '/posts/hello-world', views: '42' },
      { path: '/posts/kubernetes-setup', views: '18' },
    ];
  }

  // Top pages 7 days
  if (text.includes('FROM analytics_events') && text.includes('GROUP BY path') && text.includes("7 days")) {
    return [{ path: '/posts/hello-world', views: '10' }];
  }

  // Top referrers
  if (text.includes('GROUP BY referrer')) {
    return [{ referrer: 'https://google.com', views: '25' }];
  }

  // Daily trend
  if (text.includes('DATE(created_at) as date')) {
    return [
      { date: '2026-03-30', views: '5' },
      { date: '2026-03-31', views: '8' },
    ];
  }

  // Delegate common queries (schema_migrations, generic COUNT, etc.)
  return defaultQueryHandler(text, params);
}

describe('Analytics Routes', () => {
  const pool = createMockPool(analyticsQueryHandler);
  const app = createApp(pool);

  describe('POST /api/v1/analytics/pageview', () => {
    it('records a pageview', async () => {
      const res = await request(app)
        .post('/api/v1/analytics/pageview')
        .send({
          path: '/posts/hello-world',
          referrer: 'https://google.com',
          session_id: '550e8400-e29b-41d4-a716-446655440000',
        });
      expect(res.status).toBe(201);
      expect(res.body.ok).toBe(true);
    });

    it('records a pageview without referrer', async () => {
      const res = await request(app)
        .post('/api/v1/analytics/pageview')
        .send({
          path: '/posts/hello-world',
          session_id: '550e8400-e29b-41d4-a716-446655440000',
        });
      expect(res.status).toBe(201);
    });

    it('rejects missing path', async () => {
      const res = await request(app)
        .post('/api/v1/analytics/pageview')
        .send({
          session_id: '550e8400-e29b-41d4-a716-446655440000',
        });
      expect(res.status).toBe(400);
      expect(res.body.error).toBe('Validation failed');
    });

    it('rejects invalid session_id', async () => {
      const res = await request(app)
        .post('/api/v1/analytics/pageview')
        .send({
          path: '/posts/hello-world',
          session_id: 'not-a-uuid',
        });
      expect(res.status).toBe(400);
    });

    it('rejects missing session_id', async () => {
      const res = await request(app)
        .post('/api/v1/analytics/pageview')
        .send({
          path: '/posts/hello-world',
        });
      expect(res.status).toBe(400);
    });
  });

  describe('GET /api/v1/admin/analytics', () => {
    it('returns aggregated analytics', async () => {
      const res = await request(app).get('/api/v1/admin/analytics');
      expect(res.status).toBe(200);
      expect(res.body.totalPageviews).toBe(60);
      expect(res.body.uniqueSessions).toBe(15);
      expect(res.body.topPages.allTime).toHaveLength(2);
      expect(res.body.topPages.last7Days).toHaveLength(1);
      expect(res.body.topReferrers).toHaveLength(1);
      expect(res.body.dailyTrend).toHaveLength(2);
    });

    it('returns correct top page structure', async () => {
      const res = await request(app).get('/api/v1/admin/analytics');
      expect(res.body.topPages.allTime[0]).toEqual({
        path: '/posts/hello-world',
        views: 42,
      });
    });
  });
});
