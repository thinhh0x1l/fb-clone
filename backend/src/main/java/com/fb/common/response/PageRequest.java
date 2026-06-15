package com.fb.common.response;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lớp yêu cầu phân trang (Page Request).
 *
 * Chứa các tham số phân trang được sử dụng trong các API lấy danh sách.
 * Hỗ trợ phân trang cơ bản với số trang, kích thước trang, và sắp xếp.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {

    /** Số trang hiện tại (bắt đầu từ 0) */
    @Min(value = 0)
    @Builder.Default
    private int page = 0;

    /** Số lượng phần tử trên mỗi trang (tối đa 100) */
    @Min(value = 1)
    @Max(value = 100)
    @Builder.Default
    private int size = 20;

    /** Trường sắp xếp (tùy chọn, ví dụ: "createdAt", "name") */
    private String sort;

    /** Hướng sắp xếp: "asc" (tăng dần) hoặc "desc" (giảm dần) */
    @Builder.Default
    private String direction = "desc";

    /**
     * Tạo yêu cầu phân trang với số trang và kích thước.
     *
     * @param page số trang (bắt đầu từ 0)
     * @param size số lượng phần tử trên mỗi trang
     * @return đối tượng PageRequest
     */
    public static PageRequest of(int page, int size) {
        return PageRequest.builder()
                .page(page)
                .size(size)
                .build();
    }

    /**
     * Tạo yêu cầu phân trang đầy đủ với sắp xếp.
     *
     * @param page số trang (bắt đầu từ 0)
     * @param size số lượng phần tử trên mỗi trang
     * @param sort trường sắp xếp
     * @param direction hướng sắp xếp ("asc" hoặc "desc")
     * @return đối tượng PageRequest
     */
    public static PageRequest of(int page, int size, String sort, String direction) {
        return PageRequest.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .direction(direction)
                .build();
    }
}
