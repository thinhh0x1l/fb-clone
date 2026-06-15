package com.fb.common.enums;

/**
 * Các lý do phát hiện spam.
 *
 * Định nghĩa các tiêu chí mà hệ thống sử dụng
 * để xác định nội dung hoặc hành vi là spam.
 */
public enum SpamReason {
    /** Nội dung trùng lặp - cùng nội dung được đăng nhiều lần */
    DUPLICATE_CONTENT("Phát hiện nội dung trùng lặp"),
    /** Vượt quá giới hạn tốc độ đăng bài */
    RATE_LIMIT_EXCEEDED("Vượt quá giới hạn tốc độ đăng bài"),
    /** Liên kết đáng ngờ - URL chứa phần tử độc hại */
    SUSPICIOUS_LINKS("Phát hiện liên kết đáng ngờ"),
    /** Hành động hàng loạt bất thường */
    BULK_MASS_ACTION("Phát hiện hành động hàng loạt"),
    /** Mẫu spam từ tài khoản mới */
    NEW_ACCOUNT_SPAM("Mẫu spam từ tài khoản mới"),
    /** Phát hiện từ khóa spam */
    KEYWORD_FILTER("Phát hiện từ khóa spam"),
    /** Hành vi bất thường đáng ngờ */
    BEHAVIORAL_SIGNAL("Mẫu hành vi bất thường"),
    /** Được nhiều người dùng báo cáo */
    USER_REPORTED("Nhiều người dùng báo cáo")
    ;

    /** Mô tả chi tiết về lý do spam */
    private final String description;

    SpamReason(String description) {
        this.description = description;
    }

    /**
     * Lấy mô tả chi tiết của lý do spam.
     *
     * @return chuỗi mô tả
     */
    public String getDescription() {
        return description;
    }
}
