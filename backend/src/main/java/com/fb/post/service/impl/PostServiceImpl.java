package com.fb.post.service.impl;

import com.fb.post.model.Post;
import com.fb.post.repository.PostRepository;
import com.fb.post.cache.PostCacheManager;
import com.fb.post.validator.PostValidator;
import com.fb.post.dto.CreatePostRequest;
import com.fb.post.dto.UpdatePostRequest;
import com.fb.post.dto.PostResponse;
import com.fb.post.mapper.PostMapper;
import com.fb.post.service.PostService;
import com.fb.auth.model.User;
import com.fb.auth.repository.UserRepository;
import com.fb.feed.dto.FeedResponse;
import com.fb.feed.service.FeedService;
import com.fb.common.exception.ResourceNotFoundException;
import com.fb.common.exception.UnauthorizedException;
import com.fb.common.response.PagedResponse;
import com.fb.event.PostCreatedEvent;
import com.fb.event.PostDeletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostMapper postMapper;
    private final PostValidator validator;
    private final PostCacheManager cacheManager;
    private final FeedService feedService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public PostResponse createPost(Long userId, CreatePostRequest request) {
        validator.validateCreate(request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        Post post = new Post();
        post.setContent(request.getContent());
        post.setUser(user);
        post.setVisibility(request.getVisibility() != null ? request.getVisibility() : com.fb.common.enums.Visibility.PUBLIC);

        if (request.getMediaUrls() != null && !request.getMediaUrls().isEmpty()) {
            List<com.fb.post.model.PostMedia> mediaList = new java.util.ArrayList<>();
            for (int i = 0; i < request.getMediaUrls().size(); i++) {
                com.fb.post.model.PostMedia media = new com.fb.post.model.PostMedia();
                media.setPost(post);
                media.setUrl(request.getMediaUrls().get(i));
                media.setType("IMAGE");
                media.setOrderIndex(i);
                mediaList.add(media);
            }
            post.setMedia(mediaList);
        }

        post = postRepository.save(post);

        cacheManager.cachePost(post);

        feedService.fanoutPost(post.getId(), user.getId());

        eventPublisher.publishEvent(new PostCreatedEvent(this, userId, post.getId(), post.getContent()));

        log.info("Tạo bài viết thành công: {} bởi người dùng {}", post.getId(), userId);
        return postMapper.toResponse(post, false);
    }

    @Override
    public PostResponse getPost(Long postId, Long currentUserId) {
        Post cachedPost = cacheManager.getCachedPost(postId);
        if (cachedPost != null) {
            return postMapper.toResponse(cachedPost, false);
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết"));

        cacheManager.cachePost(post);

        return postMapper.toResponse(post, false);
    }

    @Override
    public PagedResponse<PostResponse> getUserPosts(Long userId, Long currentUserId, int page, int size) {
        List<Post> posts = postRepository.findByUserIdWithUser(userId);

        List<PostResponse> response = posts.stream()
                .skip((long) page * size)
                .limit(size)
                .map(post -> postMapper.toResponse(post, false))
                .collect(Collectors.toList());

        return PagedResponse.of(response, page, size, posts.size());
    }

    @Override
    public PagedResponse<PostResponse> getFeedPosts(Long userId, int page, int size) {
        FeedResponse feedResponse = feedService.getFeed(userId, page, size);
        return PagedResponse.of(feedResponse.getPosts(), page, size, feedResponse.getPosts().size());
    }

    @Override
    @Transactional
    public PostResponse updatePost(Long postId, Long userId, UpdatePostRequest request) {
        validator.validateUpdate(request);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết"));

        if (!post.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Bạn không có quyền chỉnh sửa bài viết này");
        }

        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }
        if (request.getVisibility() != null) {
            post.setVisibility(request.getVisibility());
        }

        post = postRepository.save(post);

        cacheManager.cachePost(post);

        log.info("Cập nhật bài viết thành công: {}", postId);
        return postMapper.toResponse(post, false);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết"));

        if (!post.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Bạn không có quyền xóa bài viết này");
        }

        post.softDelete();
        postRepository.save(post);

        cacheManager.evictPost(postId);

        feedService.removePostFromFeeds(post.getId());

        eventPublisher.publishEvent(new PostDeletedEvent(this, userId, postId));

        log.info("Xóa bài viết thành công: {}", postId);
    }
}
