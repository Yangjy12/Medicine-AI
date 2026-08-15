package com.xinglin.ai.repository;

import com.xinglin.ai.entity.AiMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiMessageRepository extends JpaRepository<AiMessage, Long> {
    Page<AiMessage> findByConversationIdAndUserIdOrderByCreatedAtAsc(Long conversationId, Long userId, Pageable pageable);
}
