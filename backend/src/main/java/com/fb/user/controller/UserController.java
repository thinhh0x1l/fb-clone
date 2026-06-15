package com.fb.user.controller;

import com.fb.common.constant.AppConstant;
import com.fb.common.response.ApiResponse;
import com.fb.common.util.HashIdUtil;
import com.fb.security.CurrentUser;
import com.fb.security.JwtAuthenticationFilter;
import com.fb.user.dto.UpdateProfileRequest;
import com.fb.user.dto.UserResponse;
import com.fb.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Điều khiển người dùng - xử lý thông tin hồ sơ và quản lý tài khoản
 */
@Slf4j
@RestController
@RequestMapping(AppConstant.API_VERSION + "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final HashIdUtil hashIdUtil;

    /**
     * Lấy thông tin hồ sơ người dùng hiện tại
     *
     * @param principal thông tin người dùng đã xác thực
     * @return thông tin hồ sơ người dùng
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal) {
        log.info("Lấy thông tin hồ sơ người dùng hiện tại: {}", principal.getUserId());
        UserResponse response = userService.getProfile(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Lấy thông tin hồ sơ người dùng theo ID
     *
     * @param id mã HashId của người dùng
     * @return thông tin hồ sơ người dùng
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String id) {
        // Giải mã HashId để lấy ID thực
        Long realId = hashIdUtil.decode(id);
        log.info("Lấy thông tin người dùng theo ID: {}", realId);
        UserResponse response = userService.getProfileById(realId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Cập nhật thông tin hồ sơ người dùng
     *
     * @param principal thông tin người dùng đã xác thực
     * @param request thông tin cập nhật
     * @return thông tin hồ sơ sau khi cập nhật
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        log.info("Cập nhật hồ sơ người dùng: {}", principal.getUserId());
        UserResponse response = userService.updateProfile(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật hồ sơ thành công", response));
    }
}
