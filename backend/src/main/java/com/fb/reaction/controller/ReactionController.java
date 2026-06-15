package com.fb.reaction.controller;

import com.fb.common.constant.AppConstant;
import com.fb.common.response.ApiResponse;
import com.fb.common.util.HashIdUtil;
import com.fb.reaction.dto.ReactionRequest;
import com.fb.reaction.dto.ReactionResponse;
import com.fb.reaction.service.ReactionService;
import com.fb.security.CurrentUser;
import com.fb.security.JwtAuthenticationFilter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Điều khiển phản ứng - xử lý like/reaction cho bài viết và bình luận
 */
@Slf4j
@RestController
@RequestMapping(AppConstant.API_VERSION)
@RequiredArgsConstructor
public class ReactionController {

    private final ReactionService reactionService;
    private final HashIdUtil hashIdUtil;

    /**
     * Bật/tắt phản ứng cho bài viết
     *
     * @param postId mã HashId của bài viết
     * @param principal thông tin người dùng đã xác thực
     * @param request thông tin phản ứng
     * @return kết quả phản ứng
     */
    @PostMapping("/posts/{postId}/reactions")
    public ResponseEntity<ApiResponse<ReactionResponse>> togglePostReaction(
            @PathVariable String postId,
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal,
            @Valid @RequestBody ReactionRequest request) {
        Long realPostId = hashIdUtil.decode(postId);
        log.info("Bật/tắt phản ứng cho bài viết ID: {} bởi người dùng: {}", realPostId, principal.getUserId());
        ReactionResponse response = reactionService.togglePostReaction(realPostId, principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Lấy thông tin phản ứng của bài viết
     *
     * @param postId mã HashId của bài viết
     * @param principal thông tin người dùng đã xác thực
     * @return thông tin phản ứng
     */
    @GetMapping("/posts/{postId}/reactions")
    public ResponseEntity<ApiResponse<ReactionResponse>> getPostReactions(
            @PathVariable String postId,
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal) {
        Long realPostId = hashIdUtil.decode(postId);
        log.info("Lấy thông tin phản ứng cho bài viết ID: {}", realPostId);
        ReactionResponse response = reactionService.getPostReactions(realPostId, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Bật/tắt phản ứng cho bình luận
     *
     * @param commentId mã HashId của bình luận
     * @param principal thông tin người dùng đã xác thực
     * @param request thông tin phản ứng
     * @return kết quả phản ứng
     */
    @PostMapping("/comments/{commentId}/reactions")
    public ResponseEntity<ApiResponse<ReactionResponse>> toggleCommentReaction(
            @PathVariable String commentId,
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal,
            @Valid @RequestBody ReactionRequest request) {
        Long realCommentId = hashIdUtil.decode(commentId);
        log.info("Bật/tắt phản ứng cho bình luận ID: {} bởi người dùng: {}", realCommentId, principal.getUserId());
        ReactionResponse response = reactionService.toggleCommentReaction(realCommentId, principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Lấy thông tin phản ứng của bình luận
     *
     * @param commentId mã HashId của bình luận
     * @param principal thông tin người dùng đã xác thực
     * @return thông tin phản ứng
     */
    @GetMapping("/comments/{commentId}/reactions")
    public ResponseEntity<ApiResponse<ReactionResponse>> getCommentReactions(
            @PathVariable String commentId,
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal) {
        Long realCommentId = hashIdUtil.decode(commentId);
        log.info("Lấy thông tin phản ứng cho bình luận ID: {}", realCommentId);
        ReactionResponse response = reactionService.getCommentReactions(realCommentId, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
