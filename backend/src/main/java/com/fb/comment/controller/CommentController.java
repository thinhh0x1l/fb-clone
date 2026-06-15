package com.fb.comment.controller;

import com.fb.comment.dto.CommentResponse;
import com.fb.comment.dto.CreateCommentRequest;
import com.fb.comment.dto.UpdateCommentRequest;
import com.fb.comment.service.CommentService;
import com.fb.common.constant.AppConstant;
import com.fb.common.response.ApiResponse;
import com.fb.common.response.PagedResponse;
import com.fb.common.util.HashIdUtil;
import com.fb.security.CurrentUser;
import com.fb.security.JwtAuthenticationFilter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Điều khiển bình luận - xử lý CRUD bình luận và phản hồi
 */
@Slf4j
@RestController
@RequestMapping(AppConstant.API_VERSION + "/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final HashIdUtil hashIdUtil;

    /**
     * Tạo bình luận mới cho bài viết
     *
     * @param postId mã HashId của bài viết
     * @param principal thông tin người dùng đã xác thực
     * @param request thông tin bình luận cần tạo
     * @return bình luận đã tạo
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable String postId,
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal,
            @Valid @RequestBody CreateCommentRequest request) {
        Long realPostId = hashIdUtil.decode(postId);
        log.info("Tạo bình luận mới cho bài viết ID: {} bởi người dùng: {}", realPostId, principal.getUserId());
        CommentResponse response = commentService.createComment(realPostId, principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Tạo bình luận thành công", response));
    }

    /**
     * Lấy danh sách bình luận của bài viết
     *
     * @param postId mã HashId của bài viết
     * @param principal thông tin người dùng đã xác thực
     * @param page số trang
     * @param size kích thước trang
     * @return danh sách bình luận phân trang
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<CommentResponse>>> getPostComments(
            @PathVariable String postId,
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long realPostId = hashIdUtil.decode(postId);
        log.info("Lấy danh sách bình luận cho bài viết ID: {}", realPostId);
        PagedResponse<CommentResponse> response = commentService.getPostComments(realPostId, principal.getUserId(), page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Cập nhật bình luận
     *
     * @param postId mã HashId của bài viết
     * @param commentId mã HashId của bình luận
     * @param principal thông tin người dùng đã xác thực
     * @param request thông tin cập nhật
     * @return bình luận sau khi cập nhật
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable String postId,
            @PathVariable String commentId,
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal,
            @Valid @RequestBody UpdateCommentRequest request) {
        Long realCommentId = hashIdUtil.decode(commentId);
        log.info("Cập nhật bình luận ID: {} bởi người dùng: {}", realCommentId, principal.getUserId());
        CommentResponse response = commentService.updateComment(realCommentId, principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật bình luận thành công", response));
    }

    /**
     * Xóa bình luận
     *
     * @param postId mã HashId của bài viết
     * @param commentId mã HashId của bình luận
     * @param principal thông tin người dùng đã xác thực
     * @return phản hồi thành công
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable String postId,
            @PathVariable String commentId,
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal) {
        Long realCommentId = hashIdUtil.decode(commentId);
        log.info("Xóa bình luận ID: {} bởi người dùng: {}", realCommentId, principal.getUserId());
        commentService.deleteComment(realCommentId, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok("Xóa bình luận thành công"));
    }

    /**
     * Lấy danh sách phản hồi của bình luận
     *
     * @param postId mã HashId của bài viết
     * @param commentId mã HashId của bình luận
     * @param principal thông tin người dùng đã xác thực
     * @return danh sách phản hồi
     */
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getCommentReplies(
            @PathVariable String postId,
            @PathVariable String commentId,
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal) {
        Long realCommentId = hashIdUtil.decode(commentId);
        log.info("Lấy danh sách phản hồi cho bình luận ID: {}", realCommentId);
        List<CommentResponse> response = commentService.getCommentReplies(realCommentId, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
