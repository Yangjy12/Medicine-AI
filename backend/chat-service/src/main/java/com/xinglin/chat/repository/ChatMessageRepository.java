package com.xinglin.chat.repository;

import com.xinglin.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findByConversationIdAndStatusOrderBySeqDesc(Long conversationId, String status, Pageable pageable);
    Page<ChatMessage> findByConversationIdAndSeqGreaterThanAndStatusOrderBySeqAsc(Long conversationId, Long seq, String status, Pageable pageable);
    Optional<ChatMessage> findBySenderIdAndClientMsgId(Long senderId, String clientMsgId);
}
