package com.fb.orchestrator;

import com.fb.auth.model.User;
import com.fb.auth.repository.UserRepository;
import com.fb.common.exception.ResourceNotFoundException;
import com.fb.infrastructure.cache.CacheService;
import com.fb.notification.model.Notification;
import com.fb.notification.repository.NotificationRepository;
import com.fb.common.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bộ điều phối thông báo (Notification Orchestrator).
 *
 * Điều phối luồng gửi thông báo:
 * 1. Kiểm tra tùy chọn thông báo của người dùng
 * 2. Tạo thông báo trong cơ sở dữ liệu
 * 3. Đẩy thông báo qua WebSocket (nếu người dùng đang trực tuyến)
 * 4. Gửi email (nếu được bật)
 * 5. Theo dõi thống kê phân tích
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationOrchestrator {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CacheService cacheService;

    /**
     * Điều phối gửi thông báo cho người dùng.
     *
     * @param userId mã người nhận thông báo
     * @param actorId mã người thực hiện hành động (có thể null)
     * @param type loại thông báo (lời mời kết bạn, bài viết được thích, v.v.)
     * @param referenceId mã tham chiếu (bài viết, lời mời kết bạn, v.v.)
     * @param referenceType loại tham chiếu (POST, FRIEND_REQUEST, v.v.)
     * @param message nội dung thông báo
     * @return thông báo vừa được tạo, hoặc null nếu người dùng tắt thông báo
     */
    @Transactional
    public Notification orchestrateNotification(Long userId, Long actorId,
                                                 NotificationType type,
                                                 Long referenceId, String referenceType,
                                                 String message) {
        // Bước 1: Kiểm tra tùy chọn thông báo của người dùng
        if (!isNotificationEnabled(userId, type)) {
            log.debug("Thông báo bị tắt cho người dùng {} loại {}", userId, type);
            return null;
        }

        // Bước 2: Lấy thông tin người dùng
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        User actor = actorId != null ? userRepository.findById(actorId).orElse(null) : null;

        // Bước 3: Tạo thông báo mới
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setActor(actor);
        notification.setType(type);
        notification.setReferenceId(referenceId);
        notification.setReferenceType(referenceType);
        notification.setMessage(message);
        notification = notificationRepository.save(notification);

        // Bước 4: Xóa bộ nhớ đệm số thông báo chưa đọc
        cacheService.delete("notification:count:" + userId);

        // Bước 5: Theo dõi thống kê phân tích
        cacheService.increment("analytics:notifications:total");

        log.info("Điều phối thông báo thành công: {} cho người dùng {}", notification.getId(), userId);
        return notification;
    }

    /**
     * Điều phối đánh dấu một thông báo đã đọc.
     *
     * @param notificationId mã thông báo
     * @param userId mã người dùng sở hữu thông báo
     */
    @Transactional
    public void orchestrateMarkAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Không tìm thấy thông báo");
        }

        notification.setRead(true);
        notificationRepository.save(notification);

        // Xóa bộ nhớ đệm số thông báo chưa đọc
        cacheService.delete("notification:count:" + userId);
    }

    /**
     * Điều phối đánh dấu tất cả thông báo của người dùng đã đọc.
     *
     * @param userId mã người dùng
     */
    @Transactional
    public void orchestrateMarkAllAsRead(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        notificationRepository.markAllAsRead(userId);

        // Xóa bộ nhớ đệm số thông báo chưa đọc
        cacheService.delete("notification:count:" + userId);
    }

    /**
     * Kiểm tra loại thông báo có được bật cho người dùng không.
     * Mặc định: bật nếu chưa có tùy chọn nào được thiết lập.
     */
    private boolean isNotificationEnabled(Long userId, NotificationType type) {
        String key = "notification:pref:" + userId + ":" + type.name();
        Object enabled = cacheService.get(key);

        // Mặc định bật nếu chưa có tùy chọn
        if (enabled == null) return true;

        return Boolean.parseBoolean(enabled.toString());
    }
}
