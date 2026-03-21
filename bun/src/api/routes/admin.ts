import { Router } from 'express';
import type { PostService } from '../../services/posts.ts';
import { CreatePostSchema, UpdatePostSchema } from '../../types.ts';
import { parsePagination, paginate } from '../../utils/pagination.ts';

export function createAdminRouter(postService: PostService): Router {
  const router = Router();

  // POST /api/v1/admin/posts — create
  router.post('/posts', async (req, res) => {
    const parsed = CreatePostSchema.safeParse(req.body);
    if (!parsed.success) {
      res.status(400).json({ error: 'Validation failed', details: parsed.error.issues });
      return;
    }
    const post = await postService.create(parsed.data);
    res.status(201).json(post);
  });

  // GET /api/v1/admin/posts — all statuses, filterable
  router.get('/posts', async (req, res) => {
    const { page, size, offset } = parsePagination(
      req.query as { page?: string; size?: string },
    );
    const { posts, total } = await postService.listAll({
      status: req.query.status as string | undefined,
      source: req.query.source as string | undefined,
      search: req.query.search as string | undefined,
      page,
      size,
      offset,
    });
    res.json(paginate(posts, page, size, total));
  });

  // GET /api/v1/admin/posts/:id — by numeric ID
  router.get('/posts/:id', async (req, res) => {
    const id = Number(req.params.id);
    if (isNaN(id)) {
      res.status(400).json({ error: 'Invalid post ID' });
      return;
    }
    const post = await postService.getById(id);
    if (!post) {
      res.status(404).json({ error: 'Post not found' });
      return;
    }
    res.json(post);
  });

  // PUT /api/v1/admin/posts/:id — update
  router.put('/posts/:id', async (req, res) => {
    const id = Number(req.params.id);
    if (isNaN(id)) {
      res.status(400).json({ error: 'Invalid post ID' });
      return;
    }
    const parsed = UpdatePostSchema.safeParse(req.body);
    if (!parsed.success) {
      res.status(400).json({ error: 'Validation failed', details: parsed.error.issues });
      return;
    }
    const post = await postService.update(id, parsed.data);
    if (!post) {
      res.status(404).json({ error: 'Post not found' });
      return;
    }
    res.json(post);
  });

  // DELETE /api/v1/admin/posts/:id
  router.delete('/posts/:id', async (req, res) => {
    const id = Number(req.params.id);
    if (isNaN(id)) {
      res.status(400).json({ error: 'Invalid post ID' });
      return;
    }
    const deleted = await postService.delete(id);
    if (!deleted) {
      res.status(404).json({ error: 'Post not found' });
      return;
    }
    res.status(204).end();
  });

  // POST /api/v1/admin/posts/:id/publish — shortcut
  router.post('/posts/:id/publish', async (req, res) => {
    const id = Number(req.params.id);
    if (isNaN(id)) {
      res.status(400).json({ error: 'Invalid post ID' });
      return;
    }
    const post = await postService.publish(id);
    if (!post) {
      res.status(404).json({ error: 'Post not found' });
      return;
    }
    res.json(post);
  });

  // GET /api/v1/admin/tags
  router.get('/tags', async (_req, res) => {
    const tags = await postService.listTags();
    res.json(tags);
  });

  return router;
}
