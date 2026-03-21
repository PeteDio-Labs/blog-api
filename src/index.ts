import 'dotenv/config';
import { createApp } from './app.ts';
import { pool } from './db/pool.ts';
import { runMigrations } from './db/migrate.ts';
import { seedPosts } from './db/seed.ts';
import { logger } from './utils/logger.ts';
import { config } from './config.ts';
import { appUp } from './metrics/index.ts';

async function main(): Promise<void> {
  logger.raw('Starting blog-api...');

  // Run migrations
  logger.info('Running migrations...');
  await runMigrations(pool);

  // Seed posts
  logger.info('Checking seed data...');
  await seedPosts(pool);

  // Start server
  const app = createApp(pool);
  app.listen(config.port, () => {
    appUp.set(1);
    logger.raw('═══════════════════════════════════════════════════════');
    logger.raw(`  blog-api v${config.version}`);
    logger.raw(`  Started: ${new Date().toISOString()}`);
    logger.raw(`  Port: ${config.port}`);
    logger.raw(`  Environment: ${config.environment}`);
    logger.raw('═══════════════════════════════════════════════════════');
  });
}

main().catch((err) => {
  logger.error('Failed to start blog-api', { error: String(err) });
  process.exit(1);
});
