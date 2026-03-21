---
title: How the Blog Writes Itself
slug: how-the-blog-writes-itself
status: PUBLISHED
tags: [blog-agent, llm, ollama, automation]
excerpt: The blog-agent is a multi-agent LLM pipeline that turns deploy events and cluster data into blog posts. Here's how it works.
---

This blog isn't written by hand — or at least, it won't be for much longer. A service called `blog-agent` watches what happens in the homelab and writes posts about it.

## The Pipeline

Blog-agent runs a three-stage pipeline for every piece of content:

```
Trigger (event / cron / API call)
    │
    ▼
┌──────────────┐
│ Context Agent │  Gathers data: cluster state, git history, recent events
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ Writer Agent  │  Produces a markdown draft from the context
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ Review Agent  │  Quality gate: accuracy, formatting, tone
└──────┬───────┘
       │
       ▼
Save draft to Blog API → Notify owner on Discord
```

Each agent is a separate LLM call with its own system prompt and focus area. The Review Agent can send the draft back to the Writer for up to two revision rounds before accepting it.

## What Triggers a Post

| Content Type | Trigger | What Gets Written |
|---|---|---|
| Deploy changelog | A service gets deployed (notification-service event) | What changed, what version, which service |
| Weekly recap | Cron — every Monday at noon CST | Summary of the week's deploys, incidents, and changes |
| How-to / tutorial | Manual API call with a topic | Step-by-step guide written from cluster context |
| Docs audit | Scheduled or on-demand | Drift report — what's documented vs. what's actually running |

## The LLM

Blog-agent uses a custom Ollama model called `petedio-writer`, built on `qwen2.5:7b`. The model runs on pve01 at `192.168.50.59:11434` — no external API calls, no tokens, no rate limits.

The Modelfile sets the tone:

- Temperature 0.8 for some personality without hallucination
- Repeat penalty 1.15 to avoid the LLM loop of repeating itself
- 4096 token context for longer posts

The system prompt tells the writer to use a direct, technical, first-person voice — like you're explaining something to a friend who also runs a homelab. No corporate speak, no filler paragraphs.

## Human in the Loop

Every post starts as a `DRAFT`. The blog-agent saves it to the Blog API and sends a Discord DM:

> Draft ready: "How the Blog Writes Itself" — review at dev.petedillo.com

The human (me) reads it, edits if needed, and publishes. The goal is to eventually graduate to fully autonomous publishing once the quality bar is consistently met — but for now, nothing goes live without a review.

## What's Next

The pipeline is deployed and generating content. The remaining work:

- **Event-driven changelogs**: Wire the SSE listener to notification-service deploy events so changelogs write themselves on every deploy
- **Weekly recap cron**: The scheduler exists but needs the new Blog API (currently being rewritten from Spring Boot to Bun/Express)
- **Draft review UI**: The blog frontend needs a draft view so posts can be reviewed and published from the browser instead of the API

The blog-agent is the first step toward a homelab that documents itself. The coding agent — which will read GitHub issues, write code, and ship PRs — is next.
