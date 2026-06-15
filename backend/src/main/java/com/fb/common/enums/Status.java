package com.fb.common.enums;

/**
 * Các trạng thái của tài khoản người dùng.
 *
 * Theo dõi trạng thái hiện tại của tài khoản
 * trong hệ thống, ảnh hưởng đến quyền truy cập.
 */
public enum Status {
    /** Hoạt động - tài khoản bình thường, có đầy đủ quyền truy cập */
    ACTIVE,
    /** Không hoạt động - tài khoản bị vô hiệu hóa tạm thời */
    INACTIVE,
    /** Đã xóa - tài khoản đã bị xóa vĩnh viễn */
    DELETED
}
