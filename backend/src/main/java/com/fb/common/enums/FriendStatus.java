package com.fb.common.enums;

/**
 * Các trạng thái của lời mời kết bạn.
 *
 * Theo dõi vòng đời của một lời mời kết bạn
 * từ khi được gửi cho đến khi được xử lý.
 */
public enum FriendStatus {
    /** Lời mời đang chờ được xử lý */
    PENDING,
    /** Lời mời đã được chấp nhận */
    ACCEPTED,
    /** Lời mời đã bị từ chối */
    REJECTED,
    /** Lời mời đã bị hủy bởi người gửi */
    CANCELLED
}
