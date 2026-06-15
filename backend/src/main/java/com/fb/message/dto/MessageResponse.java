package com.fb.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO response cho tin nhắn
 */
@Data
@Builder
@AllArgsConstructor
public class MessageResponse {

    private Long id;
    private Long conversationId;
    private SenderInfo sender;
    private String content; // Nội dung đã giải mã
    private String type;
    private boolean read;
    private LocalDateTime createdAt;

    /**
     * Thông tin người gửi
     */
    @Data
    @Builder
    @AllArgsConstructor
    public static class SenderInfo {
        private Long id;
        private String username;
        private String displayName;
        private String avatar;
    }
}
