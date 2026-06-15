package com.fb.search.service;

import com.fb.search.dto.SearchResponse;

/**
 * Service tìm kiếm
 * Tìm kiếm người dùng và bài viết theo từ khóa
 */
public interface SearchService {

    /**
     * Tìm kiếm người dùng và bài viết
     * @param query từ khóa tìm kiếm
     * @param page trang hiện tại
     * @param size số lượng mỗi trang
     * @return kết quả tìm kiếm
     */
    SearchResponse search(String query, int page, int size);
}
