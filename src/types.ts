import { z } from 'zod';

export const PostStatus = z.enum(['DRAFT', 'UNLISTED', 'PUBLISHED', 'ARCHIVED']);
export type PostStatus = z.infer<typeof PostStatus>;

export const PostSource = z.enum(['seed', 'blog-agent', 'manual']);
export type PostSource = z.infer<typeof PostSource>;

export const CreatePostSchema = z.object({
  title: z.string().min(1).max(500),
  content: z.string().default(''),
  excerpt: z.string().max(1000).optional(),
  status: PostStatus.default('DRAFT'),
  source: PostSource.default('manual'),
  isFeatured: z.boolean().default(false),
  tags: z.array(z.string()).default([]),
});
export type CreatePostInput = z.infer<typeof CreatePostSchema>;

export const UpdatePostSchema = z.object({
  title: z.string().min(1).max(500).optional(),
  content: z.string().optional(),
  excerpt: z.string().max(1000).optional(),
  status: PostStatus.optional(),
  isFeatured: z.boolean().optional(),
  tags: z.array(z.string()).optional(),
});
export type UpdatePostInput = z.infer<typeof UpdatePostSchema>;

export interface TagRow {
  id: number;
  name: string;
  slug: string;
  post_count: number;
}

export interface TagResponse {
  id: number;
  name: string;
  slug: string;
  postCount: number;
}

export interface PostRow {
  id: number;
  title: string;
  slug: string;
  content: string;
  excerpt: string | null;
  status: string;
  source: string;
  is_featured: boolean;
  view_count: number;
  created_at: Date;
  updated_at: Date;
  published_at: Date | null;
}

export interface PostResponse {
  id: number;
  title: string;
  slug: string;
  content: string;
  excerpt: string | null;
  status: string;
  source: string;
  isFeatured: boolean;
  viewCount: number;
  createdAt: string;
  updatedAt: string;
  publishedAt: string | null;
  tags: TagResponse[];
}
