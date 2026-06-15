package com.fb.post.repository;

import com.fb.post.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho bài viết
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * Lấy bài viết theo người dùng (đã optimize N+1)
     */
    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.user.id = :userId AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Post> findByUserIdWithUser(@Param("userId") Long userId);

    /**
     * Lấy bài viết theo danh sách người dùng (để build feed)
     */
    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.user.id IN :userIds AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Post> findByUserIdsWithUser(@Param("userIds") List<Long> userIds);

    /**
     * Lấy bài viết công khai
     */
    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.visibility = 'PUBLIC' AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Post> findPublicPosts();

    /**
     * Tìm kiếm bài viết theo nội dung
     */
    @Query("SELECT p FROM Post p WHERE LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%')) AND p.deletedAt IS NULL")
    List<Post> searchByContent(@Param("query") String query, Pageable pageable);

    /**
     * Đếm bài viết theo người dùng
     */
    @Query("SELECT COUNT(p) FROM Post p WHERE p.user.id = :userId AND p.deletedAt IS NULL")
    long countByUserId(@Param("userId") Long userId);
}
