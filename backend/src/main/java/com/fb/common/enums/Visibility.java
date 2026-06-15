package com.fb.common.enums;

/**
 * Các chế độ hiển thị của bài viết.
 *
 * Kiểm soát ai có thể xem bài viết,
 * từ công khai cho tất cả mọi người đến chỉ mình tôi.
 */
public enum Visibility {
    /** Công khai - tất cả mọi người đều có thể xem */
    PUBLIC,
    /** Chỉ bạn bè - chỉ những người trong danh sách bạn bè */
    FRIENDS,
    /** Chỉ mình tôi - chỉ tác giả bài viết mới xem được */
    ONLY_ME,
    /** Tùy chỉnh - chọn lọc người xem cụ thể */
    CUSTOM
}
