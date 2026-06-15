package com.fb.feed.controller;

import com.fb.common.constant.AppConstant;
import com.fb.common.response.ApiResponse;
import com.fb.common.util.HashIdUtil;
import com.fb.feed.dto.FeedResponse;
import com.fb.feed.service.FeedService;
import com.fb.security.CurrentUser;
import com.fb.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Điều khiển trang tin tức - hiển thị bài viết từ bạn bè và những người theo dõi
 */
@Slf4j
@RestController
@RequestMapping(AppConstant.API_VERSION + "/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;
    private final HashIdUtil hashIdUtil;

    /**
     * Lấy trang tin tức cho người dùng
     *
     * @param principal thông tin người dùng đã xác thực
     * @param page số trang
     * @param size kích thước trang
     * @return nội dung trang tin tức
     */
    @GetMapping
    public ResponseEntity<ApiResponse<FeedResponse>> getFeed(
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Lấy trang tin tức cho người dùng: {}, trang: {}, kích thước: {}", principal.getUserId(), page, size);
        FeedResponse response = feedService.getFeed(principal.getUserId(), page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
