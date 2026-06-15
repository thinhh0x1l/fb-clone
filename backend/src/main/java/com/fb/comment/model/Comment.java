package com.fb.comment.model;

import com.fb.auth.model.User;
import com.fb.infrastructure.persistence.BaseEntity;
import com.fb.post.model.Post;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity bình luận trong bài đăng
 */
@Entity
@Table(name = "comments")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment extends BaseEntity {

    /** Nội dung bình luận */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /** Bài đăng chứa bình luận này */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    /** Người viết bình luận */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Bình luận cha (nếu là phản hồi) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    /** Số lượng lượt thích */
    @Builder.Default
    @Column(nullable = false)
    private long likesCount = 0;

    /** Số lượng phản hồi */
    @Builder.Default
    @Column(nullable = false)
    private long repliesCount = 0;

    /** Độ sâu của bình luận trong chuỗi phản hồi */
    @Builder.Default
    @Column(nullable = false)
    private int depth = 0;
}
