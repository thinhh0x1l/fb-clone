package com.fb.friend.controller;

import com.fb.common.constant.AppConstant;
import com.fb.common.response.ApiResponse;
import com.fb.common.util.HashIdUtil;
import com.fb.friend.dto.FriendRequestDto;
import com.fb.friend.dto.FriendResponse;
import com.fb.friend.service.FriendService;
import com.fb.security.CurrentUser;
import com.fb.security.JwtAuthenticationFilter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Điều khiển bạn bè - xử lý yêu cầu kết bạn, chấp nhận, từ chối và quản lý bạn bè
 */
@Slf4j
@RestController
@RequestMapping(AppConstant.API_VERSION + "/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;
    private final HashIdUtil hashIdUtil;

    /**
     * Gửi yêu cầu kết bạn
     *
     * @param principal thông tin người dùng đã xác thực
     * @param request thông tin yêu cầu kết bạn
     * @return thông tin yêu cầu kết bạn đã gửi
     */
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<FriendResponse>> sendFriendRequest(
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal,
            @Valid @RequestBody FriendRequestDto request) {
        log.info("Gửi yêu cầu kết bạn bởi người dùng: {}", principal.getUserId());
        FriendResponse response = friendService.sendFriendRequest(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Gửi yêu cầu kết bạn thành công", response));
    }

    /**
     * Chấp nhận yêu cầu kết bạn
     *
     * @param id mã HashId của yêu cầu kết bạn
     * @param principal thông tin người dùng đã xác thực
     * @return thông tin yêu cầu kết bạn đã chấp nhận
     */
    @PutMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<FriendResponse>> acceptFriendRequest(
            @PathVariable String id,
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal) {
        Long realId = hashIdUtil.decode(id);
        log.info("Chấp nhận yêu cầu kết bạn ID: {} bởi người dùng: {}", realId, principal.getUserId());
        FriendResponse response = friendService.acceptFriendRequest(principal.getUserId(), realId);
        return ResponseEntity.ok(ApiResponse.ok("Chấp nhận yêu cầu kết bạn thành công", response));
    }

    /**
     * Từ chối yêu cầu kết bạn
     *
     * @param id mã HashId của yêu cầu kết bạn
     * @param principal thông tin người dùng đã xác thực
     * @return thông tin yêu cầu kết bạn đã từ chối
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<FriendResponse>> rejectFriendRequest(
            @PathVariable String id,
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal) {
        Long realId = hashIdUtil.decode(id);
        log.info("Từ chối yêu cầu kết bạn ID: {} bởi người dùng: {}", realId, principal.getUserId());
        FriendResponse response = friendService.rejectFriendRequest(principal.getUserId(), realId);
        return ResponseEntity.ok(ApiResponse.ok("Từ chối yêu cầu kết bạn thành công", response));
    }

    /**
     * Hủy yêu cầu kết bạn
     *
     * @param id mã HashId của yêu cầu kết bạn
     * @param principal thông tin người dùng đã xác thực
     * @return phản hồi thành công
     */
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelFriendRequest(
            @PathVariable String id,
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal) {
        Long realId = hashIdUtil.decode(id);
        log.info("Hủy yêu cầu kết bạn ID: {} bởi người dùng: {}", realId, principal.getUserId());
        friendService.cancelFriendRequest(principal.getUserId(), realId);
        return ResponseEntity.ok(ApiResponse.ok("Hủy yêu cầu kết bạn thành công"));
    }

    /**
     * Xóa bạn bè
     *
     * @param friendId mã HashId của bạn bè
     * @param principal thông tin người dùng đã xác thực
     * @return phản hồi thành công
     */
    @DeleteMapping("/{friendId}")
    public ResponseEntity<ApiResponse<Void>> removeFriend(
            @PathVariable String friendId,
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal) {
        Long realFriendId = hashIdUtil.decode(friendId);
        log.info("Xóa bạn bè ID: {} bởi người dùng: {}", realFriendId, principal.getUserId());
        friendService.removeFriend(principal.getUserId(), realFriendId);
        return ResponseEntity.ok(ApiResponse.ok("Xóa bạn bè thành công"));
    }

    /**
     * Lấy danh sách yêu cầu kết bạn đang chờ xử lý
     *
     * @param principal thông tin người dùng đã xác thực
     * @return danh sách yêu cầu kết bạn
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<FriendResponse>>> getPendingRequests(
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal) {
        log.info("Lấy danh sách yêu cầu kết bạn đang chờ của người dùng: {}", principal.getUserId());
        List<FriendResponse> response = friendService.getPendingRequests(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Lấy danh sách bạn bè đã chấp nhận
     *
     * @param principal thông tin người dùng đã xác thực
     * @return danh sách bạn bè
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<FriendResponse>>> getAcceptedFriends(
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal) {
        log.info("Lấy danh sách bạn bè của người dùng: {}", principal.getUserId());
        List<FriendResponse> response = friendService.getAcceptedFriends(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Lấy thông tin quan hệ bạn bè giữa hai người dùng
     *
     * @param userId mã HashId của người dùng cần kiểm tra
     * @param principal thông tin người dùng đã xác thực
     * @return thông tin quan hệ bạn bè
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<FriendResponse>> getFriendship(
            @PathVariable String userId,
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal) {
        Long realUserId = hashIdUtil.decode(userId);
        log.info("Lấy thông tin quan hệ bạn bè với người dùng ID: {}", realUserId);
        FriendResponse response = friendService.getFriendship(principal.getUserId(), realUserId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Lấy số lượng bạn bè của người dùng
     *
     * @param userId mã HashId của người dùng
     * @return số lượng bạn bè
     */
    @GetMapping("/{userId}/count")
    public ResponseEntity<ApiResponse<Long>> getFriendCount(@PathVariable String userId) {
        Long realUserId = hashIdUtil.decode(userId);
        log.info("Lấy số lượng bạn bè của người dùng ID: {}", realUserId);
        long count = friendService.getFriendCount(realUserId);
        return ResponseEntity.ok(ApiResponse.ok(count));
    }

    /**
     * Kiểm tra quan hệ bạn bè giữa hai người dùng
     *
     * @param userId mã HashId của người dùng cần kiểm tra
     * @param principal thông tin người dùng đã xác thực
     * @return true nếu là bạn bè, false nếu không
     */
    @GetMapping("/check/{userId}")
    public ResponseEntity<ApiResponse<Boolean>> checkFriendship(
            @PathVariable String userId,
            @CurrentUser JwtAuthenticationFilter.WebSocketPrincipal principal) {
        Long realUserId = hashIdUtil.decode(userId);
        log.info("Kiểm tra quan hệ bạn bè với người dùng ID: {}", realUserId);
        boolean areFriends = friendService.areFriends(principal.getUserId(), realUserId);
        return ResponseEntity.ok(ApiResponse.ok(areFriends));
    }
}
