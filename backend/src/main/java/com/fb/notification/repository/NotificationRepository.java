package com.fb.notification.repository;

import com.fb.notification.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository cho thông báo
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Lấy danh sách thông báo theo người dùng
     */
    @Query("SELECT n FROM Notification n JOIN FETCH n.actor WHERE n.user.id = :userId AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdWithActor(@Param("userId") Long userId, Pageable pageable);

    /**
     * Đếm thông báo chưa đọc
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.read = false AND n.deletedAt IS NULL")
    long countUnreadByUserId(@Param("userId") Long userId);

    /**
     * Đánh dấu tất cả đã đọc
     */
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.user.id = :userId AND n.read = false")
    int markAllAsRead(@Param("userId") Long userId);

    /**
     * Đánh dấu đã đọc
     */
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.id = :id AND n.user.id = :userId")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);
}
