package com.fb.notification.service.impl;

import com.fb.auth.model.User;
import com.fb.auth.repository.UserRepository;
import com.fb.common.constant.CacheKey;
import com.fb.common.enums.NotificationType;
import com.fb.common.response.PagedResponse;
import com.fb.common.exception.BadRequestException;
import com.fb.common.exception.ResourceNotFoundException;
import com.fb.infrastructure.cache.CacheService;
import com.fb.notification.dto.NotificationResponse;
import com.fb.notification.mapper.NotificationMapper;
import com.fb.notification.model.Notification;
import com.fb.notification.repository.NotificationRepository;
import com.fb.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;
    private final CacheService cacheService;

    @Override
    @Transactional
    public NotificationResponse createNotification(Long userId, Long actorId, NotificationType type,
                                                    Long referenceId, String referenceType, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Tạo thông báo thất bại - Không tìm thấy người dùng ID: {}", userId);
                    return new ResourceNotFoundException("Không tìm thấy người dùng");
                });

        User actor = actorId != null ? userRepository.findById(actorId).orElse(null) : null;

        Notification notification = Notification.builder()
                .user(user)
                .actor(actor)
                .type(type)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .message(message)
                .build();

        notification = notificationRepository.save(notification);

        cacheService.deleteByPattern(CacheKey.NOTIFICATION_USER + userId);

        log.info("Tạo thông báo thành công - User ID: {}, Loại: {}, Actor ID: {}", userId, type, actorId);
        return notificationMapper.toNotificationResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getUserNotifications(Long userId, int page, int size) {
        Page<Notification> notificationPage = notificationRepository.findByUserIdWithActor(
                userId, PageRequest.of(page, size));

        List<NotificationResponse> notifications = notificationMapper.toNotificationResponseList(
                notificationPage.getContent());

        log.debug("Lấy danh sách thông báo thành công - User ID: {}, Số lượng: {}", userId, notifications.size());
        return PagedResponse.of(notifications, page, size, notificationPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        String cacheKey = CacheKey.NOTIFICATION_USER + userId + ":unread";
        Long cached = cacheService.get(cacheKey, Long.class);
        if (cached != null) {
            return cached;
        }

        long count = notificationRepository.countUnreadByUserId(userId);

        cacheService.set(cacheKey, count, java.time.Duration.ofSeconds(30));
        log.debug("Đếm thông báo chưa đọc - User ID: {}, Số lượng: {}", userId, count);
        return count;
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> {
                    log.warn("Đánh dấu đã đọc thất bại - Không tìm thấy thông báo ID: {}", notificationId);
                    return new ResourceNotFoundException("Không tìm thấy thông báo");
                });

        if (!notification.getUser().getId().equals(userId)) {
            throw new BadRequestException("Bạn chỉ có thể đánh dấu thông báo của mình đã đọc");
        }

        notificationRepository.markAsRead(notificationId, userId);

        cacheService.deleteByPattern(CacheKey.NOTIFICATION_USER + userId);

        log.info("Đánh dấu thông báo đã đọc - Notification ID: {}, User ID: {}", notificationId, userId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);

        cacheService.deleteByPattern(CacheKey.NOTIFICATION_USER + userId);

        log.info("Đánh dấu tất cả thông báo đã đọc - User ID: {}", userId);
    }

    @Override
    @Transactional
    public void deleteNotification(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> {
                    log.warn("Xóa thông báo thất bại - Không tìm thấy thông báo ID: {}", notificationId);
                    return new ResourceNotFoundException("Không tìm thấy thông báo");
                });

        if (!notification.getUser().getId().equals(userId)) {
            throw new BadRequestException("Bạn chỉ có thể xóa thông báo của mình");
        }

        notificationRepository.delete(notification);

        cacheService.deleteByPattern(CacheKey.NOTIFICATION_USER + userId);

        log.info("Xóa thông báo thành công - Notification ID: {}, User ID: {}", notificationId, userId);
    }
}
