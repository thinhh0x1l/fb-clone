package com.fb.feed.dto;

import com.fb.post.dto.PostResponse;
import lombok.*;

import java.util.List;

/**
 * DTO dùng để trả về kết quả truy vấn feed tin tức.
 * Bao gồm danh sách bài viết, nguồn cấp dữ liệu và cờ phân trang.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedResponse {

    /** danh sách bài viết trong trang hiện tại */
    private List<PostResponse> posts;

    /** nguồn cấp dữ liệu (home, explore, trending, ...) */
    private String source;

    /** còn bài viết tiếp theo để tải thêm hay không */
    private boolean hasMore;
}
