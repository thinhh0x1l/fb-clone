package com.fb.event;

import lombok.Getter;

/**
 * Sự kiện được phát sinh khi một tin nhắn được gửi thành công.
 * Sử dụng để đẩy tin nhắn qua WebSocket, gửi thông báo,
 * và cập nhật thống kê.
 */
@Getter
public class MessageSentEvent extends BaseEvent {

    /** Mã tin nhắn */
    private final Long messageId;

    /** Mã cuộc trò chuyện */
    private final Long conversationId;

    /** Mã người gửi tin nhắn */
    private final Long senderId;

    /** Mã người nhận tin nhắn */
    private final Long receiverId;

    /**
     * Khởi tạo sự kiện tin nhắn được gửi.
     *
     * @param source đối tượng phát sinh sự kiện
     * @param userId mã người dùng (người gửi)
     * @param messageId mã tin nhắn
     * @param conversationId mã cuộc trò chuyện
     * @param senderId mã người gửi
     * @param receiverId mã người nhận
     */
    public MessageSentEvent(Object source, Long userId, Long messageId, Long conversationId, Long senderId, Long receiverId) {
        super(source, userId);
        this.messageId = messageId;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.receiverId = receiverId;
    }
}
