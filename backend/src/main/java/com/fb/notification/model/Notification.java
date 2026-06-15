package com.fb.notification.model;

import com.fb.auth.model.User;
import com.fb.common.enums.NotificationType;
import com.fb.infrastructure.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity thông báo gửi đến người dùng
 */
@Entity
@Table(name = "notifications")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    /** Người nhận thông báo */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Loại thông báo (bình luận, thích, v.v.) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    /** ID của đối tượng liên quan (bài đăng, bình luận, v.v.) */
    @Column(nullable = false)
    private Long referenceId;

    /** Loại đối tượng liên quan (post, comment, v.v.) */
    @Column(length = 50)
    private String referenceType;

    /** Nội dung thông báo */
    @Column(length = 500)
    private String message;

    /** Người thực hiện hành động gây ra thông báo */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    /** Trạng thái đã đọc của thông báo */
    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private boolean read = false;
}
