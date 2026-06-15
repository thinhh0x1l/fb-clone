# Technology Stack

## Overview

Full-stack social networking platform using Spring Boot 4.1 + Vue 3.5 + Element Plus 2.14.

---

## Version Compatibility Matrix

| Technology | Version | Release Date | Compatibility |
|------------|---------|--------------|---------------|
| Java | 21 LTS | Sep 2023 | Spring Boot 4.1 required |
| Spring Boot | 4.1.0 | Jun 2025 | Requires Java 21+ |
| Spring Framework | 7.2.x | Jun 2025 | Spring Boot 4.1 dependency |
| Spring Security | 7.4.x | Jun 2025 | Compatible with Spring Boot 4.1 |
| Vue | 3.5.x | Latest stable | Compatible with all Vue 3 ecosystem |
| Vite | 6.3.x | Latest stable | Vue 3 recommended build tool |
| TypeScript | 5.8.x | Latest stable | Full Vue 3 + Element Plus support |
| Element Plus | 2.14.2 | Jun 2025 | Vue 3.5+ compatible |
| Pinia | 3.0.x | Latest stable | Vue 3.5+ compatible |
| Vue Router | 4.5.x | Latest stable | Vue 3.5+ compatible |

---

## Backend

### Core Framework
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 LTS | Language runtime |
| Spring Boot | 4.1.0 | Application framework |
| Spring Framework | 7.2.x | Core IoC, AOP |
| Spring Security | 7.4.x | Authentication & Authorization |
| Spring Data JPA | 3.4.x | ORM / Database access |
| Spring WebSocket | 7.2.x | Real-time communication |
| Spring Validation | 4.0.x | Input validation (Jakarta) |

### Authentication & Security
| Technology | Version | Purpose |
|------------|---------|---------|
| Nimbus JOSE+JWT | 10.x | JWT token generation/verification (via spring-security-oauth2-jose) |
| Spring Security OAuth2 Resource Server | 7.4.x | JWT validation & authentication |
| Spring Security OAuth2 Client | 7.4.x | Social login (Google, Facebook) |
| BCrypt | Built-in | Password hashing |
| CORS Filter | Built-in | Cross-origin requests |

> **Why Nimbus over jjwt?**
> - Spring Security uses Nimbus JOSE+JWT internally (`spring-security-oauth2-jose`)
> - `JwtDecoder`, `JwtEncoder` are built on Nimbus - no extra dependencies needed
> - Full JOSE/JWS/JWE/JWK specification support
> - Better standards compliance and enterprise-grade features
> - Simpler dependency management (one starter vs three jjwt packages)

### Database & Cache
| Technology | Version | Purpose |
|------------|---------|---------|
| PostgreSQL | 16 | Primary relational database |
| Redis | 7.4 | Caching, session store, pub/sub |
| Flyway | 11.x | Database migration |
| HikariCP | Built-in | Connection pooling |

### Search Engine
| Technology | Version | Purpose |
|------------|---------|---------|
| Elasticsearch | 8.15.x | Full-text search, autocomplete |

### Object Storage
| Technology | Version | Purpose |
|------------|---------|---------|
| MinIO | Latest | S3-compatible media storage |

### Messaging & Events
| Technology | Version | Purpose |
|------------|---------|---------|
| Spring Events | Built-in | In-process event bus |
| Redis Pub/Sub | Built-in | Cross-instance event propagation |

### Utilities
| Technology | Version | Purpose |
|------------|---------|---------|
| MapStruct | 1.6.x | DTO mapping |
| Lombok | 1.18.x | Boilerplate reduction |
| Bucket4j | 8.x | Rate limiting |
| OpenAPI 3 | 2.3.x | API documentation (Swagger UI) |

### Build Tool
| Technology | Version | Purpose |
|------------|---------|---------|
| Maven | 3.9.x | Build automation |
| Maven Wrapper | 3.9.x | Consistent build across team |

---

## Frontend

### Core Framework
| Technology | Version | Purpose |
|------------|---------|---------|
| Vue | 3.5.x | UI framework |
| TypeScript | 5.8.x | Type safety |
| Vite | 6.3.x | Build tool & dev server |
| Vue Router | 4.5.x | Client-side routing |
| Pinia | 3.0.x | State management |

### UI Components & Styling
| Technology | Version | Purpose |
|------------|---------|---------|
| Element Plus | 2.14.2 | UI component library |
| SCSS (sass) | 1.83.x | CSS preprocessor |
| @element-plus/icons-vue | 2.3.x | Element Plus icon set |
| @vueuse/core | 12.x | Composable utilities |

### HTTP & Real-time
| Technology | Version | Purpose |
|------------|---------|---------|
| Axios | 1.8.x | HTTP client |
| socket.io-client | 4.8.x | WebSocket communication |

### Forms & Validation
| Technology | Version | Purpose |
|------------|---------|---------|
| Element Plus Form | Built-in | Form validation with rules |
| @vuelidate/core | 5.x | Additional form validation (optional) |

### Media
| Technology | Version | Purpose |
|------------|---------|---------|
| @element-plus/upload | Built-in | File upload component |
| Cropperjs | 1.6.x | Image cropping |

### Utilities
| Technology | Version | Purpose |
|------------|---------|---------|
| Day.js | 1.11.x | Date formatting |
| Nprogress | 0.2.x | Page load progress bar |
| @iconify/vue | 4.x | Icon framework |

### Linting & Formatting
| Technology | Version | Purpose |
|------------|---------|---------|
| ESLint | 9.x | Code linting |
| Prettier | 3.x | Code formatting |
| @vue/eslint-config-typescript | 14.x | Vue + TS linting |

---

## DevOps & Infrastructure

### Containerization
| Technology | Version | Purpose |
|------------|---------|---------|
| Docker | Latest | Container packaging |
| Docker Compose | V2+ | Local multi-service setup |
| Nginx | Latest | Reverse proxy, static file serving |

### CI/CD (Future)
| Technology | Purpose |
|------------|---------|
| GitHub Actions | CI/CD pipeline |
| SonarQube | Code quality analysis |

### Monitoring (Future)
| Technology | Purpose |
|------------|---------|
| Spring Actuator | Health checks, metrics |
| Prometheus | Metrics collection |
| Grafana | Dashboard visualization |

---

## Package Dependencies (No Conflict)

### Backend pom.xml - Key Dependencies
```xml
<!-- Spring Boot Starter Parent 4.1.0 -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.1.0</version>
</parent>

<!-- Core -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>

<!-- Database -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>

<!-- Security (Nimbus JOSE+JWT included via spring-security-oauth2-jose) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>

<!-- Utilities -->
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.6.3</version>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>

<!-- API Documentation -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>

<!-- Rate Limiting -->
<dependency>
    <groupId>com.bucket4j</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.10.1</version>
</dependency>
```

### Frontend package.json - Key Dependencies
```json
{
  "dependencies": {
    "vue": "^3.5.13",
    "vue-router": "^4.5.0",
    "pinia": "^3.0.2",
    "element-plus": "^2.14.2",
    "@element-plus/icons-vue": "^2.3.1",
    "axios": "^1.8.4",
    "socket.io-client": "^4.8.3",
    "dayjs": "^1.11.13",
    "@vueuse/core": "^12.8.2",
    "nprogress": "^0.2.0",
    "cropperjs": "^1.6.2"
  },
  "devDependencies": {
    "typescript": "~5.8.3",
    "vite": "^6.3.5",
    "@vitejs/plugin-vue": "^5.2.4",
    "sass": "^1.83.4",
    "eslint": "^9.28.0",
    "prettier": "^3.5.3",
    "@vue/eslint-config-typescript": "^14.5.0"
  }
}
```

---

## Project Structure

```
project-root/
├── backend/                              # Spring Boot 4.1
│   ├── src/main/java/com/fb/
│   │   ├── config/                       # Configuration classes
│   │   │   ├── SecurityConfig.java
│   │   │   ├── WebSocketConfig.java
│   │   │   ├── CorsConfig.java
│   │   │   ├── RedisConfig.java
│   │   │   └── MinioConfig.java
│   │   ├── modules/
│   │   │   ├── auth/
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── dto/
│   │   │   │   └── entity/
│   │   │   ├── user/
│   │   │   ├── post/
│   │   │   ├── comment/
│   │   │   ├── reaction/
│   │   │   ├── friend/
│   │   │   ├── message/
│   │   │   ├── notification/
│   │   │   ├── media/
│   │   │   └── search/
│   │   └── shared/
│   │       ├── dto/
│   │       ├── entity/
│   │       ├── exception/
│   │       └── utils/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── application-dev.yml
│   │   ├── application-prod.yml
│   │   └── db/migration/
│   └── pom.xml
│
├── frontend/                             # Vue 3.5 + Element Plus
│   ├── public/
│   ├── src/
│   │   ├── api/                          # API service layer
│   │   │   ├── auth.ts
│   │   │   ├── user.ts
│   │   │   ├── post.ts
│   │   │   ├── friend.ts
│   │   │   ├── message.ts
│   │   │   └── notification.ts
│   │   ├── assets/
│   │   │   ├── styles/
│   │   │   │   ├── variables.scss        # SCSS variables
│   │   │   │   ├── mixins.scss           # SCSS mixins
│   │   │   │   ├── element-override.scss # Element Plus theme
│   │   │   │   └── global.scss           # Global styles
│   │   │   └── images/
│   │   ├── components/
│   │   │   ├── common/                   # Shared components
│   │   │   │   ├── AppHeader.vue
│   │   │   │   ├── AppSidebar.vue
│   │   │   │   ├── AppFooter.vue
│   │   │   │   ├── LoadingSpinner.vue
│   │   │   │   ├── ImageUploader.vue
│   │   │   │   └── ConfirmDialog.vue
│   │   │   ├── auth/
│   │   │   │   ├── LoginForm.vue
│   │   │   │   └── RegisterForm.vue
│   │   │   ├── feed/
│   │   │   │   ├── NewsFeed.vue
│   │   │   │   └── FeedItem.vue
│   │   │   ├── post/
│   │   │   │   ├── CreatePost.vue
│   │   │   │   ├── PostCard.vue
│   │   │   │   └── PostDetail.vue
│   │   │   ├── comment/
│   │   │   │   ├── CommentList.vue
│   │   │   │   └── CommentItem.vue
│   │   │   ├── message/
│   │   │   │   ├── ChatList.vue
│   │   │   │   ├── ChatWindow.vue
│   │   │   │   └── MessageItem.vue
│   │   │   ├── notification/
│   │   │   │   ├── NotificationList.vue
│   │   │   │   └── NotificationItem.vue
│   │   │   └── profile/
│   │   │       ├── ProfileHeader.vue
│   │   │       ├── ProfileAbout.vue
│   │   │       └── ProfilePosts.vue
│   │   ├── composables/
│   │   │   ├── useAuth.ts
│   │   │   ├── useSocket.ts
│   │   │   ├── useInfiniteScroll.ts
│   │   │   └── useUpload.ts
│   │   ├── stores/
│   │   │   ├── auth.ts
│   │   │   ├── user.ts
│   │   │   ├── post.ts
│   │   │   ├── message.ts
│   │   │   ├── notification.ts
│   │   │   └── app.ts
│   │   ├── router/
│   │   │   ├── index.ts
│   │   │   └── guards.ts
│   │   ├── types/
│   │   │   ├── user.ts
│   │   │   ├── post.ts
│   │   │   ├── message.ts
│   │   │   └── common.ts
│   │   ├── utils/
│   │   │   ├── request.ts               # Axios instance
│   │   │   ├── storage.ts               # LocalStorage helpers
│   │   │   └── format.ts                # Date/number formatters
│   │   ├── views/
│   │   │   ├── auth/
│   │   │   │   ├── LoginView.vue
│   │   │   │   └── RegisterView.vue
│   │   │   ├── home/
│   │   │   │   └── HomeView.vue
│   │   │   ├── profile/
│   │   │   │   └── ProfileView.vue
│   │   │   ├── message/
│   │   │   │   └── MessageView.vue
│   │   │   ├── notification/
│   │   │   │   └── NotificationView.vue
│   │   │   ├── settings/
│   │   │   │   └── SettingsView.vue
│   │   │   └── search/
│   │   │       └── SearchView.vue
│   │   ├── App.vue
│   │   └── main.ts
│   ├── index.html
│   ├── vite.config.ts
│   ├── tsconfig.json
│   ├── .eslintrc.cjs
│   └── package.json
│
├── nginx/
│   └── default.conf
├── docker-compose.yml
├── .gitignore
└── README.md
```

---

## SCSS Structure

```scss
// assets/styles/variables.scss
// Element Plus theme overrides
$--color-primary: #1877f2;      // Facebook blue
$--color-success: #42b72a;
$--color-warning: #f7b928;
$--color-danger: #fa383e;
$--color-info: #909399;

// Custom variables
$border-radius-base: 8px;
$box-shadow-base: 0 1px 2px rgba(0, 0, 0, 0.1);
$font-family-base: 'Segoe UI', Helvetica, Arial, sans-serif;

// Breakpoints
$breakpoint-sm: 576px;
$breakpoint-md: 768px;
$breakpoint-lg: 992px;
$breakpoint-xl: 1200px;

// assets/styles/global.scss
@use 'variables' as *;
@use 'mixins' as *;

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: $font-family-base;
  background-color: #f0f2f5;
}

// Scrollbar styling
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}
```

---

## API Design

### REST Conventions
```
# Auth
POST   /api/v1/auth/register          → Register user
POST   /api/v1/auth/login             → Login
POST   /api/v1/auth/refresh           → Refresh token
POST   /api/v1/auth/logout            → Logout
POST   /api/v1/auth/forgot-password   → Request password reset
POST   /api/v1/auth/reset-password    → Reset password

# Users
GET    /api/v1/users/me               → Get current user profile
GET    /api/v1/users/{id}             → Get user profile
PUT    /api/v1/users/me               → Update profile
PUT    /api/v1/users/me/avatar        → Upload avatar
PUT    /api/v1/users/me/cover         → Upload cover photo
DELETE /api/v1/users/me               → Delete account

# Posts
GET    /api/v1/posts                  → Get feed (paginated)
POST   /api/v1/posts                  → Create post
GET    /api/v1/posts/{id}             → Get post detail
PUT    /api/v1/posts/{id}             → Update post
DELETE /api/v1/posts/{id}             → Delete post
GET    /api/v1/users/{id}/posts       → Get user's posts

# Comments
GET    /api/v1/posts/{id}/comments    → Get comments
POST   /api/v1/posts/{id}/comments    → Add comment
PUT    /api/v1/comments/{id}          → Update comment
DELETE /api/v1/comments/{id}          → Delete comment

# Reactions
POST   /api/v1/posts/{id}/reactions   → Add reaction
DELETE /api/v1/posts/{id}/reactions   → Remove reaction

# Friends
GET    /api/v1/friends                → Get friend list
GET    /api/v1/friends/requests       → Get pending requests
POST   /api/v1/friends/request        → Send friend request
PUT    /api/v1/friends/{id}/accept    → Accept request
DELETE /api/v1/friends/{id}/reject    → Reject request
DELETE /api/v1/friends/{id}           → Remove friend
GET    /api/v1/friends/suggestions    → Get friend suggestions

# Messages
GET    /api/v1/conversations          → Get conversation list
POST   /api/v1/conversations          → Create conversation
GET    /api/v1/conversations/{id}     → Get messages (paginated)
POST   /api/v1/conversations/{id}/messages → Send message

# Notifications
GET    /api/v1/notifications          → Get notifications (paginated)
PUT    /api/v1/notifications/read     → Mark all as read
PUT    /api/v1/notifications/{id}/read → Mark one as read

# Search
GET    /api/v1/search?q=keyword       → Global search
GET    /api/v1/search/users?q=keyword → Search users
GET    /api/v1/search/posts?q=keyword → Search posts

# Media
POST   /api/v1/media/upload           → Upload file
DELETE /api/v1/media/{id}             → Delete file
```

### WebSocket Events
```
# Authentication
connect → { token: "jwt_token" }

# Messaging
Client → Server:
  - message:send { conversationId, content, type }
  - typing:start { conversationId }
  - typing:stop { conversationId }
  - message:read { conversationId, messageId }

Server → Client:
  - message:new { conversationId, message }
  - typing:indicator { userId, conversationId, isTyping }
  - message:read { conversationId, messageId, userId }

# Notifications
Server → Client:
  - notification:new { type, data }
  - notification:count { unreadCount }

# Presence
Server → Client:
  - user:online { userId }
  - user:offline { userId }
  - user:status { userId, status }
```

---

## Recommended Additional Documents

| Document | Purpose |
|----------|---------|
| `SETUP.md` | Detailed dev environment setup guide |
| `API_REFERENCE.md` | Auto-generated API docs (OpenAPI) |
| `DATABASE.md` | ERD diagram and schema documentation |
| `DEPLOYMENT.md` | Production deployment guide |
| `CONTRIBUTING.md` | Contribution guidelines |
| `CHANGELOG.md` | Version history |
