package com.fb.notification.service;

import com.fb.common.response.PagedResponse;
import com.fb.common.enums.NotificationType;
import com.fb.notification.dto.NotificationResponse;

/**
 * Service quản lý thông báo
 * Tạo, xem, đánh dấu đã đọc và xóa thông báo
 */
public interface NotificationService {

    /**
     * Tạo thông báo mới
     * @param userId ID người nhận thông báo
     * @param actorId ID người thực hiện hành động
     * @param type loại thông báo
     * @param referenceId ID tham chiếu
     * @param referenceType loại tham chiếu
     * @param message nội dung thông báo
     * @return thông báo đã tạo
     */
    NotificationResponse createNotification(Long userId, Long actorId, NotificationType type,
                                            Long referenceId, String referenceType, String message);

    /**
     * Lấy danh sách thông báo của người dùng
     * @param userId ID người dùng
     * @param page trang hiện tại
     * @param size số lượng mỗi trang
     * @return danh sách thông báo phân trang
     */
    PagedResponse<NotificationResponse> getUserNotifications(Long userId, int page, int size);

    /**
     * Đếm thông báo chưa đọc
     * @param userId ID người dùng
     * @return số lượng thông báo chưa đọc
     */
    long getUnreadCount(Long userId);

    /**
     * Đánh dấu thông báo đã đọc
     * @param userId ID người dùng
     * @param notificationId ID thông báo
     */
    void markAsRead(Long userId, Long notificationId);

    /**
     * Đánh dấu tất cả thông báo đã đọc
     * @param userId ID người dùng
     */
    void markAllAsRead(Long userId);

    /**
     * Xóa thông báo
     * @param userId ID người dùng
     * @param notificationId ID thông báo
     */
    void deleteNotification(Long userId, Long notificationId);
}
