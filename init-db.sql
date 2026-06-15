-- Tạo database cho PostgreSQL
-- File này chạy tự động khi khởi động container

-- Tạo extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTension IF NOT EXISTS "pg_trgm";

-- Tạo indexes cho full-text search
-- Sẽ được tạo sau khi các bảng được tạo bởi Flyway
