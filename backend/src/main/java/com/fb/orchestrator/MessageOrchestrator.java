package com.fb.orchestrator;

import com.fb.auth.model.User;
import com.fb.auth.repository.UserRepository;
import com.fb.common.enums.ConversationType;
import com.fb.common.enums.MessageType;
import com.fb.common.exception.ResourceNotFoundException;
import com.fb.common.exception.TooManyRequestsException;
import com.fb.infrastructure.cache.CacheService;
import com.fb.message.model.Conversation;
import com.fb.message.model.Message;
import com.fb.message.repository.ConversationRepository;
import com.fb.message.repository.MessageRepository;
import com.fb.event.MessageSentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageOrchestrator {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final CacheService cacheService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Message orchestrateSendMessage(Long conversationId, Long senderId,
                                           String content, MessageType type) {
        Conversation conversation = conversationRepository.findByIdWithParticipants(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc trò chuyện"));

        if (!conversation.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(senderId))) {
            throw new ResourceNotFoundException("Không phải thành viên của cuộc trò chuyện này");
        }

        if (isRateLimited(senderId)) {
            throw new TooManyRequestsException("Vượt quá giới hạn gửi tin nhắn");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(content);
        message.setType(type);
        message = messageRepository.save(message);

        conversation.setLastMessage(message);
        conversation.setLastMessageAt(message.getCreatedAt());
        conversationRepository.save(conversation);

        eventPublisher.publishEvent(new MessageSentEvent(this, senderId, message.getId(), conversationId, senderId, null));

        trackMessageSent(senderId, conversationId);

        log.info("Điều phối tin nhắn thành công: {} trong cuộc trò chuyện {}", message.getId(), conversationId);
        return message;
    }

    @Transactional
    public Conversation orchestrateCreateConversation(Long creatorId,
                                                       List<Long> participantIds,
                                                       String name,
                                                       String initialMessage) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        Conversation conversation = new Conversation();
        conversation.setType(participantIds.size() > 1 ? ConversationType.GROUP : ConversationType.DIRECT);
        conversation.setName(name);

        Set<User> participants = new HashSet<>();
        participants.add(creator);

        for (Long participantId : participantIds) {
            User participant = userRepository.findById(participantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người tham gia"));
            if (!participant.getId().equals(creatorId)) {
                participants.add(participant);
            }
        }

        conversation.setParticipants(participants);
        conversation = conversationRepository.save(conversation);

        if (initialMessage != null && !initialMessage.isEmpty()) {
            orchestrateSendMessage(conversation.getId(), creatorId, initialMessage, MessageType.TEXT);
        }

        log.info("Điều phối cuộc trò chuyện thành công: {} bởi người dùng {}", conversation.getId(), creatorId);
        return conversation;
    }

    @Transactional
    public void orchestrateMarkAsRead(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc trò chuyện"));

        if (!conversation.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(userId))) {
            throw new ResourceNotFoundException("Không phải thành viên của cuộc trò chuyện này");
        }

        List<Message> unreadMessages = messageRepository
                .findByConversationIdWithSender(conversationId,
                        org.springframework.data.domain.PageRequest.of(0, 1000))
                .getContent();

        unreadMessages.stream()
                .filter(m -> !m.isRead() && !m.getSender().getId().equals(userId))
                .forEach(m -> {
                    m.setRead(true);
                    messageRepository.save(m);
                });

        cacheService.delete("conversation:unread:" + conversationId + ":" + userId);

        log.info("Đánh dấu tin nhắn đã đọc trong cuộc trò chuyện {} bởi người dùng {}", conversationId, userId);
    }

    private boolean isRateLimited(Long userId) {
        String key = "ratelimit:message:" + userId;
        Object count = cacheService.get(key);

        if (count == null) {
            cacheService.set(key, 1, java.time.Duration.ofSeconds(60));
            return false;
        }

        int currentCount = count instanceof Number ? ((Number) count).intValue() : 0;
        if (currentCount >= 30) {
            return true;
        }

        cacheService.increment(key);
        return false;
    }

    private void trackMessageSent(Long senderId, Long conversationId) {
        cacheService.increment("analytics:messages:total");
        cacheService.increment("analytics:user:messages:" + senderId);
    }
}
