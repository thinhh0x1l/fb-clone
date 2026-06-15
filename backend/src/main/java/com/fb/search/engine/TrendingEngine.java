package com.fb.search.engine;

import com.fb.post.model.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Thuật toán Chủ đề Thịnh hành
 * 
 * Phát hiện hashtag và chủ đề thịnh hành dựa trên:
 * 1. Khối lượng: Số lượng bài viết sử dụng hashtag
 * 2. Vận tốc: Tốc độ tăng trong việc sử dụng
 * 3. Đa dạng: Số lượng người dùng duy nhất sử dụng
 * 4. Tương tác: Tương tác trung bình trên bài viết có hashtag
 * 
 * Tham khảo: https://blog.twitter.com/engineering/
 *             en_us/topics/experiments/2018/trend-algorithm-update
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TrendingEngine {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TRENDING_HASHTAG_KEY = "trending:hashtags";
    private static final String HASHTAG_USAGE_KEY = "hashtag:usage:";
    private static final String HASHTAG_VELOCITY_KEY = "hashtag:velocity:";
    private static final String HASHTAG_USERS_KEY = "hashtag:users:";
    private static final String HASHTAG_ENGAGEMENT_KEY = "hashtag:engagement:";

    private static final Pattern HASHTAG_PATTERN = Pattern.compile("#(\\w+)");
    private static final int TRENDING_WINDOW_HOURS = 24;
    private static final int MIN_POSTS_FOR_TRENDING = 10;

    /**
     * Trích xuất hashtag từ văn bản
     */
    public List<String> extractHashtags(String text) {
        if (text == null) return List.of();
        
        Matcher matcher = HASHTAG_PATTERN.matcher(text);
        List<String> hashtags = new ArrayList<>();
        
        while (matcher.find()) {
            hashtags.add(matcher.group(1).toLowerCase());
        }
        
        return hashtags;
    }

    /**
     * Ghi lại việc sử dụng hashtag
     */
    public void recordHashtagUsage(String hashtag, Long postId, Long userId, int engagement) {
        String usageKey = HASHTAG_USAGE_KEY + hashtag;
        String usersKey = HASHTAG_USERS_KEY + hashtag;
        String engagementKey = HASHTAG_ENGAGEMENT_KEY + hashtag;
        
        // Tăng số lượng sử dụng
        redisTemplate.opsForValue().increment(usageKey);
        redisTemplate.expire(usageKey, 7, java.util.concurrent.TimeUnit.DAYS);
        
        // Theo dõi người dùng duy nhất
        redisTemplate.opsForSet().add(usersKey, userId.toString());
        redisTemplate.expire(usersKey, 7, java.util.concurrent.TimeUnit.DAYS);
        
        // Theo dõi tương tác
        redisTemplate.opsForValue().increment(engagementKey, engagement);
        redisTemplate.expire(engagementKey, 7, java.util.concurrent.TimeUnit.DAYS);
        
        // Tính toán và cập nhật vận tốc
        updateVelocity(hashtag);
    }

    /**
     * Cập nhật vận tốc hashtag (tốc độ thay đổi)
     */
    private void updateVelocity(String hashtag) {
        String usageKey = HASHTAG_USAGE_KEY + hashtag;
        String velocityKey = HASHTAG_VELOCITY_KEY + hashtag;
        
        Object currentCount = redisTemplate.opsForValue().get(usageKey);
        if (currentCount == null) return;
        
        long count = currentCount instanceof Number ? ((Number) currentCount).longValue() : 0;
        
        // Lưu số lượng với timestamp để tính vận tốc
        String historyKey = "hashtag:history:" + hashtag + ":" + System.currentTimeMillis();
        redisTemplate.opsForValue().set(historyKey, count, 24, java.util.concurrent.TimeUnit.HOURS);
        
        // Tính vận tốc (bài viết mỗi giờ)
        long oneHourAgo = System.currentTimeMillis() - 3600000;
        String prevKey = "hashtag:history:" + hashtag + ":" + oneHourAgo;
        Object prevCount = redisTemplate.opsForValue().get(prevKey);
        
        if (prevCount != null) {
            long prev = prevCount instanceof Number ? ((Number) prevCount).longValue() : 0;
            long velocity = count - prev;
            redisTemplate.opsForValue().set(velocityKey, velocity, 24, java.util.concurrent.TimeUnit.HOURS);
        }
    }

    /**
     * Tính điểm thịnh hành cho hashtag
     */
    private double calculateTrendingScore(String hashtag) {
        String usageKey = HASHTAG_USAGE_KEY + hashtag;
        String velocityKey = HASHTAG_VELOCITY_KEY + hashtag;
        String usersKey = HASHTAG_USERS_KEY + hashtag;
        String engagementKey = HASHTAG_ENGAGEMENT_KEY + hashtag;
        
        // Lấy chỉ số
        Object countObj = redisTemplate.opsForValue().get(usageKey);
        Object velocityObj = redisTemplate.opsForValue().get(velocityKey);
        Object usersObj = redisTemplate.opsForSet().size(usersKey);
        Object engagementObj = redisTemplate.opsForValue().get(engagementKey);
        
        long count = countObj instanceof Number ? ((Number) countObj).longValue() : 0;
        long velocity = velocityObj instanceof Number ? ((Number) velocityObj).longValue() : 0;
        long uniqueUsers = usersObj instanceof Number ? ((Number) usersObj).longValue() : 0;
        long engagement = engagementObj instanceof Number ? ((Number) engagementObj).longValue() : 0;
        
        // Ngưỡng tối thiểu
        if (count < MIN_POSTS_FOR_TRENDING) return 0.0;
        
        // Tính điểm
        double volumeScore = Math.min(count / 1000.0, 1.0) * 0.3;
        double velocityScore = Math.min(velocity / 100.0, 1.0) * 0.3;
        double diversityScore = Math.min(uniqueUsers / 100.0, 1.0) * 0.2;
        double engagementScore = count > 0 ? Math.min((double) engagement / count / 10.0, 1.0) * 0.2 : 0;
        
        return volumeScore + velocityScore + diversityScore + engagementScore;
    }

    /**
     * Lấy hashtag thịnh hành
     */
    public List<TrendingTopic> getTrendingHashtags(int limit) {
        Set<Object> cachedTrending = redisTemplate.opsForZSet()
                .reverseRange(TRENDING_HASHTAG_KEY, 0, limit - 1);
        
        if (cachedTrending != null && !cachedTrending.isEmpty()) {
            return cachedTrending.stream()
                    .map(Object::toString)
                    .map(tag -> TrendingTopic.builder()
                            .tag(tag)
                            .score(redisTemplate.opsForZSet()
                                    .score(TRENDING_HASHTAG_KEY, tag))
                            .postCount(getPostCount(tag))
                            .uniqueUsers(getUniqueUsers(tag))
                            .build())
                    .collect(Collectors.toList());
        }
        
        return List.of();
    }

    /**
     * Cập nhật hashtag thịnh hành (tác vụ định kỳ)
     */
    @Scheduled(fixedRate = 300000) // Mỗi 5 phút
    public void updateTrending() {
        log.info("Đang cập nhật hashtag thịnh hành...");
        
        // Lấy tất cả hashtag từ 24 giờ qua
        Set<String> recentHashtags = getRecentHashtags();
        
        for (String hashtag : recentHashtags) {
            double score = calculateTrendingScore(hashtag);
            if (score > 0) {
                redisTemplate.opsForZSet().add(TRENDING_HASHTAG_KEY, hashtag, score);
            }
        }
        
        // Cắt xuống top 100
        Long size = redisTemplate.opsForZSet().size(TRENDING_HASHTAG_KEY);
        if (size != null && size > 100) {
            redisTemplate.opsForZSet().removeRange(TRENDING_HASHTAG_KEY, 100, -1);
        }
        
        // Đặt TTL
        redisTemplate.expire(TRENDING_HASHTAG_KEY, 1, java.util.concurrent.TimeUnit.HOURS);
        
        log.info("Cập nhật thịnh hành hoàn thành. {} hashtag được theo dõi", recentHashtags.size());
    }

    /**
     * Lấy hashtag gần đây
     */
    private Set<String> getRecentHashtags() {
        // TODO: Lấy từ khóa sử dụng hashtag
        return Set.of();
    }

    /**
     * Lấy số lượng bài viết
     */
    private long getPostCount(String hashtag) {
        Object count = redisTemplate.opsForValue().get(HASHTAG_USAGE_KEY + hashtag);
        return count instanceof Number ? ((Number) count).longValue() : 0;
    }

    /**
     * Lấy số lượng người dùng duy nhất
     */
    private long getUniqueUsers(String hashtag) {
        Long count = redisTemplate.opsForSet().size(HASHTAG_USERS_KEY + hashtag);
        return count != null ? count : 0;
    }

    /**
     * Kết quả chủ đề thịnh hành
     */
    @lombok.Data
    @lombok.Builder
    public static class TrendingTopic {
        private String tag;
        private Double score;
        private long postCount;
        private long uniqueUsers;
    }
}
