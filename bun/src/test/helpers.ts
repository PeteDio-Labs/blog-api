import type { Pool, PoolClient, QueryResult, QueryResultRow } from 'pg';

type QueryHandler = (text: string, params?: unknown[]) => QueryResultRow[];

export function createMockPool(handler: QueryHandler): Pool {
  const mockQuery = (text: string, params?: unknown[]): QueryResult => {
    const rows = handler(text, params);
    return {
      rows,
      command: '',
      rowCount: rows.length,
      oid: 0,
      fields: [],
    } as QueryResult;
  };

  const mockClient: Partial<PoolClient> = {
    query: mockQuery as PoolClient['query'],
    release: () => {},
  };

  return {
    query: mockQuery,
    connect: async () => mockClient as PoolClient,
    end: async () => {},
    on: () => {},
  } as unknown as Pool;
}

export function defaultQueryHandler(text: string, params?: unknown[]): QueryResultRow[] {
  // schema_migrations
  if (text.includes('schema_migrations') && text.includes('CREATE TABLE')) return [];
  if (text.includes('schema_migrations') && text.includes('SELECT')) return [];
  if (text.includes('schema_migrations') && text.includes('INSERT')) return [];

  // COUNT queries
  if (text.includes('COUNT(*)')) return [{ count: '0' }];
  if (text.includes('COUNT(DISTINCT')) return [{ count: '0' }];

  // SELECT 1 (health check)
  if (text.trim() === 'SELECT 1') return [{ '?column?': 1 }];

  // Default: empty result
  void params;
  return [];
}
