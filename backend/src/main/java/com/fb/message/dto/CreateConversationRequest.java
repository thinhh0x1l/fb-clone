package com.fb.message.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

/**
 * DTO dùng để tạo cuộc trò chuyện mới.
 * Bắt buộc phải có ít nhất một người tham gia.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateConversationRequest {

    /** danh sách mã định danh người tham gia (bắt buộc, không được để trống) */
    @NotEmpty(message = "Participants list cannot be empty")
    private List<Long> participantIds;

    /** tên nhóm (tùy chọn, chỉ áp dụng cho cuộc trò chuyện nhóm) */
    private String name;
}
