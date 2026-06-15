package com.fb.post.cache;

import com.fb.infrastructure.cache.MultiTierCache;
import com.fb.post.model.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Quản lý cache cho bài viết
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostCacheManager {

    private final MultiTierCache cache;

    private static final String POST_KEY_PREFIX = "post:";
    private static final int POST_TTL = 600; // 10 phút

    /**
     * Lưu bài viết vào cache
     */
    public void cachePost(Post post) {
        String key = POST_KEY_PREFIX + post.getId();
        cache.set(key, post, POST_TTL);
        log.debug("Đã cache bài viết: {}", post.getId());
    }

    /**
     * Lấy bài viết từ cache
     */
    public Post getCachedPost(Long postId) {
        String key = POST_KEY_PREFIX + postId;
        return cache.get(key, Post.class);
    }

    /**
     * Xóa bài viết khỏi cache
     */
    public void evictPost(Long postId) {
        String key = POST_KEY_PREFIX + postId;
        cache.delete(key);
        log.debug("Đã xóa cache bài viết: {}", postId);
    }

    /**
     * Xóa cache của nhiều bài viết
     */
    public void evictPosts(List<Long> postIds) {
        postIds.forEach(this::evictPost);
    }
}
