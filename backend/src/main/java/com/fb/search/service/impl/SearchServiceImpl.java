package com.fb.search.service.impl;

import com.fb.auth.repository.UserRepository;
import com.fb.post.dto.PostResponse;
import com.fb.post.mapper.PostMapper;
import com.fb.post.model.Post;
import com.fb.post.repository.PostRepository;
import com.fb.search.dto.SearchResponse;
import com.fb.search.service.SearchService;
import com.fb.user.dto.UserResponse;
import com.fb.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Triển khai service tìm kiếm
 * Tìm kiếm người dùng theo username/displayName và bài viết theo nội dung
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final UserMapper userMapper;
    private final PostMapper postMapper;

    /**
     * Tìm kiếm người dùng và bài viết
     * Tìm người dùng theo username hoặc displayName
     * Tìm bài viết theo nội dung
     */
    @Override
    @Transactional(readOnly = true)
    public SearchResponse search(String query, int page, int size) {
        // Tìm kiếm người dùng
        List<UserResponse> users = userRepository.searchByUsernameOrDisplayName(query).stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());

        // Tìm kiếm bài viết
        List<Post> allPosts = postRepository.searchByContent(query, PageRequest.of(page, size));
        List<PostResponse> posts = allPosts.stream()
                .filter(p -> !p.isDeleted())
                .map(p -> postMapper.toPostResponse(p, false))
                .collect(Collectors.toList());

        log.info("Tìm kiếm thành công - Từ khóa: '{}', Số người dùng: {}, Số bài viết: {}", query, users.size(), posts.size());

        return SearchResponse.builder()
                .users(users)
                .posts(posts)
                .build();
    }
}
