package com.fb.reaction.model;

import com.fb.auth.model.User;
import com.fb.common.enums.ReactionType;
import com.fb.infrastructure.persistence.BaseEntity;
import com.fb.comment.model.Comment;
import com.fb.post.model.Post;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity phản ứng (like, love, v.v.) đối với bài đăng hoặc bình luận
 */
@Entity
@Table(name = "reactions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "post_id"}),
        @UniqueConstraint(columnNames = {"user_id", "comment_id"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reaction extends BaseEntity {

    /** Người thực hiện phản ứng */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Bài đăng được phản ứng */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    /** Bình luận được phản ứng */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    /** Loại phản ứng (like, love, haha, v.v.) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReactionType type;
}
