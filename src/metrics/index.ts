import { Registry, Counter, Gauge, Histogram } from 'prom-client';

export const register = new Registry();

export const appUp = new Gauge({
  name: 'blog_api_up',
  help: '1=running, 0=offline',
  registers: [register],
});

export const httpRequestsTotal = new Counter({
  name: 'blog_api_http_requests_total',
  help: 'Total HTTP requests',
  labelNames: ['method', 'route', 'status'] as const,
  registers: [register],
});

export const httpRequestDuration = new Histogram({
  name: 'blog_api_http_request_duration_seconds',
  help: 'HTTP request duration in seconds',
  labelNames: ['method', 'route'] as const,
  buckets: [0.001, 0.005, 0.01, 0.05, 0.1, 0.5, 1, 2, 5],
  registers: [register],
});

export const postViewsTotal = new Counter({
  name: 'blog_api_post_views_total',
  help: 'Total post view increments',
  labelNames: ['slug'] as const,
  registers: [register],
});

export async function getMetrics(): Promise<string> {
  return register.metrics();
}
