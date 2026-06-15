package com.fb.post.controller;

import com.fb.common.constant.AppConstant;
import com.fb.common.response.ApiResponse;
import com.fb.common.response.PagedResponse;
import com.fb.common.util.HashIdUtil;
import com.fb.post.dto.CreatePostRequest;
import com.fb.post.dto.PostResponse;
import com.fb.post.dto.UpdatePostRequest;
import com.fb.post.service.PostService;
import com.fb.security.CurrentUser;
import com.fb.security.JwtAuthenticationFilter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(AppConstant.API_VERSION + "/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final HashIdUtil hashIdUtil;

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal,
            @Valid @RequestBody CreatePostRequest request) {
        log.info("Tạo bài viết mới bởi người dùng: {}", principal.getUserId());
        PostResponse response = postService.createPost(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Tạo bài viết thành công", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(
            @PathVariable String id,
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal) {
        Long realId = hashIdUtil.decode(id);
        log.info("Lấy thông tin bài viết ID: {}", realId);
        PostResponse response = postService.getPost(realId, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable String id,
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal,
            @Valid @RequestBody UpdatePostRequest request) {
        Long realId = hashIdUtil.decode(id);
        log.info("Cập nhật bài viết ID: {} bởi người dùng: {}", realId, principal.getUserId());
        PostResponse response = postService.updatePost(realId, principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật bài viết thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable String id,
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal) {
        Long realId = hashIdUtil.decode(id);
        log.info("Xóa bài viết ID: {} bởi người dùng: {}", realId, principal.getUserId());
        postService.deletePost(realId, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok("Xóa bài viết thành công"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PagedResponse<PostResponse>>> getUserPosts(
            @PathVariable String userId,
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long realUserId = hashIdUtil.decode(userId);
        log.info("Lấy danh sách bài viết của người dùng ID: {}", realUserId);
        PagedResponse<PostResponse> response = postService.getUserPosts(realUserId, principal.getUserId(), page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/feed")
    public ResponseEntity<ApiResponse<PagedResponse<PostResponse>>> getFeedPosts(
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Lấy trang tin tức cho người dùng: {}", principal.getUserId());
        PagedResponse<PostResponse> response = postService.getFeedPosts(principal.getUserId(), page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
