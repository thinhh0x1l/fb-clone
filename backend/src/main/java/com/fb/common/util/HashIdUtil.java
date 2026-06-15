package com.fb.common.util;

import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Tiện ích HashId để mã hóa/giải mã ID
 * 
 * HashId chuyển đổi số nguyên (Long) thành chuỗi ngắn, an toàn
 * để hiển thị trong URL và API response
 * 
 * Ví dụ:
 *   encode(123) → "xGBz3K"
 *   decode("xGBz3K") → 123
 */
@Component
public class HashIdUtil {

    private final Hashids hashids;

    /**
     * Khởi tạo với salt từ cấu hình
     * Salt giúp mỗi hệ thống tạo ra chuỗi hash khác nhau
     */
    public HashIdUtil(@Value("${app.hashid.salt:facebook-clone-salt-2024}") String salt) {
        this.hashids = new Hashids(salt, 6); // Độ dài tối thiểu 6 ký tự
    }

    /**
     * Mã hóa Long thành chuỗi HashId
     * 
     * @param id ID cần mã hóa
     * @return Chuỗi hash an toàn để hiển thị
     */
    public String encode(Long id) {
        if (id == null || id <= 0) {
            return null;
        }
        return hashids.encode(id);
    }

    /**
     * Giải mã chuỗi HashId về Long
     * 
     * @param hashId Chuỗi hash cần giải mã
     * @return ID gốc (Long)
     */
    public Long decode(String hashId) {
        if (hashId == null || hashId.isEmpty()) {
            return null;
        }
        
        long[] decoded = hashids.decode(hashId);
        if (decoded.length == 0) {
            return null;
        }
        
        return decoded[0];
    }

    /**
     * Mã hóa danh sách IDs
     * 
     * @param ids Danh sách ID cần mã hóa
     * @return Danh sách HashIds
     */
    public List<String> encodeList(List<Long> ids) {
        if (ids == null) {
            return new ArrayList<>();
        }
        
        return ids.stream()
                .map(this::encode)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    /**
     * Giải mã danh sách HashIds
     * 
     * @param hashIds Danh sách HashIds cần giải mã
     * @return Danh sách IDs gốc
     */
    public List<Long> decodeList(List<String> hashIds) {
        if (hashIds == null) {
            return new ArrayList<>();
        }
        
        return hashIds.stream()
                .map(this::decode)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    /**
     * Kiểm tra chuỗi có phải HashId hợp lệ không
     */
    public boolean isValid(String hashId) {
        if (hashId == null || hashId.isEmpty()) {
            return false;
        }
        return decode(hashId) != null;
    }
}
