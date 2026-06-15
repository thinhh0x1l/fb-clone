package com.fb.message.service;

import com.fb.message.dto.ConversationResponse;
import com.fb.message.dto.CreateConversationRequest;
import com.fb.message.dto.MessageResponse;
import com.fb.message.dto.SendMessageRequest;
import com.fb.common.response.PagedResponse;

import java.util.List;

/**
 * Service quản lý tin nhắn
 * Tạo cuộc trò chuyện, gửi tin nhắn, quản lý đã đọc/chưa đọc
 */
public interface MessageService {

    /**
     * Tạo cuộc trò chuyện mới
     * @param userId ID người tạo
     * @param request thông tin cuộc trò chuyện
     * @return thông tin cuộc trò chuyện
     */
    ConversationResponse createConversation(Long userId, CreateConversationRequest request);

    /**
     * Lấy thông tin cuộc trò chuyện
     * @param userId ID người dùng
     * @param conversationId ID cuộc trò chuyện
     * @return thông tin cuộc trò chuyện
     */
    ConversationResponse getConversation(Long userId, Long conversationId);

    /**
     * Lấy danh sách cuộc trò chuyện của người dùng
     * @param userId ID người dùng
     * @return danh sách cuộc trò chuyện
     */
    List<ConversationResponse> getUserConversations(Long userId);

    /**
     * Gửi tin nhắn
     * @param userId ID người gửi
     * @param conversationId ID cuộc trò chuyện
     * @param request thông tin tin nhắn
     * @return tin nhắn đã gửi
     */
    MessageResponse sendMessage(Long userId, Long conversationId, SendMessageRequest request);

    /**
     * Lấy tin nhắn trong cuộc trò chuyện
     * @param userId ID người dùng
     * @param conversationId ID cuộc trò chuyện
     * @param page trang hiện tại
     * @param size số lượng mỗi trang
     * @return danh sách tin nhắn phân trang
     */
    PagedResponse<MessageResponse> getConversationMessages(Long userId, Long conversationId, int page, int size);

    /**
     * Lấy hoặc tạo cuộc trò chuyện trực tiếp
     * @param userId ID người dùng
     * @param otherUserId ID người dùng khác
     * @return thông tin cuộc trò chuyện
     */
    ConversationResponse getOrCreateDirectConversation(Long userId, Long otherUserId);

    /**
     * Đánh dấu tin nhắn đã đọc
     * @param userId ID người dùng
     * @param conversationId ID cuộc trò chuyện
     */
    void markAsRead(Long userId, Long conversationId);

    /**
     * Đếm tin nhắn chưa đọc
     * @param userId ID người dùng
     * @return số lượng tin nhắn chưa đọc
     */
    long getUnreadCount(Long userId);

    /**
     * Xóa cuộc trò chuyện (soft delete)
     * @param userId ID người dùng
     * @param conversationId ID cuộc trò chuyện
     */
    void deleteConversation(Long userId, Long conversationId);
}
