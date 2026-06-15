package com.fb.message.dto;

import com.fb.common.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO dùng để gửi tin nhắn trong một cuộc trò chuyện.
 * Bắt buộc phải có nội dung và loại tin nhắn.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    /** nội dung văn bản của tin nhắn (bắt buộc) */
    @NotBlank(message = "Message content is required")
    private String content;

    /** loại tin nhắn (TEXT, IMAGE, FILE, ...) (bắt buộc) */
    @NotNull(message = "Message type is required")
    private MessageType type;
}
