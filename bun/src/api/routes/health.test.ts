import { describe, it, expect } from 'vitest';
import request from 'supertest';
import { createApp } from '../../app.ts';
import { createMockPool, defaultQueryHandler } from '../../test/helpers.ts';

describe('Health Routes', () => {
  const pool = createMockPool(defaultQueryHandler);
  const app = createApp(pool);

  it('GET /health returns UP', async () => {
    const res = await request(app).get('/health');
    expect(res.status).toBe(200);
    expect(res.body.status).toBe('UP');
  });

  it('GET /health/live returns UP', async () => {
    const res = await request(app).get('/health/live');
    expect(res.status).toBe(200);
    expect(res.body.status).toBe('UP');
  });

  it('GET /health/ready returns UP with DB connected', async () => {
    const res = await request(app).get('/health/ready');
    expect(res.status).toBe(200);
    expect(res.body.status).toBe('UP');
    expect(res.body.database).toBe('connected');
  });

  it('GET /metrics returns prometheus format', async () => {
    const res = await request(app).get('/metrics');
    expect(res.status).toBe(200);
    expect(res.headers['content-type']).toContain('text/plain');
    expect(res.text).toContain('blog_api_up');
  });
});
