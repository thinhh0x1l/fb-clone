package com.fb.post.validator;

import com.fb.common.exception.BadRequestException;
import com.fb.post.dto.CreatePostRequest;
import com.fb.post.dto.UpdatePostRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Xác thực dữ liệu bài viết
 */
@Slf4j
@Component
public class PostValidator {

    private static final int MAX_CONTENT_LENGTH = 10000;
    private static final int MAX_IMAGES = 20;
    private static final List<String> ALLOWED_VISIBILITY = List.of("PUBLIC", "FRIENDS", "ONLY_ME", "CUSTOM");

    /**
     * Xác thực yêu cầu tạo bài viết
     */
    public void validateCreate(CreatePostRequest request) {
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new BadRequestException("Nội dung bài viết không được để trống");
        }

        if (request.getContent().length() > MAX_CONTENT_LENGTH) {
            throw new BadRequestException("Nội dung bài viết vượt quá " + MAX_CONTENT_LENGTH + " ký tự");
        }

        if (request.getVisibility() != null && !ALLOWED_VISIBILITY.contains(request.getVisibility())) {
            throw new BadRequestException("Trạng thái hiển thị không hợp lệ");
        }

        if (request.getMediaUrls() != null && request.getMediaUrls().size() > MAX_IMAGES) {
            throw new BadRequestException("Số lượng ảnh vượt quá " + MAX_IMAGES);
        }
    }

    /**
     * Xác thực yêu cầu cập nhật bài viết
     */
    public void validateUpdate(UpdatePostRequest request) {
        if (request.getContent() != null && request.getContent().length() > MAX_CONTENT_LENGTH) {
            throw new BadRequestException("Nội dung bài viết vượt quá " + MAX_CONTENT_LENGTH + " ký tự");
        }

        if (request.getVisibility() != null && !ALLOWED_VISIBILITY.contains(request.getVisibility())) {
            throw new BadRequestException("Trạng thái hiển thị không hợp lệ");
        }
    }
}
