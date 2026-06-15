package com.fb.friend.dto;

import com.fb.common.enums.FriendStatus;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO dùng để trả về thông tin chi tiết của một mối quan hệ bạn bè.
 * Bao gồm thông tin người gửi, người nhận, trạng thái và thời gian tạo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendResponse {

    /** mã định danh duy nhất của mối quan hệ bạn bè */
    private Long id;

    /** thông tin người gửi yêu cầu kết bạn */
    private UserInfo requester;

    /** thông tin người nhận yêu cầu kết bạn */
    private UserInfo addressee;

    /** trạng thái của mối quan hệ (PENDING, ACCEPTED, REJECTED, BLOCKED) */
    private FriendStatus status;

    /** lời nhắn kèm theo yêu cầu kết bạn */
    private String message;

    /** thời gian tạo mối quan hệ */
    private LocalDateTime createdAt;

    /**
     * Thông tin tóm tắt của người dùng trong ngữ cảnh bạn bè.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        /** mã định danh của người dùng */
        private Long id;

        /** tên đăng nhập */
        private String username;

        /** tên hiển thị */
        private String displayName;

        /** URL ảnh đại diện */
        private String avatar;
    }
}
