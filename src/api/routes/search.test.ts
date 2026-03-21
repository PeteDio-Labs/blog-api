import { describe, it, expect } from 'vitest';
import request from 'supertest';
import { createApp } from '../../app.ts';
import { createMockPool } from '../../test/helpers.ts';
import type { QueryResultRow } from 'pg';

const now = new Date();

function searchQueryHandler(text: string): QueryResultRow[] {
  if (text.trim() === 'SELECT 1') return [{ '?column?': 1 }];

  if (text.includes('COUNT(DISTINCT')) return [{ count: '1' }];

  if (text.includes('DISTINCT p.*') && text.includes('LIKE')) {
    return [
      {
        id: 1,
        title: 'Kubernetes Guide',
        slug: 'kubernetes-guide',
        content: '## K8s',
        excerpt: 'A guide',
        status: 'PUBLISHED',
        source: 'manual',
        is_featured: false,
        view_count: 3,
        created_at: now,
        updated_at: now,
        published_at: now,
      },
    ];
  }

  if (text.includes('FROM tags t') && text.includes('post_tags')) {
    return [{ id: 1, name: 'kubernetes', slug: 'kubernetes', post_count: 1, post_id: 1 }];
  }

  return [];
}

describe('Search Routes', () => {
  const pool = createMockPool(searchQueryHandler);
  const app = createApp(pool);

  it('GET /api/v1/search?q=kubernetes returns results', async () => {
    const res = await request(app).get('/api/v1/search?q=kubernetes');
    expect(res.status).toBe(200);
    expect(res.body.data).toHaveLength(1);
    expect(res.body.data[0].title).toBe('Kubernetes Guide');
  });

  it('GET /api/v1/search without q returns 400', async () => {
    const res = await request(app).get('/api/v1/search');
    expect(res.status).toBe(400);
    expect(res.body.error).toContain('required');
  });

  it('GET /api/v1/search?q= (empty) returns 400', async () => {
    const res = await request(app).get('/api/v1/search?q=');
    expect(res.status).toBe(400);
  });
});
