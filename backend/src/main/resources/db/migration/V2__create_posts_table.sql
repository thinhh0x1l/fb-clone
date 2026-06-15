-- V2: Tạo bảng bài viết
CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    content TEXT,
    user_id BIGINT NOT NULL REFERENCES users(id),
    visibility VARCHAR(20) DEFAULT 'PUBLIC',
    likes_count BIGINT DEFAULT 0,
    comments_count BIGINT DEFAULT 0,
    shares_count BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_posts_user_id ON posts(user_id);
CREATE INDEX idx_posts_created_at ON posts(created_at DESC);
CREATE INDEX idx_posts_deleted_at ON posts(deleted_at);

-- V2: Tạo bảng media bài viết
CREATE TABLE post_media (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES posts(id),
    url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),
    type VARCHAR(50) NOT NULL,
    order_index INTEGER DEFAULT 0,
    width INTEGER,
    height INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_post_media_post_id ON post_media(post_id);
CREATE INDEX idx_post_media_deleted_at ON post_media(deleted_at);
