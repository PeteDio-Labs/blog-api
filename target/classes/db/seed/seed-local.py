#!/usr/bin/env python3
"""
Local Blog Post Seeder
======================
Seeds the local PostgreSQL database with blog posts from blog-posts.json.
Uses ON CONFLICT clauses for idempotent seeding (safe to run multiple times).

Usage:
    # From the blog directory:
    cd /Users/pedrodelgadillo/PeteDio-Labs/developed-apps/blog
    python3 api/src/main/resources/db/seed/seed-local.py
    
    # Or with custom connection:
    python3 seed-local.py --host localhost --port 5432 --db petedillo_blog --user petedillo --password dev_password

Requirements:
    pip3 install psycopg2-binary
"""

import json
import os
import sys
import argparse
from pathlib import Path
from datetime import datetime

try:
    import psycopg2
    from psycopg2.extras import execute_values
except ImportError:
    print("ERROR: psycopg2-binary is required. Install with:")
    print("  pip3 install psycopg2-binary")
    sys.exit(1)


def load_blog_posts(json_path: str) -> dict:
    """Load blog posts from JSON file."""
    with open(json_path, 'r', encoding='utf-8') as f:
        return json.load(f)


def seed_tags(cursor, posts: list) -> dict:
    """Insert all unique tags and return tag name -> id mapping."""
    # Collect all unique tags
    all_tags = set()
    for post in posts:
        for tag in post.get('tags', []):
            all_tags.add(tag)
    
    tag_map = {}
    for tag_name in all_tags:
        slug = tag_name.lower().replace(' ', '-')
        cursor.execute("""
            INSERT INTO tags (name, slug, post_count)
            VALUES (%s, %s, 0)
            ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name
            RETURNING id
        """, (tag_name, slug))
        tag_id = cursor.fetchone()[0]
        tag_map[tag_name] = tag_id
        print(f"  ✓ Tag: {tag_name} (id={tag_id})")
    
    return tag_map


def seed_post(cursor, post: dict, tag_map: dict) -> int:
    """Insert a single blog post and return its ID."""
    # Parse publishedAt if present
    published_at = None
    if post.get('publishedAt'):
        try:
            published_at = datetime.fromisoformat(post['publishedAt'].replace('Z', '+00:00'))
        except (ValueError, TypeError):
            published_at = None
    
    cursor.execute("""
        INSERT INTO blog_posts (slug, title, content, excerpt, status, is_featured, published_at, created_at, updated_at)
        VALUES (%s, %s, %s, %s, %s, %s, %s, NOW(), NOW())
        ON CONFLICT (slug) DO NOTHING
        RETURNING id
    """, (
        post['slug'],
        post['title'],
        post['content'],
        post.get('excerpt', ''),
        post.get('status', 'DRAFT'),
        post.get('isFeatured', False),
        published_at
    ))
    
    result = cursor.fetchone()
    if result:
        post_id = result[0]
        print(f"  ✓ Post: {post['slug']} (id={post_id})")
        return post_id
    else:
        # Post already exists, get its ID
        cursor.execute("SELECT id FROM blog_posts WHERE slug = %s", (post['slug'],))
        result = cursor.fetchone()
        if result:
            print(f"  ○ Post already exists: {post['slug']} (id={result[0]})")
            return result[0]
        return None


def seed_post_tags(cursor, post_id: int, tags: list, tag_map: dict):
    """Link post to tags via post_tags junction table."""
    for tag_name in tags:
        tag_id = tag_map.get(tag_name)
        if tag_id:
            cursor.execute("""
                INSERT INTO post_tags (post_id, tag_id)
                VALUES (%s, %s)
                ON CONFLICT (post_id, tag_id) DO NOTHING
            """, (post_id, tag_id))


def seed_media(cursor, post_id: int, media_items: list):
    """Insert media items for a post."""
    for media in media_items:
        media_type = media.get('mediaType', 'EXTERNAL_IMAGE')
        external_url = media.get('externalUrl')
        display_order = media.get('displayOrder', 0)
        alt_text = media.get('altText', '')
        caption = media.get('caption', '')
        
        cursor.execute("""
            INSERT INTO blog_media (blog_post_id, media_type, external_url, display_order, alt_text, caption, created_at)
            VALUES (%s, %s, %s, %s, %s, %s, NOW())
            ON CONFLICT DO NOTHING
        """, (post_id, media_type, external_url, display_order, alt_text, caption))


def update_tag_counts(cursor):
    """Update post_count for all tags."""
    cursor.execute("""
        UPDATE tags
        SET post_count = (
            SELECT COUNT(*) FROM post_tags WHERE tag_id = tags.id
        )
    """)
    print("  ✓ Updated tag post counts")


def main():
    parser = argparse.ArgumentParser(description='Seed local PostgreSQL with blog posts')
    parser.add_argument('--host', default='localhost', help='Database host (default: localhost)')
    parser.add_argument('--port', type=int, default=5432, help='Database port (default: 5432)')
    parser.add_argument('--db', default='petedillo_blog', help='Database name (default: petedillo_blog)')
    parser.add_argument('--user', default='petedillo', help='Database user (default: petedillo)')
    parser.add_argument('--password', default='dev_password', help='Database password (default: dev_password)')
    parser.add_argument('--json', default=None, help='Path to blog-posts.json (default: auto-detect)')
    args = parser.parse_args()
    
    # Find blog-posts.json
    if args.json:
        json_path = args.json
    else:
        # Try to find it relative to this script
        script_dir = Path(__file__).parent
        json_path = script_dir / 'blog-posts.json'
        if not json_path.exists():
            print(f"ERROR: Cannot find blog-posts.json at {json_path}")
            sys.exit(1)
    
    print(f"\n{'='*60}")
    print("Blog Post Seeder - Local Development")
    print(f"{'='*60}")
    print(f"Database: {args.host}:{args.port}/{args.db}")
    print(f"JSON Source: {json_path}")
    print(f"{'='*60}\n")
    
    # Load posts
    data = load_blog_posts(json_path)
    posts = data.get('posts', [])
    print(f"Found {len(posts)} posts to seed\n")
    
    # Connect to database
    try:
        conn = psycopg2.connect(
            host=args.host,
            port=args.port,
            dbname=args.db,
            user=args.user,
            password=args.password
        )
        conn.autocommit = False
        cursor = conn.cursor()
        print("✓ Connected to database\n")
    except psycopg2.Error as e:
        print(f"ERROR: Failed to connect to database: {e}")
        sys.exit(1)
    
    try:
        # Step 1: Seed tags
        print("Seeding tags...")
        tag_map = seed_tags(cursor, posts)
        print(f"  Total: {len(tag_map)} tags\n")
        
        # Step 2: Seed posts with media
        print("Seeding posts...")
        for post in posts:
            post_id = seed_post(cursor, post, tag_map)
            if post_id:
                # Link tags
                seed_post_tags(cursor, post_id, post.get('tags', []), tag_map)
                # Add media
                seed_media(cursor, post_id, post.get('media', []))
        print(f"  Total: {len(posts)} posts processed\n")
        
        # Step 3: Update tag counts
        print("Updating statistics...")
        update_tag_counts(cursor)
        print()
        
        # Commit
        conn.commit()
        print("✓ All changes committed successfully!\n")
        
        # Summary
        cursor.execute("SELECT COUNT(*) FROM blog_posts")
        post_count = cursor.fetchone()[0]
        cursor.execute("SELECT COUNT(*) FROM tags")
        tag_count = cursor.fetchone()[0]
        cursor.execute("SELECT COUNT(*) FROM blog_media")
        media_count = cursor.fetchone()[0]
        
        print(f"{'='*60}")
        print("Database Summary")
        print(f"{'='*60}")
        print(f"  Posts: {post_count}")
        print(f"  Tags:  {tag_count}")
        print(f"  Media: {media_count}")
        print(f"{'='*60}\n")
        
    except Exception as e:
        conn.rollback()
        print(f"ERROR: {e}")
        print("Transaction rolled back.")
        sys.exit(1)
    finally:
        cursor.close()
        conn.close()


if __name__ == '__main__':
    main()
