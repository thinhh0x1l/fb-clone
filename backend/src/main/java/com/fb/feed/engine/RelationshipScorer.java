package com.fb.feed.engine;

import com.fb.auth.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Đánh giá độ mạnh mối quan hệ giữa hai người dùng
 * dựa trên tần suất tương tác và tín hiệu xã hội
 */
@Slf4j
@Component
public class RelationshipScorer {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String INTERACTION_KEY = "interaction:";

    public RelationshipScorer(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Đánh giá mối quan hệ giữa người xem và tác giả (0.0 - 1.0)
     */
    public double score(User viewer, User author, Map<String, Object> context) {
        if (viewer.getId().equals(author.getId())) {
            return 1.0; // Bài viết của chính mình luôn xếp hạng cao nhất
        }

        double score = 0.0;

        // Yếu tố 1: Tương tác trực tiếp (lượt thích, bình luận, tin nhắn)
        score += getInteractionScore(viewer.getId(), author.getId()) * 0.4;

        // Yếu tố 2: Bạn bè chung
        score += getMutualFriendsScore(viewer.getId(), author.getId()) * 0.2;

        // Yếu tố 3: Lượt xem hồ sơ
        score += getProfileViewScore(viewer.getId(), author.getId()) * 0.15;

        // Yếu tố 4: Tần suất gắn thẻ
        score += getTagFrequencyScore(viewer.getId(), author.getId()) * 0.15;

        // Yếu tố 5: Tính gần đây của tương tác
        score += getRecencyInteractionScore(viewer.getId(), author.getId()) * 0.1;

        return Math.min(score, 1.0);
    }

    /**
     * Đánh giá dựa trên tương tác trực tiếp
     */
    private double getInteractionScore(Long viewerId, Long authorId) {
        String key = INTERACTION_KEY + viewerId + ":" + authorId;
        Object count = redisTemplate.opsForValue().get(key);
        
        if (count == null) return 0.0;
        
        int interactions = count instanceof Number ? ((Number) count).intValue() : 0;
        
        // Chuẩn hóa: 100+ tương tác = điểm 1.0
        return Math.min(interactions / 100.0, 1.0);
    }

    /**
     * Đánh giá dựa trên bạn bè chung
     */
    private double getMutualFriendsScore(Long viewerId, Long authorId) {
        String viewerFriendsKey = "friends:" + viewerId;
        String authorFriendsKey = "friends:" + authorId;
        
        Set<Object> viewerFriends = redisTemplate.opsForSet().members(viewerFriendsKey);
        Set<Object> authorFriends = redisTemplate.opsForSet().members(authorFriendsKey);
        
        if (viewerFriends == null || authorFriends == null) return 0.0;
        
        // Đếm bạn bè chung
        Set<Object> mutual = new java.util.HashSet<>(viewerFriends);
        mutual.retainAll(authorFriends);
        
        // Chuẩn hóa: 50+ bạn bè chung = điểm 1.0
        return Math.min(mutual.size() / 50.0, 1.0);
    }

    /**
     * Đánh giá dựa trên lượt xem hồ sơ
     */
    private double getProfileViewScore(Long viewerId, Long authorId) {
        String key = "profile:view:" + viewerId + ":" + authorId;
        Object views = redisTemplate.opsForValue().get(key);
        
        if (views == null) return 0.0;
        
        int viewCount = views instanceof Number ? ((Number) views).intValue() : 0;
        
        // Chuẩn hóa: 20+ lượt xem = điểm 1.0
        return Math.min(viewCount / 20.0, 1.0);
    }

    /**
     * Đánh giá dựa trên tần suất gắn thẻ
     */
    private double getTagFrequencyScore(Long viewerId, Long authorId) {
        String key = "tag:frequency:" + viewerId + ":" + authorId;
        Object tags = redisTemplate.opsForValue().get(key);
        
        if (tags == null) return 0.0;
        
        int tagCount = tags instanceof Number ? ((Number) tags).intValue() : 0;
        
        // Chuẩn hóa: 10+ thẻ = điểm 1.0
        return Math.min(tagCount / 10.0, 1.0);
    }

    /**
     * Đánh giá dựa trên thời gian tương tác gần đây
     */
    private double getRecencyInteractionScore(Long viewerId, Long authorId) {
        String key = "interaction:last:" + viewerId + ":" + authorId;
        Object lastInteraction = redisTemplate.opsForValue().get(key);
        
        if (lastInteraction == null) return 0.0;
        
        long lastInteractionTime = lastInteraction instanceof Number ? 
                ((Number) lastInteraction).longValue() : 0L;
        
        long hoursSince = java.time.Duration.ofMillis(
                System.currentTimeMillis() - lastInteractionTime).toHours();
        
        // Điểm giảm theo thời gian: 0 giờ = 1.0, 168 giờ (1 tuần) = 0.0
        return Math.max(0, 1.0 - (hoursSince / 168.0));
    }

    /**
     * Ghi lại tương tác giữa hai người dùng
     */
    public void recordInteraction(Long userId, Long targetUserId) {
        String key = INTERACTION_KEY + userId + ":" + targetUserId;
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, 30, TimeUnit.DAYS);
        
        // Cập nhật thời gian tương tác cuối cùng
        String lastKey = "interaction:last:" + userId + ":" + targetUserId;
        redisTemplate.opsForValue().set(lastKey, System.currentTimeMillis(), 30, TimeUnit.DAYS);
    }
}
