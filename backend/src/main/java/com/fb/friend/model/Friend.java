package com.fb.friend.model;

import com.fb.auth.model.User;
import com.fb.common.enums.FriendStatus;
import com.fb.infrastructure.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity quản lý mối quan hệ bạn bè giữa hai người dùng
 */
@Entity
@Table(name = "friends", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"requester_id", "addressee_id"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Friend extends BaseEntity {

    /** Người gửi lời mời kết bạn */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    /** Người nhận lời mời kết bạn */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addressee_id", nullable = false)
    private User addressee;

    /** Trạng thái của lời mời kết bạn */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private FriendStatus status = FriendStatus.PENDING;

    /** Lời nhắn đính kèm khi gửi lời mời kết bạn */
    @Column(length = 500)
    private String message;
}
