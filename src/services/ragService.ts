import type { Pool } from 'pg';
import { logger } from '../utils/logger.ts';
import { config } from '../config.ts';

export interface RagChunk {
  id: number;
  postId: number | null;
  sourceType: string;
  sourceRef: string;
  chunkIndex: number;
  chunkText: string;
  similarity: number;
}

// Split text into overlapping chunks targeting ~500 tokens (approx 400 words / ~2000 chars)
// Splits on paragraph boundaries first, hard-caps at MAX_CHUNK_CHARS.
const MAX_CHUNK_CHARS = 2000;
const OVERLAP_CHARS = 200;
const MIN_CHUNK_CHARS = 100;

function chunkText(text: string): string[] {
  const paragraphs = text.split(/\n{2,}/).map((p) => p.trim()).filter((p) => p.length >= MIN_CHUNK_CHARS);
  const chunks: string[] = [];
  let current = '';

  for (const para of paragraphs) {
    if (current.length + para.length + 2 <= MAX_CHUNK_CHARS) {
      current = current ? `${current}\n\n${para}` : para;
    } else {
      if (current) chunks.push(current);
      // Para itself too long — hard split with overlap
      if (para.length > MAX_CHUNK_CHARS) {
        let pos = 0;
        while (pos < para.length) {
          chunks.push(para.slice(pos, pos + MAX_CHUNK_CHARS));
          pos += MAX_CHUNK_CHARS - OVERLAP_CHARS;
        }
        current = '';
      } else {
        current = para;
      }
    }
  }
  if (current) chunks.push(current);
  return chunks;
}

async function embedText(text: string): Promise<number[]> {
  const response = await fetch(`${config.ollama.url}/api/embed`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ model: 'nomic-embed-text', input: text }),
    signal: AbortSignal.timeout(30_000),
  });
  if (!response.ok) {
    throw new Error(`Ollama embed failed: ${response.status} ${await response.text()}`);
  }
  const data = await response.json() as { embeddings: number[][] };
  return data.embeddings[0]!;
}

export class RagService {
  constructor(private pool: Pool) {}

  async ingest(opts: {
    postId?: number;
    text: string;
    sourceType: string;
    sourceRef: string;
  }): Promise<number> {
    const { postId, text, sourceType, sourceRef } = opts;
    const chunks = chunkText(text);
    if (chunks.length === 0) return 0;

    // Delete existing chunks for this source before re-ingesting
    if (postId) {
      await this.pool.query('DELETE FROM embeddings WHERE post_id = $1', [postId]);
    } else {
      await this.pool.query(
        'DELETE FROM embeddings WHERE source_type = $1 AND source_ref = $2',
        [sourceType, sourceRef],
      );
    }

    let ingested = 0;
    for (let i = 0; i < chunks.length; i++) {
      try {
        const vector = await embedText(chunks[i]!);
        const vectorLiteral = `[${vector.join(',')}]`;
        await this.pool.query(
          `INSERT INTO embeddings (post_id, source_type, source_ref, chunk_index, chunk_text, embedding)
           VALUES ($1, $2, $3, $4, $5, $6::vector)`,
          [postId ?? null, sourceType, sourceRef, i, chunks[i], vectorLiteral],
        );
        ingested++;
      } catch (err) {
        logger.warn(`Failed to embed chunk ${i} for ${sourceRef}: ${err instanceof Error ? err.message : String(err)}`);
      }
    }

    logger.info(`RAG ingest complete: ${sourceRef} (${sourceType}) — ${ingested}/${chunks.length} chunks`);
    return ingested;
  }

  async query(opts: {
    queryText: string;
    topK?: number;
    sourceTypes?: string[];
  }): Promise<RagChunk[]> {
    const { queryText, topK = 5, sourceTypes } = opts;

    const queryVector = await embedText(queryText);
    const vectorLiteral = `[${queryVector.join(',')}]`;

    const sourceFilter = sourceTypes && sourceTypes.length > 0
      ? `AND source_type = ANY($3)`
      : '';
    const params: unknown[] = [vectorLiteral, topK];
    if (sourceTypes && sourceTypes.length > 0) params.push(sourceTypes);

    const client = await this.pool.connect();
    let rows: Array<{
      id: number;
      post_id: number | null;
      source_type: string;
      source_ref: string;
      chunk_index: number;
      chunk_text: string;
      similarity: number;
    }>;
    try {
      // Increase ivfflat probes so queries work with small datasets
      await client.query('SET ivfflat.probes = 10');
      const result = await client.query<typeof rows[0]>(
        `SELECT id, post_id, source_type, source_ref, chunk_index, chunk_text,
                1 - (embedding <=> $1::vector) AS similarity
         FROM embeddings
         WHERE embedding IS NOT NULL ${sourceFilter}
         ORDER BY embedding <=> $1::vector
         LIMIT $2`,
        params,
      );
      rows = result.rows;
    } finally {
      client.release();
    }

    return rows.map((r) => ({
      id: r.id,
      postId: r.post_id,
      sourceType: r.source_type,
      sourceRef: r.source_ref,
      chunkIndex: r.chunk_index,
      chunkText: r.chunk_text,
      similarity: r.similarity,
    }));
  }
}
