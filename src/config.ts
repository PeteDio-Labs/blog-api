export const config = {
  port: Number(process.env.PORT) || 8080,
  environment: process.env.APP_ENVIRONMENT || 'dev',
  version: process.env.APP_VERSION || '1.0.0',

  db: {
    host: process.env.DB_HOST || 'localhost',
    port: Number(process.env.DB_PORT) || 5432,
    database: process.env.DB_NAME || 'petedillo_blog',
    user: process.env.DB_USERNAME || 'blog_app',
    password: process.env.DB_PASSWORD || 'dev_app_password',
  },

  cors: {
    origins: (process.env.CORS_ORIGINS || '*').split(','),
  },

  log: {
    level: process.env.LOG_LEVEL || 'info',
  },

  internalUrl: process.env.INTERNAL_URL || 'http://192.168.50.241',
} as const;
