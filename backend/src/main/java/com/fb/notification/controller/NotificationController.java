package com.fb.notification.controller;

import com.fb.common.constant.AppConstant;
import com.fb.common.response.ApiResponse;
import com.fb.common.response.PagedResponse;
import com.fb.common.util.HashIdUtil;
import com.fb.notification.dto.NotificationResponse;
import com.fb.notification.service.NotificationService;
import com.fb.security.CurrentUser;
import com.fb.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Điều khiển thông báo - xử lý lấy, đánh dấu đọc và xóa thông báo
 */
@Slf4j
@RestController
@RequestMapping(AppConstant.API_VERSION + "/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final HashIdUtil hashIdUtil;

    /**
     * Lấy danh sách thông báo của người dùng
     *
     * @param principal thông tin người dùng đã xác thực
     * @param page số trang
     * @param size kích thước trang
     * @return danh sách thông báo phân trang
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>> getNotifications(
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Lấy danh sách thông báo của người dùng: {}", principal.getUserId());
        PagedResponse<NotificationResponse> response = notificationService.getUserNotifications(
                principal.getUserId(), page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Lấy số lượng thông báo chưa đọc
     *
     * @param principal thông tin người dùng đã xác thực
     * @return số lượng thông báo chưa đọc
     */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal) {
        log.info("Lấy số lượng thông báo chưa đọc của người dùng: {}", principal.getUserId());
        long count = notificationService.getUnreadCount(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(count));
    }

    /**
     * Đánh dấu thông báo đã đọc
     *
     * @param id mã HashId của thông báo
     * @param principal thông tin người dùng đã xác thực
     * @return phản hồi thành công
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable String id,
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal) {
        Long realId = hashIdUtil.decode(id);
        log.info("Đánh dấu thông báo ID: {} đã đọc bởi người dùng: {}", realId, principal.getUserId());
        notificationService.markAsRead(principal.getUserId(), realId);
        return ResponseEntity.ok(ApiResponse.ok("Đánh dấu thông báo đã đọc thành công"));
    }

    /**
     * Đánh dấu tất cả thông báo đã đọc
     *
     * @param principal thông tin người dùng đã xác thực
     * @return phản hồi thành công
     */
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal) {
        log.info("Đánh dấu tất cả thông báo đã đọc bởi người dùng: {}", principal.getUserId());
        notificationService.markAllAsRead(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok("Đánh dấu tất cả thông báo đã đọc thành công"));
    }

    /**
     * Xóa thông báo
     *
     * @param id mã HashId của thông báo
     * @param principal thông tin người dùng đã xác thực
     * @return phản hồi thành công
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable String id,
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal) {
        Long realId = hashIdUtil.decode(id);
        log.info("Xóa thông báo ID: {} bởi người dùng: {}", realId, principal.getUserId());
        notificationService.deleteNotification(principal.getUserId(), realId);
        return ResponseEntity.ok(ApiResponse.ok("Xóa thông báo thành công"));
    }
}
