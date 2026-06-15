package com.fb.common.enums;

/**
 * Các loại cuộc trò chuyện trong ứng dụng nhắn tin.
 *
 * Phân biệt giữa cuộc trò chuyện trực tiếp (1-1)
 * và cuộc trò chuyện nhóm (nhiều người).
 */
public enum ConversationType {
    /** Cuộc trò chuyện trực tiếp giữa hai người dùng */
    DIRECT,
    /** Cuộc trò chuyện nhóm với nhiều người tham gia */
    GROUP
}
