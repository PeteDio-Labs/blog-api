-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Embeddings table for RAG — stores chunked text + 768-dim nomic-embed-text vectors
CREATE TABLE IF NOT EXISTS embeddings (
  id           SERIAL PRIMARY KEY,
  post_id      INTEGER REFERENCES blog_posts(id) ON DELETE CASCADE,
  source_type  VARCHAR(50) NOT NULL DEFAULT 'post',   -- 'post' | 'session' | 'doc'
  source_ref   TEXT NOT NULL DEFAULT '',              -- slug, filename, etc.
  chunk_index  INTEGER NOT NULL DEFAULT 0,
  chunk_text   TEXT NOT NULL,
  embedding    vector(768),
  created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- IVFFlat index for cosine similarity search (good up to ~1M vectors)
CREATE INDEX IF NOT EXISTS embeddings_vector_idx
  ON embeddings USING ivfflat (embedding vector_cosine_ops)
  WITH (lists = 100);

CREATE INDEX IF NOT EXISTS embeddings_post_id_idx ON embeddings (post_id);
CREATE INDEX IF NOT EXISTS embeddings_source_type_idx ON embeddings (source_type);
