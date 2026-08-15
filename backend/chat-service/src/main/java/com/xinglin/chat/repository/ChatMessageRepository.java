package com.xinglin.chat.repository;

import com.xinglin.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findByConversationIdAndStatusOrderBySeqDesc(Long conversationId, String status, Pageable pageable);
    Page<ChatMessage> findByConversationIdAndSeqGreaterThanAndStatusOrderBySeqAsc(Long conversationId, Long seq, String status, Pageable pageable);
    Page<ChatMessage> findByConversationIdAndStatusInOrderBySeqDesc(Long conversationId, Collection<String> statuses, Pageable pageable);
    Page<ChatMessage> findByConversationIdAndSeqGreaterThanAndStatusInOrderBySeqAsc(Long conversationId, Long seq, Collection<String> statuses, Pageable pageable);
    Optional<ChatMessage> findTopByConversationIdAndStatusOrderBySeqDesc(Long conversationId, String status);
    Optional<ChatMessage> findBySenderIdAndClientMsgId(Long senderId, String clientMsgId);
}
