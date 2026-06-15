package com.fb.notification.dto;

import com.fb.common.enums.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO dùng để trả về thông tin chi tiết của một thông báo.
 * Bao gồm loại thông báo, người thực hiện hành động và trạng thái đã đọc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    /** mã định danh duy nhất của thông báo */
    private Long id;

    /** loại thông báo (LIKE, COMMENT, FRIEND_REQUEST, MESSAGE, ...) */
    private NotificationType type;

    /** mã định danh của đối tượng liên quan (bài viết, bình luận, ...) */
    private Long referenceId;

    /** loại đối tượng liên quan (POST, COMMENT, FRIEND, ...) */
    private String referenceType;

    /** nội dung thông báo hiển thị cho người dùng */
    private String message;

    /** thông tin người thực hiện hành động gây ra thông báo */
    private ActorInfo actor;

    /** trạng thái đã đọc của thông báo */
    private boolean read;

    /** thời gian tạo thông báo */
    private LocalDateTime createdAt;

    /**
     * Thông tin tóm tắt của người thực hiện hành động.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActorInfo {
        /** mã định danh của người thực hiện */
        private Long id;

        /** tên đăng nhập */
        private String username;

        /** tên hiển thị */
        private String displayName;

        /** URL ảnh đại diện */
        private String avatar;
    }
}
