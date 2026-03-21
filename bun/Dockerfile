FROM oven/bun:1-alpine AS builder
WORKDIR /app
COPY package.json bun.lock ./
RUN bun install --frozen-lockfile
COPY tsconfig.json ./
COPY src/ src/
RUN bun build src/index.ts --outdir dist --target node

FROM oven/bun:1-alpine
WORKDIR /app
COPY --from=builder /app/dist/ dist/
COPY --from=builder /app/node_modules/ node_modules/
COPY package.json ./
COPY seed-posts/ seed-posts/
EXPOSE 8080
CMD ["bun", "run", "dist/index.js"]
