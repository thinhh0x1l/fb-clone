package com.fb.comment.mapper;

import com.fb.comment.dto.CommentResponse;
import com.fb.comment.model.Comment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper chuyển đổi dữ liệu Comment
 * Chuyển đổi giữa Entity Comment và CommentResponse DTO
 * Hỗ trợ comment gốc và comment reply (nested)
 */
@Component
public class CommentMapper {

    /**
     * Alias cho toCommentResponse
     */
    public CommentResponse toResponse(Comment comment) {
        return toCommentResponse(comment, false);
    }

    /**
     * Alias cho toCommentResponseWithReplies - nhận groupedByParent map
     */
    public CommentResponse toResponseWithReplies(Comment comment, java.util.Map<Long, List<Comment>> groupedByParent) {
        List<CommentResponse> replies = groupedByParent.getOrDefault(comment.getId(), new ArrayList<>()).stream()
                .map(c -> toCommentResponse(c, false))
                .collect(Collectors.toList());
        return toCommentResponseWithReplies(comment, false, replies);
    }

    /**
     * Chuyển đổi Comment entity sang CommentResponse DTO
     * @param comment Comment entity từ database
     * @param isLiked trạng thái liked của comment cho người dùng hiện tại
     * @return CommentResponse DTO cho API response
     */
    public CommentResponse toCommentResponse(Comment comment, boolean isLiked) {
        if (comment == null) {
            return null;
        }

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .user(toUserInfo(comment.getUser()))
                .postId(comment.getPost().getId())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .likesCount(comment.getLikesCount())
                .repliesCount(comment.getRepliesCount())
                .depth(comment.getDepth())
                .replies(new ArrayList<>())
                .isLiked(isLiked)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    /**
     * Chuyển đổi Comment entity sang CommentResponse DTO kèm danh sách replies
     * @param comment Comment entity từ database
     * @param isLiked trạng thái liked của comment
     * @param replies danh sách replies đã chuyển đổi
     * @return CommentResponse DTO với replies
     */
    public CommentResponse toCommentResponseWithReplies(Comment comment, boolean isLiked, List<CommentResponse> replies) {
        if (comment == null) {
            return null;
        }

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .user(toUserInfo(comment.getUser()))
                .postId(comment.getPost().getId())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .likesCount(comment.getLikesCount())
                .repliesCount(comment.getRepliesCount())
                .depth(comment.getDepth())
                .replies(replies != null ? replies : Collections.emptyList())
                .isLiked(isLiked)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    /**
     * Chuyển đổi User entity sang UserInfo nested DTO trong Comment
     * Chứa thông tin cơ bản của người bình luận
     */
    public CommentResponse.UserInfo toUserInfo(com.fb.auth.model.User user) {
        if (user == null) {
            return null;
        }

        return CommentResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .avatar(user.getAvatar())
                .build();
    }
}
