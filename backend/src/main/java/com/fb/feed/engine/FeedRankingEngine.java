package com.fb.feed.engine;

import com.fb.common.constant.AppConstant;
import com.fb.post.model.Post;
import com.fb.auth.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Công cụ xếp hạng Bảng tin
 * 
 * Triển khai thuật toán tính điểm đa yếu tố tương tự Facebook:
 * 1. Điểm Quan hệ (40%) - Mức độ thân thiết giữa người dùng và tác giả
 * 2. Điểm Chất lượng Nội dung (30%) - Tốc độ tương tác, loại nội dung
 * 3. Điểm Tính gần đây (20%) - Phân rã theo thời gian
 * 4. Điểm Đa dạng (10%) - Sự đa dạng nội dung
 * 
 * Tham khảo: https://engineering.fb.com/2021/01/21/ engineering/optimizing-newsfeed/
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FeedRankingEngine {

    private final RelationshipScorer relationshipScorer;
    private final ContentScorer contentScorer;
    private final RecencyScorer recencyScorer;
    private final DiversityScorer diversityScorer;

    /**
     * Xếp hạng bài viết cho bảng tin của người dùng
     */
    public List<RankedPost> rankFeed(User user, List<Post> posts, Map<String, Object> context) {
        List<RankedPost> rankedPosts = posts.stream()
                .map(post -> {
                    double score = calculateScore(user, post, context);
                    return new RankedPost(post, score);
                })
                .sorted(Comparator.comparingDouble(RankedPost::getScore).reversed())
                .collect(Collectors.toList());

        // Áp dụng bộ lọc đa dạng
        List<DiversityScorer.RankedPost> diversityInput = rankedPosts.stream()
                .map(rp -> new DiversityScorer.RankedPost(rp.getPost(), rp.getScore()))
                .collect(Collectors.toList());
        List<DiversityScorer.RankedPost> filtered = diversityScorer.applyDiversityFilter(diversityInput, 10);
        rankedPosts = filtered.stream()
                .map(rp -> new RankedPost(rp.getPost(), rp.getScore()))
                .collect(Collectors.toList());

        log.debug("Đã xếp hạng bảng tin cho người dùng {}: {} bài viết được chấm điểm", user.getId(), rankedPosts.size());
        return rankedPosts;
    }

    /**
     * Tính điểm tổng hợp cho một bài viết
     */
    private double calculateScore(User user, Post post, Map<String, Object> context) {
        double relationshipScore = relationshipScorer.score(user, post.getUser(), context);
        double contentScore = contentScorer.score(post, context);
        double recencyScore = recencyScorer.score(post.getCreatedAt(), context);
        double diversityScore = diversityScorer.score(post, context);

        double compositeScore =
                relationshipScore * AppConstant.FEED_RELATIONSHIP_WEIGHT +
                contentScore * AppConstant.FEED_CONTENT_WEIGHT +
                recencyScore * AppConstant.FEED_RECENCY_WEIGHT +
                diversityScore * AppConstant.FEED_DIVERSITY_WEIGHT;

        // Áp dụng điểm thưởng
        compositeScore *= applyBoosts(user, post, context);

        return compositeScore;
    }

    /**
     * Áp dụng điểm thưởng dựa trên các tín hiệu khác nhau
     */
    private double applyBoosts(User user, Post post, Map<String, Object> context) {
        double boost = 1.0;

        // Điểm thưởng cho bài viết có tốc độ tương tác cao
        if (post.getLikesCount() > 100) {
            boost *= 1.2;
        }

        // Điểm thưởng cho bài viết có phương tiện
        if (post.getMedia() != null && !post.getMedia().isEmpty()) {
            boost *= 1.15;
        }

        // Điểm thưởng cho bài viết có nhiều bình luận (thảo luận)
        if (post.getCommentsCount() > 50) {
            boost *= 1.1;
        }

        // Điểm thưởng cho bài viết từ bạn bè thân thiết
        // (được xử lý bởi điểm quan hệ, nhưng điểm thưởng thêm cho bạn thân)
        // TODO: Kiểm tra xem người dùng có trong danh sách "Bạn bè thân thiết" không

        // Phạt cho bài viết người dùng đã xem
        if (hasUserSeenPost(user, post)) {
            boost *= 0.5;
        }

        // Phạt cho bài viết người dùng đã ẩn
        if (hasUserHiddenPost(user, post)) {
            boost *= 0.1;
        }

        return boost;
    }

    /**
     * Kiểm tra người dùng đã xem bài viết chưa
     */
    private boolean hasUserSeenPost(User user, Post post) {
        // TODO: Kiểm tra Redis cho các bài viết đã xem
        return false;
    }

    /**
     * Kiểm tra người dùng đã ẩn bài viết chưa
     */
    private boolean hasUserHiddenPost(User user, Post post) {
        // TODO: Kiểm tra Redis cho các bài viết đã ẩn
        return false;
    }

    /**
     * Bài viết đã xếp hạng với điểm
     */
    public static class RankedPost {
        private final Post post;
        private final double score;

        public RankedPost(Post post, double score) {
            this.post = post;
            this.score = score;
        }

        public Post getPost() { return post; }
        public double getScore() { return score; }
    }
}
