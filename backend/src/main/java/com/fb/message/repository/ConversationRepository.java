package com.fb.message.repository;

import com.fb.message.model.Conversation;
import com.fb.message.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho hội thoại
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /**
     * Lấy danh sách hội thoại của người dùng (đã optimize N+1)
     */
    @Query("SELECT DISTINCT c FROM Conversation c JOIN FETCH c.participants WHERE :participantId IN (SELECT u.id FROM c.participants u) AND c.deletedAt IS NULL ORDER BY c.lastMessageAt DESC")
    List<Conversation> findByParticipantIdWithParticipants(@Param("participantId") Long participantId);

    /**
     * Lấy hội thoại trực tiếp giữa 2 người
     */
    @Query("SELECT c FROM Conversation c WHERE c.type = 'DIRECT' AND EXISTS (SELECT 1 FROM c.participants p WHERE p.id = :userId1) AND EXISTS (SELECT 1 FROM c.participants p WHERE p.id = :userId2) AND c.deletedAt IS NULL")
    Optional<Conversation> findDirectConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * Lấy hội thoại theo ID (đã optimize N+1)
     */
    @Query("SELECT c FROM Conversation c JOIN FETCH c.participants WHERE c.id = :id AND c.deletedAt IS NULL")
    Optional<Conversation> findByIdWithParticipants(@Param("id") Long id);
}
