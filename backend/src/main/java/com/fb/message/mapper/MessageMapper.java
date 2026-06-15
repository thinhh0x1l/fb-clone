package com.fb.message.mapper;

import com.fb.message.model.Message;
import com.fb.message.dto.MessageResponse;
import com.fb.auth.model.User;
import org.springframework.stereotype.Component;

/**
 * Mapper cho tin nhắn
 */
@Component
public class MessageMapper {

    /**
     * Chuyển đổi Message entity sang MessageResponse
     */
    public MessageResponse toMessageResponse(Message message) {
        if (message == null) {
            return null;
        }

        return MessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .sender(mapSender(message.getSender()))
                .content(message.getEncryptedContent()) // Sẽ được giải mã ở service
                .type(message.getType().name())
                .read(message.isRead())
                .createdAt(message.getCreatedAt())
                .build();
    }

    /**
     * Chuyển đổi User sang SenderInfo
     */
    private MessageResponse.SenderInfo mapSender(User user) {
        if (user == null) {
            return null;
        }

        return MessageResponse.SenderInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .avatar(user.getAvatar())
                .build();
    }
}
