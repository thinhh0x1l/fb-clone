package com.fb.orchestrator;

import com.fb.auth.model.User;
import com.fb.friend.engine.FriendSuggestionEngine;
import com.fb.friend.model.Friend;
import com.fb.friend.repository.FriendRepository;
import com.fb.auth.repository.UserRepository;
import com.fb.common.enums.FriendStatus;
import com.fb.common.exception.BadRequestException;
import com.fb.common.exception.ResourceNotFoundException;
import com.fb.infrastructure.cache.CacheService;
import com.fb.event.FriendRequestEvent;
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
public class FriendOrchestrator {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final FriendSuggestionEngine suggestionEngine;
    private final CacheService cacheService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Friend orchestrateSendRequest(Long requesterId, Long addresseeId, String message) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người gửi lời mời"));
        User addressee = userRepository.findById(addresseeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người nhận lời mời"));

        if (requesterId.equals(addresseeId)) {
            throw new BadRequestException("Không thể gửi lời mời kết bạn cho chính mình");
        }

        if (areFriends(requesterId, addresseeId)) {
            throw new BadRequestException("Đã là bạn bè");
        }

        if (isBlocked(requesterId, addresseeId)) {
            throw new BadRequestException("Không thể gửi lời mời kết bạn");
        }

        if (hasPendingRequest(requesterId, addresseeId)) {
            throw new BadRequestException("Lời mời kết bạn đang chờ xử lý");
        }

        Friend friend = new Friend();
        friend.setRequester(requester);
        friend.setAddressee(addressee);
        friend.setStatus(FriendStatus.PENDING);
        friend.setMessage(message);
        friend = friendRepository.save(friend);

        eventPublisher.publishEvent(new FriendRequestEvent(this, requesterId, requesterId, addresseeId, friend.getId()));

        updateSocialGraphCache(requesterId, addresseeId);

        log.info("Điều phối lời mời kết bạn thành công: {} -> {}", requesterId, addresseeId);
        return friend;
    }

    @Transactional
    public Friend orchestrateAcceptRequest(Long requestId, Long userId) {
        Friend friend = friendRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lời mời kết bạn"));

        if (!friend.getAddressee().getId().equals(userId)) {
            throw new BadRequestException("Không có quyền thực hiện thao tác này");
        }

        if (friend.getStatus() != FriendStatus.PENDING) {
            throw new BadRequestException("Lời mời không còn ở trạng thái chờ xử lý");
        }

        friend.setStatus(FriendStatus.ACCEPTED);
        friend = friendRepository.save(friend);

        Long requesterId = friend.getRequester().getId();
        Long addresseeId = friend.getAddressee().getId();

        invalidateFriendCache(requesterId);
        invalidateFriendCache(addresseeId);
        updateSocialGraphCache(requesterId, addresseeId);

        invalidateSuggestions(requesterId);
        invalidateSuggestions(addresseeId);

        log.info("Chấp nhận lời mời kết bạn thành công: {} bởi {}", requestId, userId);
        return friend;
    }

    @Transactional
    public void orchestrateUnfriend(Long userId, Long friendId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        User friendUser = userRepository.findById(friendId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        java.util.Optional<Friend> friendship = friendRepository.findFriendshipBetween(userId, friendId);

        if (friendship.isEmpty()) {
            throw new BadRequestException("Không phải bạn bè");
        }

        Friend f = friendship.get();
        f.softDelete();
        friendRepository.save(f);

        invalidateFriendCache(userId);
        invalidateFriendCache(friendId);
        removeFromSocialGraph(userId, friendId);

        log.info("Hủy kết bạn thành công: {} <-> {}", userId, friendId);
    }

    public List<FriendSuggestionEngine.FriendSuggestion> getSuggestions(Long userId, int limit) {
        return suggestionEngine.suggestFriends(userId, limit);
    }

    private boolean areFriends(Long userId1, Long userId2) {
        return friendRepository.areFriends(userId1, userId2);
    }

    private boolean isBlocked(Long requesterId, Long addresseeId) {
        String blockKey = "block:" + addresseeId + ":" + requesterId;
        return cacheService.hasKey(blockKey);
    }

    private boolean hasPendingRequest(Long requesterId, Long addresseeId) {
        return friendRepository.hasPendingRequest(requesterId, addresseeId);
    }

    private void updateSocialGraphCache(Long userId1, Long userId2) {
        cacheService.sAdd("graph:adjacency:" + userId1, userId2.toString());
        cacheService.sAdd("graph:adjacency:" + userId2, userId1.toString());
    }

    private void removeFromSocialGraph(Long userId1, Long userId2) {
        cacheService.sRemove("graph:adjacency:" + userId1, userId2.toString());
        cacheService.sRemove("graph:adjacency:" + userId2, userId1.toString());
    }

    private void invalidateFriendCache(Long userId) {
        cacheService.delete("friends:" + userId);
    }

    private void invalidateSuggestions(Long userId) {
        cacheService.delete("friend:suggestion:" + userId);
    }
}
