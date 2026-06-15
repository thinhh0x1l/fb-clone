package com.fb.message.service.impl;

import com.fb.message.model.Conversation;
import com.fb.message.model.Message;
import com.fb.message.repository.ConversationRepository;
import com.fb.message.repository.MessageRepository;
import com.fb.message.dto.ConversationResponse;
import com.fb.message.dto.CreateConversationRequest;
import com.fb.message.dto.SendMessageRequest;
import com.fb.message.dto.MessageResponse;
import com.fb.message.mapper.MessageMapper;
import com.fb.message.service.MessageService;
import com.fb.auth.model.User;
import com.fb.auth.repository.UserRepository;
import com.fb.common.exception.ResourceNotFoundException;
import com.fb.common.exception.UnauthorizedException;
import com.fb.common.response.PagedResponse;
import com.fb.common.util.MessageCryptoUtil;
import com.fb.event.MessageSentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageServiceImpl implements MessageService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;
    private final MessageCryptoUtil cryptoUtil;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ConversationResponse createConversation(Long userId, CreateConversationRequest request) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        Conversation conversation = new Conversation();
        conversation.setType(request.getParticipantIds().size() > 1 ?
                com.fb.common.enums.ConversationType.GROUP : com.fb.common.enums.ConversationType.DIRECT);
        conversation.setName(request.getName());

        java.util.Set<User> participants = new HashSet<>();
        participants.add(creator);

        for (Long participantId : request.getParticipantIds()) {
            User participant = userRepository.findById(participantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người tham gia"));
            if (!participant.getId().equals(userId)) {
                participants.add(participant);
            }
        }

        conversation.setParticipants(participants);
        conversation = conversationRepository.save(conversation);

        return toConversationResponse(conversation, userId);
    }

    @Override
    public ConversationResponse getConversation(Long userId, Long conversationId) {
        Conversation conversation = conversationRepository.findByIdWithParticipants(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hội thoại"));

        if (!conversation.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(userId))) {
            throw new UnauthorizedException("Bạn không phải là thành viên của hội thoại này");
        }

        return toConversationResponse(conversation, userId);
    }

    @Override
    public List<ConversationResponse> getUserConversations(Long userId) {
        List<Conversation> conversations = conversationRepository.findByParticipantIdWithParticipants(userId);
        return conversations.stream()
                .map(c -> toConversationResponse(c, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(Long userId, Long conversationId, SendMessageRequest request) {
        Conversation conversation = conversationRepository.findByIdWithParticipants(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hội thoại"));

        if (!conversation.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(userId))) {
            throw new UnauthorizedException("Bạn không phải là thành viên của hội thoại này");
        }

        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        String encryptedContent = cryptoUtil.encrypt(request.getContent());

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setEncryptedContent(encryptedContent);
        message.setType(request.getType());
        message = messageRepository.save(message);

        conversation.setLastMessage(message);
        conversation.setLastMessageAt(message.getCreatedAt());
        conversationRepository.save(conversation);

        eventPublisher.publishEvent(new MessageSentEvent(this, userId, message.getId(), conversationId, userId, null));

        log.info("Gửi tin nhắn thành công: {} trong hội thoại {}", message.getId(), conversationId);
        return messageMapper.toMessageResponse(message);
    }

    @Override
    public PagedResponse<MessageResponse> getConversationMessages(Long userId, Long conversationId, int page, int size) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hội thoại"));

        if (!conversation.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(userId))) {
            throw new UnauthorizedException("Bạn không phải là thành viên của hội thoại này");
        }

        Page<Message> messages = messageRepository.findByConversationIdWithSender(
                conversationId, PageRequest.of(page, size));

        List<MessageResponse> response = messages.map(message -> {
            MessageResponse msgResponse = messageMapper.toMessageResponse(message);
            String decryptedContent = cryptoUtil.decrypt(message.getEncryptedContent());
            msgResponse.setContent(decryptedContent);
            return msgResponse;
        }).getContent();

        return PagedResponse.of(response, page, size, messages.getTotalElements());
    }

    @Override
    public ConversationResponse getOrCreateDirectConversation(Long userId, Long otherUserId) {
        java.util.Optional<Conversation> existing = conversationRepository.findDirectConversation(userId, otherUserId);
        if (existing.isPresent()) {
            return toConversationResponse(existing.get(), userId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        User other = userRepository.findById(otherUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        Conversation conversation = new Conversation();
        conversation.setType(com.fb.common.enums.ConversationType.DIRECT);

        java.util.Set<User> participants = new HashSet<>();
        participants.add(user);
        participants.add(other);
        conversation.setParticipants(participants);

        conversation = conversationRepository.save(conversation);
        return toConversationResponse(conversation, userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hội thoại"));

        if (!conversation.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(userId))) {
            throw new UnauthorizedException("Bạn không phải là thành viên của hội thoại này");
        }

        messageRepository.markAsRead(conversationId, userId);
        log.info("Đánh dấu tin nhắn đã đọc trong hội thoại {} bởi {}", conversationId, userId);
    }

    @Override
    public long getUnreadCount(Long userId) {
        List<Conversation> conversations = conversationRepository.findByParticipantIdWithParticipants(userId);
        long totalUnread = 0;
        for (Conversation conv : conversations) {
            totalUnread += messageRepository.countUnreadByConversationId(conv.getId(), userId);
        }
        return totalUnread;
    }

    @Override
    @Transactional
    public void deleteConversation(Long userId, Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hội thoại"));

        if (!conversation.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(userId))) {
            throw new UnauthorizedException("Bạn không phải là thành viên của hội thoại này");
        }

        conversation.softDelete();
        conversationRepository.save(conversation);
        log.info("Xóa cuộc trò chuyện thành công: {} bởi {}", conversationId, userId);
    }

    private ConversationResponse toConversationResponse(Conversation conversation, Long userId) {
        List<ConversationResponse.ParticipantInfo> participantInfos = conversation.getParticipants().stream()
                .map(p -> ConversationResponse.ParticipantInfo.builder()
                        .id(p.getId())
                        .username(p.getUsername())
                        .displayName(p.getDisplayName())
                        .avatar(p.getAvatar())
                        .build())
                .collect(Collectors.toList());

        int unreadCount = (int) messageRepository.countUnreadByConversationId(conversation.getId(), userId);

        return ConversationResponse.builder()
                .id(conversation.getId())
                .name(conversation.getName())
                .type(conversation.getType())
                .participants(participantInfos)
                .lastMessageAt(conversation.getLastMessageAt())
                .unreadCount(unreadCount)
                .createdAt(conversation.getCreatedAt())
                .build();
    }
}
