package com.fb.friend.mapper;

import com.fb.auth.model.User;
import com.fb.friend.dto.FriendResponse;
import com.fb.friend.model.Friend;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper chuyển đổi dữ liệu Friend
 * Chuyển đổi giữa Entity Friend và FriendResponse DTO
 */
@Component
public class FriendMapper {

    /**
     * Alias cho toFriendResponse
     */
    public FriendResponse toResponse(Friend friend) {
        return toFriendResponse(friend);
    }

    /**
     * Chuyển đổi Friend entity sang FriendResponse DTO
     * @param friend Friend entity từ database
     * @return FriendResponse DTO cho API response
     */
    public FriendResponse toFriendResponse(Friend friend) {
        if (friend == null) {
            return null;
        }

        return FriendResponse.builder()
                .id(friend.getId())
                .requester(toUserInfo(friend.getRequester()))
                .addressee(toUserInfo(friend.getAddressee()))
                .status(friend.getStatus())
                .message(friend.getMessage())
                .createdAt(friend.getCreatedAt())
                .build();
    }

    /**
     * Chuyển đổi danh sách Friend entities sang FriendResponse DTOs
     * @param friends danh sách Friend entities
     * @return danh sách FriendResponse DTOs
     */
    public List<FriendResponse> toFriendResponseList(List<Friend> friends) {
        if (friends == null) {
            return new ArrayList<>();
        }
        return friends.stream()
                .map(this::toFriendResponse)
                .collect(Collectors.toList());
    }

    /**
     * Chuyển đổi User entity sang UserInfo nested DTO trong Friend
     * Chứa thông tin cơ bản của người dùng trong quan hệ bạn bè
     */
    public FriendResponse.UserInfo toUserInfo(User user) {
        if (user == null) {
            return null;
        }

        return FriendResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .avatar(user.getAvatar())
                .build();
    }
}
