package com.fb.event;

import lombok.Getter;

/**
 * Sự kiện được phát sinh khi một bài viết bị xóa.
 * Sử dụng để xóa bài viết khỏi luồng tin tức, xóa bộ nhớ đệm,
 * và cập nhật các dịch vụ liên quan.
 */
@Getter
public class PostDeletedEvent extends BaseEvent {

    /** Mã bài viết bị xóa */
    private final Long postId;

    /** Mã tác giả bài viết */
    private final Long authorId;

    /**
     * Khởi tạo sự kiện bài viết bị xóa.
     *
     * @param source đối tượng phát sinh sự kiện
     * @param userId mã người dùng (tác giả)
     * @param postId mã bài viết bị xóa
     */
    public PostDeletedEvent(Object source, Long userId, Long postId) {
        super(source, userId);
        this.postId = postId;
        this.authorId = userId;
    }
}
