package com.fb.orchestrator;

import com.fb.auth.model.User;
import com.fb.feed.engine.FeedRankingEngine;
import com.fb.feed.service.FeedService;
import com.fb.post.model.Post;
import com.fb.post.repository.PostRepository;
import com.fb.common.exception.ResourceNotFoundException;
import com.fb.infrastructure.cache.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostOrchestrator {

    private final PostRepository postRepository;
    private final FeedService feedService;
    private final FeedRankingEngine feedRankingEngine;
    private final CacheService cacheService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Post orchestrateCreatePost(User author, String content, String visibility,
                                       List<String> mediaUrls) {
        Post post = createPost(author, content, visibility, mediaUrls);
        log.info("Tạo bài viết thành công: {}", post.getId());

        List<String> hashtags = extractHashtags(content);
        if (!hashtags.isEmpty()) {
            indexHashtags(post, hashtags);
        }

        List<Long> mentions = extractMentions(content);
        if (!mentions.isEmpty()) {
            notifyMentions(post, mentions, author);
        }

        feedService.fanoutPost(post.getId(), author.getId());

        updateTrending(hashtags, post);

        trackPostCreation(author, post);

        eventPublisher.publishEvent(new com.fb.event.PostCreatedEvent(this, author.getId(), post.getId(), content));

        return post;
    }

    @Transactional
    public void orchestrateDeletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết"));

        post.softDelete();
        postRepository.save(post);

        feedService.removePostFromFeeds(postId);

        cacheService.delete("post:" + postId);

        eventPublisher.publishEvent(new com.fb.event.PostDeletedEvent(this, userId, postId));

        log.info("Xóa bài viết thành công: {} bởi người dùng {}", postId, userId);
    }

    private Post createPost(User author, String content, String visibility,
                             List<String> mediaUrls) {
        Post post = new Post();
        post.setContent(content);
        post.setUser(author);
        post.setVisibility(com.fb.common.enums.Visibility.valueOf(visibility));

        if (mediaUrls != null && !mediaUrls.isEmpty()) {
            List<com.fb.post.model.PostMedia> mediaList = new ArrayList<>();
            AtomicInteger index = new AtomicInteger(0);
            for (String url : mediaUrls) {
                com.fb.post.model.PostMedia media = new com.fb.post.model.PostMedia();
                media.setPost(post);
                media.setUrl(url);
                media.setType("IMAGE");
                media.setOrderIndex(index.getAndIncrement());
                mediaList.add(media);
            }
            post.setMedia(mediaList);
        }

        return postRepository.save(post);
    }

    private List<String> extractHashtags(String content) {
        if (content == null) return List.of();
        java.util.regex.Matcher matcher =
                java.util.regex.Pattern.compile("#(\\w+)").matcher(content);
        java.util.List<String> hashtags = new java.util.ArrayList<>();
        while (matcher.find()) {
            hashtags.add(matcher.group(1).toLowerCase());
        }
        return hashtags;
    }

    private List<Long> extractMentions(String content) {
        if (content == null) return List.of();
        return new java.util.ArrayList<>();
    }

    private void indexHashtags(Post post, List<String> hashtags) {
        for (String hashtag : hashtags) {
            String key = "hashtag:" + hashtag;
            cacheService.zAdd(key, post.getId().toString(), System.currentTimeMillis());
            cacheService.expire(key, 7 * 86400L, java.util.concurrent.TimeUnit.SECONDS);
        }
    }

    private void notifyMentions(Post post, List<Long> mentionedUsers, User author) {
        for (Long userId : mentionedUsers) {
            log.info("Người dùng {} được nhắc đến trong bài viết {} bởi {}", userId, post.getId(), author.getId());
        }
    }

    private void updateTrending(List<String> hashtags, Post post) {
        for (String hashtag : hashtags) {
            String key = "trending:hashtag:" + hashtag;
            cacheService.increment(key);
            cacheService.expire(key, 86400L, java.util.concurrent.TimeUnit.SECONDS);
        }
    }

    private void trackPostCreation(User author, Post post) {
        String key = "analytics:user:posts:" + author.getId();
        cacheService.increment(key);
        cacheService.expire(key, 86400L, java.util.concurrent.TimeUnit.SECONDS);

        cacheService.increment("analytics:posts:total");
    }
}
