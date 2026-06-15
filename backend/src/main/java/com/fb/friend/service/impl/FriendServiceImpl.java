package com.fb.friend.service.impl;

import com.fb.friend.model.Friend;
import com.fb.friend.repository.FriendRepository;
import com.fb.friend.dto.FriendRequestDto;
import com.fb.friend.dto.FriendResponse;
import com.fb.friend.mapper.FriendMapper;
import com.fb.friend.service.FriendService;
import com.fb.common.enums.FriendStatus;
import com.fb.auth.model.User;
import com.fb.auth.repository.UserRepository;
import com.fb.common.exception.BadRequestException;
import com.fb.common.exception.ResourceNotFoundException;
import com.fb.common.exception.UnauthorizedException;
import com.fb.event.FriendRequestEvent;
import com.fb.infrastructure.cache.MultiTierCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendServiceImpl implements FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final FriendMapper friendMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final MultiTierCache cache;

    private static final String FRIENDS_KEY_PREFIX = "friends:";
    private static final int FRIENDS_TTL = 1800;

    @Override
    @Transactional
    public FriendResponse sendFriendRequest(Long requesterId, FriendRequestDto request) {
        Long addresseeId = request.getUserId();

        if (requesterId.equals(addresseeId)) {
            throw new BadRequestException("Không thể tự gửi yêu cầu kết bạn cho bản thân");
        }

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng gửi"));
        User addressee = userRepository.findById(addresseeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng nhận"));

        if (friendRepository.areFriends(requesterId, addresseeId)) {
            throw new BadRequestException("Đã là bạn bè");
        }

        if (friendRepository.hasPendingRequest(requesterId, addresseeId)) {
            throw new BadRequestException("Yêu cầu kết bạn đang chờ xử lý");
        }

        if (friendRepository.hasPendingRequest(addresseeId, requesterId)) {
            throw new BadRequestException("Người này đã gửi yêu cầu kết bạn cho bạn");
        }

        Friend friend = new Friend();
        friend.setRequester(requester);
        friend.setAddressee(addressee);
        friend.setStatus(FriendStatus.PENDING);
        friend.setMessage(request.getMessage());
        friend = friendRepository.save(friend);

        eventPublisher.publishEvent(new FriendRequestEvent(this, requesterId, requesterId, addresseeId, friend.getId()));

        log.info("Gửi yêu cầu kết bạn thành công: {} -> {}", requesterId, addresseeId);
        return friendMapper.toResponse(friend);
    }

    @Override
    @Transactional
    public FriendResponse acceptFriendRequest(Long userId, Long friendRequestId) {
        Friend friend = friendRepository.findById(friendRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu kết bạn"));

        if (!friend.getAddressee().getId().equals(userId)) {
            throw new UnauthorizedException("Bạn chỉ có thể chấp nhận yêu cầu gửi cho mình");
        }

        if (friend.getStatus() != FriendStatus.PENDING) {
            throw new BadRequestException("Yêu cầu này không còn ở trạng thái chờ");
        }

        friend.setStatus(FriendStatus.ACCEPTED);
        friend = friendRepository.save(friend);

        invalidateFriendCache(friend.getRequester().getId());
        invalidateFriendCache(friend.getAddressee().getId());

        log.info("Chấp nhận yêu cầu kết bạn thành công: {}", friendRequestId);
        return friendMapper.toResponse(friend);
    }

    @Override
    @Transactional
    public FriendResponse rejectFriendRequest(Long userId, Long friendRequestId) {
        Friend friend = friendRepository.findById(friendRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu kết bạn"));

        if (!friend.getAddressee().getId().equals(userId)) {
            throw new UnauthorizedException("Bạn chỉ có thể từ chối yêu cầu gửi cho mình");
        }

        friend.setStatus(FriendStatus.REJECTED);
        friend = friendRepository.save(friend);

        log.info("Từ chối yêu cầu kết bạn thành công: {}", friendRequestId);
        return friendMapper.toResponse(friend);
    }

    @Override
    @Transactional
    public void cancelFriendRequest(Long userId, Long friendRequestId) {
        Friend friend = friendRepository.findById(friendRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu kết bạn"));

        if (!friend.getRequester().getId().equals(userId)) {
            throw new UnauthorizedException("Bạn chỉ có thể hủy yêu cầu mình gửi");
        }

        friend.setStatus(FriendStatus.CANCELLED);
        friendRepository.save(friend);

        log.info("Hủy yêu cầu kết bạn thành công: {}", friendRequestId);
    }

    @Override
    @Transactional
    public void removeFriend(Long userId, Long friendId) {
        if (!friendRepository.areFriends(userId, friendId)) {
            throw new BadRequestException("Không phải bạn bè");
        }

        friendRepository.findFriendshipBetween(userId, friendId)
                .ifPresent(f -> {
                    f.softDelete();
                    friendRepository.save(f);
                });

        invalidateFriendCache(userId);
        invalidateFriendCache(friendId);

        log.info("Hủy kết bạn thành công: {} <-> {}", userId, friendId);
    }

    @Override
    public List<FriendResponse> getPendingRequests(Long userId) {
        List<Friend> pendingAsAddressee = friendRepository.findPendingRequestsForUser(userId);
        List<Friend> pendingAsRequester = friendRepository.findSentRequestsByUser(userId);

        java.util.List<FriendResponse> responses = new java.util.ArrayList<>();
        pendingAsAddressee.forEach(f -> responses.add(friendMapper.toResponse(f)));
        pendingAsRequester.forEach(f -> responses.add(friendMapper.toResponse(f)));

        return responses;
    }

    @Override
    public List<FriendResponse> getAcceptedFriends(Long userId) {
        List<Friend> friends = friendRepository.findAcceptedFriends(userId);
        return friends.stream()
                .map(friendMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public FriendResponse getFriendship(Long userId, Long otherUserId) {
        Friend friend = friendRepository.findFriendshipBetween(userId, otherUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mối quan hệ bạn bè"));
        return friendMapper.toResponse(friend);
    }

    @Override
    public long getFriendCount(Long userId) {
        return friendRepository.countFriends(userId);
    }

    @Override
    public boolean areFriends(Long userId1, Long userId2) {
        return friendRepository.areFriends(userId1, userId2);
    }

    private void invalidateFriendCache(Long userId) {
        cache.delete(FRIENDS_KEY_PREFIX + userId);
    }
}
