package com.fb.media.controller;

import com.fb.common.annotation.RateLimit;
import com.fb.common.constant.AppConstant;
import com.fb.common.response.ApiResponse;
import com.fb.common.util.HashIdUtil;
import com.fb.media.dto.MediaResponse;
import com.fb.media.service.MediaService;
import com.fb.security.CurrentUser;
import com.fb.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Điều khiển phương tiện - xử lý tải lên và quản lý tệp tin
 */
@Slf4j
@RestController
@RequestMapping(AppConstant.API_VERSION + "/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;
    private final HashIdUtil hashIdUtil;

    /**
     * Tải lên một tệp tin
     *
     * @param principal thông tin người dùng đã xác thực
     * @param file tệp tin cần tải lên
     * @return thông tin tệp tin đã tải lên
     */
    @PostMapping("/upload")
    @RateLimit(capacity = 5, refillTokens = 5, refillDurationSeconds = 1)
    public ResponseEntity<ApiResponse<MediaResponse>> upload(
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal,
            @RequestParam("file") MultipartFile file) {
        log.info("Tải lên tệp tin '{}' bởi người dùng: {}", file.getOriginalFilename(), principal.getUserId());
        MediaResponse response = mediaService.upload(file, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tải lên tệp tin thành công", response));
    }

    /**
     * Tải lên nhiều tệp tin
     *
     * @param principal thông tin người dùng đã xác thực
     * @param files danh sách tệp tin cần tải lên
     * @return danh sách thông tin tệp tin đã tải lên
     */
    @PostMapping("/upload-multiple")
    @RateLimit(capacity = 3, refillTokens = 3, refillDurationSeconds = 1)
    public ResponseEntity<ApiResponse<List<MediaResponse>>> uploadMultiple(
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal,
            @RequestParam("files") List<MultipartFile> files) {
        log.info("Tải lên {} tệp tin bởi người dùng: {}", files.size(), principal.getUserId());
        List<MediaResponse> response = mediaService.uploadMultiple(files, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tải lên tệp tin thành công", response));
    }

    /**
     * Xóa tệp tin
     *
     * @param principal thông tin người dùng đã xác thực
     * @param id mã HashId của tệp tin cần xóa
     * @return phản hồi thành công
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal,
            @PathVariable String id) {
        Long realId = hashIdUtil.decode(id);
        log.info("Xóa tệp tin ID: {} bởi người dùng: {}", realId, principal.getUserId());
        mediaService.delete(realId, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok("Xóa tệp tin thành công"));
    }
}
