package com.fb.auth.service.impl;

import com.fb.auth.model.User;
import com.fb.auth.repository.UserRepository;
import com.fb.auth.dto.RegisterRequest;
import com.fb.auth.dto.LoginRequest;
import com.fb.auth.dto.AuthResponse;
import com.fb.auth.dto.RefreshTokenRequest;
import com.fb.common.exception.BadRequestException;
import com.fb.common.exception.ResourceNotFoundException;
import com.fb.security.blacklist.TokenBlacklist;
import com.fb.security.JwtTokenProvider;
import com.fb.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service xác thực
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklist tokenBlacklist;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email đã được sử dụng");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Tên người dùng đã tồn tại");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName());
        user = userRepository.save(user);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        log.info("Đăng ký thành công: {}", user.getId());
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .displayName(user.getDisplayName())
                        .avatar(user.getAvatar())
                        .build())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Email hoặc mật khẩu không đúng"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Email hoặc mật khẩu không đúng");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        log.info("Đăng nhập thành công: {}", user.getId());
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .displayName(user.getDisplayName())
                        .avatar(user.getAvatar())
                        .build())
                .build();
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        if (tokenBlacklist.isBlacklisted(request.getRefreshToken())) {
            throw new BadRequestException("Token đã bị thu hồi");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(request.getRefreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        tokenBlacklist.blacklist(request.getRefreshToken(), 604800);

        log.info("Làm mới token thành công: {}", user.getId());
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .displayName(user.getDisplayName())
                        .avatar(user.getAvatar())
                        .build())
                .build();
    }

    public void logout(String accessToken, String refreshToken) {
        if (accessToken != null) {
            tokenBlacklist.blacklist(accessToken, 900);
        }
        if (refreshToken != null) {
            tokenBlacklist.blacklist(refreshToken, 604800);
        }
        log.info("Đăng xuất thành công");
    }

    @Override
    public User getCurrentUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
    }
}
