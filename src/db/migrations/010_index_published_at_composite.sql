-- Composite index covering the hot list path:
--   WHERE status = 'PUBLISHED' ORDER BY published_at DESC NULLS LAST
-- The standalone status + published_at indexes can't satisfy both
-- predicate and sort in a single scan; this one can, eliminating the
-- top-N sort once the table grows past a few hundred posts.
CREATE INDEX IF NOT EXISTS idx_blog_posts_status_published_at
  ON blog_posts (status, published_at DESC NULLS LAST);
