import type { Pool } from 'pg';
import type {
  PostRow,
  PostResponse,
  TagRow,
  TagResponse,
  CreatePostInput,
  UpdatePostInput,
} from '../types.ts';

function generateSlug(title: string): string {
  return title
    .toLowerCase()
    .replace(/[^a-z0-9\s-]/g, '')
    .replace(/\s+/g, '-')
    .replace(/^-+|-+$/g, '');
}

function mapTag(row: TagRow): TagResponse {
  return {
    id: row.id,
    name: row.name,
    slug: row.slug,
    postCount: row.post_count,
  };
}

function mapPost(row: PostRow, tags: TagResponse[]): PostResponse {
  return {
    id: row.id,
    title: row.title,
    slug: row.slug,
    content: row.content,
    excerpt: row.excerpt,
    status: row.status,
    source: row.source,
    isFeatured: row.is_featured,
    viewCount: row.view_count,
    createdAt: row.created_at.toISOString(),
    updatedAt: row.updated_at.toISOString(),
    publishedAt: row.published_at?.toISOString() ?? null,
    tags,
  };
}

export class PostService {
  constructor(private pool: Pool) {}

  private async getTagsForPost(postId: number): Promise<TagResponse[]> {
    const { rows } = await this.pool.query<TagRow>(
      `SELECT t.* FROM tags t
       JOIN post_tags pt ON t.id = pt.tag_id
       WHERE pt.post_id = $1
       ORDER BY t.name`,
      [postId],
    );
    return rows.map(mapTag);
  }

  private async getTagsForPosts(
    postIds: number[],
  ): Promise<Map<number, TagResponse[]>> {
    if (postIds.length === 0) return new Map();

    const { rows } = await this.pool.query<TagRow & { post_id: number }>(
      `SELECT t.*, pt.post_id FROM tags t
       JOIN post_tags pt ON t.id = pt.tag_id
       WHERE pt.post_id = ANY($1)
       ORDER BY t.name`,
      [postIds],
    );

    const map = new Map<number, TagResponse[]>();
    for (const row of rows) {
      const postId = row.post_id;
      if (!map.has(postId)) map.set(postId, []);
      map.get(postId)!.push(mapTag(row));
    }
    return map;
  }

  private async resolveTags(tagNames: string[]): Promise<number[]> {
    const tagIds: number[] = [];
    for (const name of tagNames) {
      const normalized = name.toLowerCase().trim();
      if (!normalized) continue;
      const tagSlug = normalized.replace(/\s+/g, '-');

      const { rows } = await this.pool.query<{ id: number }>(
        `INSERT INTO tags (name, slug, post_count)
         VALUES ($1, $2, 0)
         ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name
         RETURNING id`,
        [normalized, tagSlug],
      );
      tagIds.push(rows[0]!.id);
    }
    return tagIds;
  }

  private async syncPostTags(
    postId: number,
    tagIds: number[],
  ): Promise<void> {
    // Remove old tags
    await this.pool.query('DELETE FROM post_tags WHERE post_id = $1', [postId]);

    // Decrement all tag counts, then re-increment for assigned tags
    // Simpler: recalculate post_count for affected tags
    for (const tagId of tagIds) {
      await this.pool.query(
        'INSERT INTO post_tags (post_id, tag_id) VALUES ($1, $2) ON CONFLICT DO NOTHING',
        [postId, tagId],
      );
    }

    // Recalculate post_count for all tags
    await this.pool.query(
      `UPDATE tags SET post_count = (
         SELECT COUNT(*) FROM post_tags WHERE tag_id = tags.id
       )`,
    );
  }

  // --- Public endpoints ---

  async listPublished(
    _page: number,
    size: number,
    offset: number,
  ): Promise<{ posts: PostResponse[]; total: number }> {
    const countResult = await this.pool.query<{ count: string }>(
      "SELECT COUNT(*) as count FROM blog_posts WHERE status = 'PUBLISHED'",
    );
    const total = Number(countResult.rows[0]?.count ?? 0);

    const { rows } = await this.pool.query<PostRow>(
      `SELECT * FROM blog_posts WHERE status = 'PUBLISHED'
       ORDER BY published_at DESC NULLS LAST
       LIMIT $1 OFFSET $2`,
      [size, offset],
    );

    const tagsMap = await this.getTagsForPosts(rows.map((r) => r.id));
    const posts = rows.map((r) => mapPost(r, tagsMap.get(r.id) ?? []));

    return { posts, total };
  }

  async getBySlug(slug: string): Promise<PostResponse | null> {
    const { rows } = await this.pool.query<PostRow>(
      "SELECT * FROM blog_posts WHERE slug = $1 AND status IN ('PUBLISHED', 'UNLISTED')",
      [slug],
    );
    if (rows.length === 0) return null;

    const post = rows[0]!;
    // Increment view count
    await this.pool.query(
      'UPDATE blog_posts SET view_count = view_count + 1 WHERE id = $1',
      [post.id],
    );
    post.view_count += 1;

    const tags = await this.getTagsForPost(post.id);
    return mapPost(post, tags);
  }

  async search(
    query: string,
    _page: number,
    size: number,
    offset: number,
  ): Promise<{ posts: PostResponse[]; total: number }> {
    const pattern = `%${query.toLowerCase()}%`;

    const countResult = await this.pool.query<{ count: string }>(
      `SELECT COUNT(DISTINCT p.id) as count FROM blog_posts p
       LEFT JOIN post_tags pt ON p.id = pt.post_id
       LEFT JOIN tags t ON pt.tag_id = t.id
       WHERE p.status = 'PUBLISHED'
         AND (LOWER(p.title) LIKE $1 OR LOWER(p.content) LIKE $1 OR LOWER(t.name) LIKE $1)`,
      [pattern],
    );
    const total = Number(countResult.rows[0]?.count ?? 0);

    const { rows } = await this.pool.query<PostRow>(
      `SELECT DISTINCT p.* FROM blog_posts p
       LEFT JOIN post_tags pt ON p.id = pt.post_id
       LEFT JOIN tags t ON pt.tag_id = t.id
       WHERE p.status = 'PUBLISHED'
         AND (LOWER(p.title) LIKE $1 OR LOWER(p.content) LIKE $1 OR LOWER(t.name) LIKE $1)
       ORDER BY p.published_at DESC NULLS LAST
       LIMIT $2 OFFSET $3`,
      [pattern, size, offset],
    );

    const tagsMap = await this.getTagsForPosts(rows.map((r) => r.id));
    const posts = rows.map((r) => mapPost(r, tagsMap.get(r.id) ?? []));

    return { posts, total };
  }

  // --- Admin endpoints ---

  async listAll(filters: {
    status?: string;
    source?: string;
    search?: string;
    page: number;
    size: number;
    offset: number;
  }): Promise<{ posts: PostResponse[]; total: number }> {
    const conditions: string[] = [];
    const params: unknown[] = [];
    let paramIdx = 1;

    if (filters.status) {
      conditions.push(`status = $${paramIdx++}`);
      params.push(filters.status);
    }
    if (filters.source) {
      conditions.push(`source = $${paramIdx++}`);
      params.push(filters.source);
    }
    if (filters.search) {
      conditions.push(`(LOWER(title) LIKE $${paramIdx} OR LOWER(content) LIKE $${paramIdx})`);
      params.push(`%${filters.search.toLowerCase()}%`);
      paramIdx++;
    }

    const where = conditions.length > 0 ? `WHERE ${conditions.join(' AND ')}` : '';

    const countResult = await this.pool.query<{ count: string }>(
      `SELECT COUNT(*) as count FROM blog_posts ${where}`,
      params,
    );
    const total = Number(countResult.rows[0]?.count ?? 0);

    const { rows } = await this.pool.query<PostRow>(
      `SELECT * FROM blog_posts ${where}
       ORDER BY created_at DESC
       LIMIT $${paramIdx} OFFSET $${paramIdx + 1}`,
      [...params, filters.size, filters.offset],
    );

    const tagsMap = await this.getTagsForPosts(rows.map((r) => r.id));
    const posts = rows.map((r) => mapPost(r, tagsMap.get(r.id) ?? []));

    return { posts, total };
  }

  async getById(id: number): Promise<PostResponse | null> {
    const { rows } = await this.pool.query<PostRow>(
      'SELECT * FROM blog_posts WHERE id = $1',
      [id],
    );
    if (rows.length === 0) return null;

    const tags = await this.getTagsForPost(id);
    return mapPost(rows[0]!, tags);
  }

  async create(input: CreatePostInput): Promise<PostResponse> {
    const slug = generateSlug(input.title);
    const publishedAt = input.status === 'PUBLISHED' ? new Date() : null;

    const { rows } = await this.pool.query<PostRow>(
      `INSERT INTO blog_posts (title, slug, content, excerpt, status, source, is_featured, published_at)
       VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
       RETURNING *`,
      [
        input.title,
        slug,
        input.content,
        input.excerpt ?? null,
        input.status,
        input.source,
        input.isFeatured,
        publishedAt,
      ],
    );

    const post = rows[0]!;
    if (input.tags.length > 0) {
      const tagIds = await this.resolveTags(input.tags);
      await this.syncPostTags(post.id, tagIds);
    }

    const tags = await this.getTagsForPost(post.id);
    return mapPost(post, tags);
  }

  async update(id: number, input: UpdatePostInput): Promise<PostResponse | null> {
    const existing = await this.pool.query<PostRow>(
      'SELECT * FROM blog_posts WHERE id = $1',
      [id],
    );
    if (existing.rows.length === 0) return null;

    const sets: string[] = [];
    const params: unknown[] = [];
    let paramIdx = 1;

    if (input.title !== undefined) {
      sets.push(`title = $${paramIdx++}`);
      params.push(input.title);
    }
    if (input.content !== undefined) {
      sets.push(`content = $${paramIdx++}`);
      params.push(input.content);
    }
    if (input.excerpt !== undefined) {
      sets.push(`excerpt = $${paramIdx++}`);
      params.push(input.excerpt);
    }
    if (input.status !== undefined) {
      sets.push(`status = $${paramIdx++}`);
      params.push(input.status);
      if (
        input.status === 'PUBLISHED' &&
        existing.rows[0]!.published_at === null
      ) {
        sets.push(`published_at = $${paramIdx++}`);
        params.push(new Date());
      }
    }
    if (input.isFeatured !== undefined) {
      sets.push(`is_featured = $${paramIdx++}`);
      params.push(input.isFeatured);
    }

    if (sets.length > 0) {
      params.push(id);
      await this.pool.query(
        `UPDATE blog_posts SET ${sets.join(', ')} WHERE id = $${paramIdx}`,
        params,
      );
    }

    if (input.tags !== undefined) {
      const tagIds = await this.resolveTags(input.tags);
      await this.syncPostTags(id, tagIds);
    }

    return this.getById(id);
  }

  async delete(id: number): Promise<boolean> {
    const result = await this.pool.query(
      'DELETE FROM blog_posts WHERE id = $1',
      [id],
    );
    // Recalculate tag counts after deletion
    await this.pool.query(
      `UPDATE tags SET post_count = (
         SELECT COUNT(*) FROM post_tags WHERE tag_id = tags.id
       )`,
    );
    return (result.rowCount ?? 0) > 0;
  }

  async publish(id: number): Promise<PostResponse | null> {
    return this.update(id, { status: 'PUBLISHED' });
  }

  async listTags(): Promise<TagResponse[]> {
    const { rows } = await this.pool.query<TagRow>(
      'SELECT * FROM tags ORDER BY post_count DESC, name ASC',
    );
    return rows.map(mapTag);
  }

  async getPostCount(): Promise<number> {
    const { rows } = await this.pool.query<{ count: string }>(
      'SELECT COUNT(*) as count FROM blog_posts',
    );
    return Number(rows[0]?.count ?? 0);
  }

  async getMostRecentDate(): Promise<string | null> {
    const { rows } = await this.pool.query<{ published_at: Date | null }>(
      "SELECT published_at FROM blog_posts WHERE status = 'PUBLISHED' ORDER BY published_at DESC NULLS LAST LIMIT 1",
    );
    return rows[0]?.published_at?.toISOString() ?? null;
  }
}
