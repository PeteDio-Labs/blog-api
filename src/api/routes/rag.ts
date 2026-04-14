import { Router } from 'express';
import { z } from 'zod';
import type { RagService } from '../../services/ragService.ts';
import type { PostService } from '../../services/posts.ts';

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

export function createRagRouter(ragService: RagService, postService: PostService): Router {
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

  // POST /api/v1/rag/ingest-all-posts
  // One-shot backfill: re-ingests all PUBLISHED posts into the vector store.
  // Safe to call multiple times — ingest() deletes existing chunks before re-inserting.
  router.post('/ingest-all-posts', async (_req, res) => {
    const { posts } = await postService.listAll({ status: 'PUBLISHED', page: 1, size: 500, offset: 0 });
    let ingested = 0;
    let failed = 0;
    for (const post of posts) {
      try {
        const text = [post.title, post.content].filter(Boolean).join('\n\n');
        await ragService.ingest({
          postId: post.id,
          text,
          sourceType: 'post',
          sourceRef: post.slug,
        });
        ingested++;
      } catch {
        failed++;
      }
    }
    res.json({ ingested, failed, total: posts.length });
  });

  return router;
}
