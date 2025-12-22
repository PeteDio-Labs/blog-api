-- ============================================================================
-- Migration: V14__sprint-2-infrastructure-defer.sql
-- Description: Sprint 2 Infrastructure Planning & Deferral - Blog Post
-- Sprint: Sprint 2
-- Author: Pedro Delgadillo
-- Date: December 21, 2025
-- Purpose: Insert Sprint 2 Infrastructure Planning blog post documenting
--          multi-environment setup planning and strategic deferral to Sprint 3
-- ============================================================================

-- Insert Sprint 2 Infrastructure Planning post
INSERT INTO blog_posts (
    title,
    slug,
    content,
    excerpt,
    status,
    is_featured,
    published_at
) VALUES (
    'Sprint 2 Part 3: Infrastructure Analysis & Deferral',
    'sprint-2-infrastructure-defer',
    '# Sprint 2 Part 3: Infrastructure Analysis & Deferral

**Sprint Duration**: November 19-29, 2025
**Status**: 📝 PLANNING COMPLETE (Execution Deferred)
**Focus**: Multi-environment setup planning, infrastructure optimization, sprint scope management

---

## Overview

Sprint 2 Infrastructure Phase involved comprehensive planning for multi-environment deployment, network policies, cluster expansion, and monitoring setup. However, after assessing scope and dependencies, infrastructure tasks were strategically deferred to Sprint 3 to prioritize application layer completion (API + UI). This document explains the deferral rationale, planning work completed, and infrastructure roadmap for Sprint 3.

---

## Deferral Decision & Rationale

### Why Defer Infrastructure Work?

**Priority Alignment**
1. **Application Layer First**: Admin UI and comprehensive testing took priority
2. **Feature Completeness**: Users need functional blog before multi-env deployment
3. **Dependency Chain**: Infrastructure depends on application stability
4. **Testing Requirements**: Multi-env setup requires dedicated testing cycle

**Sprint 2 Reality Check**
- Scope creep: 5 planned infrastructure phases vs. 2 application phases
- Complexity: Each infrastructure phase requires 2-3 day turnaround time
- Interdependencies: Multi-env setup blocks testing, testing blocks monitoring
- Team capacity: Solo developer managing multiple complex domains

**Strategic Decision**
```
Spring 2 Goal: Complete application layer (API + UI)
Result: 100% achievement with 52 unit tests and production admin UI
Infra Deferral: Strategic not tactical—infrastructure still planned, just not executed
```

---

## Phase 3: GitOps Multi-Environment (PLANNING COMPLETE)
**Status**: 📝 Documentation Complete → ⏳ Execution Deferred to Sprint 3

### Planned Deliverables

**Kubernetes Namespace Structure**
```
dev namespace:
  - Blog API (2 replicas)
  - Blog UI (2 replicas)
  - PostgreSQL (single instance)
  - Redis (optional caching)

stage namespace:
  - Blog API (3 replicas)
  - Blog UI (3 replicas)
  - PostgreSQL (HA setup)
  - Redis (caching layer)
  - Monitoring sidecar injections

prod namespace:
  - Blog API (5 replicas, auto-scaling)
  - Blog UI (5 replicas, auto-scaling)
  - PostgreSQL (HA with backups)
  - Redis (cluster mode)
  - Full monitoring stack
```

**GitOps Repository Structure**
```
blog-gitops/
├── base/
│   ├── blog-api/
│   │   ├── kustomization.yaml
│   │   ├── deployment.yaml
│   │   ├── service.yaml
│   │   └── configmap.yaml
│   ├── blog-ui/
│   │   ├── kustomization.yaml
│   │   ├── deployment.yaml
│   │   ├── service.yaml
│   │   └── nginx-config.yaml
│   └── postgres/
│       ├── kustomization.yaml
│       ├── statefulset.yaml
│       ├── service.yaml
│       └── pvc.yaml
└── overlays/
    ├── dev/
    │   ├── kustomization.yaml
    │   ├── replicas.yaml
    │   └── resources.yaml
    ├── stage/
    │   ├── kustomization.yaml
    │   ├── replicas.yaml
    │   ├── resources.yaml
    │   └── monitoring.yaml
    └── prod/
        ├── kustomization.yaml
        ├── replicas.yaml
        ├── resources.yaml
        ├── monitoring.yaml
        ├── autoscaling.yaml
        └── backup-policy.yaml
```

**ArgoCD Application Configuration**
```yaml
# argocd/applications/blog-dev.yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: blog-dev
  namespace: argocd
spec:
  project: default
  source:
    repoURL: https://github.com/petedillo/blog-gitops
    path: overlays/dev
    targetRevision: main
  destination:
    server: https://kubernetes.default.svc
    namespace: dev
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
```

**Sealed Secrets per Environment**
- `dev-secrets.yaml` - Encrypted dev credentials
- `stage-secrets.yaml` - Encrypted stage credentials
- `prod-secrets.yaml` - Encrypted prod credentials (with extra encryption layer)

### Promotion Workflow (Planned)

**Git-Based Promotion**
```
1. Dev deployment: Code commit → GitHub Actions → Docker build → Nexus push → ArgoCD sync (dev)
2. Manual promotion: Merge branch dev→stage in blog-gitops repo
3. Stage deployment: ArgoCD auto-syncs stage overlay
4. Manual promotion: Merge branch stage→prod in blog-gitops repo
5. Prod deployment: ArgoCD auto-syncs prod overlay
```

**Safety Mechanisms**
- Manual merge approvals for stage and prod
- Different branch protection rules (more strict for prod)
- Sealed secrets unique per environment
- Resource quotas enforced per namespace
- Pod disruption budgets for high availability

---

## Phase 4: Network Policies & Security (PLANNING COMPLETE)
**Status**: 📝 Documentation Complete → ⏳ Execution Deferred to Sprint 3

### Network Isolation Strategy

**Namespace Isolation**
```yaml
# Network policy: Default deny all ingress/egress
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: default-deny
  namespace: dev
spec:
  podSelector: {}
  policyTypes:
  - Ingress
  - Egress
```

**Cross-Namespace Communication**
```yaml
# Allow dev to prod comms only for metrics collection
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-metrics-export
  namespace: dev
spec:
  podSelector: {}
  policyTypes:
  - Egress
  egress:
  - to:
    - namespaceSelector:
        matchLabels:
          name: monitoring
    ports:
    - protocol: TCP
      port: 9090  # Prometheus scrape port
```

**Tailscale Integration (Planned)**
- VPN tunnel for admin access
- Zero-trust network architecture
- Encrypted control plane communication
- Audit logging of all connections

### Planned Security Hardening

**Pod Security Standards**
```yaml
apiVersion: policy/v1beta1
kind: PodSecurityPolicy
metadata:
  name: restricted
spec:
  privileged: false
  allowPrivilegeEscalation: false
  requiredDropCapabilities:
  - ALL
  volumes:
  - ''configMap''
  - ''emptyDir''
  - ''projected''
  - ''secret''
  - ''downwardAPI''
  - ''persistentVolumeClaim''
```

**RBAC (Role-Based Access Control)**
```yaml
# Limit CI/CD user to deploy-only permissions
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: ci-cd-deployer
  namespace: dev
rules:
- apiGroups: ["apps"]
  resources: ["deployments"]
  verbs: ["get", "list", "watch", "patch", "update"]
- apiGroups: [""]
  resources: ["configmaps", "secrets"]
  verbs: ["get", "list"]
```

---

## Phase 5: Cluster Expansion (PLANNING COMPLETE)
**Status**: 📝 Documentation Complete → ⏳ Execution Deferred to Sprint 3

### Planned Expansion Strategy

**Current Cluster**
```
k8s-node1: 4 cores, 16GB RAM (control plane)
k8s-node2: 4 cores, 8GB RAM (worker)
Total: 8 cores, 24GB RAM
Usage: ~60% capacity with dev workloads
```

**Phase 3 Expansion Plan**
```
Add Raspberry Pi 4B (8GB RAM variant):
  - Node Name: k8s-node3
  - CPU: 4 ARM cores
  - RAM: 8GB
  - Storage: 256GB NVMe via USB
  - Cost: ~$100
  - Purpose: Edge computing + failover

New Total: 12 cores (x86 + ARM), 32GB RAM
Capacity After: ~40% (headroom for stage/prod)
```

**Multi-Architecture Considerations**
- ARM vs x86 image compatibility
- Container image tags: `:latest-amd64`, `:latest-arm64`
- Node affinity rules for architecture-specific workloads
- Build pipeline updates for multi-arch builds

### Infrastructure Monitoring (Planned)

**Prometheus Stack**
- Node exporter on each Kubernetes node
- kube-state-metrics for Kubernetes objects
- Scrape intervals: 30s (short cycle)

**Grafana Dashboards**
- Cluster overview (CPU, memory, disk)
- Pod health and restart counts
- Network traffic per namespace
- Persistent volume usage

**Loki Logging**
- Container logs aggregation
- Multi-tenant logging per namespace
- Log retention: 30 days dev, 60 days stage, 90 days prod
- Search and filtering by pod labels

---

## Infrastructure Documentation Created

**Planning Documents Completed**

| Document | Content | Status |
|----------|---------|--------|
| `stage-namespace.md` | Stage environment Kubernetes manifests | ✅ Complete |
| `prod-namespace.md` | Production environment hardening | ✅ Complete |
| `network-policies.md` | Tailscale + K8s NetworkPolicy specs | ✅ Complete |
| `promotion-workflow.md` | Git-based promotion procedures | ✅ Complete |
| `operations/` | Daily ops, disaster recovery runbooks | ✅ Complete |

**Documentation Benefits**
- Clear handoff to Sprint 3 team
- Decisions documented with rationale
- Architecture understood before implementation
- No rework needed—implementation can start immediately

---

## Why This Deferral Was Correct

### 1. Application Stability First
Infrastructure changes are risky when application layer isn''t stable. Multi-env testing required:
- Dev environment fully functional ✅
- Admin UI complete ✅
- API endpoints tested ✅
- Then safe to expand to stage/prod

### 2. Scope Management
Sprint 2 attempted too much:
- Phase 1 (API): 17 tasks → ✅ Delivered
- Phase 2 (UI): 5 tasks → ✅ Delivered
- Phase 3-5 (Infra): 12+ tasks → 📝 Planned
- Total: 34+ tasks for 1 developer in 2 weeks

### 3. Testing Requirements
Multi-env setup requires dedicated QA cycle:
- Dev deployment test: 2 days
- Stage deployment test: 2 days
- Prod deployment test: 2 days
- Rollback procedures: 1 day
- Not possible in parallel with app development

### 4. Dependency Chain
```
Monitoring needs Stage/Prod working
Stage/Prod needs Network Policies
Network Policies need Talos/Tailscale setup
Tailscale setup needs cluster stable
↑
Cluster stable requires app layer complete
```

### 5. Team Capacity
- Solo developer (Pedro)
- Complex multi-domain expertise required (K8s, Docker, Networking, Monitoring)
- Quality over quantity: deliver working features vs. rushing infrastructure

---

## Sprint 2 Actual Deliverables

### ✅ Application Layer (100% Complete)
- 52 unit tests (80%+ coverage)
- Production admin UI with Spring Security
- Complete API implementation
- Cyberpunk neon theme across UI
- Markdown table rendering
- Media gallery optimization
- Environment awareness

### 📝 Infrastructure Planning (100% Complete)
- Multi-env Kubernetes manifests drafted
- ArgoCD overlay structure designed
- Network policies documented
- Promotion workflow procedures written
- Monitoring stack planned
- Cluster expansion roadmap created

### ⏳ Infrastructure Execution (Deferred to Sprint 3)
- Namespace creation
- Sealed secrets deployment
- Network policy enforcement
- Monitoring stack deployment
- Cluster node addition

---

## Lessons Learned - Infrastructure Planning

1. **Documentation before implementation** saves massive time—no rework needed
2. **Realistic scope estimation** is more valuable than optimistic timelines
3. **Single-developer constraints** require hard prioritization decisions
4. **Application stability** must precede infrastructure scaling
5. **Dependency mapping** prevents circular scheduling problems
6. **Planning documentation** enables confident handoffs and parallel work
7. **Deferral isn''t failure**—it''s strategic resource management
8. **Infrastructure-as-Code** becomes critical at multi-env scale
9. **Sealed Secrets design** should happen during planning, not implementation
10. **Network policy planning** prevents security incidents during rollout

---

## Sprint 3 Roadmap - Infrastructure Focus

**Week 1: Multi-Environment Setup**
- Create stage namespace with full manifests
- Create prod namespace with hardened policies
- Test promotion workflow dev→stage
- Deploy monitoring stack to dev (baseline)

**Week 2: Network & Security**
- Implement Tailscale VPN tunnel
- Deploy network policies across namespaces
- Test cross-namespace communication restrictions
- RBAC policy enforcement verification

**Week 3: Cluster Expansion & Monitoring**
- Prepare Raspberry Pi 4B node
- Add node to MicroK8s cluster
- Validate multi-arch image pulls
- Complete monitoring dashboard setup

**Week 4: Testing & Documentation**
- End-to-end promotion workflow testing (dev→stage→prod)
- Disaster recovery procedure validation
- Operations runbook testing
- Final documentation updates

---

## Success Criteria - Infrastructure Readiness

| Criteria | Current | Sprint 3 Target |
|----------|---------|-----------------|
| Environments | 1 (dev) | 3 (dev, stage, prod) |
| Sealed Secrets | Deployed (dev) | Per-env encryption |
| Network Policies | Planned | Enforced |
| Promotion Path | Manual | GitOps automated |
| Monitoring Stack | Planned | Deployed |
| Cluster Capacity | 24GB | 32GB (post-expansion) |
| Multi-arch Support | None | Planned |

---

## Conclusion

Sprint 2 Infrastructure Phase represents strategic planning that enables confident Sprint 3 execution. By prioritizing application layer completion and deferring infrastructure execution, Sprint 2 delivered stable, tested features users actually interact with. The comprehensive infrastructure planning documentation ensures Sprint 3 infrastructure work can proceed without rework or architectural revisits.

**Key Achievement**: Complete infrastructure roadmap with detailed planning documents—ready for immediate execution in Sprint 3.

**Strategic Message**: Good software engineering is about making hard prioritization choices. Sprint 2 proved it by shipping application features (52 tests, admin UI, neon theme) instead of over-committing to infrastructure that depends on those features being solid.

---

**Planning Completion Date**: November 29, 2025
**Documentation Status**: ✅ COMPLETE
**Execution Status**: ⏳ DEFERRED TO SPRINT 3
**Planning Documents**: 5 files, 38KB
**Ready for Sprint 3 Implementation**: Yes ✅',
    'Strategic infrastructure planning deferred to Sprint 3: Multi-env setup, network policies, cluster expansion—all planned with execution saved for dedicated infrastructure sprint.',
    'PUBLISHED',
    FALSE,
    CURRENT_TIMESTAMP
);

-- Get the inserted post ID and add tags
DO $$
DECLARE
    post_id BIGINT;
BEGIN
    SELECT id INTO post_id FROM blog_posts WHERE slug = 'sprint-2-infrastructure-defer';

    -- Insert tags into tags table if they don't exist, then link to post
    INSERT INTO tags (name, slug) VALUES
        ('sprint-2', 'sprint-2'),
        ('infrastructure', 'infrastructure'),
        ('kubernetes', 'kubernetes'),
        ('devops', 'devops'),
        ('planning', 'planning')
    ON CONFLICT (name) DO NOTHING;

    -- Link tags to post via post_tags junction table
    INSERT INTO post_tags (post_id, tag_id)
    SELECT post_id, t.id FROM tags t WHERE name IN ('sprint-2', 'infrastructure', 'kubernetes', 'devops', 'planning')
    ON CONFLICT DO NOTHING;

    -- Update tag post counts
    UPDATE tags SET post_count = (SELECT COUNT(*) FROM post_tags WHERE tag_id = tags.id)
    WHERE name IN ('sprint-2', 'infrastructure', 'kubernetes', 'devops', 'planning');
END $$;

-- ============================================================================
-- Solo Developer: Pedro Delgadillo
-- Status: Sprint 2 Infrastructure Planning Complete
-- Next: Sprint 3 Infrastructure Execution
-- ============================================================================
