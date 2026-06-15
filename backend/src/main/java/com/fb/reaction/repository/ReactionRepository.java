package com.fb.reaction.repository;

import com.fb.reaction.model.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho tương tác (like, reaction)
 */
@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {

    /**
     * Tìm tương tác của người dùng với bài viết
     */
    Optional<Reaction> findByUserIdAndPostId(Long userId, Long postId);

    /**
     * Tìm tương tác của người dùng với bình luận
     */
    Optional<Reaction> findByUserIdAndCommentId(Long userId, Long commentId);

    /**
     * Đếm tương tác theo bài viết và loại
     */
    @Query("SELECT COUNT(r) FROM Reaction r WHERE r.post.id = :postId AND r.type = :type AND r.deletedAt IS NULL")
    long countByPostIdAndType(@Param("postId") Long postId, @Param("type") String type);

    /**
     * Đếm tổng tương tác theo bài viết
     */
    @Query("SELECT COUNT(r) FROM Reaction r WHERE r.post.id = :postId AND r.deletedAt IS NULL")
    long countByPostId(@Param("postId") Long postId);

    /**
     * Lấy tất cả tương tác theo bài viết
     */
    @Query("SELECT r FROM Reaction r JOIN FETCH r.user WHERE r.post.id = :postId AND r.deletedAt IS NULL")
    List<Reaction> findByPostIdWithUser(@Param("postId") Long postId);

    /**
     * Kiểm tra người dùng đã tương tác chưa
     */
    @Query("SELECT COUNT(r) > 0 FROM Reaction r WHERE r.user.id = :userId AND r.post.id = :postId AND r.deletedAt IS NULL")
    boolean existsByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);
}
