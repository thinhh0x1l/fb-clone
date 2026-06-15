package com.fb.media.dto;

import lombok.*;

/**
 * DTO dùng để trả về thông tin chi tiết của một phương tiện (ảnh, video).
 * Bao gồm URL truy cập, ảnh thu nhỏ, loại phương tiện và kích thước.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaResponse {

    /** mã định danh duy nhất của phương tiện */
    private Long id;

    /** URL đầy đủ để truy cập phương tiện */
    private String url;

    /** URL ảnh thu nhỏ của phương tiện */
    private String thumbnailUrl;

    /** loại phương tiện (IMAGE, VIDEO) */
    private String type;

    /** kích thước file (bytes) */
    private Long size;
}
