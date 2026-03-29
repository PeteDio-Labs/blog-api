import { Router } from 'express';
import { z } from 'zod';
import type { RagService } from '../../services/ragService.ts';

const IngestSchema = z.object({
  postId: z.number().int().positive().optional(),
  text: z.string().min(1),
  sourceType: z.enum(['post', 'session', 'doc']).default('post'),
  sourceRef: z.string().min(1),
});

const QuerySchema = z.object({
  query: z.string().min(1),
  topK: z.number().int().min(1).max(20).default(5),
  sourceTypes: z.array(z.enum(['post', 'session', 'doc'])).optional(),
});

export function createRagRouter(ragService: RagService): Router {
  const router = Router();

  // POST /api/v1/rag/ingest
  router.post('/ingest', async (req, res) => {
    const parsed = IngestSchema.safeParse(req.body);
    if (!parsed.success) {
      res.status(400).json({ error: 'Validation failed', details: parsed.error.issues });
      return;
    }
    const ingested = await ragService.ingest(parsed.data);
    res.json({ chunks: ingested });
  });

  // POST /api/v1/rag/query
  router.post('/query', async (req, res) => {
    const parsed = QuerySchema.safeParse(req.body);
    if (!parsed.success) {
      res.status(400).json({ error: 'Validation failed', details: parsed.error.issues });
      return;
    }
    const { query, topK, sourceTypes } = parsed.data;
    const results = await ragService.query({ queryText: query, topK, sourceTypes });
    res.json({ results, count: results.length });
  });

  return router;
}
