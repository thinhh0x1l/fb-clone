package com.fb.user.service;

import com.fb.user.dto.UpdateProfileRequest;
import com.fb.user.dto.UserResponse;

/**
 * Service quản lý thông tin người dùng
 * Xem và cập nhật hồ sơ người dùng
 */
public interface UserService {

    /**
     * Lấy thông tin hồ sơ người dùng theo ID
     * @param userId ID người dùng
     * @return thông tin hồ sơ
     */
    UserResponse getProfile(Long userId);

    /**
     * Lấy thông tin hồ sơ người dùng theo ID người dùng khác
     * @param targetUserId ID người dùng cần xem
     * @return thông tin hồ sơ
     */
    UserResponse getProfileById(Long targetUserId);

    /**
     * Cập nhật hồ sơ người dùng
     * @param userId ID người dùng
     * @param request thông tin cần cập nhật
     * @return thông tin hồ sơ mới
     */
    UserResponse updateProfile(Long userId, UpdateProfileRequest request);
}
