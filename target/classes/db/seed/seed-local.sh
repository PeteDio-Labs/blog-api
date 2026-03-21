#!/bin/bash
# =============================================================================
# Local Blog Post Seeder
# =============================================================================
# Seeds the local PostgreSQL database with blog posts.
# Uses ON CONFLICT clauses for idempotent seeding (safe to run multiple times).
#
# Usage:
#   cd /Users/pedrodelgadillo/PeteDio-Labs/developed-apps/blog
#   ./api/src/main/resources/db/seed/seed-local.sh
#
# Or via docker-compose exec:
#   docker-compose exec postgres psql -U petedillo -d petedillo_blog -f /seed.sql
# =============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SEED_SQL="$SCRIPT_DIR/seed.sql"

# Default connection parameters (for docker-compose setup)
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-petedillo_blog}"
DB_USER="${DB_USER:-petedillo}"
DB_PASSWORD="${DB_PASSWORD:-dev_password}"

echo ""
echo "============================================================"
echo "Blog Post Seeder - Local Development"
echo "============================================================"
echo "Database: $DB_HOST:$DB_PORT/$DB_NAME"
echo "SQL File: $SEED_SQL"
echo "============================================================"
echo ""

# Check if docker-compose postgres is running
if docker-compose ps 2>/dev/null | grep -q "postgres"; then
    echo "Using docker-compose exec to run seeding..."
    docker-compose exec -T postgres psql -U "$DB_USER" -d "$DB_NAME" < "$SEED_SQL"
elif command -v psql &> /dev/null; then
    echo "Using direct psql connection..."
    PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" < "$SEED_SQL"
else
    echo "ERROR: Neither docker-compose postgres nor psql is available."
    echo "Please ensure either:"
    echo "  1. Docker containers are running: docker-compose up -d"
    echo "  2. psql is installed: brew install postgresql"
    exit 1
fi

echo ""
echo "============================================================"
echo "Seeding complete! Checking results..."
echo "============================================================"
echo ""

# Show summary
if docker-compose ps 2>/dev/null | grep -q "postgres"; then
    docker-compose exec -T postgres psql -U "$DB_USER" -d "$DB_NAME" -c "
        SELECT 'Posts' as entity, COUNT(*) as count FROM blog_posts
        UNION ALL
        SELECT 'Tags', COUNT(*) FROM tags
        UNION ALL
        SELECT 'Media', COUNT(*) FROM blog_media
        UNION ALL
        SELECT 'Post-Tags', COUNT(*) FROM post_tags;
    "
else
    PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "
        SELECT 'Posts' as entity, COUNT(*) as count FROM blog_posts
        UNION ALL
        SELECT 'Tags', COUNT(*) FROM tags
        UNION ALL
        SELECT 'Media', COUNT(*) FROM blog_media
        UNION ALL
        SELECT 'Post-Tags', COUNT(*) FROM post_tags;
    "
fi

echo ""
echo "✓ Done!"
