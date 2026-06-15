package com.fb.friend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO dùng để gửi yêu cầu kết bạn đến một người dùng khác.
 * Bắt buộc phải có userId của người nhận.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestDto {

    /** mã định danh của người dùng muốn kết bạn (bắt buộc) */
    @NotNull(message = "User ID is required")
    private Long userId;

    /** lời nhắn ngắn gọn gửi kèm yêu cầu kết bạn */
    private String message;
}
