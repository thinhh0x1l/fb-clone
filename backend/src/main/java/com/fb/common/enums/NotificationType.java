package com.fb.common.enums;

/**
 * Các loại thông báo trong ứng dụng.
 *
 * Định nghĩa tất cả các loại thông báo có thể gửi đến người dùng,
 * giúp phân loại và xử lý thông báo theo từng loại riêng biệt.
 */
public enum NotificationType {
    /** Lời mời kết bạn mới */
    FRIEND_REQUEST,
    /** Lời mời kết bạn được chấp nhận */
    FRIEND_ACCEPTED,
    /** Bài viết được thích */
    POST_LIKED,
    /** Bài viết được bình luận */
    POST_COMMENTED,
    /** Bài viết được chia sẻ */
    POST_SHARED,
    /** Bình luận được thích */
    COMMENT_LIKED,
    /** Tin nhắn mới được nhận */
    MESSAGE_RECEIVED,
    /** Người dùng được nhắc đến trong bài viết hoặc bình luận */
    MENTION
}
