package com.fb.message.model;

import com.fb.auth.model.User;
import com.fb.common.enums.ConversationType;
import com.fb.infrastructure.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity cuộc trò chuyện (tin nhắn trực tiếp hoặc nhóm)
 */
@Entity
@Table(name = "conversations")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation extends BaseEntity {

    /** Tên cuộc trò chuyện (đối với nhóm) */
    @Column(length = 100)
    private String name;

    /** Loại cuộc trò chuyện (trực tiếp hoặc nhóm) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ConversationType type = ConversationType.DIRECT;

    /** Danh sách thành viên tham gia cuộc trò chuyện */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "conversation_participants",
        joinColumns = @JoinColumn(name = "conversation_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> participants = new HashSet<>();

    /** Tin nhắn cuối cùng trong cuộc trò chuyện */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_message_id")
    private Message lastMessage;

    /** Thời điểm gửi tin nhắn cuối cùng */
    private LocalDateTime lastMessageAt;
}
