package com.fb.feed.engine;

import com.fb.post.model.Post;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * Scores posts based on recency with exponential decay
 */
@Slf4j
@Component
public class RecencyScorer {

    /**
     * Score recency (0.0 - 1.0)
     * Uses exponential decay: newer posts score higher
     */
    public double score(LocalDateTime postTime, Map<String, Object> context) {
        if (postTime == null) return 0.0;

        long minutesSincePosted = java.time.Duration.between(postTime, LocalDateTime.now()).toMinutes();
        
        // Exponential decay with half-life of 4 hours (240 minutes)
        double halfLife = 240.0;
        double decay = Math.pow(0.5, minutesSincePosted / halfLife);
        
        return decay;
    }
}
