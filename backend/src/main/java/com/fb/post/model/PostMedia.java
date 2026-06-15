package com.fb.post.model;

import com.fb.infrastructure.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity phương tiện đính kèm trong bài đăng
 */
@Entity
@Table(name = "post_media")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostMedia extends BaseEntity {

    /** Bài đăng chứa phương tiện này */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    /** Đường dẫn URL của phương tiện */
    @Column(nullable = false)
    private String url;

    /** Đường dẫn URL hình thu nhỏ */
    private String thumbnailUrl;

    /** Loại phương tiện (ảnh, video, v.v.) */
    @Column(length = 20)
    private String type;

    /** Thứ tự hiển thị của phương tiện */
    @Builder.Default
    @Column(nullable = false)
    private int orderIndex = 0;

    /** Chiều rộng của phương tiện (pixel) */
    private Integer width;

    /** Chiều cao của phương tiện (pixel) */
    private Integer height;
}
