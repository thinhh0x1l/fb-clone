package com.fb.comment.dto;

import com.fb.common.constant.AppConstant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO dùng để tạo bình luận mới hoặc phản hồi một bình luận có sẵn.
 * Chỉ cần nội dung bình luận, trường parentId là tùy chọn.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {

    /** nội dung văn bản của bình luận, không được để trống, tối đa số ký tự theo AppConstant.MAX_COMMENT_LENGTH */
    @NotBlank(message = "Comment content is required")
    @Size(max = AppConstant.MAX_COMMENT_LENGTH, message = "Comment must not exceed " + AppConstant.MAX_COMMENT_LENGTH + " characters")
    private String content;

    /** mã định danh của bình luận cha (null nếu là bình luận gốc trên bài viết) */
    private Long parentId;
}
