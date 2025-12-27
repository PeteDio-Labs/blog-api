-- Create normalized tags table
CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    post_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create many-to-many join table
CREATE TABLE post_tags (
    post_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (post_id, tag_id),
    CONSTRAINT fk_post_tags_post FOREIGN KEY (post_id)
        REFERENCES blog_posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_post_tags_tag FOREIGN KEY (tag_id)
        REFERENCES tags(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_post_tags_post ON post_tags(post_id);
CREATE INDEX idx_post_tags_tag ON post_tags(tag_id);
CREATE INDEX idx_tags_name ON tags(name);
CREATE INDEX idx_tags_slug ON tags(slug);

-- Migrate existing data from blog_tags to new schema
INSERT INTO tags (name, slug, created_at)
SELECT
    LOWER(tag_name) as name,
    LOWER(REGEXP_REPLACE(tag_name, '[^a-zA-Z0-9]+', '-', 'g')) as slug,
    MIN(created_at) as created_at
FROM blog_tags
GROUP BY LOWER(tag_name), LOWER(REGEXP_REPLACE(tag_name, '[^a-zA-Z0-9]+', '-', 'g'))
ORDER BY LOWER(tag_name);

-- Populate post_tags junction table
INSERT INTO post_tags (post_id, tag_id, created_at)
SELECT DISTINCT bt.blog_post_id, t.id, bt.created_at
FROM blog_tags bt
JOIN tags t ON LOWER(bt.tag_name) = t.name;

-- Update post counts
UPDATE tags t
SET post_count = (
    SELECT COUNT(*) FROM post_tags pt WHERE pt.tag_id = t.id
);

-- Drop old blog_tags table
DROP TABLE IF EXISTS blog_tags;

-- Comments
COMMENT ON TABLE tags IS 'Normalized tags table with post counts';
COMMENT ON TABLE post_tags IS 'Many-to-many relationship between posts and tags';
COMMENT ON COLUMN tags.post_count IS 'Cached count of posts with this tag';
