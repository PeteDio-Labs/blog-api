import { Router } from 'express';
import type { PostService } from '../../services/posts.ts';
import { parsePagination, paginate } from '../../utils/pagination.ts';

export function createSearchRouter(postService: PostService): Router {
  const router = Router();

  // GET /api/v1/search?q=...
  router.get('/', async (req, res) => {
    const q = req.query.q as string | undefined;
    if (!q || q.trim().length === 0) {
      res.status(400).json({ error: 'Query parameter "q" is required' });
      return;
    }

    const { page, size, offset } = parsePagination(
      req.query as { page?: string; size?: string },
    );
    const { posts, total } = await postService.search(q.trim(), page, size, offset);
    res.json(paginate(posts, page, size, total));
  });

  return router;
}
