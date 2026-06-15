package com.fb.search.controller;

import com.fb.common.constant.AppConstant;
import com.fb.common.response.ApiResponse;
import com.fb.common.util.HashIdUtil;
import com.fb.search.dto.SearchResponse;
import com.fb.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Điều khiển tìm kiếm - tìm kiếm người dùng, bài viết và nội dung khác
 */
@Slf4j
@RestController
@RequestMapping(AppConstant.API_VERSION + "/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final HashIdUtil hashIdUtil;

    /**
     * Tìm kiếm nội dung theo từ khóa
     *
     * @param query từ khóa tìm kiếm
     * @param page số trang
     * @param size kích thước trang
     * @return kết quả tìm kiếm
     */
    @GetMapping
    public ResponseEntity<ApiResponse<SearchResponse>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Tìm kiếm với từ khóa: '{}', trang: {}, kích thước: {}", query, page, size);
        SearchResponse response = searchService.search(query, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
