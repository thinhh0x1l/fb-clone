package com.fb.friend.service;

import com.fb.friend.dto.FriendRequestDto;
import com.fb.friend.dto.FriendResponse;

import java.util.List;

/**
 * Service quản lý bạn bè
 * Gửi, chấp nhận, từ chối lời mời kết bạn và quản lý danh sách bạn bè
 */
public interface FriendService {

    /**
     * Gửi lời mời kết bạn
     * @param userId ID người gửi
     * @param request thông tin lời mời
     * @return kết quả lời mời
     */
    FriendResponse sendFriendRequest(Long userId, FriendRequestDto request);

    /**
     * Chấp nhận lời mời kết bạn
     * @param userId ID người nhận
     * @param friendRequestId ID lời mời
     * @return kết quả lời mời
     */
    FriendResponse acceptFriendRequest(Long userId, Long friendRequestId);

    /**
     * Từ chối lời mời kết bạn
     * @param userId ID người nhận
     * @param friendRequestId ID lời mời
     * @return kết quả lời mời
     */
    FriendResponse rejectFriendRequest(Long userId, Long friendRequestId);

    /**
     * Hủy lời mời kết bạn
     * @param userId ID người gửi
     * @param friendRequestId ID lời mời
     */
    void cancelFriendRequest(Long userId, Long friendRequestId);

    /**
     * Xóa bạn bè
     * @param userId ID người xóa
     * @param friendId ID bạn bè cần xóa
     */
    void removeFriend(Long userId, Long friendId);

    /**
     * Lấy danh sách lời mời đang chờ
     * @param userId ID người nhận
     * @return danh sách lời mời
     */
    List<FriendResponse> getPendingRequests(Long userId);

    /**
     * Lấy danh sách bạn bè đã chấp nhận
     * @param userId ID người dùng
     * @return danh sách bạn bè
     */
    List<FriendResponse> getAcceptedFriends(Long userId);

    /**
     * Lấy thông tin mối quan hệ bạn bè
     * @param userId ID người dùng thứ nhất
     * @param otherUserId ID người dùng thứ hai
     * @return thông tin mối quan hệ
     */
    FriendResponse getFriendship(Long userId, Long otherUserId);

    /**
     * Đếm số lượng bạn bè
     * @param userId ID người dùng
     * @return số lượng bạn bè
     */
    long getFriendCount(Long userId);

    /**
     * Kiểm tra hai người có phải bạn bè không
     * @param userId1 ID người dùng thứ nhất
     * @param userId2 ID người dùng thứ hai
     * @return true nếu là bạn bè
     */
    boolean areFriends(Long userId1, Long userId2);
}
