package com.fb.search.engine;

import com.fb.auth.model.User;
import com.fb.post.model.Post;
import com.fb.search.dto.SearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Công cụ xếp hạng Tìm kiếm
 * 
 * Triển khai xếp hạng kết quả tìm kiếm với nhiều tín hiệu:
 * 1. Mức độ liên quan (chất lượng khớp văn bản)
 * 2. Mức độ phổ biến (chỉ số tương tác)
 * 3. Tính gần đây (sự mới mẻ)
 * 4. Cá nhân hóa (khoảng cách đồ thị xã hội)
 * 
 * Tham khảo: https://engineering.fb.com/2020/04/06/
 *             data-infrastructure/search-ranking/
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchRankingEngine {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Xếp hạng kết quả tìm kiếm
     */
    public <T> List<RankedResult<T>> rankResults(List<T> results, String query, Long userId) {
        return results.stream()
                .map(item -> {
                    double score = calculateScore(item, query, userId);
                    return new RankedResult<>(item, score);
                })
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .collect(Collectors.toList());
    }

    /**
     * Tính điểm xếp hạng cho kết quả tìm kiếm
     */
    private <T> double calculateScore(T item, String query, Long userId) {
        double score = 0.0;

        if (item instanceof User user) {
            score = scoreUser(user, query, userId);
        } else if (item instanceof Post post) {
            score = scorePost(post, query, userId);
        }

        return score;
    }

    /**
     * Đánh giá kết quả tìm kiếm người dùng
     */
    private double scoreUser(User user, String query, Long userId) {
        double score = 0.0;

        // Yếu tố 1: Chất lượng khớp tên
        score += getNameMatchScore(user, query) * 0.4;

        // Yếu tố 2: Khớp chính xác tên người dùng
        if (user.getUsername() != null && 
            user.getUsername().toLowerCase().equals(query.toLowerCase())) {
            score += 0.3;
        }

        // Yếu tố 3: Bạn bè chung
        score += getMutualFriendsScore(userId, user.getId()) * 0.2;

        // Yếu tố 4: Độ hoàn thiện hồ sơ
        score += getProfileCompletenessScore(user) * 0.1;

        return score;
    }

    /**
     * Đánh giá kết quả tìm kiếm bài viết
     */
    private double scorePost(Post post, String query, Long userId) {
        double score = 0.0;

        // Yếu tố 1: Mức độ liên quan nội dung
        score += getContentRelevanceScore(post, query) * 0.4;

        // Yếu tố 2: Mức độ tương tác
        score += getEngagementScore(post) * 0.3;

        // Yếu tố 3: Tính gần đây
        score += getRecencyScore(post) * 0.2;

        // Yếu tố 4: Mối quan hệ với tác giả
        score += getAuthorRelationshipScore(userId, post.getUser().getId()) * 0.1;

        return score;
    }

    /**
     * Điểm chất lượng khớp tên
     */
    private double getNameMatchScore(User user, String query) {
        String displayName = user.getDisplayName().toLowerCase();
        String queryLower = query.toLowerCase();

        // Khớp chính xác
        if (displayName.equals(queryLower)) {
            return 1.0;
        }

        // Bắt đầu bằng truy vấn
        if (displayName.startsWith(queryLower)) {
            return 0.9;
        }

        // Chứa truy vấn
        if (displayName.contains(queryLower)) {
            return 0.7;
        }

        // Khớp mờ (khoảng cách Levenshtein)
        double similarity = calculateSimilarity(displayName, queryLower);
        return similarity * 0.5;
    }

    /**
     * Điểm mức độ liên quan nội dung
     */
    private double getContentRelevanceScore(Post post, String query) {
        if (post.getContent() == null) return 0.0;

        String content = post.getContent().toLowerCase();
        String queryLower = query.toLowerCase();

        // Khớp cụm từ chính xác
        if (content.contains(queryLower)) {
            // Đếm số lần xuất hiện
            int count = countOccurrences(content, queryLower);
            return Math.min(count / 5.0, 1.0);
        }

        // Khớp từ
        String[] queryWords = queryLower.split("\\s+");
        int matchedWords = 0;
        for (String word : queryWords) {
            if (content.contains(word)) {
                matchedWords++;
            }
        }

        return (double) matchedWords / queryWords.length;
    }

    /**
     * Điểm mức độ tương tác
     */
    private double getEngagementScore(Post post) {
        int totalEngagement = (int) (post.getLikesCount() + post.getCommentsCount() + post.getSharesCount());
        // Chuẩn hóa: 1000+ tương tác = điểm 1.0
        return Math.min(totalEngagement / 1000.0, 1.0);
    }

    /**
     * Điểm tính gần đây
     */
    private double getRecencyScore(Post post) {
        if (post.getCreatedAt() == null) return 0.0;

        long hoursSincePosted = java.time.Duration.between(
                post.getCreatedAt(), java.time.LocalDateTime.now()).toHours();

        // Phân rã hàm mũ với bánpériode 24 giờ
        return Math.pow(0.5, hoursSincePosted / 24.0);
    }

    /**
     * Điểm bạn bè chung
     */
    private double getMutualFriendsScore(Long userId1, Long userId2) {
        String friendsKey1 = "friends:" + userId1;
        String friendsKey2 = "friends:" + userId2;

        Set<Object> friends1 = redisTemplate.opsForSet().members(friendsKey1);
        Set<Object> friends2 = redisTemplate.opsForSet().members(friendsKey2);

        if (friends1 == null || friends2 == null) return 0.0;

        Set<Object> mutual = new HashSet<>(friends1);
        mutual.retainAll(friends2);

        return Math.min(mutual.size() / 20.0, 1.0);
    }

    /**
     * Điểm mối quan hệ với tác giả
     */
    private double getAuthorRelationshipScore(Long viewerId, Long authorId) {
        if (viewerId.equals(authorId)) return 1.0;

        String key = "interaction:" + viewerId + ":" + authorId;
        Object count = redisTemplate.opsForValue().get(key);

        if (count == null) return 0.0;

        int interactions = count instanceof Number ? ((Number) count).intValue() : 0;
        return Math.min(interactions / 50.0, 1.0);
    }

    /**
     * Điểm độ hoàn thiện hồ sơ
     */
    private double getProfileCompletenessScore(User user) {
        int completeness = 0;
        int total = 7;

        if (user.getAvatar() != null) completeness++;
        if (user.getBio() != null && !user.getBio().isEmpty()) completeness++;
        if (user.getWorkplace() != null) completeness++;
        if (user.getEducation() != null) completeness++;
        if (user.getLocation() != null) completeness++;
        if (user.getCoverPhoto() != null) completeness++;
        if (user.getBirthday() != null) completeness++;

        return (double) completeness / total;
    }

    /**
     * Tính độ tương đồng chuỗi (dựa trên Levenshtein)
     */
    private double calculateSimilarity(String s1, String s2) {
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 1.0;

        int distance = levenshteinDistance(s1, s2);
        return 1.0 - ((double) distance / maxLen);
    }

    /**
     * Tính khoảng cách Levenshtein
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= s2.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[s1.length()][s2.length()];
    }

    /**
     * Đếm số lần xuất hiện của chuỗi con
     */
    private int countOccurrences(String text, String substring) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }

    /**
     * Kết quả tìm kiếm đã xếp hạng
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class RankedResult<T> {
        private T item;
        private double score;
    }
}
