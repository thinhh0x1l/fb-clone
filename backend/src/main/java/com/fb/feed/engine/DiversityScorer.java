package com.fb.feed.engine;

import com.fb.post.model.Post;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Ensures feed diversity - prevents seeing too much of the same content type
 */
@Slf4j
@Component
public class DiversityScorer {

    /**
     * Score diversity (0.0 - 1.0)
     */
    public double score(Post post, Map<String, Object> context) {
        // Default diversity score - will be adjusted during filtering
        return 0.5;
    }

    /**
     * Apply diversity filter to prevent content fatigue
     */
    public List<RankedPost> applyDiversityFilter(List<RankedPost> rankedPosts, int maxConsecutive) {
        if (rankedPosts == null || rankedPosts.size() <= maxConsecutive) {
            return rankedPosts;
        }

        List<RankedPost> filtered = new ArrayList<>();
        Map<String, Integer> consecutiveCount = new HashMap<>();

        for (RankedPost ranked : rankedPosts) {
            String contentType = getContentType(ranked.getPost());
            int currentConsecutive = consecutiveCount.getOrDefault(contentType, 0);

            if (currentConsecutive < maxConsecutive) {
                filtered.add(ranked);
                consecutiveCount.put(contentType, currentConsecutive + 1);
            } else {
                // Skip this post, will be added later if needed
                continue;
            }

            // Reset count for other types
            consecutiveCount.forEach((key, value) -> {
                if (!key.equals(contentType)) {
                    consecutiveCount.put(key, 0);
                }
            });
        }

        return filtered;
    }

    private String getContentType(Post post) {
        if (post.getMedia() != null && !post.getMedia().isEmpty()) {
            boolean hasVideo = post.getMedia().stream()
                    .anyMatch(m -> "VIDEO".equals(m.getType()));
            return hasVideo ? "video" : "image";
        }
        return "text";
    }

    /**
     * Ranked post with score (inner class for diversity filter)
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
