import pino from 'pino';
import type { Logger as PinoLogger } from 'pino';
import { config } from '../config.ts';

const pinoInstance: PinoLogger = pino({
  level: config.log.level,
  name: 'blog-api',
  transport:
    config.environment !== 'production'
      ? {
          target: 'pino-pretty',
          options: { colorize: true, translateTime: 'SYS:standard' },
        }
      : undefined,
});

export const logger = {
  debug: (msg: string, obj?: Record<string, unknown>) =>
    obj ? pinoInstance.debug(obj, msg) : pinoInstance.debug(msg),
  info: (msg: string, obj?: Record<string, unknown>) =>
    obj ? pinoInstance.info(obj, msg) : pinoInstance.info(msg),
  warn: (msg: string, obj?: Record<string, unknown>) =>
    obj ? pinoInstance.warn(obj, msg) : pinoInstance.warn(msg),
  error: (msg: string, obj?: Record<string, unknown>) =>
    obj ? pinoInstance.error(obj, msg) : pinoInstance.error(msg),
  raw: (msg: string) => console.log(msg),
  pino: pinoInstance,
};
