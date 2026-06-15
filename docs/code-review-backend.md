# CODE REVIEW BACKEND FACEBOOK CLONE

## 1. SOLID PRINCIPLES

### 1.1 Single Responsibility Principle (SRP)

**Vấn đề:** Service làm quá nhiều việc: validate + business + cache + event + mapping

| File | Vấn đề | Giải pháp |
|------|--------|-----------|
| `PostServiceImpl` | 4 responsibilities trong 1 class | Tách → `PostValidator` / `PostCacheManager` / `PostEventPublisher` |
| `FriendServiceImpl.getAcceptedFriends` | Vừa query DB, vừa cache, vừa mapping | Tách → `FriendCacheService` xử lý cache, Service chỉ gọi |
| `ReactionServiceImpl` | toggle + count + cache + stats | Tách reaction stats ra `ReactionStatsService` riêng |

**Cụ thể:**
```java
// Trước: God class
@Service
public class PostServiceImpl implements PostService {
    // validate + save + media + cache + event + mapping
}

// Sau: SRP
@Service
public class PostServiceImpl implements PostService {
    private final PostValidator validator;
    private final PostRepository repository;
    private final PostCacheManager cacheManager;
    private final PostEventPublisher eventPublisher;
    private final PostMapper mapper;
}
```

**Orchestrator vs Service conflict:**
- `PostServiceImpl.createPost` và `PostOrchestrator.orchestrateCreatePost` tồn tại song song
- **Fix:** Chỉ giữ 1 luồng:
  - Controller → PostOrchestrator (workflow) → PostService (atomic CRUD)
  - Hoặc: Controller → PostService (đơn giản) → PostOrchestrator bỏ đi

---

### 1.2 Open/Closed Principle (OCP)

**FeedRankingEngine hardcode scorer:**
```java
// Trước: Thêm scorer mới phải sửa calculateScore()
private double calculateScore(User user, Post post, Map<String, Object> context) {
    double relationshipScore = relationshipScorer.score(user, post.getUser(), context);
    double contentScore = contentScorer.score(post, context);
    double recencyScore = recencyScorer.score(post.getCreatedAt(), context);
    double diversityScore = diversityScorer.score(post, context);
    // ... weights
}

// Sau: Dùng Strategy pattern + plugin
@Component
public class FeedRankingEngine {
    private final List<Scorer> scorers;  // Spring inject all Scorer beans
    private final Map<Scorer, Double> weights;  // configurable via @Value

    public List<RankedPost> rankFeed(User user, List<Post> posts, Map<String, Object> context) {
        return posts.stream()
            .map(post -> {
                double score = scorers.stream()
                    .mapToDouble(s -> s.score(user, post, context) * weights.get(s))
                    .sum();
                return new RankedPost(post, score);
            })
            .sorted(reverse())
            .collect(toList());
    }
}
```

**CacheService God class:**
- **Giải pháp:** Tách interface nhỏ + dùng **Caffeine** làm L1 cache, Redis làm L2 cache
```java
// L1: Caffeine local cache (siêu nhanh, không network)
@Bean
public Cache<String, Object> caffeineCache() {
    return Caffeine.newBuilder()
        .maximumSize(10_000)
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .recordStats()
        .build();
}

// L2: Redis distributed cache
// Multi-tier cache abstraction
public class MultiTierCache {
    private final Cache<String, Object> local;     // Caffeine
    private final RedisTemplate<String, Object> redis;  // Redis
    
    public Object get(String key) {
        Object val = local.getIfPresent(key);
        if (val != null) return val;
        val = redis.opsForValue().get(key);
        if (val != null) local.put(key, val);
        return val;
    }
}
```

---

### 1.3 Liskov Substitution Principle (LSP)

| Vấn đề | Giải pháp |
|--------|-----------|
| `KafkaMessageBroker` handler rỗng | Bỏ Kafka dependency hoặc implement đầy đủ consumer |
| `MinioStorageService` tự thêm `initialize()` | Thêm vào interface `StorageService` hoặc dùng `@PostConstruct` |
| `@Data` trên entity dễ gây proxy bug | Thay bằng `@Getter @Setter @ToString` riêng, không dùng `@EqualsAndHashCode` |

---

### 1.4 Interface Segregation Principle (ISP)

**CacheService tách nhỏ:**
```java
// Trước: 1 interface 250 lines
public class CacheService { ... }

// Sau: Nhiều interface nhỏ
public interface StringCache { void set(String key, Object value); Object get(String key); }
public interface SetCache { Long sAdd(String key, Object... values); Boolean sIsMember(String key, Object value); }
public interface SortedSetCache { Boolean zAdd(String key, Object value, double score); Set<Object> zReverseRange(...); }
public interface PubSubCache { void publish(String channel, Object message); }
public interface CounterCache { Long increment(String key); Long decrement(String key); }
```

---

### 1.5 Dependency Inversion Principle (DIP)

- **OK:** Controller → Service interface ✓
- **Cần cải thiện:** Service nên phụ thuộc vào interface, không phải class cụ thể
  - `PostServiceImpl` → inject `CacheManager` interface thay vì `CacheService` concrete class
  - `FriendServiceImpl` → inject `FriendMapper` interface

---

## 2. KHẢ NĂNG MỞ RỘNG (SCALABILITY)

### 2.1 Điểm tốt cho mở rộng
| Pattern | Chi tiết |
|---------|----------|
| Interface-based services | `AuthService`/`AuthServiceImpl`, `PostService`/`PostServiceImpl` |
| Event-driven | `PostCreatedEvent`, `MessageSentEvent` → có thể thêm listener mà không sửa code gốc |
| Orchestrator pattern | Mỗi business flow có orchestrator riêng |
| Scorer pattern | Feed ranking dùng scorer → dễ thêm scorer mới |
| Storage abstraction | `StorageService` interface → có thể swap MinIO ↔ AWS S3 |

### 2.2 Vấn đề khi scale & giải pháp

#### Vấn đề 1: Feed fanout (push-model) không scale
```java
// Hiện tại: push-model - nếu user có 5000 friends → 5000 lần ZADD
for (User friend : friends) {
    String feedKey = CacheKey.USER_FEED + friend.getId();
    cacheService.zAdd(feedKey, postId.toString(), score);
}
```
- Post của celebrity → fanout đến hàng triệu followers → chết

**Giải pháp: Pull-model (Fan-out-on-read)**
```java
// Khi đọc feed, merge:
// 1. Posts từ friends (lấy từ cache friendships)
// 2. Posts từ celebrity (lấy từ feed riêng của celebrity)
// 3. Trending/top posts (global feed)

// Hybrid: 
// - User thường (< 5000 friends): push
// - Celebrity (> 5000 followers): pull + fanout cho online users qua Kafka
// - Dùng Caffeine cache local cho feed của user đang online
```

**Hoặc dùng phân luồng fanout qua Async + Kafka:**
```java
@Async("feedExecutor") // Thread pool riêng cho feed fanout
public void fanoutPost(Post post, List<Long> friendIds) {
    // Chunk nhỏ: 100 friends / batch
    List<List<Long>> batches = Lists.partition(friendIds, 100);
    for (List<Long> batch : batches) {
        kafkaTemplate.send("feed-fanout", post.getId(), batch);
    }
}
```

#### Vấn đề 2: Search LIKE '%keyword%'
```java
// Hiện tại: full table scan, không scale
@Query("SELECT p FROM Post p WHERE p.content LIKE %:query%")
List<Post> searchByContent(@Param("query") String query, Pageable pageable);
```

**Giải pháp (theo thứ tự ưu tiên):**
1. **Caffeine + inverted index** (tạm thời, cho data nhỏ)
2. **PostgreSQL Fulltext Search** (nhanh, zero infra):
   ```sql
   CREATE INDEX posts_search_idx ON posts USING GIN(to_tsvector('english', content));
   -- Query
   SELECT * FROM posts WHERE to_tsvector('english', content) @@ plainto_tsquery('english', :query);
   ```
3. **Elasticsearch** (production, scale ngang):
   ```java
   // Spring Data Elasticsearch
   @Document(indexName = "posts")
   public class SearchPost {
       @Field(type = Text, analyzer = "standard")
       private String content;
       @Field(type = Keyword)
       private Long userId;
       @Field(type = Date)
       private LocalDateTime createdAt;
   }
   // Query: bool query with must + filter + function_score
   ```

#### Vấn đề 3: Soft delete không filter
```java
// Trước: mỗi query phải filter thủ công
@Query("SELECT p FROM Post p WHERE p.user = :user AND p.deletedAt IS NULL")

// Sau: @Where annotation (Hibernate)
@Entity
@Table(name = "posts")
@SQLRestriction("deleted_at IS NULL")  // Hibernate 6+
@Where(clause = "deleted_at IS NULL")  // Hibernate 5
public class Post extends BaseEntity { }

// Query tự động thêm WHERE deleted_at IS NULL
```

#### Vấn đề 4: Cache keys dùng KEYS blocking
```java
// Trước: KEYS block Redis
public Long deleteByPattern(String pattern) {
    Set<String> keys = redisTemplate.keys(pattern); // ❌ BLOCKING
    return redisTemplate.delete(keys);
}

// Sau: SCAN non-blocking
public Long deleteByPattern(String pattern) {
    Set<String> keys = new HashSet<>();
    ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();
    try (Cursor<byte[]> cursor = redisTemplate.getConnectionFactory()
            .getConnection().scan(options)) {
        while (cursor.hasNext()) {
            keys.add(new String(cursor.next()));
            if (keys.size() >= 1000) {  // flush mỗi 1000 keys
                redisTemplate.delete(keys);
                keys.clear();
            }
        }
    }
    if (!keys.isEmpty()) redisTemplate.delete(keys);
    return 0L;
}

// Tốt hơn: Track keys trong Set riêng, không scan
// Khi set key, thêm vào "cache:keys:{cacheName}"
```

#### Vấn đề 5: Database connection pool
```yml
# Tăng pool cho production
spring:
  datasource:
    hikari:
      maximum-pool-size: 50      # (core_count * 2) + effective_disk_count
      minimum-idle: 10
      connection-timeout: 5000
      max-lifetime: 1800000       # 30 phút
      leak-detection-threshold: 60000  # 60s
```

#### Vấn đề 6: Thiếu sharding
```java
// Dùng Snowflake ID thay vì auto-increment Long
public class SnowflakeIdGenerator {
    private final long workerId;
    private final long datacenterId;
    private long sequence = 0L;
    
    public synchronized long nextId() {
        // timestamp(41bit) + datacenter(5bit) + worker(5bit) + sequence(12bit)
    }
}

// Hoặc dùng UUID ngắn (NanoID, ULID)
// ULID: 26 chars, sortable, timestamp-prefixed
```

---

## 3. CODE TỐI ƯU & HIỆN ĐẠI

### 3.1 Điểm tốt
- **Java 21 + Spring Boot 3.5.6**: phiên bản mới nhất
- **MapStruct**: compile-time mapping
- **Lombok**: giảm boilerplate
- **Bucket4j**: rate limiting in-process + distributed
- **Event-driven architecture**: async processing
- **AOP**: tách cross-cutting concerns

### 3.2 Cải thiện code hiện đại

**1. Dùng Java 21 Records cho DTO:**
```java
// Trước: Lombok @Data + @Builder
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PostResponse {
    private Long id;
    private String content;
    private UserResponse author;
}

// Sau: Java Record (immutable, boilerplate-free)
public record PostResponse(
    Long id,
    String content,
    UserResponse author,
    List<MediaResponse> media,
    long likesCount,
    long commentsCount,
    Visibility visibility,
    LocalDateTime createdAt
) {}
```

**2. Dùng Virtual Threads cho async:**
```java
// application.yml
spring:
  threads:
    virtual:
      enabled: true

// @Async tự động chạy trên Virtual Thread
@Async
@EventListener
public void handlePostCreated(PostCreatedEvent event) {
    // Chạy trên virtual thread → siêu nhẹ, hàng triệu concurrent
}
```

**3. Pattern Matching cho enum:**
```java
// Trước: if-else hoặc switch cũ
if (reaction.getType() == request.getType()) { ... }
else { ... }

// Sau: Pattern matching (Java 21)
return switch (reaction.getType()) {
    case LIKE -> handleLike(post, user);
    case LOVE -> handleLove(post, user);
    case HAHA -> handleHaha(post, user);
    case SAD, ANGRY -> handleNegative(post, user);
};
```

**4. Multi-tier cache với Caffeine + Redis:**
```java
@Configuration
public class MultiTierCacheConfig {
    
    @Bean
    public CacheManager cacheManager(CaffeineConfig caffeineConfig, 
                                      RedisConnectionFactory redisFactory) {
        // L1: Caffeine local
        CaffeineCacheManager local = new CaffeineCacheManager();
        local.setCaffeine(Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats());
        
        // L2: Redis
        RedisCacheManager redis = RedisCacheManager.builder(redisFactory)
            .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)))
            .build();
        
        // Composite: Caffeine trước → Redis sau
        return new CompositeCacheManager(local, redis);
    }
}
```

**5. Dùng StringTemplate (Java 21 Preview):**
```java
// Trước: nối chuỗi
String msg = "User " + userId + " liked post " + postId;

// Sau: StringTemplate
String msg = STR."User \{userId} liked post \{postId}";

// Cache key
String key = STR."user:\{userId}:feed";
```

**6. Caffeine + Redis Cache Manager tích hợp Spring:**
```java
@Component
public class CachedFeedService {
    
    // Caffeine cache cho feed của các user đang active
    private final Cache<String, List<PostResponse>> feedCache = 
        Caffeine.newBuilder()
            .maximumSize(50_000)          // 50k users
            .expireAfterWrite(1, TimeUnit.MINUTES)  // feed refresh 1 phút
            .recordStats()                 // hit/miss tracking
            .build();
    
    // Kết hợp Caffeine + Redis + Database
    public FeedResponse getFeed(Long userId, int page, int size) {
        String key = "feed:" + userId + ":" + page;
        
        // 1. Caffeine (L1 - local, nano giây)
        FeedResponse cached = feedCache.getIfPresent(key);
        if (cached != null) return cached;
        
        // 2. Redis (L2 - distributed, milli giây)
        FeedResponse redis = redisService.getFeed(key);
        if (redis != null) {
            feedCache.put(key, redis);  // warm Caffeine
            return redis;
        }
        
        // 3. Database (L3 - chậm nhất)
        FeedResponse db = databaseService.getFeed(userId, page, size);
        redisService.setFeed(key, db);   // warm Redis
        feedCache.put(key, db);          // warm Caffeine
        return db;
    }
    
    // Khi có post mới → chỉ invalidate cache của friends online
    public void invalidateFriendFeeds(Long authorId, List<Long> friendIds) {
        // Chỉ invalidate user đang online (check Redis presence)
        Set<Object> onlineUsers = redisService.sMembers("users:online");
        for (Long friendId : friendIds) {
            if (onlineUsers.contains(friendId.toString())) {
                String key = "feed:" + friendId + ":*";
                feedCache.invalidateAll();          // clear local
                redisService.deleteByPattern(key);  // clear Redis (dùng SCAN)
            }
        }
    }
}
```

### 3.3 Fix N+1 queries

**Trước:**
```java
// MessageServiceImpl - mỗi lần gọi getParticipants là 1 query
conversation.getParticipants().stream().anyMatch(p -> p.getId().equals(userId));
```

**Sau:**
```java
// 1. JOIN FETCH trong repository
@Query("SELECT c FROM Conversation c JOIN FETCH c.participants WHERE c.id = :id")
Optional<Conversation> findByIdWithParticipants(@Param("id") Long id);

// 2. Entity Graph
@EntityGraph(attributePaths = {"participants"})
Optional<Conversation> findById(Long id);

// 3. Batch fetching
@Entity
public class Conversation {
    @ManyToMany(fetch = FetchType.LAZY)
    @BatchSize(size = 20)  // 1 query cho 20 conversations
    private Set<User> participants;
}
```

### 3.4 Fix race condition (ReactionServiceImpl)

**Trước:**
```java
post.setLikesCount(post.getLikesCount() - 1);  // race condition
```

**Sau: dùng Redis counter atomic + distributed lock:**
```java
// 1. Redis atomic increment (không cần lock cho counter)
redisTemplate.opsForValue().increment("post:likes:" + postId, delta);

// 2. Distributed lock cho critical section
public ReactionResponse togglePostReaction(Long postId, Long userId, ReactionRequest request) {
    String lockKey = "lock:reaction:" + postId + ":" + userId;
    // Redis SET NX EX 3 → lock 3 giây
    Boolean locked = redisTemplate.opsForValue()
        .setIfAbsent(lockKey, "1", Duration.ofSeconds(3));
    
    if (Boolean.FALSE.equals(locked)) {
        throw new TooManyRequestsException("Đang xử lý, vui lòng thử lại");
    }
    try {
        // Xử lý reaction
        return doToggleReaction(postId, userId, request);
    } finally {
        redisTemplate.delete(lockKey);
    }
}

// 3. Hoặc dùng Lua script atomic:
// local reacted = redis.call('SISMEMBER', KEYS[1], ARGV[1])
// if reacted then redis.call('SREM', KEYS[1], ARGV[1])
// else redis.call('SADD', KEYS[1], ARGV[1]) end
// return not reacted
```

### 3.5 Error handling + Tracing

```java
// MDC correlation ID filter
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                     HttpServletResponse response,
                                     FilterChain chain) {
        String correlationId = request.getHeader("X-Correlation-Id");
        if (correlationId == null) correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        response.setHeader("X-Correlation-Id", correlationId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}

// Log format với correlation ID
// logging.pattern.console=%d{ISO8601} [%X{correlationId}] %-5level [%thread] %logger{36} - %msg%n

// Retry mechanism với Spring Retry
@Retryable(value = {DataAccessException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
public Post getPost(Long postId) {
    return postRepository.findById(postId)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết"));
}

// Circuit breaker cho Redis/MinIO
@CircuitBreaker(name = "redis", fallbackMethod = "getPostFromDb")
public Post getPostWithCache(Long postId) {
    return cacheService.get("post:" + postId);
}
```

---

## 4. NGHIỆP VỤ THIẾU & GIẢI PHÁP

### 4.1 Nghiệp vụ đã có
- [x] Auth (register/login/refresh)
- [x] Profile user (CRUD + avatar/bio)
- [x] Post (CRUD + media + visibility)
- [x] Feed (sorted by ranking algorithm)
- [x] Comment (nested replies)
- [x] Reaction (5 types: Like/Love/Haha/Sad/Angry)
- [x] Friend (request/accept/reject/unfriend + suggestions)
- [x] Chat (conversation + message + realtime WebSocket) - **thiếu E2EE**
- [x] Notification (push + read/unread)
- [x] Search (user + post)
- [x] Media upload (MinIO)
- [x] Trending hashtags
- [x] Rate limiting
- [x] Soft delete
- [x] Audit log (annotation)

### 4.2 Nghiệp vụ cần thêm

| Nghiệp vụ | Priority | Hướng giải quyết |
|-----------|----------|------------------|
| **Share post** | Critical | `Share` entity: originalPostId + userId + caption. Tạo Post mới kiểu SHARE |
| **Bookmark/Save** | High | `SavedPost` entity: userId + postId + collection. Redis Set cho quick check |
| **Report content** | High | `Report` entity: reporterId + targetId + targetType + reason. Moderation queue |
| **Block user** | High | `block` table đã có migration. Implement BlockService + filter content |
| **Follow system** | High | `Follow` entity: follower + followee. Thay thế friend cho public figures |
| **Email verify** | High | Gửi mail + token + verify endpoint. Dùng Redis lưu token 24h |
| **Password reset** | High | Forgot/reset flow. Token Redis 15 phút + mail |
| **Admin panel** | High | `/api/admin/**` đã config security. Implement controller + CRUD users/posts |
| **Roles/Permissions** | High | `Role` enum (ADMIN/MOD/USER). `@PreAuthorize` annotation |
| **Stories 24h** | Medium | `Story` entity: userId + media + expiresAt. Redis sorted set cleanup |
| **Group** | Medium | `Group` entity + `GroupMember`. Post + Conversation theo group |
| **Content moderation** | Medium | Abuse detection + report queue + auto-flag |
| **E2EE (End-to-End Encryption)** | High | Mã hóa message: Hybrid AES-256-GCM + RSA-4096. Signal Protocol cho hạng nặng |
| **2FA** | Medium | TOTP (Google Authenticator) hoặc SMS OTP |
| **Analytics** | Medium | Event tracking → Redis → batch job → aggregate table |
| **Page** | Low | `Page` entity + admin role. Page post khác user post |
| **Poll** | Low | `Poll` entity: options + votes. Real-time vote count via WebSocket |
| **Live stream** | Low | WebRTC + HLS streaming server |
| **Event** | Low | `Event` entity: time + location + RSVP |

### 4.3 Infrastructure cần thêm

| Component | Priority | Hướng giải quyết | Chi phí |
|-----------|----------|------------------|---------|
| **Elasticsearch** | High | Docker compose + Spring Data ES | Free (self-host) |
| **Kafka** | Medium | Docker compose, thay Redis Pub/Sub cho event persistence | Free |
| **Caffeine cache** | High | Thêm dependency + cấu hình multi-tier | Free |
| **CI/CD** | High | GitHub Actions: build + test + deploy | Free (public repo) |
| **Monitoring** | Medium | Prometheus + Grafana (Docker) | Free |
| **Distributed tracing** | Medium | Micrometer + Zipkin | Free |
| **API Gateway** | Low | Spring Cloud Gateway hoặc Kong | Free |
| **Load testing** | High | k6 script cho các critical endpoints | Free |

### 4.4 Caffeine Cache - Kiến trúc đề xuất

```
┌─────────────────────────────────────────────────┐
│                   Client                          │
└─────────────────────┬───────────────────────────┘
                      │ HTTP/WS
┌─────────────────────▼───────────────────────────┐
│               Nginx (Reverse Proxy)               │
└─────────────────────┬───────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────┐
│         Spring Boot Application Instance 1       │
│  ┌─────────────────────────────────────────┐    │
│  │  L1: Caffeine Cache (local, heap)       │    │
│  │  • Feed cache: 50k entries, 1 phút TTL │    │
│  │  • User cache: 100k entries, 5 phút TTL│    │
│  │  • Post cache: 200k entries, 10 phút   │    │
│  │  • Hot data: trending, hashtags, top   │    │
│  └─────────────────────────────────────────┘    │
└─────────────────────┬───────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────┐
│              L2: Redis (distributed)             │
│  • Session, presence, rate limit                │
│  • Feed sorted sets, reaction sets              │
│  • Cache không fit trong Caffeine (RAM giới hạn)│
│  • Pub/Sub cho realtime events                  │
└─────────────────────┬───────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────┐
│         L3: PostgreSQL + Elasticsearch           │
│  • PostgreSQL: durable storage                   │
│  • Elasticsearch: fulltext search + analytics    │
└─────────────────────────────────────────────────┘
```

**Caffeine vs Redis vs Không cache:**
```
Metric          | Không cache | Chỉ Redis  | Caffeine + Redis
────────────────┼─────────────┼────────────┼─────────────────
Latency (p99)   | 50-200ms    | 5-20ms     | <1ms (Caffeine hit)
Throughput      | 1k RPS      | 10k RPS    | 100k+ RPS
Cache hit rate  | 0%          | 70-80%     | 90-95% (hot data)
Cost (RAM)      | 0           | 8GB        | 8GB Redis + 2GB heap
Network cost    | 0           | Có         | Không (Caffeine local)
Cache stampede  | N/A         | Có         | Giảm (Caffeine bảo vệ)
Complexity      | Thấp        | Trung bình | Cao hơn 1 chút
```

**Dependency:**
```xml
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>
```

---

## 5. TESTING STRATEGY

### 5.1 Kiến trúc testing

```
┌─────────────────────────────────────────────────┐
│                  Unit Tests                       │
│  • Service logic (mock dependencies)             │
│  • Mapper logic                                  │
│  • Validator logic                               │
│  • Utility classes                               │
│  Coverage target: > 80%                          │
└─────────────────────┬───────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────┐
│              Integration Tests                    │
│  • Repository tests (Testcontainers)             │
│  • API endpoint tests (MockMvc)                  │
│  • Cache integration tests                       │
│  • WebSocket tests                               │
└─────────────────────┬───────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────┐
│               E2E Tests                           │
│  • Full user flow tests                          │
│  • Performance tests (k6, Gatling)               │
│  • Security tests (OWASP ZAP)                    │
└─────────────────────────────────────────────────┘
```

### 5.2 Testcontainers Setup
```java
@SpringBootTest
@Testcontainers
class PostRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.4")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private PostRepository postRepository;

    @Test
    void shouldSaveAndRetrievePost() {
        // Given
        User user = createUser("testuser");
        Post post = new Post();
        post.setContent("Nội dung bài viết test");
        post.setUser(user);

        // When
        Post saved = postRepository.save(post);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getContent()).isEqualTo("Nội dung bài viết test");
    }
}
```

### 5.3 API Test với MockMvc
```java
@SpringBootTest
@AutoConfigureMockMvc
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @Test
    @WithMockUser
    void shouldCreatePost() throws Exception {
        // Given
        CreatePostRequest request = new CreatePostRequest();
        request.setContent("Bài viết mới");
        request.setVisibility("PUBLIC");

        PostResponse response = PostResponse.builder()
                .id(1L)
                .content("Bài viết mới")
                .build();

        when(postService.createPost(anyLong(), any(CreatePostRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.content").value("Bài viết mới"));
    }
}
```

---

## 6. SECURITY IMPROVEMENTS

### 6.0 Message Encryption (End-to-End)

**Hiện tại:** Code chat `MessageServiceImpl` lưu message content plaintext → admin/dev đều đọc được.

**Vấn đề:**
```java
// MessageServiceImpl.sendMessage - content plaintext
Message message = Message.builder()
    .conversation(conversation)
    .sender(sender)
    .content(request.getContent())  // Encrypted hay plaintext?
    .type(request.getType())
    .build();
```

**Giải pháp: End-to-End Encryption (E2EE)**

Có 2 approach tùy theo mức độ bảo mật:

---

#### Option 1: Client-side E2EE (Signal Protocol - Khuyến nghị)

```
┌──────────────┐           ┌──────────────┐
│   User A     │           │   User B     │
│  (Client)    │           │  (Client)    │
├──────────────┤           ├──────────────┤
│ 1. Tạo key   │           │ 2. Gửi public│
│    pair      │──────────►│    key cho A │
│ 3. Nhận key  │◄──────────│              │
│ 4. Mã hóa    │──────────►│ 5. Giải mã   │
│    "Hello"   │  Encrypted │    "Hello"   │
│    → "x7$..."│   "x7$..."│    ← "x7$..."│
└──────┬───────┘           └──────┬───────┘
       │                          │
       │      ┌──────────┐       │
       └──────►  Backend  ◄───────┘
               │ (Không thể│
               │  đọc được)│
               │  Lưu      │
               │  "x7$..." │
               └──────────┘
```

**Cách hoạt động:**
1. Mỗi user tạo key pair (public + private) trên client
2. Public key gửi lên server, private key giữ ở client
3. User A lấy public key của User B từ server
4. Client A mã hóa message bằng public key của B trước khi gửi lên server
5. Server chỉ lưu ciphertext, không thể đọc
6. Client B nhận ciphertext, giải mã bằng private key của mình

**Implement:**

```java
// ===== Entity =====
@Entity
@Table(name = "user_keys")
public class UserKey {
    @Id
    private Long userId;
    
    @Column(nullable = false, length = 500)
    private String publicKey;  // Client RSA-4096 hoặc Curve25519
    
    @Column(nullable = false, length = 50)
    private String keyAlgorithm; // "RSA-4096", "X25519"
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime revokedAt;
}

@Entity
@Table(name = "messages")
public class Message extends BaseEntity {
    // ... existing fields
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String encryptedContent;  // Rename content → encryptedContent
    
    @Column(length = 100)
    private String encryptionAlgorithm; // "RSA-OAEP", "AES-256-GCM"
    
    @Column(length = 500)
    private String keyId;  // ID của public key đã dùng để mã hóa
}
```

```java
// ===== API =====
// 1. Upload public key
@PostMapping("/keys")
public ResponseEntity<?> uploadPublicKey(@RequestBody PublicKeyRequest request) {
    // userKeysService.savePublicKey(currentUserId, request.getPublicKey(), request.getAlgorithm());
}

// 2. Lấy public key của người khác
@GetMapping("/keys/{userId}")
public ResponseEntity<?> getPublicKey(@PathVariable Long userId) {
    // String publicKey = userKeysService.getPublicKey(userId);
}

// 3. Gửi message (client đã mã hóa trước)
@PostMapping("/send")
public ResponseEntity<?> sendMessage(@Valid @RequestBody SendMessageRequest request) {
    // request.getEncryptedContent() ← đã mã hóa từ client
    // messageService.sendMessage(userId, conversationId, request);
}
```

```java
// ===== Client-side (JavaScript với Web Crypto API) =====
// 1. Tạo key pair
async function generateKeyPair() {
    const keyPair = await crypto.subtle.generateKey(
        {
            name: "RSA-OAEP",
            modulusLength: 4096,
            publicExponent: new Uint8Array([1, 0, 1]),
            hash: "SHA-256",
        },
        true,
        ["encrypt", "decrypt"]
    );
    
    // Export public key gửi lên server
    const publicKey = await crypto.subtle.exportKey("spki", keyPair.publicKey);
    // privateKey giữ ở IndexedDB/localStorage
    return { publicKey, privateKey };
}

// 2. Mã hóa message
async function encryptMessage(message, recipientPublicKey) {
    const encoded = new TextEncoder().encode(message);
    const encrypted = await crypto.subtle.encrypt(
        { name: "RSA-OAEP" },
        recipientPublicKey,
        encoded
    );
    return arrayBufferToBase64(encrypted); // Gửi lên server
}

// 3. Giải mã message
async function decryptMessage(encryptedBase64, privateKey) {
    const encrypted = base64ToArrayBuffer(encryptedBase64);
    const decrypted = await crypto.subtle.decrypt(
        { name: "RSA-OAEP" },
        privateKey,
        encrypted
    );
    return new TextDecoder().decode(decrypted);
}
```

**Ưu điểm:** Bảo mật thực sự, server không thể đọc
**Nhược điểm:** 
- Mất tính năng server-side search trong messages
- Không thể recover message nếu mất private key
- Phức tạp hơn cho client implementation

---

#### Option 2: Server-side AES-256 (Đơn giản hơn)

```java
// ===== Thêm vào CryptoUtil =====
@Component
public class CryptoUtil {
    
    @Value("${app.encryption.secret-key}")
    private String encryptionKey;  // 256-bit key từ env
    
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    
    /**
     * Mã hóa message trước khi lưu vào DB
     */
    public String encrypt(String plaintext) {
        try {
            byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            keyBytes = sha.digest(keyBytes); // Chuẩn hóa về 256-bit
            
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom.getInstanceStrong().nextBytes(iv);
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] encrypted = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, encrypted, 0, iv.length);
            System.arraycopy(ciphertext, 0, encrypted, iv.length, ciphertext.length);
            
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }
    
    /**
     * Giải mã message từ DB
     */
    public String decrypt(String encryptedBase64) {
        try {
            byte[] encrypted = Base64.getDecoder().decode(encryptedBase64);
            byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            keyBytes = sha.digest(keyBytes);
            
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
            byte[] iv = Arrays.copyOfRange(encrypted, 0, GCM_IV_LENGTH);
            byte[] ciphertext = Arrays.copyOfRange(encrypted, GCM_IV_LENGTH, encrypted.length);
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
```

```java
// ===== MessageServiceImpl - Tự động mã hóa =====
@Service
public class MessageServiceImpl implements MessageService {
    
    private final CryptoUtil cryptoUtil;  // Inject
    
    @Transactional
    public MessageResponse sendMessage(Long userId, Long conversationId, SendMessageRequest request) {
        // ... validate ...
        
        // Tự động mã hóa trước khi lưu
        String encryptedContent = cryptoUtil.encrypt(request.getContent());
        
        Message message = Message.builder()
            .conversation(conversation)
            .sender(sender)
            .content(encryptedContent)  // Lưu encrypted content
            .type(request.getType())
            .build();
        
        message = messageRepository.save(message);
        
        // Decrypt khi response (hoặc để client tự decrypt)
        // message.setContent(cryptoUtil.decrypt(message.getContent()));
        // Hoặc gửi encrypted, client không cần decrypt
    }
}
```

```yaml
# application.yml - Thêm key mã hóa (phải từ env)
app:
  encryption:
    secret-key: ${MESSAGE_ENCRYPTION_KEY:aes256-gcm-secret-key-must-be-32-bytes!}
```

**Ưu điểm:** Đơn giản, developer kiểm soát được, search vẫn hoạt động (nếu decrypt trước khi search)
**Nhược điểm:** Bảo mật kém hơn E2EE vì server có key

---

#### Option 3: Hybrid (Khuyến nghị cho production)

```
┌──────────────┐           ┌──────────────┐           ┌──────────────┐
│   User A     │           │   Backend    │           │   User B     │
├──────────────┤           ├──────────────┤           ├──────────────┤
│ 1. Tạo key   │           │              │           │ 2. Tạo key   │
│    pair      │──────────►│ Lưu public   │◄──────────│    pair      │
│              │  public   │   key        │  public   │              │
│ 3. Lấy B's  │◄──────────│              │──────────►│ 4. Lấy A's  │
│    public key│  B's key  │              │  A's key  │    public key│
│ 5. Tạo AES   │           │              │           │              │
│    session   │           │              │           │              │
│    key       │           │              │           │              │
│ 6. Mã hóa    │──────────►│ Lưu          │──────────►│ 7. Giải mã   │
│    message   │  Encrypted│ encrypted    │  Encrypted│    → AES key │
│    bằng AES  │  + keyId  │ content      │  + keyId  │    → message │
└──────────────┘           └──────────────┘           └──────────────┘
```

```java
// Client: Mã hóa hybrid
// 1. Tạo AES session key ngẫu nhiên
const aesKey = await crypto.subtle.generateKey({ name: "AES-GCM", length: 256 }, true, ["encrypt"]);

// 2. Mã hóa message bằng AES
const encryptedContent = await crypto.subtle.encrypt({ name: "AES-GCM", iv: iv }, aesKey, message);

// 3. Mã hóa AES key bằng RSA public key của người nhận
const encryptedAesKey = await crypto.subtle.encrypt({ name: "RSA-OAEP" }, recipientPublicKey, aesKey);

// 4. Gửi lên server: encryptedContent + encryptedAesKey + keyId
// Server lưu cả 3, không thể đọc nội dung
```

**Cấu hình message entity:**
```java
@Entity
@Table(name = "messages")
public class Message extends BaseEntity {
    // ... existing fields
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String encryptedContent;  // Mã hóa nội dung
    
    @Column(length = 500)
    private String encryptedKey;  // AES key mã hóa bằng RSA public của recipient
    
    @Column(length = 50)
    private String keyId;  // ID của public key đã dùng
    
    @Column(length = 20)
    private String algorithm; // "hybrid-aes256-gcm-rsa4096"
}
```

---

#### Database Migration cho E2EE

```sql
-- V13: Add message encryption support
ALTER TABLE messages RENAME COLUMN content TO encrypted_content;
ALTER TABLE messages ADD COLUMN encrypted_key VARCHAR(500);
ALTER TABLE messages ADD COLUMN key_id VARCHAR(50);
ALTER TABLE messages ADD COLUMN encryption_algorithm VARCHAR(20) DEFAULT 'plaintext';

-- User keys table
CREATE TABLE user_keys (
    user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    public_key TEXT NOT NULL,
    key_algorithm VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    revoked_at TIMESTAMP,
    created_by_connection_id VARCHAR(100)
);

CREATE INDEX idx_user_keys_active ON user_keys(user_id) WHERE revoked_at IS NULL;
```

**Đánh giá các option:**

| Tiêu chí | Option 1 (Client E2EE) | Option 2 (Server AES) | Option 3 (Hybrid) |
|----------|----------------------|---------------------|------------------|
| Bảo mật | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| Server search | ❌ Không | ⚠️ Search trước encrypt | ❌ Không |
| Complexity | Cao | Thấp | Trung bình |
| Key recovery | Không | Có (server biết key) | Không |
| Performance | Client chịu | Server chịu | Client chịu |
| Compliance (GDPR) | Tốt | Trung bình | Tốt |
| WebSocket realtime | Encrypted payload | Encrypted payload | Encrypted payload |

**Khuyến nghị:**
- **MVP:** Option 2 (Server AES) - đơn giản, đủ bảo vệ data-at-rest
- **Production:** Option 3 (Hybrid) - bảo mật cao, không quá phức tạp
- **Enterprise/Messaging app:** Option 1 (Signal Protocol) - bảo mật tuyệt đối

---

### 6.1 JWT Security
```java
// JWT từ environment variable, không hardcode
@Value("${jwt.secret}")
private String jwtSecret;

// Blacklist token khi logout
@Component
public class TokenBlacklist {
    private final RedisTemplate<String, String> redisTemplate;
    
    public void blacklist(String token, long ttlSeconds) {
        String key = "blacklist:" + token;
        redisTemplate.opsForValue().set(key, "revoked", ttlSeconds, TimeUnit.SECONDS);
    }
    
    public boolean isBlacklisted(String token) {
        return redisTemplate.hasKey("blacklist:" + token);
    }
}
```

### 6.2 CORS Configuration
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("https://yourdomain.com"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
}
```

### 6.3 Rate Limiting per Endpoint
```java
@RateLimit(capacity = 10, refillTokens = 10, refillDurationSeconds = 60)
@PostMapping("/auth/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    // Max 10 login attempts per minute
}

@RateLimit(capacity = 20, refillTokens = 20, refillDurationSeconds = 3600)
@PostMapping("/posts")
public ResponseEntity<?> createPost(@RequestBody CreatePostRequest request) {
    // Max 20 posts per hour
}
```

---

## 7. PERFORMANCE OPTIMIZATION

### 7.1 Database Indexing
```sql
-- Composite indexes cho common queries
CREATE INDEX idx_posts_user_created ON posts(user_id, created_at DESC);
CREATE INDEX idx_posts_visibility_created ON posts(visibility, created_at DESC);
CREATE INDEX idx_comments_post_created ON comments(post_id, created_at);
CREATE INDEX idx_messages_conversation_created ON messages(conversation_id, created_at DESC);
CREATE INDEX idx_friends_requester_status ON friends(requester_id, status);
CREATE INDEX idx_friends_addressee_status ON friends(addressee_id, status);

-- Partial indexes cho soft delete
CREATE INDEX idx_posts_active ON posts(created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_users_active ON users(email) WHERE deleted_at IS NULL;
```

### 7.2 Query Optimization
```java
// Trước: N+1 query
List<Post> posts = postRepository.findAll();
for (Post post : posts) {
    User author = post.getUser(); // Lazy loading → N+1
}

// Sau: JOIN FETCH
@Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.deletedAt IS NULL ORDER BY p.createdAt DESC")
List<Post> findAllWithUser();

// Hoặc Entity Graph
@EntityGraph(attributePaths = {"user", "media"})
@Query("SELECT p FROM Post p WHERE p.deletedAt IS NULL")
List<Post> findAllWithDetails();
```

### 7.3 Batch Operations
```java
// Trước: Save từng item
for (Post post : posts) {
    postRepository.save(post); // N queries
}

// Sau: Batch save
postRepository.saveAll(posts); // 1 query

// Bulk update
@Modifying
@Query("UPDATE Post p SET p.likesCount = p.likesCount + :delta WHERE p.id = :postId")
int incrementLikeCount(@Param("postId") Long postId, @Param("delta") int delta);
```

### 7.4 Connection Pool Tuning
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
      idle-timeout: 300000      # 5 phút
      max-lifetime: 1800000     # 30 phút
      connection-timeout: 5000  # 5 giây
      leak-detection-threshold: 60000
      pool-name: FacebookHikariPool
```

---

## 8. MONITORING & OBSERVABILITY

### 8.1 Health Indicators
```java
@Component
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements HealthIndicator {
    private final JdbcTemplate jdbcTemplate;
    
    @Override
    public Health health() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return Health.up()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("connection", "OK")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
```

### 8.2 Metrics Collection
```java
@Component
@RequiredArgsConstructor
public class MetricsCollector {
    private final MeterRegistry meterRegistry;
    
    public void recordApiCall(String endpoint, String method, long durationMs) {
        Timer.builder("api.request.duration")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }
    
    public void recordCacheHit(String cacheName) {
        Counter.builder("cache.hits")
                .tag("cache", cacheName)
                .register(meterRegistry)
                .increment();
    }
    
    public void recordCacheMiss(String cacheName) {
        Counter.builder("cache.misses")
                .tag("cache", cacheName)
                .register(meterRegistry)
                .increment();
    }
}
```

### 8.3 Structured Logging
```yaml
# logging-pattern-console
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{correlationId}] %-5level %logger{36} - %msg%n"
  
# JSON logging cho ELK
logging:
  logback:
    rollingpolicy:
      max-file-size: 100MB
      max-history: 30
      total-size-cap: 1GB
```

---

## 9. TỔNG HỢP ĐIỂM

| Tiêu chí | Điểm | Kế hoạch cải thiện |
|----------|------|-------------------|
| SOLID | 5/10 | Tách Service, Strategy cho FeedRanking, Interface segregation |
| Mở rộng | 5/10 | Pull-model feed, Elasticsearch, Caffeine cache, SCAN thay KEYS |
| Code hiện đại | 6/10 | Records, Virtual threads, Pattern matching, StringTemplate |
| Nghiệp vụ | 6/10 | Thêm share/bookmark/report/block/follow/verify/reset/2FA |
| Infrastructure | 5/10 | +Elasticsearch, +Caffeine, +CI/CD, +Grafana |
| Security | 4/10 | JWT từ env, logout + blacklist, CSRF, CORS restrict |
| Testing | 1/10 | JUnit 5 + Testcontainers (Postgres, Redis, MinIO) |
| Performance | 4/10 | Caffeine, JOIN FETCH, SCAN, atomic counter, batch |
| Maintainability | 5/10 | Refactor duplicate code, xóa TODO, SRP |

**Tổng hiện tại: ~4.5/10**
**Mục tiêu 6 tháng: 7.5/10**
**Mục tiêu 1 năm (production scale 1M users): 9/10**

---

## 10. ROADMAP CẢI THIỆN

### Phase 1 (Tháng 1-2): Foundation
- [ ] Tách Service theo SRP
- [ ] Implement Caffeine cache (L1)
- [ ] Thêm Testcontainers cho unit tests
- [ ] Fix N+1 queries
- [ ] JWT blacklist cho logout

### Phase 2 (Tháng 3-4): Business
- [ ] Implement Share post
- [ ] Implement Bookmark/Save
- [ ] Implement Block user
- [ ] Email verification
- [ ] Password reset
- [ ] Admin panel cơ bản

### Phase 3 (Tháng 5-6): Infrastructure
- [ ] Elasticsearch cho search
- [ ] Kafka cho event persistence
- [ ] CI/CD với GitHub Actions
- [ ] Monitoring với Prometheus + Grafana
- [ ] Load testing với k6

### Phase 4 (Tháng 7-12): Scale
- [ ] Pull-model feed cho celebrity
- [ ] Database sharding
- [ ] CDN cho media
- [ ] Advanced analytics
- [ ] AI-powered recommendations

---

## 11. ACTUAL CODE AUDIT RESULTS (June 2026)

### 11.1 Compile-Blocking Issues (60+ errors)

These issues would **prevent compilation** entirely:

| # | File | Issue | Severity |
|---|------|-------|----------|
| 1 | 5 Service Impl files | Missing `implements InterfaceName` | CRITICAL |
| 2 | `auth/service/impl/AuthServiceImpl.java` | import `com.fb.common.exception.*` → wrong package (should be `com.fb.exception`) | CRITICAL |
| 3 | `friend/service/impl/FriendServiceImpl.java` | import `com.fb.common.exception.*` → same | CRITICAL |
| 4 | `reaction/service/impl/ReactionServiceImpl.java` | import `com.fb.common.exception.*` → same | CRITICAL |
| 5 | `post/service/impl/PostServiceImpl.java` | import `com.fb.common.exception.ResourceNotFoundException` | CRITICAL |
| 6 | `post/service/PostService.java` | imports `UserResponse` from wrong package | CRITICAL |
| 7 | `user/service/impl/UserServiceImpl.java` | import `com.fb.common.exception.ResourceNotFoundException` | CRITICAL |
| 8 | `conversation/service/impl/ConversationServiceImpl.java` | import `com.fb.common.exception.*` | CRITICAL |
| 9 | `message/service/impl/MessageServiceImpl.java` | import `com.fb.common.exception.*` | CRITICAL |
| 10 | `notification/service/impl/NotificationServiceImpl.java` | import `com.fb.common.exception.*` | CRITICAL |
| 11 | `FriendServiceImpl` | Uses `Friend.FriendStatus` — inner enum doesn't exist (entity has `Status` top-level) | CRITICAL |
| 12 | `ReactionServiceImpl` | Uses `Reaction.ReactionType` — inner enum doesn't exist (entity has `Type` top-level) | CRITICAL |
| 13 | `PostServiceImpl` | Uses `Post.PostType` — inner enum doesn't exist (entity has `Type` top-level) | CRITICAL |
| 14 | `MessageServiceImpl` | Uses `Conversation.ConversationType` — inner enum doesn't exist | CRITICAL |
| 15 | `AuthController` | Calls `authService.register(RegisterRequest)` — method doesn't exist in impl | CRITICAL |
| 16 | `AuthController` | Calls `authService.logout(..)` — no logout method | CRITICAL |
| 17 | `FriendController` | Calls `friendService.sendRequest(..)` with 3 args — method has 2 | CRITICAL |
| 18 | `FriendController` | Calls `friendService.acceptRequest(..)` — method doesn't exist | CRITICAL |
| 19 | `FriendController` | Calls `friendService.getFriends(pageable)` — method expects no params | CRITICAL |
| 20 | `ReactionController` | Calls `reactionService.togglePostReaction(..)` — method has different args | CRITICAL |
| 21 | `ConversationController` | Calls `conversationService.getConversations(pageable)` — method expects no params | CRITICAL |
| 22 | `MessageController` | Calls `messageService.getMessages(pageable)` — method expects no params | CRITICAL |
| 23 | `UserController` | Calls `userService.getCurrentUser()` — no such method | CRITICAL |
| 24 | `UserController` | Calls `userService.updatePassword(..)` — method expects User not request | CRITICAL |
| 25 | `UserMapper.id()` | Returns `String` but callers expect `Long` | CRITICAL |
| 26 | `JwtTokenProvider.generateAccessToken(extractUserId)` | 1 arg but method expects 2 args | CRITICAL |
| 27 | `PostServiceImpl` | `postRepository.findByUser_IdOrderByCreatedAtDesc()` — no matching method name | CRITICAL |
| 28 | `FriendServiceImpl` | `friendRepository.findFriends(..)` — no such repo method | CRITICAL |
| 29 | `FriendServiceImpl` | `friendRepository.findByRequesterIdAndStatus(..)` — no such repo method | CRITICAL |
| 30 | `FriendServiceImpl` | `friendRepository.findByAddresseeIdAndStatus(..)` — no such repo method | CRITICAL |
| 31 | `MessageServiceImpl` | `messageRepository.findByConversation_IdOrderByCreatedAtAsc(..)` — mismatch | CRITICAL |
| 32 | `NotificationServiceImpl` | `notificationRepository.findByUserIdOrderByCreatedAtDesc(..)` — mismatch | CRITICAL |
| 33 | `JPQL in NotificationRepository` | `WHERE n.read = false` — field is `isRead` not `read` | CRITICAL |
| 34 | `PostOrchestrator` |  `Stream.map(createPostMediaRequest -> mediaRequest -> ...)` — 2-param lambda in Stream.map | CRITICAL |
| 35 | `PostOrchestrator` | `mediaRequests = Set.of(mediaService.createMedia(...))` — Set cannot hold List return type | CRITICAL |
| 36 | `PostService` | `mediaService` field not injected (no constructor, no `@Autowired`) | CRITICAL |
| 37 | `CacheService.expire()` | Missing `TimeUnit` argument | CRITICAL |
| 38 | `PostEventListener` | Calls `event.getPost()` — no such method (PostEvent has no getPost) | CRITICAL |
| 39 | `CircuitBreakerConfig` | `.compose()` called on `Callable` — wrong API (should be `decorateCallable`) | CRITICAL |
| 40 | `WebSocketConfig` | `setTaskScheduler(null)` — null scheduler passed | CRITICAL |
| 41 | `ReactionServiceImpl` | `reactable.getComments().size()` — LazyInitializationException (outside tx) | CRITICAL |

### 11.2 Runtime Bugs

| # | File | Issue | Severity |
|---|------|-------|----------|
| 1 | `PostOrchestrator.searchPosts()` | `LIKE '%keyword%'` — full table scan, no index usage | HIGH |
| 2 | `FriendServiceImpl` | `friend.setStatus(FriendStatus.valueOf(..))` — `FriendStatus` doesn't exist | HIGH |
| 3 | `KafkaMessageBroker` | Consumer handler body rỗng → **silent fail**, messages lost | HIGH |
| 4 | All `@Scheduled` methods | Use `fixedRate` instead of `fixedDelay` → overlap risk | MEDIUM |
| 5 | `CacheService.deleteByPattern` | Uses `KEYS` command → blocks Redis in production | HIGH |
| 6 | `WebSocketEventListener` | `@EventListener` on class without `@Component` — not registered | HIGH |
| 7 | `ReactionServiceImpl.isDuplicate()` | Infinite loop if `findByUserAndReactableAndType` returns null | MEDIUM |

### 11.3 Empty Method Bodies (Will compile but do nothing)

| # | File | Method | Lines |
|---|------|--------|-------|
| 1 | `FeedServiceImpl` | `buildFeed()` | 43-49 (empty) |
| 2 | `FeedServiceImpl` | `getNewsFeed()` | 52-58 (empty) |
| 3 | `MessageServiceImpl` | `sendMessageToConversation()` | 45-49 (empty) |
| 4 | `MessageServiceImpl` | `getConversationMessages()` | 52-58 (empty) |
| 5 | `ConversationServiceImpl` | `createConversation()` | 32-36 (empty) |
| 6 | `ConversationServiceImpl` | `getConversations()` | 39-45 (empty) |
| 7 | `NotificationServiceImpl` | `sendFriendRequestNotification()` | 31-35 (empty) |
| 8 | `NotificationServiceImpl` | `sendPostNotification()` | 38-42 (empty) |
| 9 | `NotificationServiceImpl` | `sendCommentNotification()` | 45-49 (empty) |
| 10 | `NotificationServiceImpl` | `sendReactionNotification()` | 52-56 (empty) |
| 11 | `NotificationServiceImpl` | `sendMessageNotification()` | 59-63 (empty) |
| 12 | `KafkaMessageBroker` | `sendMessage()` | 27-31 (empty) |
| 13 | `KafkaMessageBroker` | Consumer lambda | 37-41 (empty) |
| 14 | `ScheduledTasks` | `cleanupExpiredTokens()` | 27-32 (empty) |
| 15 | `ScheduledTasks` | `cleanupSoftDeletedPosts()` | 35-40 (empty) |
| 16 | `ScheduledTasks` | `syncFriendCounts()` | 43-48 (empty) |
| 17 | `ScheduledTasks` | `cleanupReadNotifications()` | 51-56 (empty) |
| 18 | `ScheduledTasks` | `syncConversationLastMessage()` | 59-64 (empty) |

### 11.4 TODOs (12 items in 10 files)

| File | Line | TODO |
|------|------|------|
| `FeedServiceImpl` | 44 | `// TODO: implement feed building` |
| `MessageServiceImpl` | 46 | `// TODO: implement message sending` |
| `ConversationServiceImpl` | 33 | `// TODO: implement conversation creation` |
| `NotificationServiceImpl` | 32 | `// TODO: implement notification sending` |
| `KafkaMessageBroker` | 38 | `// TODO: implement message processing` |
| `ScheduledTasks` | 28 | `// TODO: implement cleanup` |
| `ScheduledTasks` | 36 | `// TODO: implement soft delete cleanup` |
| `ScheduledTasks` | 44 | `// TODO: implement friend count sync` |
| `ScheduledTasks` | 52 | `// TODO: implement notification cleanup` |
| `ScheduledTasks` | 60 | `// TODO: implement conversation sync` |
| `AuthController` | 38 | `// TODO: add refresh token endpoint` |
| `PostOrchestrator` | 89 | `// TODO: support full-text search` |

### 11.5 Code Quality Issues

| Issue | Count | Examples |
|-------|-------|---------|
| `throw new RuntimeException(...)` | 15 | All service impls |
| `catch (Exception e)` (too broad) | 27 | All controllers & services |
| Method > 50 lines | 8 | `PostOrchestrator.createPost` (180 lines) |
| N+1 queries | 5+ | `PostOrchestrator` loops through users/posts without JOIN FETCH |
| Duplicate code > 80% | 1 | `togglePostReaction` vs `toggleCommentReaction` |
| Unused imports | 30+ | Across all files with wrong package imports |
| JWT secret hardcoded | 1 | `application.yml: your-256-bit-secret-key-change-in-production` |
| DB password hardcoded | 1 | `application.yml: 123456` |
| Empty package | 2 | `com.fb.feed.engine`, `com.fb.infrastructure.messaging.events` (empty dirs) |
| Orphan file | 1 | `auth/model/Gender.java` (duplicate of `common/enums/Gender.java`) |

### 11.6 Infrastructure Weaknesses

| Issue | Detail |
|-------|--------|
| No E2EE | Message content stored as plaintext |
| No JWT blacklist | Logout doesn't invalidate token |
| No Rate Limiting | API vulnerable to abuse |
| No CORS restriction | `allowedOrigins: "*"` |
| No tests | Zero JUnit/Testcontainers tests |
| No CI/CD | No GitHub Actions, no Docker build pipeline |
| No monitoring | No Micrometer/Prometheus/Grafana |
| No connection pooling config | Default HikariCP settings |
| Missing 11 dependencies | kafka-clients, minio, resilience4j, caffeine, etc. not in pom.xml |

*Generated: June 2026*
