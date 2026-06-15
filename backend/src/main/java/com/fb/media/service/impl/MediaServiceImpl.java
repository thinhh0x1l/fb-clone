package com.fb.media.service.impl;

import com.fb.common.exception.BadRequestException;
import com.fb.common.exception.ResourceNotFoundException;
import com.fb.infrastructure.storage.StorageService;
import com.fb.media.dto.MediaResponse;
import com.fb.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Triển khai service quản lý media
 * Upload, xóa file media và kiểm tra định dạng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final StorageService storageService;

    /** Kích thước tối đa cho ảnh: 10MB */
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024L;

    /** Kích thước tối đa cho video: 50MB */
    private static final long MAX_VIDEO_SIZE = 50 * 1024 * 1024L;

    /** Các loại file ảnh được phép */
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    /** Các loại file video được phép */
    private static final Set<String> ALLOWED_VIDEO_TYPES = Set.of(
            "video/mp4", "video/webm"
    );

    /** Store tạm thời media */
    private final Map<Long, MediaResponse> mediaStore = new ConcurrentHashMap<>();

    /**
     * Upload một file media
     * Kiểm tra định dạng và kích thước file trước khi upload
     */
    @Override
    public MediaResponse upload(MultipartFile file, Long userId) {
        // Kiểm tra file hợp lệ
        validateFile(file);

        // Xác định thư mục lưu trữ
        String contentType = file.getContentType();
        String directory = isVideo(contentType) ? "videos" : "images";
        String url = storageService.uploadFile(file, directory);

        // Tạo response
        Long id = System.currentTimeMillis();
        MediaResponse response = MediaResponse.builder()
                .id(id)
                .url(url)
                .thumbnailUrl(isVideo(contentType) ? url : null)
                .type(contentType)
                .size(file.getSize())
                .build();

        mediaStore.put(id, response);
        log.info("Upload media thành công - ID: {}, User ID: {}, Loại: {}", id, userId, contentType);
        return response;
    }

    /**
     * Upload nhiều file media
     */
    @Override
    public List<MediaResponse> uploadMultiple(List<MultipartFile> files, Long userId) {
        if (files == null || files.isEmpty()) {
            throw new BadRequestException("Không có file nào được cung cấp");
        }
        log.info("Upload nhiều file - Số lượng: {}, User ID: {}", files.size(), userId);
        return files.stream()
                .map(file -> upload(file, userId))
                .toList();
    }

    /**
     * Xóa media
     */
    @Override
    public void delete(Long id, Long userId) {
        MediaResponse media = mediaStore.remove(id);
        if (media == null) {
            log.warn("Xóa media thất bại - Không tìm thấy media ID: {}", id);
            throw new ResourceNotFoundException("Media", "id", id);
        }
        storageService.deleteFile(media.getUrl());
        log.info("Xóa media thành công - ID: {}, User ID: {}", id, userId);
    }

    /**
     * Kiểm tra file hợp lệ
     * Kiểm tra rỗng, loại file và kích thước
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File trống");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new BadRequestException("Không thể xác định loại file");
        }

        if (isImage(contentType)) {
            if (file.getSize() > MAX_IMAGE_SIZE) {
                throw new BadRequestException("Kích thước ảnh vượt quá giới hạn 10MB");
            }
        } else if (isVideo(contentType)) {
            if (file.getSize() > MAX_VIDEO_SIZE) {
                throw new BadRequestException("Kích thước video vượt quá giới hạn 50MB");
            }
        } else {
            throw new BadRequestException("Loại file không được hỗ trợ: " + contentType
                    + ". Các loại được phép: " + ALLOWED_IMAGE_TYPES + ", " + ALLOWED_VIDEO_TYPES);
        }
    }

    /** Kiểm tra file có phải ảnh không */
    private boolean isImage(String contentType) {
        return ALLOWED_IMAGE_TYPES.contains(contentType);
    }

    /** Kiểm tra file có phải video không */
    private boolean isVideo(String contentType) {
        return ALLOWED_VIDEO_TYPES.contains(contentType);
    }
}
