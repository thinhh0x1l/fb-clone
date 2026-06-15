package com.fb.comment.service;

import com.fb.comment.dto.CommentResponse;
import com.fb.comment.dto.CreateCommentRequest;
import com.fb.comment.dto.UpdateCommentRequest;
import com.fb.common.response.PagedResponse;

import java.util.List;

/**
 * Service quản lý bình luận
 * Tạo, cập nhật, xóa bình luận và lấy danh sách bình luận
 */
public interface CommentService {

    /**
     * Tạo bình luận mới
     * @param postId ID bài viết
     * @param userId ID người bình luận
     * @param request thông tin bình luận
     * @return bình luận đã tạo
     */
    CommentResponse createComment(Long postId, Long userId, CreateCommentRequest request);

    /**
     * Cập nhật bình luận
     * @param commentId ID bình luận
     * @param userId ID người sở hữu
     * @param request thông tin cập nhật
     * @return bình luận đã cập nhật
     */
    CommentResponse updateComment(Long commentId, Long userId, UpdateCommentRequest request);

    /**
     * Xóa bình luận (soft delete)
     * @param commentId ID bình luận
     * @param userId ID người sở hữu
     */
    void deleteComment(Long commentId, Long userId);

    /**
     * Lấy danh sách bình luận gốc của bài viết
     * @param postId ID bài viết
     * @param currentUserId ID người dùng hiện tại
     * @param page trang hiện tại
     * @param size số lượng mỗi trang
     * @return danh sách bình luận phân trang
     */
    PagedResponse<CommentResponse> getPostComments(Long postId, Long currentUserId, int page, int size);

    /**
     * Lấy danh sách trả lời của bình luận
     * @param commentId ID bình luận cha
     * @param currentUserId ID người dùng hiện tại
     * @return danh sách bình luận trả lời
     */
    List<CommentResponse> getCommentReplies(Long commentId, Long currentUserId);
}
