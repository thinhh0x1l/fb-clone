package com.fb.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Tiện ích mã hóa/giải mã tin nhắn
 * 
 * Sử dụng AES-256-GCM (Authenticated Encryption with Associated Data)
 * - AES-256: Thuật toán mã hóa đối xứng mạnh nhất hiện nay
 * - GCM: Chế độ mã hóa authenticated, chống padding oracle attack
 * - IV ngẫu nhiên cho mỗi message
 */
@Slf4j
@Component
public class MessageCryptoUtil {

    @Value("${app.encryption.secret-key:facebook-clone-aes256-secret-key-32bytes!}")
    private String encryptionKey;

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;  // 96-bit IV
    private static final int GCM_TAG_LENGTH = 128; // 128-bit auth tag
    private static final int KEY_LENGTH = 32;      // 256-bit key

    /**
     * Mã hóa tin nhắn trước khi lưu vào database
     * 
     * @param plaintext Tin nhắn gốc
     * @return Chuỗi đã mã hóa (Base64)
     */
    public String encrypt(String plaintext) {
        try {
            // Tạo key từ secret key
            byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            keyBytes = sha.digest(keyBytes); // Chuẩn hóa về 256-bit

            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

            // Tạo IV ngẫu nhiên
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom.getInstanceStrong().nextBytes(iv);

            // Mã hóa
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Ghép IV + ciphertext
            byte[] encrypted = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, encrypted, 0, iv.length);
            System.arraycopy(ciphertext, 0, encrypted, iv.length, ciphertext.length);

            String result = Base64.getEncoder().encodeToString(encrypted);
            log.debug("Mã hóa tin nhắn thành công ({} bytes → {} bytes)", 
                    plaintext.length(), result.length());
            return result;

        } catch (Exception e) {
            log.error("Lỗi mã hóa tin nhắn: {}", e.getMessage());
            throw new RuntimeException("Mã hóa tin nhắn thất bại", e);
        }
    }

    /**
     * Giải mã tin nhắn từ database
     * 
     * @param encryptedBase64 Chuỗi đã mã hóa (Base64)
     * @return Tin nhắn gốc
     */
    public String decrypt(String encryptedBase64) {
        try {
            byte[] encrypted = Base64.getDecoder().decode(encryptedBase64);

            // Tạo key từ secret key
            byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            keyBytes = sha.digest(keyBytes);

            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

            // Tách IV và ciphertext
            byte[] iv = Arrays.copyOfRange(encrypted, 0, GCM_IV_LENGTH);
            byte[] ciphertext = Arrays.copyOfRange(encrypted, GCM_IV_LENGTH, encrypted.length);

            // Giải mã
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] plaintext = cipher.doFinal(ciphertext);
            String result = new String(plaintext, StandardCharsets.UTF_8);
            log.debug("Giải mã tin nhắn thành công");
            return result;

        } catch (Exception e) {
            log.error("Lỗi giải mã tin nhắn: {}", e.getMessage());
            throw new RuntimeException("Giải mã tin nhắn thất bại", e);
        }
    }

    /**
     * Kiểm tra chuỗi có phải đã mã hóa chưa
     */
    public boolean isEncrypted(String content) {
        try {
            byte[] decoded = Base64.getDecoder().decode(content);
            // Kiểm tra độ dài tối thiểu: IV (12) + Tag (16) + 1 byte
            return decoded.length >= 29;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Mã hóa file name ngẫu nhiên
     */
    public String generateSecureFileName(String originalName) {
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}
