package com.fb.user.dto;

import com.fb.auth.model.Gender;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * DTO dùng để trả về thông tin chi tiết của người dùng.
 * Bao gồm thông tin cá nhân, tiểu sử, ảnh đại diện và ảnh bìa.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    /** mã định danh duy nhất của người dùng */
    private Long id;

    /** tên đăng nhập duy nhất */
    private String username;

    /** địa chỉ email */
    private String email;

    /** tên hiển thị */
    private String displayName;

    /** tiểu sử ngắn gọn */
    private String bio;

    /** URL ảnh đại diện */
    private String avatar;

    /** URL ảnh bìa */
    private String coverPhoto;

    /** giới tính */
    private Gender gender;

    /** ngày sinh */
    private LocalDate birthday;

    /** nơi cư trú */
    private String location;

    /** nơi làm việc */
    private String workplace;

    /** trường học */
    private String education;

    /** trạng thái xác thực email */
    private boolean emailVerified;

    /** thời gian tạo tài khoản */
    private LocalDateTime createdAt;
}
