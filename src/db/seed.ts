import { readdir, readFile } from 'node:fs/promises';
import { join } from 'node:path';
import type { Pool } from 'pg';
import { logger } from '../utils/logger.ts';

interface SeedFrontmatter {
  title: string;
  slug: string;
  status: string;
  tags: string[];
  excerpt: string;
}

function parseFrontmatter(raw: string): {
  meta: SeedFrontmatter;
  content: string;
} {
  const match = raw.match(/^---\n([\s\S]*?)\n---\n([\s\S]*)$/);
  if (!match) throw new Error('Invalid frontmatter');

  const lines = match[1]!.split('\n');
  const meta: Record<string, unknown> = {};

  for (const line of lines) {
    const colonIdx = line.indexOf(':');
    if (colonIdx === -1) continue;
    const key = line.slice(0, colonIdx).trim();
    let value: unknown = line.slice(colonIdx + 1).trim();

    // Handle YAML arrays: [tag1, tag2]
    if (typeof value === 'string' && value.startsWith('[') && value.endsWith(']')) {
      value = value
        .slice(1, -1)
        .split(',')
        .map((s) => s.trim());
    }
    // Strip surrounding quotes
    if (typeof value === 'string' && value.startsWith('"') && value.endsWith('"')) {
      value = value.slice(1, -1);
    }

    meta[key] = value;
  }

  return {
    meta: meta as unknown as SeedFrontmatter,
    content: match[2]!.trim(),
  };
}

export async function seedPosts(pool: Pool): Promise<void> {
  // seed-posts lives next to src/ in the project root
  const seedDir = join(process.cwd(), 'seed-posts');

  let files: string[];
  try {
    files = (await readdir(seedDir))
      .filter((f) => f.endsWith('.md'))
      .sort();
  } catch {
    logger.info('No seed-posts directory found, skipping seed');
    return;
  }

  if (files.length === 0) return;

  const { rows: existing } = await pool.query<{ count: string }>(
    'SELECT COUNT(*) as count FROM blog_posts',
  );
  if (Number(existing[0]?.count) > 0) {
    logger.info('Database already has posts, skipping seed');
    return;
  }

  for (const file of files) {
    const raw = await readFile(join(seedDir, file), 'utf-8');
    const { meta, content } = parseFrontmatter(raw);

    const publishedAt = meta.status === 'PUBLISHED' ? new Date() : null;

    const { rows } = await pool.query<{ id: number }>(
      `INSERT INTO blog_posts (title, slug, content, excerpt, status, source, published_at)
       VALUES ($1, $2, $3, $4, $5, 'seed', $6)
       RETURNING id`,
      [meta.title, meta.slug, content, meta.excerpt, meta.status, publishedAt],
    );
    const postId = rows[0]!.id;

    // Resolve tags
    for (const tagName of meta.tags) {
      const normalized = tagName.toLowerCase().trim();
      const tagSlug = normalized.replace(/\s+/g, '-');

      const { rows: tagRows } = await pool.query<{ id: number }>(
        `INSERT INTO tags (name, slug, post_count)
         VALUES ($1, $2, 1)
         ON CONFLICT (name) DO UPDATE SET post_count = tags.post_count + 1
         RETURNING id`,
        [normalized, tagSlug],
      );
      const tagId = tagRows[0]!.id;

      await pool.query(
        'INSERT INTO post_tags (post_id, tag_id) VALUES ($1, $2) ON CONFLICT DO NOTHING',
        [postId, tagId],
      );
    }

    logger.info(`Seeded: ${meta.title}`);
  }
}
