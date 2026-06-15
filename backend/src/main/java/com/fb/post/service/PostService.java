package com.fb.post.service;

import com.fb.common.response.PagedResponse;
import com.fb.post.dto.CreatePostRequest;
import com.fb.post.dto.PostResponse;
import com.fb.post.dto.UpdatePostRequest;

/**
 * Service quản lý bài viết
 * Tạo, xem, chỉnh sửa, xóa bài viết và lấy danh sách bài viết
 */
public interface PostService {

    /**
     * Tạo bài viết mới
     * @param userId ID người tạo
     * @param request thông tin bài viết
     * @return bài viết đã tạo
     */
    PostResponse createPost(Long userId, CreatePostRequest request);

    /**
     * Lấy thông tin bài viết
     * @param postId ID bài viết
     * @param currentUserId ID người dùng hiện tại
     * @return thông tin bài viết
     */
    PostResponse getPost(Long postId, Long currentUserId);

    /**
     * Cập nhật bài viết
     * @param postId ID bài viết
     * @param userId ID người sở hữu
     * @param request thông tin cập nhật
     * @return bài viết đã cập nhật
     */
    PostResponse updatePost(Long postId, Long userId, UpdatePostRequest request);

    /**
     * Xóa bài viết (soft delete)
     * @param postId ID bài viết
     * @param userId ID người sở hữu
     */
    void deletePost(Long postId, Long userId);

    /**
     * Lấy danh sách bài viết của người dùng
     * @param userId ID người dùng
     * @param currentUserId ID người dùng hiện tại
     * @param page trang hiện tại
     * @param size số lượng mỗi trang
     * @return danh sách bài viết phân trang
     */
    PagedResponse<PostResponse> getUserPosts(Long userId, Long currentUserId, int page, int size);

    /**
     * Lấy danh sách bài viết trên trang chủ (feed)
     * @param userId ID người dùng
     * @param page trang hiện tại
     * @param size số lượng mỗi trang
     * @return danh sách bài viết phân trang
     */
    PagedResponse<PostResponse> getFeedPosts(Long userId, int page, int size);
}
