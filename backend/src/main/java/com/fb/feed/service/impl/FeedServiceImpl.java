package com.fb.feed.service.impl;

import com.fb.auth.model.User;
import com.fb.auth.repository.UserRepository;
import com.fb.common.constant.CacheKey;
import com.fb.common.exception.ResourceNotFoundException;
import com.fb.feed.dto.FeedResponse;
import com.fb.feed.service.FeedService;
import com.fb.friend.repository.FriendRepository;
import com.fb.post.dto.PostResponse;
import com.fb.post.mapper.PostMapper;
import com.fb.post.model.Post;
import com.fb.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final PostMapper postMapper;
    private final com.fb.infrastructure.cache.CacheService cacheService;

    @Override
    @Transactional
    public void fanoutPost(Long postId, Long authorId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("Phân phối bài viết thất bại - Không tìm thấy bài viết ID: {}", postId);
                    return new ResourceNotFoundException("Không tìm thấy bài viết");
                });

        List<User> friends = friendRepository.findAcceptedFriends(authorId).stream()
                .map(f -> f.getRequester().getId().equals(authorId) ? f.getAddressee() : f.getRequester())
                .collect(Collectors.toList());

        for (User friend : friends) {
            String feedKey = CacheKey.USER_FEED + friend.getId();
            double score = post.getCreatedAt() != null ?
                    (double) post.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                    : System.currentTimeMillis();
            cacheService.zAdd(feedKey, postId.toString(), score);
        }

        log.info("Phân phối bài viết thành công - Post ID: {}, Số bạn bè: {}, User ID: {}", postId, friends.size(), authorId);
    }

    @Override
    @Transactional
    public void removePostFromFeeds(Long postId) {
        Set<String> feedKeys = cacheService.keys(CacheKey.USER_FEED + "*");
        if (feedKeys != null) {
            for (String key : feedKeys) {
                cacheService.zRemove(key, postId.toString());
            }
        }
        log.info("Xóa bài viết khỏi tất cả feed - Post ID: {}", postId);
    }

    @Override
    @Transactional(readOnly = true)
    public FeedResponse getFeed(Long userId, int page, int size) {
        String feedKey = CacheKey.USER_FEED + userId;
        long offset = (long) page * size;
        long limit = offset + size - 1;

        Set<Object> cachedPostIds = cacheService.zReverseRange(feedKey, offset, limit);

        List<PostResponse> posts = new ArrayList<>();
        if (cachedPostIds != null && !cachedPostIds.isEmpty()) {
            List<Long> postIds = cachedPostIds.stream()
                    .map(id -> Long.parseLong(id.toString()))
                    .collect(Collectors.toList());

            List<Post> postEntities = postRepository.findByUserIdsWithUser(postIds);

            Map<Long, Post> postMap = postEntities.stream()
                    .collect(Collectors.toMap(Post::getId, p -> p));

            posts = postIds.stream()
                    .filter(postMap::containsKey)
                    .map(id -> postMapper.toPostResponse(postMap.get(id), false))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            log.debug("Lấy feed từ cache - User ID: {}, Số lượng: {}", userId, posts.size());
        }

        if (posts.isEmpty()) {
            List<User> friends = friendRepository.findAcceptedFriends(userId).stream()
                    .map(f -> f.getRequester().getId().equals(userId) ? f.getAddressee() : f.getRequester())
                    .collect(Collectors.toList());

            List<User> feedUsers = new ArrayList<>(friends);
            User currentUser = userRepository.findById(userId).orElse(null);
            if (currentUser != null) {
                feedUsers.add(currentUser);
            }

            List<Long> userIds = feedUsers.stream().map(User::getId).collect(Collectors.toList());
            List<Post> postEntities = postRepository.findByUserIdsWithUser(userIds);

            posts = postEntities.stream()
                    .filter(p -> !p.isDeleted())
                    .map(p -> postMapper.toPostResponse(p, false))
                    .collect(Collectors.toList());

            log.debug("Lấy feed từ database - User ID: {}, Số lượng: {}", userId, posts.size());
            return FeedResponse.builder()
                    .posts(posts)
                    .source("database")
                    .hasMore(false)
                    .build();
        }

        boolean hasMore = cachedPostIds != null && cachedPostIds.size() == size;
        return FeedResponse.builder()
                .posts(posts)
                .source("cache")
                .hasMore(hasMore)
                .build();
    }
}
