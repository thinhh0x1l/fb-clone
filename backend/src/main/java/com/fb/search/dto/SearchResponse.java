package com.fb.search.dto;

import com.fb.post.dto.PostResponse;
import com.fb.user.dto.UserResponse;
import lombok.*;

import java.util.List;

/**
 * DTO dùng để trả về kết quả tìm kiếm tổng hợp.
 * Bao gồm danh sách người dùng và bài viết phù hợp với từ khóa tìm kiếm.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {

    /** danh sách người dùng phù hợp với từ khóa tìm kiếm */
    private List<UserResponse> users;

    /** danh sách bài viết phù hợp với từ khóa tìm kiếm */
    private List<PostResponse> posts;
}
