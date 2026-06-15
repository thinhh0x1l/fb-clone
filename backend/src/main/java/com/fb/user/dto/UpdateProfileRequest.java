package com.fb.user.dto;

import com.fb.auth.model.Gender;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

/**
 * DTO dùng để cập nhật thông tin hồ sơ cá nhân của người dùng.
 * Tất cả các trường đều tùy chọn, chỉ cập nhật các trường được gửi.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    /** tên hiển thị mới, tối thiểu 2 và tối đa 100 ký tự */
    @Size(min = 2, max = 100, message = "Display name must be between 2 and 100 characters")
    private String displayName;

    /** tiểu sử ngắn, tối đa 500 ký tự */
    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;

    /** URL ảnh đại diện mới */
    private String avatar;

    /** URL ảnh bìa mới */
    private String coverPhoto;

    /** giới tính */
    private Gender gender;

    /** ngày sinh */
    private LocalDate birthday;

    /** nơi cư trú, tối đa 255 ký tự */
    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;

    /** nơi làm việc, tối đa 255 ký tự */
    @Size(max = 255, message = "Workplace must not exceed 255 characters")
    private String workplace;

    /** trường học, tối đa 255 ký tự */
    @Size(max = 255, message = "Education must not exceed 255 characters")
    private String education;
}
