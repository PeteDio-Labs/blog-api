import { Router } from 'express';
import type { PostService } from '../../services/posts.ts';
import { parsePagination, paginate } from '../../utils/pagination.ts';
import { postViewsTotal } from '../../metrics/index.ts';

export function createPostsRouter(postService: PostService): Router {
  const router = Router();

  // GET /api/v1/posts — paginated, PUBLISHED only
  router.get('/', async (req, res) => {
    const { page, size, offset } = parsePagination(
      req.query as { page?: string; size?: string },
    );
    const { posts, total } = await postService.listPublished(page, size, offset);
    res.json(paginate(posts, page, size, total));
  });

  // GET /api/v1/posts/tag/:tag — paginated by tag slug
  router.get('/tag/:tag', async (req, res) => {
    const { page, size, offset } = parsePagination(
      req.query as { page?: string; size?: string },
    );
    const { posts, total } = await postService.listByTag(
      req.params.tag,
      page,
      size,
      offset,
    );
    res.json(paginate(posts, page, size, total));
  });

  // GET /api/v1/posts/:slug — returns PUBLISHED or UNLISTED, increments view_count
  router.get('/:slug', async (req, res) => {
    const post = await postService.getBySlug(req.params.slug);
    if (!post) {
      res.status(404).json({ error: 'Post not found' });
      return;
    }
    postViewsTotal.inc({ slug: post.slug });
    res.json(post);
  });

  return router;
}
