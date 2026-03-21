---
title: Welcome to PeteDio Labs
slug: welcome-to-petedio-labs
status: PUBLISHED
tags: [meta, homelab]
excerpt: The blog is live. Here's what PeteDio Labs is, what we're building, and why this site exists.
---

This is PeteDio Labs — a homelab where services talk to each other, infrastructure is code, and an LLM writes the blog posts.

## What Is This Place

PeteDio Labs is a two-node Proxmox cluster running MicroK8s, ArgoCD, and a growing stack of services that automate everything from Discord bot commands to deploy notifications. The goal isn't to run production workloads for paying customers — it's to build a real, observable, GitOps-managed platform and learn by doing.

The stack right now:

- **Kubernetes**: 2-node MicroK8s cluster with ArgoCD, Sealed Secrets, and MetalLB
- **Pete Bot**: A Discord bot powered by Ollama that can query infrastructure, trigger ArgoCD syncs, search the web, and hold a conversation
- **Mission Control**: A backend API + web dashboard for managing the homelab — ArgoCD, Kubernetes, Proxmox, Prometheus all in one place
- **Notification Service**: An event bus that stores infra events and fans them out to subscribers
- **Web Search Service**: Multi-provider search router with SearXNG, Brave, Tavily, and SerpAPI
- **Blog Agent**: A multi-agent LLM pipeline that generates blog content from cluster data and deploy events
- **This Blog**: The content surface for all of it

## Why a Blog

Every homelab project generates knowledge — what worked, what broke, what the architecture looks like now versus three months ago. That knowledge usually lives in commit messages, Discord conversations, and docs files that slowly drift from reality.

This blog is the fix. The blog-agent watches deploy events, reads cluster state, and writes posts about what changed and why. Weekly recaps summarize the week. Tutorials get written when something non-obvious gets built. Docs audits catch drift before it becomes tribal knowledge.

The human still reviews and approves everything. For now.

## What's Coming

The immediate roadmap:

1. **Blog API rewrite** — Spring Boot is getting replaced with Bun/Express to match the rest of the stack
2. **Reader UI** — The frontend is being redesigned as a clean, content-focused reading experience
3. **Automated changelogs** — Deploy events trigger blog posts automatically
4. **Weekly recaps** — Every Monday, a summary of what happened in the homelab

Further out, the homelab gets a proactive ops brain (the Discord bot monitors events and investigates incidents), a safety-gated autonomy runtime (automated remediations that graduate from human-approved to auto-run), and eventually a coding agent that reads GitHub issues and ships PRs.

But first — the blog works, and you're reading the first post. Welcome.
