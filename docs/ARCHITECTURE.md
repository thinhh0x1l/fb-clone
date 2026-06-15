# System Architecture

## Overview

Enterprise-grade social networking platform built with Spring Boot 4 + Vue 3, following **Domain-Driven Design (DDD)**, **CQRS**, and **Event-Driven Architecture** patterns. Designed for horizontal scalability, high availability, and eventual consistency.

---

## Architecture Principles

| Principle | Implementation |
|-----------|---------------|
| **Domain-Driven Design** | Bounded contexts, aggregates, domain events |
| **CQRS** | Separate read/write models for posts, feed |
| **Event Sourcing** | Immutable event log for critical operations |
| **SOLID** | Service interfaces, DI, AOP for cross-cutting concerns |
| **12-Factor App** | Externalized config, stateless processes |
| **Defense in Depth** | Multiple security layers |

---

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CDN (CloudFlare)                               │
│                    Static assets, DDoS protection, SSL                      │
└─────────────────────────────────┬───────────────────────────────────────────┘
                                  │
┌─────────────────────────────────▼───────────────────────────────────────────┐
│                          LOAD BALANCER (HAProxy)                            │
│                    Round-robin, health checks, SSL termination              │
└────────┬────────────────────────┬────────────────────────┬──────────────────┘
         │                        │                        │
┌────────▼────────┐  ┌───────────▼──────────┐  ┌─────────▼────────┐
│   API Gateway   │  │   API Gateway        │  │   API Gateway     │
│   (Traefik #1)  │  │   (Traefik #2)       │  │   (Traefik #3)    │
│   Rate Limiting │  │   Rate Limiting      │  │   Rate Limiting   │
│   Auth Filter   │  │   Auth Filter        │  │   Auth Filter     │
└────────┬────────┘  └───────────┬──────────┘  └─────────┬────────┘
         │                       │                        │
┌────────▼───────────────────────▼────────────────────────▼────────┐
│                     SERVICE MESH (Istio)                          │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐           │
│  │ Auth     │ │ User     │ │ Post     │ │ Feed     │           │
│  │ Service  │ │ Service  │ │ Service  │ │ Service  │           │
│  │ (x3)    │ │ (x3)    │ │ (x5)    │ │ (x3)    │           │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘           │
│       │             │             │             │                 │
│  ┌────┴─────┐ ┌────┴─────┐ ┌────┴─────┐ ┌────┴─────┐           │
│  │ Friend   │ │ Message  │ │ Notif    │ │ Media    │           │
│  │ Service  │ │ Service  │ │ Service  │ │ Service  │           │
│  │ (x2)    │ │ (x3)    │ │ (x2)    │ │ (x2)    │           │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘           │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐                        │
│  │ Search   │ │ Moderation│ │ Analytics│                        │
│  │ Service  │ │ Service   │ │ Service  │                        │
│  │ (x2)    │ │ (x1)     │ │ (x1)    │                        │
│  └──────────┘ └──────────┘ └──────────┘                        │
└──────────────────────────────────┬──────────────────────────────┘
                                   │
┌──────────────────────────────────▼──────────────────────────────┐
│                        MESSAGE BROKER                           │
│              Apache Kafka / RabbitMQ (Cluster)                  │
│  ┌────────────────────────────────────────────────────────┐   │
│  │ Topics: post.created, message.sent, notification.new   │   │
│  │         feed.fanout, user.activity, analytics.event    │   │
│  └────────────────────────────────────────────────────────┘   │
└───────────────────────────────────────────────────────────────┘
                                   │
┌──────────────────────────────────▼──────────────────────────────┐
│                        DATA LAYER                               │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │ PostgreSQL   │  │ PostgreSQL   │  │ PostgreSQL   │         │
│  │ (Primary)    │  │ (Replica #1) │  │ (Replica #2) │         │
│  │ Read/Write   │  │ Read Only    │  │ Read Only    │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │ Redis Cluster│  │ Redis Cluster│  │ Redis Cluster│         │
│  │ (Shard #1)   │  │ (Shard #2)   │  │ (Shard #3)   │         │
│  │ Cache/PubSub │  │ Cache/PubSub │  │ Cache/PubSub │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │ Elasticsearch│  │ Elasticsearch│  │ MinIO/S3     │         │
│  │ (Node #1)    │  │ (Node #2)    │  │ (Object Store)│         │
│  │ Search       │  │ Search       │  │ Media        │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
└───────────────────────────────────────────────────────────────┘
                                   │
┌──────────────────────────────────▼──────────────────────────────┐
│                    OBSERVABILITY STACK                           │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐      │
│  │Prometheus │  │ Grafana  │  │ Jaeger   │  │ ELK Stack│      │
│  │ Metrics  │  │Dashboard │  │ Tracing  │  │ Logging  │      │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘      │
└───────────────────────────────────────────────────────────────┘
```

---

## Domain-Driven Design

### Bounded Contexts

```
┌─────────────────────────────────────────────────────────────────┐
│                    SOCIAL GRAPH CONTEXT                          │
│  Aggregates: User, Friend, Block                                │
│  Domain Events: FriendRequestSent, FriendAccepted, UserBlocked  │
│  Read Model: FriendList, MutualFriends, Suggestions             │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                    CONTENT CONTEXT                               │
│  Aggregates: Post, Comment, Reaction, Share                     │
│  Domain Events: PostCreated, CommentAdded, ReactionToggled      │
│  Read Model: Feed, Timeline, PostDetail                         │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                    MESSAGING CONTEXT                             │
│  Aggregates: Conversation, Message, Participant                 │
│  Domain Events: MessageSent, ConversationCreated, MemberAdded   │
│  Read Model: ConversationList, MessageThread                    │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                    NOTIFICATION CONTEXT                          │
│  Aggregates: Notification, Preference, Delivery                 │
│  Domain Events: NotificationCreated, NotificationRead           │
│  Read Model: NotificationFeed, UnreadCount                      │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                    MEDIA CONTEXT                                 │
│  Aggregates: Media, Album, ProcessingJob                        │
│  Domain Events: MediaUploaded, MediaProcessed, ThumbnailCreated │
│  Read Model: MediaGallery, MediaDetail                          │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                    MODERATION CONTEXT                            │
│  Aggregates: Report, Action, Policy                             │
│  Domain Events: ContentReported, ActionTaken, PolicyUpdated     │
│  Read Model: ReportQueue, ModerationDashboard                   │
└─────────────────────────────────────────────────────────────────┘
```

### Aggregate Design

```
Post Aggregate (Root: Post)
├── PostMedia (Entity)
├── PostVisibility (Value Object)
├── PostStats (Value Object)
└── Domain Rules:
    - Post must have content OR media (not both empty)
    - Visibility controls audience
    - Soft delete with 30-day recovery window

User Aggregate (Root: User)
├── UserProfile (Value Object)
├── UserSettings (Value Object)
├── UserAvatar (Entity)
└── Domain Rules:
    - Username must be unique, 3-30 chars
    - Email must be verified
    - Birthday must be 13+ years ago

Conversation Aggregate (Root: Conversation)
├── Participant (Entity)
├── Message (Entity, up to 1000 per conversation)
└── Domain Rules:
    - Direct: exactly 2 participants
    - Group: 2-50 participants
    - Last message metadata for sorting
```

---

## CQRS Architecture

### Command Side (Write)

```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│ Request  │────▶│ Command  │────▶│ Handler  │────▶│ Aggregate│
│          │     │          │     │          │     │          │
└──────────┘     └──────────┘     └──────────┘     └────┬─────┘
                                                        │
                                                        ▼
                                                   ┌──────────┐
                                                   │  Event   │
                                                   │  Store   │
                                                   └──────────┘
```

### Query Side (Read)

```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│ Request  │────▶│  Query   │────▶│ Handler  │────▶│ Read     │
│          │     │          │     │          │     │ Model    │
└──────────┘     └──────────┘     └──────────┘     └──────────┘
```

### Feed Generation Strategy

```
Fanout-on-Write (for < 10K followers):
  User creates post
    → Save to Post DB
    → Publish PostCreated event
    → Feed Service receives event
    → For each follower:
        → Add post ID to follower's feed cache (Redis Sorted Set)
        → Feed = [postId1:timestamp, postId2:timestamp, ...]

Fanout-on-Read (for celebrities, > 10K followers):
  User creates post
    → Save to Post DB
    → Publish PostCreated event
    → Feed Service receives event
    → Do NOT fanout (too many followers)
    → Instead: When follower loads feed, merge:
        → Cached feed (normal posts)
        → Celebrity posts (query on demand, sorted by time)

Hybrid Strategy:
  - Users with < 10K followers: fanout-on-write
  - Users with > 10K followers: fanout-on-read
  - Threshold configurable per deployment
```

---

## Event-Driven Architecture

### Event Types

```
┌─────────────────────┬────────────────────────────────────────┐
│ Event               │ Payload                                │
├─────────────────────┼────────────────────────────────────────┤
│ post.created        │ postId, userId, content, visibility    │
│ post.deleted        │ postId, userId                         │
│ comment.created     │ commentId, postId, userId, parentId    │
│ reaction.toggled    │ targetId, targetType, userId, type     │
│ friend.requested    │ requesterId, addresseeId               │
│ friend.accepted     │ friendshipId, requesterId, addresseeId │
│ message.sent        │ messageId, conversationId, senderId    │
│ notification.pushed │ notificationId, userId, type            │
│ media.uploaded      │ mediaId, userId, url, type             │
│ user.online         │ userId, socketId                       │
│ user.offline        │ userId                                 │
│ moderation.flagged  │ contentId, contentType, reason         │
└─────────────────────┴────────────────────────────────────────┘
```

### Event Flow

```
Producer Service          Kafka/RabbitMQ           Consumer Service
      │                        │                         │
      │  1. Publish Event      │                         │
      │───────────────────────▶│                         │
      │                        │  2. Deliver Event       │
      │                        │────────────────────────▶│
      │                        │                         │
      │                        │  3. ACK                 │
      │                        │◀────────────────────────│
      │                        │                         │
      │                        │  4. Process & Update    │
      │                        │     Read Model          │
```

---

## Data Architecture

### PostgreSQL Schema Strategy

```
Schema Isolation:
├── auth_schema         → Users, credentials, sessions
├── social_schema       → Friends, blocks, follows
├── content_schema      → Posts, comments, reactions, shares
├── messaging_schema    → Conversations, messages
├── notification_schema → Notifications, preferences
├── media_schema        → Media metadata, albums
└── moderation_schema   → Reports, actions, policies

Benefits:
- Logical separation without microservices overhead
- Independent scaling per schema
- Easier data retention policies
- Clear ownership boundaries
```

### Redis Cache Strategy

```
Cache Layers:
┌─────────────────────────────────────────────────────────────┐
│ Layer 1: Session Cache (TTL: 7 days)                        │
│   Key: session:{userId}                                     │
│   Value: JWT tokens, user metadata                          │
├─────────────────────────────────────────────────────────────┤
│ Layer 2: Feed Cache (TTL: 5 minutes)                        │
│   Key: feed:{userId}                                        │
│   Value: Sorted Set [timestamp → postId]                    │
├─────────────────────────────────────────────────────────────┤
│ Layer 3: Hot Data Cache (TTL: 10 minutes)                   │
│   Key: post:{postId}                                        │
│   Value: Post object (serialized)                           │
├─────────────────────────────────────────────────────────────┤
│ Layer 4: Count Cache (TTL: 5 minutes)                       │
│   Key: counts:post:{postId}                                 │
│   Value: Hash {likes: 150, comments: 23}                    │
├─────────────────────────────────────────────────────────────┤
│ Layer 5: Online Status (TTL: 5 minutes, heartbeat renewal)  │
│   Key: user:status:{userId}                                 │
│   Value: Hash {status: online, lastSeen: timestamp}         │
├─────────────────────────────────────────────────────────────┤
│ Layer 6: Rate Limiting (Sliding Window)                     │
│   Key: ratelimit:{userId}:{endpoint}                        │
│   Value: Counter with TTL                                   │
└─────────────────────────────────────────────────────────────┘
```

### Elasticsearch Strategy

```
Indices:
├── users_index
│   ├── Fields: id, username, displayName, bio, avatar
│   ├── Analyzer: custom (edge_ngram for autocomplete)
│   └── Search: fuzzy match, prefix match
│
├── posts_index
│   ├── Fields: id, content, userId, visibility, createdAt
│   ├── Analyzer: standard + stop words
│   └── Search: full-text, phrase match, date range
│
└── hashtags_index
    ├── Fields: tag, count, trendingScore
    ├── Analyzer: lowercase
    └── Search: exact match, prefix

Indexing Strategy:
- Async indexing via Kafka consumers
- Near-real-time (< 1 second latency)
- Replication factor: 2
- Refresh interval: 1s (for search) or 30s (for bulk)
```

---

## Security Architecture

### Authentication Flow

```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│  Client   │────▶│  API GW  │────▶│  Auth    │────▶│  Redis   │
│           │     │ (Traefik)│     │  Service │     │  (Cache) │
└──────────┘     └──────────┘     └──────────┘     └──────────┘
     │                                  │
     │  1. POST /auth/login             │
     │     {email, password}            │
     │─────────────────────────────────▶│
     │                                  ├─▶ Validate credentials
     │                                  ├─▶ Generate JWT (Nimbus)
     │                                  ├─▶ Store session in Redis
     │                                  │
     │  2. Response: tokens             │
     │◀─────────────────────────────────│
     │
     │  3. GET /api/v1/posts            │
     │     Authorization: Bearer JWT    │
     │─────────────────────────────────▶│
     │                                  │
     │                     ┌────────────┤
     │                     │ Traefik:   │
     │                     │ Validate   │
     │                     │ JWT        │
     │                     └────────────┤
     │                                  │
     │  4. Response: posts              │
     │◀─────────────────────────────────│
```

### Rate Limiting Strategy

```
┌─────────────────────┬───────────────┬───────────────┬────────────────┐
│ Endpoint            │ Per User      │ Global        │ Window         │
├─────────────────────┼───────────────┼───────────────┼────────────────┤
│ POST /auth/login    │ 5 attempts    │ 100/min       │ 15 min         │
│ POST /auth/register │ 3 attempts    │ 50/min        │ 1 hour         │
│ POST /posts         │ 20 posts      │ 500/min       │ 1 hour         │
│ POST /comments      │ 30 comments   │ 1000/min      │ 1 minute       │
│ POST /messages      │ 30 messages   │ 1000/min      │ 1 minute       │
│ POST /media/upload  │ 50 files      │ 200/min       │ 1 hour         │
│ GET /search         │ 30 queries    │ 500/min       │ 1 minute       │
│ WebSocket connect   │ 1 connection  │ 1000/sec      │ Connection     │
└─────────────────────┴───────────────┴───────────────┴────────────────┘

Implementation:
- Traefik: Global rate limiting (per IP)
- Bucket4j: Per-user rate limiting (application level)
- Redis: Distributed counter for multi-instance coordination
```

---

## Observability

### Metrics (Prometheus)

```
Application Metrics:
├── http_requests_total (counter)
├── http_request_duration_seconds (histogram)
├── websocket_connections_active (gauge)
├── messages_sent_total (counter)
├── posts_created_total (counter)
├── cache_hit_ratio (gauge)
└── db_query_duration_seconds (histogram)

Business Metrics:
├── daily_active_users (gauge)
├── monthly_active_users (gauge)
├── posts_per_day (gauge)
├── messages_per_day (gauge)
├── average_session_duration (gauge)
└── user_retention_rate (gauge)
```

### Distributed Tracing (Jaeger)

```
Trace Flow:
Client Request → Traefik → Auth Service → Post Service → DB → Cache → Response

Span Attributes:
- http.method: GET
- http.url: /api/v1/posts
- http.status_code: 200
- db.system: postgresql
- cache.system: redis
- user.id: xxx
- trace.id: xxx
```

### Logging (ELK Stack)

```
Structured Logging Format:
{
  "timestamp": "2025-01-15T10:30:00Z",
  "level": "INFO",
  "service": "post-service",
  "traceId": "abc123",
  "spanId": "def456",
  "userId": "user-789",
  "message": "Post created",
  "metadata": {
    "postId": "post-123",
    "visibility": "PUBLIC",
    "mediaCount": 3
  }
}

Log Retention:
- Application logs: 30 days
- Audit logs: 1 year
- Error logs: 90 days
- Access logs: 90 days
```

---

## Deployment Architecture

### Docker Compose (Development)

```
┌─────────────────────────────────────────────────────────────┐
│                    Docker Compose                           │
│                                                              │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐      │
│  │ Traefik │  │ Backend │  │ Frontend│  │ MinIO   │      │
│  │ (proxy) │  │ (x1)    │  │ (x1)    │  │ (S3)    │      │
│  └─────────┘  └─────────┘  └─────────┘  └─────────┘      │
│                                                              │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐      │
│  │PostgreSQL│  │ Redis   │  │ Kafka   │  │ Elastic │      │
│  │ (x1)    │  │ (x1)    │  │ (x1)    │  │ (x1)    │      │
│  └─────────┘  └─────────┘  └─────────┘  └─────────┘      │
│                                                              │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐                    │
│  │Prometheus│  │ Grafana │  │ Jaeger  │                    │
│  │         │  │         │  │         │                    │
│  └─────────┘  └─────────┘  └─────────┘                    │
└─────────────────────────────────────────────────────────────┘
```

### Kubernetes (Production)

```
┌─────────────────────────────────────────────────────────────┐
│                    Kubernetes Cluster                        │
│                                                              │
│  Namespace: production                                      │
│  ├── Deployments                                            │
│  │   ├── backend (3 replicas, HPA: 3-10)                   │
│  │   ├── frontend (2 replicas)                              │
│  │   ├── traefik (2 replicas)                               │
│  │   └── media-service (2 replicas)                         │
│  ├── StatefulSets                                           │
│  │   ├── postgresql (primary + 2 replicas)                  │
│  │   ├── redis-cluster (6 nodes)                            │
│  │   ├── kafka (3 brokers)                                  │
│  │   └── elasticsearch (3 nodes)                            │
│  ├── Services                                               │
│  │   ├── ClusterIP (internal)                               │
│  │   ├── LoadBalancer (Traefik)                             │
│  │   └── NodePort (monitoring)                              │
│  └── ConfigMaps / Secrets                                   │
│                                                              │
│  Namespace: monitoring                                      │
│  ├── prometheus                                              │
│  ├── grafana                                                 │
│  └── jaeger                                                  │
└─────────────────────────────────────────────────────────────┘
```

---

## Scalability Strategy

### Horizontal Scaling

| Component | Scaling Strategy | Trigger |
|-----------|-----------------|---------|
| Backend | Pod autoscaling (HPA) | CPU > 70%, Memory > 80% |
| PostgreSQL | Read replicas | Read load > 80% |
| Redis | Cluster sharding | Memory > 70% |
| Kafka | Partition scaling | Throughput > threshold |
| Elasticsearch | Node scaling | Index size > threshold |

### Performance Targets

| Metric | Target (MVP) | Target (Scale) | Target (Enterprise) |
|--------|-------------|----------------|---------------------|
| Concurrent Users | 1,000 | 50,000 | 1,000,000 |
| Feed Load Time | < 500ms | < 200ms | < 100ms |
| Message Delivery | < 100ms | < 50ms | < 20ms |
| Image Upload (5MB) | < 3s | < 1.5s | < 1s |
| API Response (p95) | < 200ms | < 100ms | < 50ms |
| API Response (p99) | < 500ms | < 200ms | < 100ms |
| Uptime | 99.5% | 99.9% | 99.99% |
| RPO (Data Loss) | 1 hour | 5 minutes | 0 (sync replication) |
| RTO (Recovery) | 4 hours | 1 hour | 15 minutes |

---

## Disaster Recovery

```
Backup Strategy:
├── PostgreSQL
│   ├── Continuous WAL archiving
│   ├── Daily full backup
│   ├── Point-in-time recovery
│   └── Cross-region replication
│
├── Redis
│   ├── RDB snapshots (every 5 minutes)
│   ├── AOF (append-only file)
│   └── Cross-region replication
│
├── MinIO/S3
│   ├── Versioning enabled
│   ├── Cross-region replication
│   └── Lifecycle policies
│
└── Elasticsearch
    ├── Snapshot to S3
    └── Cross-cluster replication

Recovery Procedures:
├── Database failure → Promote replica to primary
├── Cache failure → Rebuild from DB (cold start)
├── Region failure → Failover to DR region
└── Full restore → Restore from backups + WAL replay
```

---

## Technology Stack Summary

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **Frontend** | Vue 3.5, Element Plus, SCSS | SPA, UI components |
| **API Gateway** | Traefik | Routing, rate limiting, SSL |
| **Backend** | Spring Boot 4.1, Java 21 | Business logic |
| **Auth** | Nimbus JOSE+JWT, Spring Security | Authentication |
| **Database** | PostgreSQL 16 | Primary data store |
| **Cache** | Redis 7.4 | Session, feed, hot data |
| **Search** | Elasticsearch 8 | Full-text search |
| **Object Storage** | MinIO (S3-compatible) | Media files |
| **Message Broker** | Kafka / RabbitMQ | Event streaming |
| **WebSocket** | Spring WebSocket + Redis Pub/Sub | Real-time |
| **Monitoring** | Prometheus + Grafana | Metrics |
| **Tracing** | Jaeger | Distributed tracing |
| **Logging** | ELK Stack | Log aggregation |
| **Container** | Docker + Kubernetes | Deployment |
| **CI/CD** | GitHub Actions | Automation |
