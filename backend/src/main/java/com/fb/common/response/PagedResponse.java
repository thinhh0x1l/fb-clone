package com.fb.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Lớp phản hồi phân trang (Paged Response).
 *
 * Chứa dữ liệu phân trang kèm theo thông tin về:
 * - Danh sách phần tử hiện tại
 * - Số trang hiện tại và kích thước trang
 * - Tổng số phần tử và tổng số trang
 * - Cờ đánh dấu trang đầu tiên và trang cuối cùng
 *
 * @param <T> kiểu dữ liệu của các phần tử trong danh sách
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {

    /** Danh sách phần tử của trang hiện tại */
    private List<T> content;

    /** Số trang hiện tại (bắt đầu từ 0) */
    private int page;

    /** Số lượng phần tử trên mỗi trang */
    private int size;

    /** Tổng số phần tử trong tất cả các trang */
    private long totalElements;

    /** Tổng số trang */
    private int totalPages;

    /** Đánh dấu có phải trang đầu tiên không */
    private boolean first;

    /** Đánh dấu có phải trang cuối cùng không */
    private boolean last;

    /**
     * Tạo phản hồi phân trang từ dữ liệu đầu vào.
     *
     * @param content danh sách phần tử của trang hiện tại
     * @param page số trang hiện tại
     * @param size số lượng phần tử trên mỗi trang
     * @param totalElements tổng số phần tử
     * @param <T> kiểu dữ liệu
     * @return đối tượng PagedResponse
     */
    public static <T> PagedResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return PagedResponse.<T>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .build();
    }
}
