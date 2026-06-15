package com.fb.event;

import lombok.Getter;

/**
 * Sự kiện được phát sinh khi một bài viết mới được tạo thành công.
 * Sử dụng để kích hoạt các hành động liên quan: phân phối nội dung,
 * gửi thông báo, cập nhật xu hướng, v.v.
 */
@Getter
public class PostCreatedEvent extends BaseEvent {

    /** Mã bài viết vừa được tạo */
    private final Long postId;

    /** Nội dung bài viết */
    private final String content;

    /** Mã tác giả bài viết */
    private final Long authorId;

    /**
     * Khởi tạo sự kiện bài viết được tạo.
     *
     * @param source đối tượng phát sinh sự kiện
     * @param userId mã người dùng (tác giả)
     * @param postId mã bài viết
     * @param content nội dung bài viết
     */
    public PostCreatedEvent(Object source, Long userId, Long postId, String content) {
        super(source, userId);
        this.postId = postId;
        this.content = content;
        this.authorId = userId;
    }
}
