package com.fb.post.dto;

import com.fb.common.constant.AppConstant;
import com.fb.common.enums.Visibility;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

/**
 * DTO dùng để tạo bài viết mới.
 * Chứa nội dung văn bản, chế độ hiển thị và danh sách URL phương tiện đính kèm.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {

    /** nội dung văn bản của bài viết, tối đa số ký tự theo AppConstant.MAX_POST_LENGTH */
    @Size(max = AppConstant.MAX_POST_LENGTH, message = "Post content must not exceed " + AppConstant.MAX_POST_LENGTH + " characters")
    private String content;

    /** chế độ hiển thị của bài viết (mặc định: công khai) */
    private Visibility visibility;

    /** danh sách URL các phương tiện (ảnh, video) muốn đính kèm */
    private List<String> mediaUrls;
}
