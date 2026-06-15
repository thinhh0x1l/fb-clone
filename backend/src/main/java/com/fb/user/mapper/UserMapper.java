package com.fb.user.mapper;

import com.fb.auth.model.User;
import com.fb.user.dto.UpdateProfileRequest;
import com.fb.user.dto.UserResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper chuyển đổi dữ liệu User
 * Chuyển đổi giữa Entity User và UserResponse DTO
 */
@Component
public class UserMapper {

    /**
     * Chuyển đổi User entity sang UserResponse DTO
     * @param user User entity từ database
     * @return UserResponse DTO cho API response, null nếu user null
     */
    public UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .bio(user.getBio())
                .avatar(user.getAvatar())
                .coverPhoto(user.getCoverPhoto())
                .gender(user.getGender())
                .birthday(user.getBirthday())
                .location(user.getLocation())
                .workplace(user.getWorkplace())
                .education(user.getEducation())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Cập nhật thông tin hồ sơ người dùng từ request
     * Chỉ cập nhật các trường có giá trị không null
     * @param user User entity cần cập nhật
     * @param request UpdateProfileRequest chứa dữ liệu mới
     */
    public void updateProfile(User user, UpdateProfileRequest request) {
        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getCoverPhoto() != null) {
            user.setCoverPhoto(request.getCoverPhoto());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getBirthday() != null) {
            user.setBirthday(request.getBirthday());
        }
        if (request.getLocation() != null) {
            user.setLocation(request.getLocation());
        }
        if (request.getWorkplace() != null) {
            user.setWorkplace(request.getWorkplace());
        }
        if (request.getEducation() != null) {
            user.setEducation(request.getEducation());
        }
    }
}
