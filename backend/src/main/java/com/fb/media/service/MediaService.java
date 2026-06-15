package com.fb.media.service;

import com.fb.media.dto.MediaResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service quản lý media (hình ảnh, video)
 * Upload, xóa media
 */
public interface MediaService {

    /**
     * Upload một file media
     * @param file file cần upload
     * @param userId ID người upload
     * @return thông tin media đã upload
     */
    MediaResponse upload(MultipartFile file, Long userId);

    /**
     * Upload nhiều file media
     * @param files danh sách file cần upload
     * @param userId ID người upload
     * @return danh sách thông tin media
     */
    List<MediaResponse> uploadMultiple(List<MultipartFile> files, Long userId);

    /**
     * Xóa media
     * @param id ID media cần xóa
     * @param userId ID người xóa
     */
    void delete(Long id, Long userId);
}
