package com.xinglin.chat.repository;

import com.xinglin.chat.entity.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {
    Optional<ChatConversation> findByConversationTypeAndMinUserIdAndMaxUserIdAndStatus(String conversationType, Long minUserId, Long maxUserId, String status);
    boolean existsByIdAndStatus(Long id, String status);
}
