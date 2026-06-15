package com.fb.common.enums;

/**
 * Các hành động kiểm duyệt nội dung.
 *
 * Định nghĩa các biện pháp xử lý khi phát hiện nội dung
 * vi phạm quy tắc cộng đồng, từ cảnh báo đến khóa tài khoản.
 */
public enum ModerationAction {
    /** Cảnh báo - phát cảnh báo cho người dùng vi phạm */
    WARNING("Cảnh báo đã được phát"),
    /** Xóa nội dung - gỡ bỏ nội dung vi phạm */
    CONTENT_REMOVED("Nội dung đã bị gỡ bỏ"),
    /** Hạn chế nội dung - giới hạn phạm vi hiển thị */
    CONTENT_RESTRICTED("Nội dung đã bị hạn chế"),
    /** Khóa tài khoản tạm thời */
    ACCOUNT_SUSPENDED("Tài khoản bị khóa tạm thời"),
    /** Khóa tài khoản vĩnh viễn */
    ACCOUNT_BANNED("Tài khoản bị khóa vĩnh viễn"),
    /** Đang chờ xử lý khiếu nại */
    APPEAL_PENDING("Khiếu nại đang chờ xử lý"),
    /** Khiếu nại được chấp nhận */
    APPEAL_APPROVED("Khiếu nại đã được chấp nhận"),
    /** Khiếu nại bị từ chối */
    APPEAL_REJECTED("Khiếu nại đã bị từ chối");

    /** Mô tả chi tiết về hành động kiểm duyệt */
    private final String description;

    ModerationAction(String description) {
        this.description = description;
    }

    /**
     * Lấy mô tả chi tiết của hành động kiểm duyệt.
     *
     * @return chuỗi mô tả
     */
    public String getDescription() {
        return description;
    }
}
