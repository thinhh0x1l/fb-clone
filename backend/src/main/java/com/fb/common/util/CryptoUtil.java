package com.fb.common.util;

import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Tiện ích mã hóa và bảo mật
 */
@Component
public class CryptoUtil {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int OTP_LENGTH = 6;
    private static final int TOKEN_LENGTH = 32;

    /**
     * Tạo mã OTP ngẫu nhiên (6 chữ số)
     */
    public String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(ThreadLocalRandom.current().nextInt(10));
        }
        return otp.toString();
    }

    /**
     * Tạo token ngẫu nhiên
     */
    public String generateToken() {
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            token.append(CHARACTERS.charAt(ThreadLocalRandom.current().nextInt(CHARACTERS.length())));
        }
        return token.toString();
    }

    /**
     * Tạo slug từ chuỗi tiếng Việt
     */
    public String toSlug(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        String slug = input.toLowerCase()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s-]+", "-")
                .replaceAll("^-|-$", "");

        return slug;
    }

    /**
     * Tạo UUID an toàn cho file name
     */
    public String generateSecureFileName(String originalName) {
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}
