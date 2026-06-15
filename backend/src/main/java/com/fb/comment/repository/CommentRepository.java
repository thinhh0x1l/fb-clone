package com.fb.comment.repository;

import com.fb.comment.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho bình luận
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Lấy bình luận theo bài viết (đã optimize N+1)
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.post.id = :postId AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    List<Comment> findByPostIdWithUser(@Param("postId") Long postId);

    /**
     * Lấy bình luận gốc theo bài viết
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.post.id = :postId AND c.parent IS NULL AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    List<Comment> findRootCommentsByPostId(@Param("postId") Long postId);

    /**
     * Lấy phản hồi của bình luận
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.parent.id = :parentId AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentId(@Param("parentId") Long parentId);

    /**
     * Đếm bình luận theo bài viết
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId AND c.deletedAt IS NULL")
    long countByPostId(@Param("postId") Long postId);

    /**
     * Lấy bình luận theo người dùng
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.post WHERE c.user.id = :userId AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    List<Comment> findByUserIdWithPost(@Param("userId") Long userId);
}
