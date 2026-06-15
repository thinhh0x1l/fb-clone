package com.fb.feed.service;

import com.fb.feed.dto.FeedResponse;

/**
 * Service quản lý trang chủ (feed)
 * Phân phối bài viết, xóa bài viết khỏi feed và lấy feed
 */
public interface FeedService {

    /**
     * Phân phối bài viết đến feed của bạn bè
     * @param postId ID bài viết
     * @param authorId ID tác giả
     */
    void fanoutPost(Long postId, Long authorId);

    /**
     * Xóa bài viết khỏi tất cả feed
     * @param postId ID bài viết
     */
    void removePostFromFeeds(Long postId);

    /**
     * Lấy feed của người dùng
     * @param userId ID người dùng
     * @param page trang hiện tại
     * @param size số lượng mỗi trang
     * @return feed response
     */
    FeedResponse getFeed(Long userId, int page, int size);
}
