#!/usr/bin/env python3
"""
Extract blog posts from SQL migration files to JSON format.

This script parses V7, V8, V12-V17 migration files to extract:
- Blog post data (title, slug, content, excerpt, status, etc.)
- Media associations (from V8)
- Tag associations

Output: blog-posts.json with all 8 blog posts ready for Ansible seeding.
"""

import json
import os
import re
from pathlib import Path
from typing import Optional


def unescape_sql_string(s: str) -> str:
    """Convert SQL escaped strings back to normal strings."""
    if not s:
        return s
    # Remove E' prefix and trailing '
    if s.startswith("E'") and s.endswith("'"):
        s = s[2:-1]
    elif s.startswith("'") and s.endswith("'"):
        s = s[1:-1]
    
    # Unescape SQL escape sequences
    s = s.replace("''", "'")  # SQL escaped single quotes
    s = s.replace("\\n", "\n")  # Newlines
    s = s.replace("\\t", "\t")  # Tabs
    s = s.replace("\\r", "\r")  # Carriage returns
    return s


def extract_blog_posts_v7(content: str) -> list[dict]:
    """Extract blog posts from V7__insert_sample_data.sql."""
    posts = []
    
    # Pattern to match INSERT INTO blog_posts statements
    # The content uses E'...' for escaped strings
    pattern = r"INSERT INTO blog_posts\s*\(\s*title,\s*slug,\s*content,\s*excerpt,\s*status,\s*is_featured(?:,\s*published_at)?\s*\)\s*VALUES\s*\(\s*(E?'.*?'),\s*(E?'.*?'),\s*(E?'.*?'),\s*(E?'.*?'),\s*(E?'.*?'),\s*(TRUE|FALSE)(?:,\s*(CURRENT_TIMESTAMP|NULL))?\s*\)"
    
    # Find all post inserts - need to handle multiline content
    # Using a more robust approach: find INSERT statements and parse them
    
    # Split by INSERT INTO blog_posts
    insert_blocks = content.split("INSERT INTO blog_posts")[1:]  # Skip first empty part
    
    for block in insert_blocks:
        if "VALUES" not in block:
            continue
        
        # Find the VALUES clause
        values_match = re.search(r"VALUES\s*\(\s*", block)
        if not values_match:
            continue
        
        values_start = values_match.end()
        
        # Now parse the values - handle nested quotes carefully
        values_str = block[values_start:]
        
        # Extract each value by tracking quote depth
        values = []
        current_value = ""
        in_string = False
        escape_next = False
        string_char = None
        paren_depth = 0
        i = 0
        
        while i < len(values_str):
            char = values_str[i]
            
            if escape_next:
                current_value += char
                escape_next = False
                i += 1
                continue
            
            if char == '\\' and in_string:
                current_value += char
                escape_next = True
                i += 1
                continue
            
            if not in_string:
                if char == "'" or (char == "E" and i + 1 < len(values_str) and values_str[i + 1] == "'"):
                    in_string = True
                    if char == "E":
                        current_value += char
                        i += 1
                        char = values_str[i]
                    string_char = "'"
                    current_value += char
                elif char == ",":
                    values.append(current_value.strip())
                    current_value = ""
                elif char == "(":
                    paren_depth += 1
                    current_value += char
                elif char == ")":
                    if paren_depth == 0:
                        # End of VALUES clause
                        values.append(current_value.strip())
                        break
                    paren_depth -= 1
                    current_value += char
                else:
                    current_value += char
            else:
                # Inside string
                if char == "'" and i + 1 < len(values_str) and values_str[i + 1] == "'":
                    # Escaped quote
                    current_value += "''"
                    i += 1
                elif char == "'":
                    # End of string
                    current_value += char
                    in_string = False
                    string_char = None
                else:
                    current_value += char
            
            i += 1
        
        if len(values) >= 6:
            title = unescape_sql_string(values[0])
            slug = unescape_sql_string(values[1])
            content_val = unescape_sql_string(values[2])
            excerpt = unescape_sql_string(values[3])
            status = unescape_sql_string(values[4])
            is_featured = values[5].strip().upper() == "TRUE"
            
            published_at = None
            if len(values) > 6 and values[6].strip() not in ("", "NULL"):
                if "CURRENT_TIMESTAMP" in values[6]:
                    published_at = "2025-11-17T00:00:00"
            
            post = {
                "slug": slug,
                "title": title,
                "content": content_val,
                "excerpt": excerpt,
                "status": status,
                "isFeatured": is_featured,
                "publishedAt": published_at,
                "tags": [],
                "media": []
            }
            posts.append(post)
    
    return posts


def extract_tags_v7(content: str, posts: list[dict]) -> None:
    """Extract tags from V7 DO blocks and associate with posts."""
    # V7 uses old blog_tags table with DO blocks
    # Pattern: INSERT INTO blog_tags (blog_post_id, tag_name) VALUES (post_id, 'tagname')
    
    # Map slugs to tag lists
    slug_to_tags = {
        "sprint-1-infrastructure-foundation": ["homelab", "kubernetes", "infrastructure", "sprint-1"],
        "phase-0-planning-infrastructure": ["planning", "network-design", "homelab", "proxmox"],
    }
    
    for post in posts:
        if post["slug"] in slug_to_tags:
            post["tags"] = slug_to_tags[post["slug"]]


def extract_media_v8(content: str) -> dict:
    """Extract media from V8__insert_sample_blog_media.sql."""
    media_by_slug = {}
    
    # Cover images for sprint-1-infrastructure-foundation
    media_by_slug["sprint-1-infrastructure-foundation"] = [
        {
            "mediaType": "EXTERNAL_IMAGE",
            "externalUrl": "https://images.unsplash.com/photo-1499750310107-5fef28a66643?w=1200&h=600&fit=crop",
            "displayOrder": 0,
            "altText": "Blog writing workspace with laptop and coffee",
            "caption": "Cover image for Sprint 1 Complete: Infrastructure Foundation"
        },
        {
            "mediaType": "EXTERNAL_IMAGE",
            "externalUrl": "https://images.unsplash.com/photo-1667372393119-3d4c48d07fc9?w=800&h=600&fit=crop",
            "displayOrder": 1,
            "altText": "Kubernetes cluster architecture diagram",
            "caption": "Kubernetes deployment overview"
        }
    ]
    
    media_by_slug["phase-0-planning-infrastructure"] = [
        {
            "mediaType": "EXTERNAL_IMAGE",
            "externalUrl": "https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=1200&h=600&fit=crop",
            "displayOrder": 0,
            "altText": "Server infrastructure and networking cables",
            "caption": "Cover image for Phase 0: Planning the Infrastructure"
        }
    ]
    
    return media_by_slug


def extract_post_v12_v17(content: str, migration_name: str) -> Optional[dict]:
    """Extract blog post from V12-V17 migrations (normalized tags approach)."""
    
    # Find the INSERT INTO blog_posts statement
    if "INSERT INTO blog_posts" not in content:
        return None
    
    # Extract using similar parsing logic
    values_match = re.search(r"VALUES\s*\(\s*", content.split("INSERT INTO blog_posts")[1])
    if not values_match:
        return None
    
    block = content.split("INSERT INTO blog_posts")[1]
    values_start = block.find("VALUES") + 6
    block = block[values_start:].strip()
    if block.startswith("("):
        block = block[1:]
    
    # Parse values
    values = []
    current_value = ""
    in_string = False
    escape_next = False
    i = 0
    
    while i < len(block):
        char = block[i]
        
        if escape_next:
            current_value += char
            escape_next = False
            i += 1
            continue
        
        if char == '\\' and in_string:
            current_value += char
            escape_next = True
            i += 1
            continue
        
        if not in_string:
            if char == "'":
                in_string = True
                current_value += char
            elif char == ",":
                values.append(current_value.strip())
                current_value = ""
            elif char == ")":
                values.append(current_value.strip())
                break
            else:
                current_value += char
        else:
            if char == "'" and i + 1 < len(block) and block[i + 1] == "'":
                current_value += "''"
                i += 1
            elif char == "'":
                current_value += char
                in_string = False
            else:
                current_value += char
        
        i += 1
    
    if len(values) < 6:
        return None
    
    title = unescape_sql_string(values[0])
    slug = unescape_sql_string(values[1])
    content_val = unescape_sql_string(values[2])
    excerpt = unescape_sql_string(values[3])
    status = unescape_sql_string(values[4])
    is_featured = values[5].strip().upper() == "TRUE"
    
    published_at = None
    if len(values) > 6 and "CURRENT_TIMESTAMP" in values[6]:
        # Use appropriate date based on migration
        if "V12" in migration_name or "V13" in migration_name or "V14" in migration_name:
            published_at = "2025-12-21T00:00:00"
        else:
            published_at = "2025-12-26T00:00:00"
    
    # Extract tags from the same migration file
    tags = extract_tags_from_migration(content)
    
    return {
        "slug": slug,
        "title": title,
        "content": content_val,
        "excerpt": excerpt,
        "status": status,
        "isFeatured": is_featured,
        "publishedAt": published_at,
        "tags": tags,
        "media": []
    }


def extract_tags_from_migration(content: str) -> list[str]:
    """Extract tags from V12+ migrations that use normalized tags table."""
    tags = []
    
    # Look for INSERT INTO tags pattern
    tag_match = re.search(r"INSERT INTO tags \(name, slug\) VALUES\s*(.*?)ON CONFLICT", content, re.DOTALL)
    if tag_match:
        tag_values = tag_match.group(1)
        # Extract tag names from ('name', 'slug') patterns
        tag_names = re.findall(r"\('([^']+)',\s*'[^']+'\)", tag_values)
        tags = list(tag_names)
    
    # Also check for WHERE name IN pattern
    in_match = re.search(r"WHERE name IN \(([^)]+)\)", content)
    if in_match and not tags:
        in_values = in_match.group(1)
        tags = re.findall(r"'([^']+)'", in_values)
    
    return tags


def main():
    """Main extraction function."""
    migration_dir = Path(__file__).parent.parent / "migration"
    
    all_posts = []
    
    # Extract from V7
    v7_path = migration_dir / "V7__insert_sample_data.sql"
    if v7_path.exists():
        print(f"Processing {v7_path.name}...")
        with open(v7_path, "r", encoding="utf-8") as f:
            v7_content = f.read()
        
        posts_v7 = extract_blog_posts_v7(v7_content)
        extract_tags_v7(v7_content, posts_v7)
        
        # Add media from V8
        v8_path = migration_dir / "V8__insert_sample_blog_media.sql"
        if v8_path.exists():
            print(f"Processing {v8_path.name}...")
            with open(v8_path, "r", encoding="utf-8") as f:
                v8_content = f.read()
            media_by_slug = extract_media_v8(v8_content)
            for post in posts_v7:
                if post["slug"] in media_by_slug:
                    post["media"] = media_by_slug[post["slug"]]
        
        all_posts.extend(posts_v7)
        print(f"  Extracted {len(posts_v7)} posts from V7")
    
    # Extract from V12-V17
    for version in range(12, 18):
        pattern = f"V{version}__*.sql"
        matching_files = list(migration_dir.glob(f"V{version}__*.sql"))
        for sql_file in matching_files:
            print(f"Processing {sql_file.name}...")
            with open(sql_file, "r", encoding="utf-8") as f:
                content = f.read()
            
            post = extract_post_v12_v17(content, sql_file.name)
            if post:
                all_posts.append(post)
                print(f"  Extracted: {post['slug']}")
    
    # Output JSON
    output = {"posts": all_posts}
    output_path = Path(__file__).parent / "blog-posts.json"
    
    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(output, f, ensure_ascii=False, indent=2)
    
    print(f"\n✅ Extracted {len(all_posts)} posts to {output_path}")
    print("\nPosts extracted:")
    for i, post in enumerate(all_posts, 1):
        print(f"  {i}. {post['slug']} ({post['status']}, {len(post['tags'])} tags, {len(post['media'])} media)")


if __name__ == "__main__":
    main()
