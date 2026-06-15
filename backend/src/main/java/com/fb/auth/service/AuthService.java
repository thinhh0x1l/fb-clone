package com.fb.auth.service;

import com.fb.auth.dto.AuthResponse;
import com.fb.auth.dto.LoginRequest;
import com.fb.auth.dto.RefreshTokenRequest;
import com.fb.auth.dto.RegisterRequest;
import com.fb.auth.model.User;

/**
 * Service xác thực người dùng
 * Xử lý đăng ký, đăng nhập, làm mới token và lấy thông tin người dùng hiện tại
 */
public interface AuthService {

    /**
     * Đăng ký tài khoản mới
     * @param request thông tin đăng ký
     * @return thông tin xác thực
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Đăng nhập
     * @param request thông tin đăng nhập
     * @return thông tin xác thực
     */
    AuthResponse login(LoginRequest request);

    /**
     * Làm mới access token
     * @param request refresh token
     * @return thông tin xác thực mới
     */
    AuthResponse refresh(RefreshTokenRequest request);

    /**
     * Lấy thông tin người dùng hiện tại theo ID
     * @param userId ID người dùng
     * @return thông tin người dùng
     */
    User getCurrentUser(Long userId);
}
