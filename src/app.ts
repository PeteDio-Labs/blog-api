import express from 'express';
import type { Application, Request, Response, NextFunction } from 'express';
import cors from 'cors';
import helmet from 'helmet';
import morgan from 'morgan';
import type { Pool } from 'pg';
import { logger } from './utils/logger.ts';
import { config } from './config.ts';
import { createRoutes } from './api/routes/index.ts';
import { PostService } from './services/posts.ts';
import { RagService } from './services/ragService.ts';
import { httpRequestsTotal, httpRequestDuration } from './metrics/index.ts';

export function createApp(pool: Pool): Application {
  const app: Application = express();

  const morganStream = {
    write: (message: string) => logger.info(message.trim()),
  };

  // Middleware
  app.use(helmet());
  app.use(cors({ origin: config.cors.origins }));
  app.use(express.json());
  app.use(express.urlencoded({ extended: true }));
  app.use(morgan('combined', { stream: morganStream }));

  // Metrics middleware
  app.use((req: Request, res: Response, next: NextFunction) => {
    const start = process.hrtime.bigint();
    res.on('finish', () => {
      const duration = Number(process.hrtime.bigint() - start) / 1e9;
      const route = req.route?.path || req.path;
      httpRequestsTotal.inc({
        method: req.method,
        route,
        status: String(res.statusCode),
      });
      httpRequestDuration.observe({ method: req.method, route }, duration);
    });
    next();
  });

  // Environment headers
  app.use((_req: Request, res: Response, next: NextFunction) => {
    res.setHeader('X-Environment', config.environment);
    res.setHeader('X-API-Version', config.version);
    next();
  });

  // Root
  app.get('/', (_req: Request, res: Response) => {
    res.json({
      name: 'blog-api',
      version: config.version,
      environment: config.environment,
      status: 'running',
    });
  });

  // Routes
  const ragService = new RagService(pool);
  const postService = new PostService(pool, ragService);
  app.use(createRoutes(pool, postService, ragService));

  // 404
  app.use((req: Request, res: Response) => {
    res.status(404).json({ error: 'Not Found', path: req.path });
  });

  // Error handler
  app.use((err: Error, _req: Request, res: Response, _next: NextFunction) => {
    logger.error('Unhandled error', { error: err.message, stack: err.stack });
    res.status(500).json({ error: 'Internal Server Error' });
  });

  return app;
}
