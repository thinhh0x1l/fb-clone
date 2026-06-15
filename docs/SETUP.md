# Development Setup Guide

## Prerequisites

### Required Software
| Software | Version | Check Command |
|----------|---------|---------------|
| Java JDK | 21+ | `java --version` |
| Node.js | 20+ | `node --version` |
| npm/pnpm | Latest | `npm --version` |
| Docker | Latest | `docker --version` |
| Docker Compose | V2+ | `docker compose version` |
| Git | Latest | `git --version` |

### Optional Tools
| Tool | Purpose |
|------|---------|
| IntelliJ IDEA | Java IDE |
| VS Code | Frontend IDE |
| DBeaver | Database client |
| Postman / Bruno | API testing |
| RedisInsight | Redis GUI |

---

## Quick Start

### 1. Clone & Setup

```bash
# Clone repository
git clone <repository-url>
cd project-root

# Copy environment files
cp backend/.env.example backend/.env
cp frontend/.env.example frontend/.env
```

### 2. Start Infrastructure

```bash
# Start PostgreSQL, Redis, MinIO
docker compose up -d

# Verify services
docker compose ps
```

Services will be available at:
- PostgreSQL: `localhost:5432`
- Redis: `localhost:6379`
- MinIO Console: `http://localhost:9001`

### 3. Backend Setup

```bash
cd backend

# Install dependencies & run migrations
./mvnw clean install

# Start application
./mvnw spring-boot:run
```

Backend runs at: `http://localhost:8080`

### 4. Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Start dev server
npm run dev
```

Frontend runs at: `http://localhost:5173`

---

## Environment Variables

### Backend (.env)
```env
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/facebook
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000

# MinIO
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin

# OAuth2 (optional)
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
```

### Frontend (.env)
```env
VITE_API_BASE_URL=http://localhost:8080
VITE_WS_URL=ws://localhost:8080/ws
```

---

## Database

### Access PostgreSQL
```bash
# Using Docker
docker exec -it facebook-postgres psql -U postgres -d facebook

# Using psql
psql -h localhost -U postgres -d facebook
```

### Migrations
```bash
# Flyway runs automatically on startup
# Manual migration:
./mvnw flyway:migrate
```

---

## Common Commands

### Backend
```bash
./mvnw clean install          # Build
./mvnw spring-boot:run        # Run
./mvnw test                   # Test
./mvnw flyway:migrate         # Run migrations
./mvnw flyway:info            # Check migration status
```

### Frontend
```bash
npm install                   # Install deps
npm run dev                   # Dev server
npm run build                 # Production build
npm run lint                  # Lint
npm run type-check            # Type check
```

---

## Troubleshooting

### Port Already in Use
```bash
# Find process using port
netstat -ano | findstr :8080

# Kill process
taskkill /PID <process-id> /F
```

### Database Connection Issues
```bash
# Check if PostgreSQL is running
docker compose logs postgres

# Restart PostgreSQL
docker compose restart postgres
```

### Redis Connection Issues
```bash
# Test connection
redis-cli ping
```
