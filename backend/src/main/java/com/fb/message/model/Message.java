package com.fb.message.model;

import com.fb.auth.model.User;
import com.fb.infrastructure.persistence.BaseEntity;
import com.fb.common.enums.MessageType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Tin nhắn trong hội thoại
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "messages")
public class Message extends BaseEntity {

    /**
     * Hội thoại chứa tin nhắn này
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    /**
     * Người gửi tin nhắn
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /**
     * Nội dung tin nhắn (đã mã hóa AES-256-GCM)
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String encryptedContent;

    /**
     * Thuật toán mã hóa
     */
    @Column(length = 50)
    private String encryptionAlgorithm = "AES-256-GCM";

    /**
     * Loại tin nhắn (text, image, file, link)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageType type = MessageType.TEXT;

    /**
     * Đã đọc chưa
     */
    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    /**
     * Lấy nội dung tin nhắn (giải mã)
     */
    public String getContent() {
        // Để service xử lý giải mã
        return encryptedContent;
    }

    /**
     * Set nội dung tin nhắn (mã hóa)
     */
    public void setContent(String content) {
        this.encryptedContent = content;
    }
}
