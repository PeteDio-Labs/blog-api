import { describe, it, expect } from 'vitest';
import request from 'supertest';
import { createApp } from '../../app.ts';
import { createMockPool } from '../../test/helpers.ts';
import type { QueryResultRow } from 'pg';

const now = new Date();

const samplePost = {
  id: 1,
  title: 'New Post',
  slug: 'new-post',
  content: 'Content here',
  excerpt: 'Excerpt',
  status: 'DRAFT',
  source: 'manual',
  is_featured: false,
  view_count: 0,
  created_at: now,
  updated_at: now,
  published_at: null,
};

function adminQueryHandler(text: string, params?: unknown[]): QueryResultRow[] {
  if (text.trim() === 'SELECT 1') return [{ '?column?': 1 }];

  // Count for admin list
  if (text.includes('COUNT(*)') && !text.includes("status = 'PUBLISHED'")) {
    return [{ count: '1' }];
  }
  if (text.includes('COUNT(*)') && text.includes("status = 'PUBLISHED'")) {
    return [{ count: '0' }];
  }

  // Admin list all
  if (text.includes('FROM blog_posts') && text.includes('ORDER BY created_at')) {
    return [samplePost];
  }

  // Get by ID
  if (text.includes('WHERE id = $1') && text.includes('SELECT *')) {
    if (params?.[0] === 1) return [samplePost];
    return [];
  }

  // Insert
  if (text.includes('INSERT INTO blog_posts')) {
    return [samplePost];
  }

  // Tags resolution
  if (text.includes('INSERT INTO tags')) {
    return [{ id: 1 }];
  }

  // post_tags
  if (text.includes('INSERT INTO post_tags')) return [];
  if (text.includes('DELETE FROM post_tags')) return [];

  // Tag count update
  if (text.includes('UPDATE tags SET post_count')) return [];

  // Tags for post
  if (text.includes('FROM tags t') && text.includes('post_tags')) {
    return [{ id: 1, name: 'test', slug: 'test', post_count: 1, post_id: 1 }];
  }

  // Delete
  if (text.includes('DELETE FROM blog_posts')) {
    if (params?.[0] === 1) return [{ id: 1 }];
    return [];
  }

  // Update
  if (text.includes('UPDATE blog_posts SET')) return [];

  // Admin tags list
  if (text.includes('FROM tags ORDER BY')) {
    return [{ id: 1, name: 'test', slug: 'test', post_count: 1 }];
  }

  return [];
}

describe('Admin Routes', () => {
  const pool = createMockPool(adminQueryHandler);
  const app = createApp(pool);

  it('POST /api/v1/admin/posts creates a post', async () => {
    const res = await request(app)
      .post('/api/v1/admin/posts')
      .send({
        title: 'New Post',
        content: 'Content here',
        tags: ['test'],
      });
    expect(res.status).toBe(201);
    expect(res.body.title).toBe('New Post');
    expect(res.body.status).toBe('DRAFT');
  });

  it('POST /api/v1/admin/posts rejects invalid body', async () => {
    const res = await request(app)
      .post('/api/v1/admin/posts')
      .send({ title: '' });
    expect(res.status).toBe(400);
    expect(res.body.error).toBe('Validation failed');
  });

  it('GET /api/v1/admin/posts lists all posts', async () => {
    const res = await request(app).get('/api/v1/admin/posts');
    expect(res.status).toBe(200);
    expect(res.body.data).toHaveLength(1);
  });

  it('GET /api/v1/admin/posts/:id returns post by ID', async () => {
    const res = await request(app).get('/api/v1/admin/posts/1');
    expect(res.status).toBe(200);
    expect(res.body.title).toBe('New Post');
  });

  it('GET /api/v1/admin/posts/:id returns 404 for unknown ID', async () => {
    const res = await request(app).get('/api/v1/admin/posts/999');
    expect(res.status).toBe(404);
  });

  it('GET /api/v1/admin/posts/:id returns 400 for non-numeric', async () => {
    const res = await request(app).get('/api/v1/admin/posts/abc');
    expect(res.status).toBe(400);
  });

  it('GET /api/v1/admin/tags lists tags', async () => {
    const res = await request(app).get('/api/v1/admin/tags');
    expect(res.status).toBe(200);
    expect(res.body).toHaveLength(1);
    expect(res.body[0].name).toBe('test');
  });
});
