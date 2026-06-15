package com.fb.comment.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO dùng để trả về thông tin chi tiết của một bình luận.
 * Hỗ trợ bình luận lồng nhau (phản hồi) với độ sâu phân cấp.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    /** mã định danh duy nhất của bình luận */
    private Long id;

    /** nội dung văn bản của bình luận */
    private String content;

    /** thông tin tóm tắt của người viết bình luận */
    private UserInfo user;

    /** mã định danh của bài viết chứa bình luận */
    private Long postId;

    /** mã định danh của bình luận cha (null nếu là bình luận gốc) */
    private Long parentId;

    /** số lượng lượt thích bình luận */
    private long likesCount;

    /** số lượng phản hồi trực tiếp */
    private long repliesCount;

    /** độ sâu lồng nhau (0 = bình luận gốc, 1 = phản hồi cấp 1, ...) */
    private int depth;

    /** danh sách phản hồi con (nếu có) */
    private List<CommentResponse> replies;

    /** người dùng hiện tại đã thích bình luận này hay chưa */
    private boolean isLiked;

    /** thời gian tạo bình luận */
    private LocalDateTime createdAt;

    /** thời gian cập nhật bình luận gần nhất */
    private LocalDateTime updatedAt;

    /**
     * Thông tin tóm tắt của người dùng trong ngữ cảnh bình luận.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
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
