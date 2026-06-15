package com.fb.message.controller;

import com.fb.common.constant.AppConstant;
import com.fb.common.response.ApiResponse;
import com.fb.common.response.PagedResponse;
import com.fb.common.util.HashIdUtil;
import com.fb.message.dto.ConversationResponse;
import com.fb.message.dto.CreateConversationRequest;
import com.fb.message.dto.MessageResponse;
import com.fb.message.dto.SendMessageRequest;
import com.fb.message.service.MessageService;
import com.fb.security.CurrentUser;
import com.fb.security.JwtAuthenticationFilter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Điều khiển tin nhắn - xử lý cuộc trò chuyện và gửi/nhận tin nhắn
 */
@Slf4j
@RestController
@RequestMapping(AppConstant.API_VERSION + "/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final HashIdUtil hashIdUtil;

    /**
     * Tạo cuộc trò chuyện mới
     *
     * @param principal thông tin người dùng đã xác thực
     * @param request thông tin cuộc trò chuyện cần tạo
     * @return thông tin cuộc trò chuyện đã tạo
     */
    @PostMapping("/conversations")
    public ResponseEntity<ApiResponse<ConversationResponse>> createConversation(
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal,
            @Valid @RequestBody CreateConversationRequest request) {
        log.info("Tạo cuộc trò chuyện mới bởi người dùng: {}", principal.getUserId());
        ConversationResponse response = messageService.createConversation(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Tạo cuộc trò chuyện thành công", response));
    }

    /**
     * Lấy danh sách cuộc trò chuyện của người dùng
     *
     * @param principal thông tin người dùng đã xác thực
     * @return danh sách cuộc trò chuyện
     */
    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getUserConversations(
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal) {
        log.info("Lấy danh sách cuộc trò chuyện của người dùng: {}", principal.getUserId());
        List<ConversationResponse> response = messageService.getUserConversations(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Lấy thông tin cuộc trò chuyện theo ID
     *
     * @param conversationId mã HashId của cuộc trò chuyện
     * @param principal thông tin người dùng đã xác thực
     * @return thông tin cuộc trò chuyện
     */
    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<ApiResponse<ConversationResponse>> getConversation(
            @PathVariable String conversationId,
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal) {
        Long realConversationId = hashIdUtil.decode(conversationId);
        log.info("Lấy thông tin cuộc trò chuyện ID: {}", realConversationId);
        ConversationResponse response = messageService.getConversation(principal.getUserId(), realConversationId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Gửi tin nhắn trong cuộc trò chuyện
     *
     * @param conversationId mã HashId của cuộc trò chuyện
     * @param principal thông tin người dùng đã xác thực
     * @param request thông tin tin nhắn cần gửi
     * @return thông tin tin nhắn đã gửi
     */
    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @PathVariable String conversationId,
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal,
            @Valid @RequestBody SendMessageRequest request) {
        Long realConversationId = hashIdUtil.decode(conversationId);
        log.info("Gửi tin nhắn trong cuộc trò chuyện ID: {} bởi người dùng: {}", realConversationId, principal.getUserId());
        MessageResponse response = messageService.sendMessage(principal.getUserId(), realConversationId, request);
        return ResponseEntity.ok(ApiResponse.ok("Gửi tin nhắn thành công", response));
    }

    /**
     * Lấy danh sách tin nhắn trong cuộc trò chuyện
     *
     * @param conversationId mã HashId của cuộc trò chuyện
     * @param principal thông tin người dùng đã xác thực
     * @param page số trang
     * @param size kích thước trang
     * @return danh sách tin nhắn phân trang
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<PagedResponse<MessageResponse>>> getConversationMessages(
            @PathVariable String conversationId,
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long realConversationId = hashIdUtil.decode(conversationId);
        log.info("Lấy danh sách tin nhắn trong cuộc trò chuyện ID: {}", realConversationId);
        PagedResponse<MessageResponse> response = messageService.getConversationMessages(
                principal.getUserId(), realConversationId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Lấy hoặc tạo cuộc trò chuyện trực tiếp giữa hai người dùng
     *
     * @param userId mã HashId của người dùng cần trò chuyện
     * @param principal thông tin người dùng đã xác thực
     * @return thông tin cuộc trò chuyện
     */
    @PostMapping("/conversations/direct/{userId}")
    public ResponseEntity<ApiResponse<ConversationResponse>> getOrCreateDirectConversation(
            @PathVariable String userId,
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal) {
        Long realUserId = hashIdUtil.decode(userId);
        log.info("Lấy/tạo cuộc trò chuyện trực tiếp với người dùng ID: {}", realUserId);
        ConversationResponse response = messageService.getOrCreateDirectConversation(principal.getUserId(), realUserId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Đánh dấu tin nhắn đã đọc
     *
     * @param conversationId mã HashId của cuộc trò chuyện
     * @param principal thông tin người dùng đã xác thực
     * @return phản hồi thành công
     */
    @PutMapping("/conversations/{conversationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable String conversationId,
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal) {
        Long realConversationId = hashIdUtil.decode(conversationId);
        log.info("Đánh dấu đã đọc cuộc trò chuyện ID: {} bởi người dùng: {}", realConversationId, principal.getUserId());
        messageService.markAsRead(principal.getUserId(), realConversationId);
        return ResponseEntity.ok(ApiResponse.ok("Đánh dấu đã đọc thành công"));
    }

    /**
     * Lấy số lượng tin nhắn chưa đọc
     *
     * @param principal thông tin người dùng đã xác thực
     * @return số lượng tin nhắn chưa đọc
     */
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal) {
        log.info("Lấy số lượng tin nhắn chưa đọc của người dùng: {}", principal.getUserId());
        long count = messageService.getUnreadCount(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(count));
    }

    /**
     * Xóa cuộc trò chuyện
     *
     * @param conversationId mã HashId của cuộc trò chuyện
     * @param principal thông tin người dùng đã xác thực
     * @return phản hồi thành công
     */
    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(
            @PathVariable String conversationId,
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal) {
        Long realConversationId = hashIdUtil.decode(conversationId);
        log.info("Xóa cuộc trò chuyện ID: {} bởi người dùng: {}", realConversationId, principal.getUserId());
        messageService.deleteConversation(principal.getUserId(), realConversationId);
        return ResponseEntity.ok(ApiResponse.ok("Xóa cuộc trò chuyện thành công"));
    }
}
