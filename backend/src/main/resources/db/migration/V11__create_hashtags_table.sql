-- V11: Tạo bảng hashtag
CREATE TABLE hashtags (
    id BIGSERIAL PRIMARY KEY,
    tag VARCHAR(100) NOT NULL UNIQUE,
    post_count BIGINT DEFAULT 0,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_hashtags_tag ON hashtags(tag);
CREATE INDEX idx_hashtags_post_count ON hashtags(post_count DESC);
CREATE INDEX idx_hashtags_deleted_at ON hashtags(deleted_at);

-- V11: Tạo bảng liên kết bài viết - hashtag
CREATE TABLE post_hashtags (
    post_id BIGINT NOT NULL REFERENCES posts(id),
    hashtag_id BIGINT NOT NULL REFERENCES hashtags(id),
    PRIMARY KEY (post_id, hashtag_id)
);

CREATE INDEX idx_post_hashtags_hashtag ON post_hashtags(hashtag_id);
