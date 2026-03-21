FROM oven/bun:1-alpine AS builder
WORKDIR /app
COPY bun/package.json bun/bun.lock ./
RUN bun install --frozen-lockfile
COPY bun/tsconfig.json ./
COPY bun/src/ src/
RUN bun build src/index.ts --outdir dist --target node

FROM oven/bun:1-alpine
WORKDIR /app
COPY --from=builder /app/dist/ dist/
COPY --from=builder /app/node_modules/ node_modules/
COPY --from=builder /app/package.json ./
COPY bun/src/db/migrations/ dist/db/migrations/
COPY seed-posts/ seed-posts/
EXPOSE 8080
CMD ["bun", "run", "dist/index.js"]
