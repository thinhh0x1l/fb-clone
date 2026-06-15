package com.fb.event;

import lombok.Getter;

/**
 * Sự kiện được phát sinh khi một lời mời kết bạn được gửi.
 * Sử dụng để gửi thông báo cho người nhận và cập nhật đồ thị xã hội.
 */
@Getter
public class FriendRequestEvent extends BaseEvent {

    /** Mã người gửi lời mời kết bạn */
    private final Long senderId;

    /** Mã người nhận lời mời kết bạn */
    private final Long receiverId;

    /** Mã mối quan hệ bạn bè */
    private final Long friendshipId;

    /**
     * Khởi tạo sự kiện lời mời kết bạn.
     *
     * @param source đối tượng phát sinh sự kiện
     * @param userId mã người dùng (người gửi)
     * @param senderId mã người gửi
     * @param receiverId mã người nhận
     * @param friendshipId mã mối quan hệ bạn bè
     */
    public FriendRequestEvent(Object source, Long userId, Long senderId, Long receiverId, Long friendshipId) {
        super(source, userId);
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.friendshipId = friendshipId;
    }
}
