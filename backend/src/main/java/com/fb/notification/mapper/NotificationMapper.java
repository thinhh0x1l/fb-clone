package com.fb.notification.mapper;

import com.fb.auth.model.User;
import com.fb.notification.dto.NotificationResponse;
import com.fb.notification.model.Notification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper chuyển đổi dữ liệu Notification
 * Chuyển đổi giữa Entity Notification và NotificationResponse DTO
 */
@Component
public class NotificationMapper {

    /**
     * Chuyển đổi Notification entity sang NotificationResponse DTO
     * @param notification Notification entity từ database
     * @return NotificationResponse DTO cho API response
     */
    public NotificationResponse toNotificationResponse(Notification notification) {
        if (notification == null) {
            return null;
        }

        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .referenceId(notification.getReferenceId())
                .referenceType(notification.getReferenceType())
                .message(notification.getMessage())
                .actor(toActorInfo(notification.getActor()))
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    /**
     * Chuyển đổi danh sách Notification entities sang NotificationResponse DTOs
     * @param notifications danh sách Notification entities
     * @return danh sách NotificationResponse DTOs
     */
    public List<NotificationResponse> toNotificationResponseList(List<Notification> notifications) {
        if (notifications == null) {
            return new ArrayList<>();
        }
        return notifications.stream()
                .map(this::toNotificationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Chuyển đổi User entity sang ActorInfo nested DTO
     * Chứa thông tin người thực hiện hành động gây ra thông báo
     */
    public NotificationResponse.ActorInfo toActorInfo(User user) {
        if (user == null) {
            return null;
        }

        return NotificationResponse.ActorInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .avatar(user.getAvatar())
                .build();
    }
}
