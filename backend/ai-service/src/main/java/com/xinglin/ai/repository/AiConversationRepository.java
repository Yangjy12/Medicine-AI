package com.xinglin.ai.repository;

import com.xinglin.ai.entity.AiConversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {
    Page<AiConversation> findByUserIdAndStatusOrderByUpdatedAtDesc(Long userId, String status, Pageable pageable);
    Optional<AiConversation> findByIdAndUserIdAndStatus(Long id, Long userId, String status);
}
