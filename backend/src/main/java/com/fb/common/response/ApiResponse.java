package com.fb.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Lớp phản hồi API tổng quát (Generic API Response).
 *
 * Sử dụng làm lớp bao bọc cho tất cả phản hồi HTTP từ API.
 * Đảm bảo cấu trúc phản hồi nhất quán với các trường:
 * - success: trạng thái thành công/thất bại
 * - message: thông báo cho người dùng
 * - data: dữ liệu trả về (tùy chọn)
 * - error: thông báo lỗi (tùy chọn)
 * - timestamp: thời điểm tạo phản hồi
 *
 * @param <T> kiểu dữ liệu của trường data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /** Trạng thái thành công của yêu cầu */
    private boolean success;

    /** Thông báo mô tả kết quả */
    private String message;

    /** Dữ liệu trả về (null nếu không có) */
    private T data;

    /** Thông báo lỗi (null nếu thành công) */
    private String error;

    /** Thời điểm tạo phản hồi */
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Tạo phản hồi thành công với dữ liệu.
     *
     * @param data dữ liệu trả về
     * @param <T> kiểu dữ liệu
     * @return phản hồi thành công
     */
    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Thành công")
                .data(data)
                .build();
    }

    /**
     * Tạo phản hồi thành công với thông báo và dữ liệu.
     *
     * @param message thông báo
     * @param data dữ liệu trả về
     * @param <T> kiểu dữ liệu
     * @return phản hồi thành công
     */
    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Tạo phản hồi thành công chỉ với thông báo.
     *
     * @param message thông báo
     * @param <T> kiểu dữ liệu
     * @return phản hồi thành công
     */
    public static <T> ApiResponse<T> ok(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    /**
     * Tạo phản hồi lỗi với thông báo.
     *
     * @param message thông báo lỗi
     * @param <T> kiểu dữ liệu
     * @return phản hồi lỗi
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .error(message)
                .build();
    }

    /**
     * Tạo phản hồi lỗi với thông báo và chi tiết lỗi.
     *
     * @param message thông báo lỗi
     * @param error chi tiết lỗi
     * @param <T> kiểu dữ liệu
     * @return phản hồi lỗi
     */
    public static <T> ApiResponse<T> error(String message, String error) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .error(error)
                .build();
    }
}
