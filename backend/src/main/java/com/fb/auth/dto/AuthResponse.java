package com.fb.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * DTO response cho xác thực
 */
@Data
@Builder
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private UserInfo user;

    /**
     * Thông tin người dùng
     */
    @Data
    @Builder
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String displayName;
        private String avatar;
    }
}
