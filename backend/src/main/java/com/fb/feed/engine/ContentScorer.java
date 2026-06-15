package com.fb.feed.engine;

import com.fb.post.model.Post;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * Scores content quality based on engagement and content signals
 */
@Slf4j
@Component
public class ContentScorer {

    private final RedisTemplate<String, Object> redisTemplate;

    public ContentScorer(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Score content quality (0.0 - 1.0)
     */
    public double score(Post post, Map<String, Object> context) {
        double score = 0.0;

        // Factor 1: Engagement velocity (likes + comments per hour)
        score += getEngagementVelocityScore(post) * 0.35;

        // Factor 2: Content type bonus
        score += getContentTypeScore(post) * 0.2;

        // Factor 3: Content length (medium = best)
        score += getContentLengthScore(post) * 0.15;

        // Factor 4: Has media
        score += getMediaScore(post) * 0.15;

        // Factor 5: Has hashtags
        score += getHashtagScore(post) * 0.1;

        // Factor 6: Original content (not shared)
        score += getOriginalityScore(post) * 0.05;

        return Math.min(score, 1.0);
    }

    /**
     * Engagement velocity: how fast engagement is growing
     */
    private double getEngagementVelocityScore(Post post) {
        if (post.getCreatedAt() == null) return 0.0;

        long hoursSincePosted = java.time.Duration.between(post.getCreatedAt(), LocalDateTime.now()).toHours();
        if (hoursSincePosted == 0) hoursSincePosted = 1;

        int totalEngagement = (int) (post.getLikesCount() + post.getCommentsCount() + post.getSharesCount());
        double velocity = (double) totalEngagement / hoursSincePosted;

        // Normalize: 50+ engagements per hour = score 1.0
        return Math.min(velocity / 50.0, 1.0);
    }

    /**
     * Content type scoring
     */
    private double getContentTypeScore(Post post) {
        // Video > Photo > Text (based on Facebook's own data)
        if (post.getMedia() != null) {
            boolean hasVideo = post.getMedia().stream()
                    .anyMatch(m -> "VIDEO".equals(m.getType()));
            if (hasVideo) return 1.0;
            
            boolean hasImage = !post.getMedia().isEmpty();
            if (hasImage) return 0.8;
        }
        
        // Text-only posts
        return 0.5;
    }

    /**
     * Content length scoring (medium posts perform best)
     */
    private double getContentLengthScore(Post post) {
        if (post.getContent() == null) return 0.3;
        
        int length = post.getContent().length();
        
        // Sweet spot: 100-500 characters
        if (length >= 100 && length <= 500) {
            return 1.0;
        } else if (length >= 50 && length < 100) {
            return 0.7;
        } else if (length > 500 && length <= 1000) {
            return 0.8;
        } else if (length > 1000) {
            return 0.6;
        } else {
            return 0.4;
        }
    }

    /**
     * Media presence scoring
     */
    private double getMediaScore(Post post) {
        if (post.getMedia() == null || post.getMedia().isEmpty()) {
            return 0.0;
        }
        
        int mediaCount = post.getMedia().size();
        
        // 1-3 images is optimal
        if (mediaCount >= 1 && mediaCount <= 3) {
            return 1.0;
        } else if (mediaCount > 3) {
            return 0.8;
        }
        
        return 0.5;
    }

    /**
     * Hashtag scoring
     */
    private double getHashtagScore(Post post) {
        if (post.getContent() == null) return 0.0;
        
        long hashtagCount = post.getContent().chars()
                .filter(c -> c == '#')
                .count();
        
        // 1-3 hashtags is optimal
        if (hashtagCount >= 1 && hashtagCount <= 3) {
            return 1.0;
        } else if (hashtagCount > 3) {
            return 0.6; // Too many hashtags looks spammy
        }
        
        return 0.3;
    }

    /**
     * Originality scoring
     */
    private double getOriginalityScore(Post post) {
        // TODO: Check if content is original or copied
        // For now, assume original
        return 0.8;
    }
}
