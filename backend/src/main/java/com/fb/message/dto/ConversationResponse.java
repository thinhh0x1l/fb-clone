package com.fb.message.dto;

import com.fb.common.enums.ConversationType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO dùng để trả về thông tin chi tiết của một cuộc trò chuyện.
 * Bao gồm danh sách người tham gia, tin nhắn cuối cùng và số tin nhắn chưa đọc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {

    /** mã định danh duy nhất của cuộc trò chuyện */
    private Long id;

    /** tên nhóm (chỉ có ý nghĩa cho cuộc trò chuyện nhóm) */
    private String name;

    /** loại cuộc trò chuyện (DIRECT hoặc GROUP) */
    private ConversationType type;

    /** danh sách thông tin người tham gia */
    private List<ParticipantInfo> participants;

    /** tin nhắn cuối cùng trong cuộc trò chuyện */
    private MessageResponse lastMessage;

    /** thời gian gửi tin nhắn cuối cùng */
    private LocalDateTime lastMessageAt;

    /** số lượng tin nhắn chưa đọc của người dùng hiện tại */
    private int unreadCount;

    /** thời gian tạo cuộc trò chuyện */
    private LocalDateTime createdAt;

    /**
     * Thông tin tóm tắt của người tham gia trong cuộc trò chuyện.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantInfo {
        /** mã định danh của người dùng */
        private Long id;

        /** tên đăng nhập */
        private String username;

        /** tên hiển thị */
        private String displayName;

        /** URL ảnh đại diện */
        private String avatar;
    }
}
