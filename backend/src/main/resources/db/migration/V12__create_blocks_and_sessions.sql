-- V12: Tạo bảng chặn người dùng và phiên làm việc
CREATE TABLE user_blocks (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    blocked_user_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT uk_user_block UNIQUE (user_id, blocked_user_id),
    CONSTRAINT chk_no_self_user_block CHECK (user_id != blocked_user_id)
);

CREATE INDEX idx_user_blocks_user ON user_blocks(user_id);
CREATE INDEX idx_user_blocks_blocked ON user_blocks(blocked_user_id);
CREATE INDEX idx_user_blocks_deleted_at ON user_blocks(deleted_at);

-- V12: Tạo bảng phiên làm việc
CREATE TABLE user_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    token_hash VARCHAR(255) NOT NULL,
    device_info VARCHAR(500),
    ip_address VARCHAR(45),
    last_active_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_user_sessions_user ON user_sessions(user_id);
CREATE INDEX idx_user_sessions_token ON user_sessions(token_hash);
CREATE INDEX idx_user_sessions_expires ON user_sessions(expires_at);
CREATE INDEX idx_user_sessions_deleted_at ON user_sessions(deleted_at);
