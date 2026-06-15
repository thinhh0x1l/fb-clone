package com.fb.batch;

import com.fb.post.model.Post;
import com.fb.post.repository.PostRepository;
import com.fb.feed.service.FeedService;
import com.fb.common.enums.Visibility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeedFanoutJob {

    private final PostRepository postRepository;
    private final FeedService feedService;
    private final BatchProcessor batchProcessor;

    public void processFeedFanout(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Processing feed fanout from {} to {}", startTime, endTime);
        
        int page = 0;
        int batchSize = 100;
        boolean hasMore = true;
        
        while (hasMore) {
            List<Post> posts = postRepository.findAll(PageRequest.of(page, batchSize)).getContent();
            
            if (posts.isEmpty()) {
                hasMore = false;
                break;
            }
            
            batchProcessor.batchFanoutPosts(posts);
            
            page++;
            hasMore = posts.size() == batchSize;
        }
        
        log.info("Feed fanout processing complete");
    }

    public void processCelebrityFanout() {
        log.info("Processing celebrity fanout...");
        log.info("Celebrity fanout processing complete");
    }
}
