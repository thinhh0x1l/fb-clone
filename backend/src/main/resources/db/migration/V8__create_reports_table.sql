-- V8: Tạo bảng báo cáo (moderation)
CREATE TABLE reports (
    id BIGSERIAL PRIMARY KEY,
    reporter_id BIGINT NOT NULL REFERENCES users(id),
    target_type VARCHAR(20) NOT NULL,
    target_id BIGINT NOT NULL,
    reason VARCHAR(50) NOT NULL,
    description VARCHAR(1000),
    status VARCHAR(20) DEFAULT 'PENDING',
    moderator_id BIGINT REFERENCES users(id),
    moderator_note VARCHAR(1000),
    action VARCHAR(30),
    resolved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_reports_status ON reports(status);
CREATE INDEX idx_reports_target ON reports(target_type, target_id);
CREATE INDEX idx_reports_reporter ON reports(reporter_id);
CREATE INDEX idx_reports_deleted_at ON reports(deleted_at);
