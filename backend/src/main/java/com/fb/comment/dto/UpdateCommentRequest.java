package com.fb.comment.dto;

import com.fb.common.constant.AppConstant;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommentRequest {

    @Size(max = AppConstant.MAX_COMMENT_LENGTH, message = "Comment must not exceed " + AppConstant.MAX_COMMENT_LENGTH + " characters")
    private String content;
}
