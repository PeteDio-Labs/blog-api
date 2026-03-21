import { describe, it, expect } from 'vitest';
import request from 'supertest';
import { createApp } from '../../app.ts';
import { createMockPool } from '../../test/helpers.ts';
import type { QueryResultRow } from 'pg';

const now = new Date();

function infoQueryHandler(text: string): QueryResultRow[] {
  if (text.trim() === 'SELECT 1') return [{ '?column?': 1 }];

  if (text.includes('COUNT(*)')) return [{ count: '3' }];

  if (text.includes('FROM tags ORDER BY')) {
    return [
      { id: 1, name: 'kubernetes', slug: 'kubernetes', post_count: 2 },
      { id: 2, name: 'homelab', slug: 'homelab', post_count: 1 },
    ];
  }

  if (text.includes('published_at') && text.includes('LIMIT 1')) {
    return [{ published_at: now }];
  }

  return [];
}

describe('Info Routes', () => {
  const pool = createMockPool(infoQueryHandler);
  const app = createApp(pool);

  it('GET /api/v1/health returns DB status and post count', async () => {
    const res = await request(app).get('/api/v1/health');
    expect(res.status).toBe(200);
    expect(res.body.status).toBe('UP');
    expect(res.body.postCount).toBe(3);
  });

  it('GET /api/v1/info returns metadata with tags', async () => {
    const res = await request(app).get('/api/v1/info');
    expect(res.status).toBe(200);
    expect(res.body.name).toBe('blog-api');
    expect(res.body.tags).toContainEqual(expect.objectContaining({ slug: 'kubernetes', name: 'kubernetes' }));
    expect(res.body.tags).toContainEqual(expect.objectContaining({ slug: 'homelab', name: 'homelab' }));
    expect(res.body.mostRecentPostDate).toBeTruthy();
  });
});
