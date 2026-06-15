package com.fb.post.dto;

import com.fb.common.constant.AppConstant;
import com.fb.common.enums.Visibility;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO dùng để cập nhật bài viết đã tồn tại.
 * Chỉ cho phép cập nhật nội dung văn bản và chế độ hiển thị.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePostRequest {

    /** nội dung văn bản mới của bài viết, tối đa số ký tự theo AppConstant.MAX_POST_LENGTH */
    @Size(max = AppConstant.MAX_POST_LENGTH, message = "Post content must not exceed " + AppConstant.MAX_POST_LENGTH + " characters")
    private String content;

    /** chế độ hiển thị mới của bài viết */
    private Visibility visibility;
}
