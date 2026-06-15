package com.fb.security.blacklist;

import com.fb.infrastructure.cache.MultiTierCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Quản lý danh sách đen JWT token
 * Dùng khi logout để vô hiệu hóa token
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenBlacklist {

    private final MultiTierCache cache;

    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final long TOKEN_TTL_SECONDS = 900; // 15 phút (thời gian hết hạn access token)

    /**
     * Thêm token vào danh sách đen
     */
    public void blacklist(String token, long ttlSeconds) {
        String key = BLACKLIST_PREFIX + hashToken(token);
        cache.set(key, "revoked", ttlSeconds);
        log.info("Đã thêm token vào danh sách đen");
    }

    /**
     * Kiểm tra token có trong danh sách đen không
     */
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + hashToken(token);
        return cache.hasKey(key);
    }

    /**
     * Xóa token khỏi danh sách đen
     */
    public void removeFromBlacklist(String token) {
        String key = BLACKLIST_PREFIX + hashToken(token);
        cache.delete(key);
    }

    /**
     * Hash token để lưu an toàn
     */
    private String hashToken(String token) {
        // Dùng SHA-256 để hash token
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            // Fallback: dùng token gốc (không lý tưởng)
            return token;
        }
    }
}
