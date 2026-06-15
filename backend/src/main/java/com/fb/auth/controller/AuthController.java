package com.fb.auth.controller;

import com.fb.auth.dto.AuthResponse;
import com.fb.auth.dto.LoginRequest;
import com.fb.auth.dto.RefreshTokenRequest;
import com.fb.auth.dto.RegisterRequest;
import com.fb.auth.service.AuthService;
import com.fb.common.constant.AppConstant;
import com.fb.common.response.ApiResponse;
import com.fb.common.util.HashIdUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Điều khiển xác thực - xử lý đăng ký, đăng nhập, làm mới token
 */
@Slf4j
@RestController
@RequestMapping(AppConstant.API_VERSION + "/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final HashIdUtil hashIdUtil;

    /**
     * Đăng ký tài khoản mới
     *
     * @param request thông tin đăng ký
     * @return thông tin xác thực sau khi đăng ký thành công
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        log.info("Yêu cầu đăng ký mới cho email: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        log.info("Đăng ký thành công cho email: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Đăng ký thành công", response));
    }

    /**
     * Đăng nhập vào hệ thống
     *
     * @param request thông tin đăng nhập
     * @return thông tin xác thực sau khi đăng nhập thành công
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("Yêu cầu đăng nhập cho email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        log.info("Đăng nhập thành công cho email: {}", request.getEmail());
        return ResponseEntity.ok(ApiResponse.ok("Đăng nhập thành công", response));
    }

    /**
     * Làm mới token truy cập
     *
     * @param request thông tin token cần làm mới
     * @return thông tin xác thực với token mới
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        log.info("Yêu cầu làm mới token");
        AuthResponse response = authService.refresh(request);
        log.info("Làm mới token thành công");
        return ResponseEntity.ok(ApiResponse.ok("Làm mới token thành công", response));
    }
}
