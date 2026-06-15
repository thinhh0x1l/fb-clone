package com.fb.message.repository;

import com.fb.message.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository cho tin nhắn
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Lấy tin nhắn theo hội thoại (đã optimize N+1)
     */
    @Query("SELECT m FROM Message m JOIN FETCH m.sender WHERE m.conversation.id = :conversationId AND m.deletedAt IS NULL ORDER BY m.createdAt DESC")
    Page<Message> findByConversationIdWithSender(@Param("conversationId") Long conversationId, Pageable pageable);

    /**
     * Đếm tin nhắn chưa đọc
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = :conversationId AND m.sender.id != :userId AND m.read = false AND m.deletedAt IS NULL")
    long countUnreadByConversationId(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    /**
     * Đánh dấu đã đọc
     */
    @Query("UPDATE Message m SET m.read = true WHERE m.conversation.id = :conversationId AND m.sender.id != :userId AND m.read = false")
    int markAsRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    /**
     * Lấy tin nhắn cuối cùng của hội thoại
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.deletedAt IS NULL ORDER BY m.createdAt DESC LIMIT 1")
    Message findLastMessageByConversationId(@Param("conversationId") Long conversationId);
}
