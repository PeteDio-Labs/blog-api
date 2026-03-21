---
title: "Homelab Inventory: What's Running and Where"
slug: homelab-inventory
status: PUBLISHED
tags: [infrastructure, homelab, kubernetes, proxmox]
excerpt: A snapshot of every node, VM, LXC, namespace, and service running in PeteDio Labs as of March 2026.
---

This is the current state of the homelab. Everything here is GitOps-managed, observable, and automated — if something's not in this list, it's not running.

## Proxmox Cluster

Two nodes, both running Proxmox VE:

| Node | Role | CPU | RAM | Storage |
|------|------|-----|-----|---------|
| **pve01** | Primary hypervisor | AMD Ryzen | 64 GB | 4.4 TB (LVM on /dev/sdb) |
| **pve02** | Secondary hypervisor | AMD Ryzen | 32 GB | 1 TB NVMe |

### VMs

| VMID | Name | Node | Purpose | IP |
|------|------|------|---------|-----|
| 100 | k8s-node1 | pve01 | MicroK8s worker | 192.168.50.60 |
| 101 | k8s-node2 | pve02 | MicroK8s worker | 192.168.50.61 |

### LXCs

| VMID | Name | Node | Purpose | IP |
|------|------|------|---------|-----|
| 113 | dev-workstation | pve01 | Headless Claude Code dev environment | 192.168.50.113 |

## Kubernetes Cluster

Two-node MicroK8s cluster with the following infrastructure:

- **ArgoCD**: GitOps continuous delivery, watches `PeteDio-Labs` GitHub org
- **Sealed Secrets**: Bitnami sealed-secrets controller for secret management
- **MetalLB**: Bare-metal load balancer, IP pool `192.168.50.240–250`
- **kube-prometheus-stack**: Prometheus + Grafana observability stack
- **Nexus**: Docker registry + Maven repository at `docker.toastedbytes.com`

### Namespaces and Services

#### `blog-dev`

| Service | Image | Port | Ingress |
|---------|-------|------|---------|
| blog-api | `docker.toastedbytes.com/blog-api` | 8080 | dev.petedillo.com/api |
| blog-ui | `docker.toastedbytes.com/blog-ui` | 80 | dev.petedillo.com |
| blog-agent | `docker.toastedbytes.com/blog-agent` | 3004 | ClusterIP only |
| postgresql | postgres:16-alpine | 5432 | ClusterIP only |

#### `mission-control`

| Service | Image | Port | Ingress |
|---------|-------|------|---------|
| mission-control-backend | `docker.toastedbytes.com/mission-control-backend` | 3001 | ClusterIP only |
| mission-control-web | `docker.toastedbytes.com/mission-control-web` | 80 | 192.168.50.240 (MetalLB) |
| notification-service | `docker.toastedbytes.com/notification-service` | 3002 | ClusterIP only |
| pete-bot | `docker.toastedbytes.com/pete-bot` | — | Discord gateway |
| postgresql | postgres:16-alpine | 5432 | ClusterIP only |

#### `web-search`

| Service | Image | Port | Ingress |
|---------|-------|------|---------|
| web-search-service | `docker.toastedbytes.com/web-search-service` | 3003 | ClusterIP only |
| searxng | `searxng/searxng:latest` | 8080 | ClusterIP only |

#### `observability`

| Service | Purpose |
|---------|---------|
| kube-prom-stack (Prometheus) | Metrics collection, alerting rules |
| Grafana | Dashboards at grafana-dev.toastedbytes.com |

## Network

| Resource | IP / URL |
|----------|----------|
| pve01 | 192.168.50.50 |
| pve02 | 192.168.50.51 |
| Ollama (on pve01) | 192.168.50.59:11434 |
| k8s-node1 | 192.168.50.60 |
| k8s-node2 | 192.168.50.61 |
| dev-workstation | 192.168.50.113 |
| MetalLB pool | 192.168.50.240–250 |
| MC Web | 192.168.50.240 |
| Blog (dev) | 192.168.50.241 |
| ArgoCD | argocd.toastedbytes.com |
| Grafana | grafana-dev.toastedbytes.com |
| Nexus | registry.toastedbytes.com |
| Docker Registry | docker.toastedbytes.com |
| Blog | dev.petedillo.com |

## Storage

LVM transfer complete on pve01's 4.4 TB `/dev/sdb`. Media LXCs (Plex, Sonarr, Radarr, etc.) run on pve01 with bind mounts to the LVM volume.

## GitOps Repos

All repos live under the `PeteDio-Labs` GitHub org:

| Repo | Purpose |
|------|---------|
| blog-api | Blog backend (Spring Boot, migrating to Bun/Express) |
| blog-ui | Blog frontend (React 19 + Vite 7) |
| blog-agent | Multi-agent LLM content engine |
| pete-bot | Discord bot (Ollama qwen2.5:7b) |
| mission-control-backend | Homelab management API |
| mission-control-web | Homelab dashboard |
| notification-service | Event bus + fan-out |
| web-search-service | Multi-provider search router |
| blog-gitops | Blog K8s manifests |
| pete-bot-gitops | Pete Bot K8s manifests |
| mission-control-gitops | MC + notification-service K8s manifests |
| petedio-labs-gitops | Control plane: ArgoCD apps, observability, web-search, infra |
| PeteDio-Labs | Monorepo workspace (apps + docs + gitops submodules) |

---

This post will be kept up to date by the blog-agent's docs audit pipeline. When infrastructure drifts from what's documented here, a new post gets written.
