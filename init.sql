-- ============================================================================
-- Database Initialization Script
-- Description: Create database and user for development
-- ============================================================================

-- Database is created by docker-compose environment variable
-- Skip database creation to avoid duplicate errors

DO
$$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'blog_app') THEN
   CREATE ROLE blog_app WITH
    LOGIN
    PASSWORD 'DevApp!2025@SecurePwd'
    NOSUPERUSER
    NOCREATEDB
    NOCREATEROLE
    NOINHERIT
    NOREPLICATION
    NOBYPASSRLS;
  END IF;
END
$$;

GRANT CONNECT ON DATABASE petedillo_blog TO blog_app;

\c petedillo_blog

-- Grant schema permissions
GRANT USAGE ON SCHEMA public TO blog_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO blog_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO blog_app;

-- Set default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO blog_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO blog_app;

-- Additional: Grant schema usage to public for Flyway migrations (petedillo runs migrations)
GRANT USAGE ON SCHEMA public TO blog_app;
ALTER DEFAULT PRIVILEGES FOR USER petedillo IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO blog_app;
ALTER DEFAULT PRIVILEGES FOR USER petedillo IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO blog_app;
