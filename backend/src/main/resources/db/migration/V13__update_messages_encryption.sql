-- V13: Cập nhật bảng messages để mã hóa tin nhắn
-- Thêm cột encryption_algorithm và đổi tên content thành encrypted_content

-- Đổi tên cột content thành encrypted_content
ALTER TABLE messages RENAME COLUMN content TO encrypted_content;

-- Thêm cột encryption_algorithm
ALTER TABLE messages ADD COLUMN encryption_algorithm VARCHAR(50) DEFAULT 'AES-256-GCM';

-- Cập nhật dữ liệu hiện có (mã hóa lại)
-- Lưu ý: Cần chạy script migration riêng cho dữ liệu cũ

-- Tạo index cho encryption_algorithm
CREATE INDEX idx_messages_encryption ON messages(encryption_algorithm);
