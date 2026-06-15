-- V5: Tạo bảng bạn bè
CREATE TABLE friends (
    id BIGSERIAL PRIMARY KEY,
    requester_id BIGINT NOT NULL REFERENCES users(id),
    addressee_id BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    message VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT uk_friendship UNIQUE (requester_id, addressee_id),
    CONSTRAINT chk_no_self_friend CHECK (requester_id != addressee_id)
);

CREATE INDEX idx_friends_requester ON friends(requester_id);
CREATE INDEX idx_friends_addressee ON friends(addressee_id);
CREATE INDEX idx_friends_status ON friends(status);
CREATE INDEX idx_friends_deleted_at ON friends(deleted_at);
