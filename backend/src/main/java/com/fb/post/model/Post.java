package com.fb.post.model;

import com.fb.auth.model.User;
import com.fb.common.enums.Visibility;
import com.fb.infrastructure.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity bài đăng trên mạng xã hội
 */
@Entity
@Table(name = "posts")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends BaseEntity {

    /** Nội dung bài đăng */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** Người tạo bài đăng */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Phạm vi hiển thị của bài đăng */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private Visibility visibility = Visibility.PUBLIC;

    /** Số lượng lượt thích */
    @Builder.Default
    @Column(nullable = false)
    private long likesCount = 0;

    /** Số lượng bình luận */
    @Builder.Default
    @Column(nullable = false)
    private long commentsCount = 0;

    /** Số lượng lượt chia sẻ */
    @Builder.Default
    @Column(nullable = false)
    private long sharesCount = 0;

    /** Danh sách phương tiện đính kèm */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostMedia> media = new ArrayList<>();
}
