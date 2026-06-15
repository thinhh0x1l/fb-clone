package com.fb.reaction.service;

import com.fb.common.enums.ReactionType;
import com.fb.reaction.dto.ReactionRequest;
import com.fb.reaction.dto.ReactionResponse;

import java.util.Map;

/**
 * Service quản lý tương tác (reaction/biểu tượng cảm xúc)
 * Bật/tắt reaction trên bài viết và bình luận
 */
public interface ReactionService {

    /**
     * Bật/tắt reaction trên bài viết
     * @param postId ID bài viết
     * @param userId ID người dùng
     * @param request loại reaction
     * @return kết quả reaction
     */
    ReactionResponse togglePostReaction(Long postId, Long userId, ReactionRequest request);

    /**
     * Bật/tắt reaction trên bình luận
     * @param commentId ID bình luận
     * @param userId ID người dùng
     * @param request loại reaction
     * @return kết quả reaction
     */
    ReactionResponse toggleCommentReaction(Long commentId, Long userId, ReactionRequest request);

    /**
     * Lấy thông tin reaction của bài viết
     * @param postId ID bài viết
     * @param userId ID người dùng
     * @return kết quả reaction
     */
    ReactionResponse getPostReactions(Long postId, Long userId);

    /**
     * Lấy thông tin reaction của bình luận
     * @param commentId ID bình luận
     * @param userId ID người dùng
     * @return kết quả reaction
     */
    ReactionResponse getCommentReactions(Long commentId, Long userId);

    /**
     * Lấy thống kê số lượng reaction theo loại của bài viết
     * @param postId ID bài viết
     * @return map loại reaction và số lượng
     */
    Map<ReactionType, Long> getPostReactionCounts(Long postId);
}
