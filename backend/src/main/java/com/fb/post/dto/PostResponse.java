package com.fb.post.dto;

import com.fb.common.enums.Visibility;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO dùng để trả về thông tin chi tiết của một bài viết.
 * Bao gồm nội dung, thông tin tác giả, phương tiện đính kèm và các chỉ số tương tác.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {

    /** mã định danh duy nhất của bài viết */
    private Long id;

    /** nội dung văn bản của bài viết */
    private String content;

    /** thông tin tóm tắt của tác giả bài viết */
    private UserInfo user;

    /** chế độ hiển thị (công khai, bạn bè, riêng tư) */
    private Visibility visibility;

    /** số lượng lượt thích */
    private long likesCount;

    /** số lượng bình luận */
    private long commentsCount;

    /** số lượng lượt chia sẻ */
    private long sharesCount;

    /** danh sách phương tiện đính kèm (ảnh, video) */
    private List<MediaResponse> media;

    /** người dùng hiện tại đã thích bài viết này hay chưa */
    private boolean isLiked;

    /** thời gian tạo bài viết */
    private LocalDateTime createdAt;

    /** thời gian cập nhật bài viết gần nhất */
    private LocalDateTime updatedAt;

    /**
     * Thông tin tóm tắt của người dùng trong ngữ cảnh bài viết.
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

    /**
     * Thông tin chi tiết của phương tiện đính kèm trong bài viết.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MediaResponse {
        /** mã định danh của phương tiện */
        private Long id;

        /** URL đầy đủ của phương tiện */
        private String url;

        /** URL ảnh thu nhỏ */
        private String thumbnailUrl;

        /** loại phương tiện (IMAGE, VIDEO) */
        private String type;

        /** thứ tự hiển thị trong bài viết */
        private int orderIndex;

        /** chiều rộng (pixels) */
        private Integer width;

        /** chiều cao (pixels) */
        private Integer height;
    }
}
