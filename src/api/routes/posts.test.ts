import { describe, it, expect } from 'vitest';
import request from 'supertest';
import { createApp } from '../../app.ts';
import { createMockPool } from '../../test/helpers.ts';
import type { QueryResultRow } from 'pg';

const now = new Date();

const samplePost = {
  id: 1,
  title: 'Test Post',
  slug: 'test-post',
  content: '## Hello',
  excerpt: 'A test post',
  status: 'PUBLISHED',
  source: 'manual',
  is_featured: false,
  view_count: 5,
  created_at: now,
  updated_at: now,
  published_at: now,
};

function postsQueryHandler(text: string, params?: unknown[]): QueryResultRow[] {
  // Health check
  if (text.trim() === 'SELECT 1') return [{ '?column?': 1 }];

  // List published count
  if (text.includes('COUNT(*)') && text.includes("status = 'PUBLISHED'")) {
    return [{ count: '1' }];
  }

  // List published
  if (text.includes('FROM blog_posts') && text.includes("status = 'PUBLISHED'") && text.includes('LIMIT')) {
    return [samplePost];
  }

  // Get by slug
  if (text.includes('WHERE slug = $1') && text.includes("'PUBLISHED', 'UNLISTED'")) {
    if (params?.[0] === 'test-post') return [samplePost];
    return [];
  }

  // View count increment
  if (text.includes('view_count = view_count + 1')) return [];

  // Tags for posts
  if (text.includes('FROM tags t') && text.includes('post_tags')) {
    return [{ id: 1, name: 'testing', slug: 'testing', post_count: 1, post_id: 1 }];
  }

  // Default
  return [];
}

describe('Posts Routes', () => {
  const pool = createMockPool(postsQueryHandler);
  const app = createApp(pool);

  it('GET /api/v1/posts returns paginated posts', async () => {
    const res = await request(app).get('/api/v1/posts');
    expect(res.status).toBe(200);
    expect(res.body.data).toHaveLength(1);
    expect(res.body.data[0].title).toBe('Test Post');
    expect(res.body.data[0].tags).toHaveLength(1);
    expect(res.body.pagination.totalElements).toBe(1);
  });

  it('GET /api/v1/posts/:slug returns post and increments views', async () => {
    const res = await request(app).get('/api/v1/posts/test-post');
    expect(res.status).toBe(200);
    expect(res.body.title).toBe('Test Post');
    expect(res.body.viewCount).toBe(6); // 5 + 1 increment
    expect(res.body.tags).toHaveLength(1);
  });

  it('GET /api/v1/posts/:slug returns 404 for unknown slug', async () => {
    const res = await request(app).get('/api/v1/posts/nonexistent');
    expect(res.status).toBe(404);
  });
});
