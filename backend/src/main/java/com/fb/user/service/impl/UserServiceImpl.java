package com.fb.user.service.impl;

import com.fb.auth.model.User;
import com.fb.auth.repository.UserRepository;
import com.fb.common.constant.CacheKey;
import com.fb.common.exception.ResourceNotFoundException;
import com.fb.infrastructure.cache.CacheService;
import com.fb.user.dto.UpdateProfileRequest;
import com.fb.user.dto.UserResponse;
import com.fb.user.mapper.UserMapper;
import com.fb.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Triển khai service quản lý người dùng
 * Xử lý lấy và cập nhật hồ sơ người dùng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CacheService cacheService;

    /**
     * Lấy thông tin hồ sơ người dùng
     * Kiểm tra cache trước, nếu không có thì truy vấn database
     */
    @Override
    public UserResponse getProfile(Long userId) {
        // Kiểm tra cache
        String cacheKey = CacheKey.USER_PROFILE + userId;
        UserResponse cached = cacheService.get(cacheKey, UserResponse.class);
        if (cached != null) {
            log.debug("Lấy hồ sơ từ cache - User ID: {}", userId);
            return cached;
        }

        // Truy vấn database
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy người dùng với ID: {}", userId);
                    return new ResourceNotFoundException("Không tìm thấy người dùng");
                });

        // Lưu vào cache
        UserResponse response = userMapper.toUserResponse(user);
        cacheService.set(cacheKey, response, java.time.Duration.ofSeconds(CacheKey.DEFAULT_TTL_SECONDS));
        log.debug("Lấy hồ sơ thành công - User ID: {}", userId);
        return response;
    }

    /**
     * Lấy thông tin hồ sơ người dùng theo ID người dùng khác
     */
    @Override
    public UserResponse getProfileById(Long targetUserId) {
        return getProfile(targetUserId);
    }

    /**
     * Cập nhật hồ sơ người dùng
     * Cập nhật thông tin và xóa cache
     */
    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Cập nhật hồ sơ thất bại - Không tìm thấy người dùng ID: {}", userId);
                    return new ResourceNotFoundException("Không tìm thấy người dùng");
                });

        // Cập nhật thông tin hồ sơ
        userMapper.updateProfile(user, request);
        user = userRepository.save(user);

        // Xóa cache hồ sơ
        cacheService.delete(CacheKey.USER_PROFILE + userId);

        log.info("Cập nhật hồ sơ thành công - User ID: {}", userId);
        return userMapper.toUserResponse(user);
    }
}
