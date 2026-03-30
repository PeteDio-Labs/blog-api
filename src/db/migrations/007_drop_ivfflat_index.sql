-- Drop IVFFlat index — sequential scan is faster with small datasets (< 10K rows)
-- Re-add IVFFlat once embeddings table has > 10K rows
DROP INDEX IF EXISTS embeddings_vector_idx;
