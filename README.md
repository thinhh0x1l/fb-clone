# Facebook Clone

A Facebook-like social network built with Spring Boot 4.1 + Vue 3.5 + Element Plus.

## Tech Stack

- **Backend**: Java 21, Spring Boot 4.1, PostgreSQL, Redis, MinIO
- **Frontend**: Vue 3.5, TypeScript, Element Plus, Vite 6, SCSS
- **Infrastructure**: Docker Compose

## Quick Start

### 1. Start Infrastructure
```bash
docker-compose up -d postgres redis minio
```

### 2. Backend
```bash
cd backend
./mvnw spring-boot:run
```

### 3. Frontend
```bash
cd frontend
npm install
npm run dev
```

### 4. Access
- Frontend: http://localhost:5173
- Backend API: http://localhost:8080/api/v1
- MinIO Console: http://localhost:9001 (minioadmin/minioadmin)

## Project Structure

```
facebook-project/
├── backend/              # Spring Boot 4.1
├── frontend/             # Vue 3.5 + Element Plus
├── docs/                 # Documentation
├── nginx/                # Nginx config
└── docker-compose.yml    # Docker services
```

## Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [Business Requirements](docs/BUSINESS.md)
- [Tech Stack](docs/TECH_STACK.md)
- [System Flow](docs/FLOW.md)
- [Setup Guide](docs/SETUP.md)
