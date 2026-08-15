package com.xinglin.chat.repository;

import com.xinglin.chat.entity.ChatConversationMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ChatConversationMemberRepository extends JpaRepository<ChatConversationMember, Long> {
    boolean existsByConversationIdAndUserIdAndStatus(Long conversationId, Long userId, String status);
    Optional<ChatConversationMember> findByConversationIdAndUserId(Long conversationId, Long userId);
    Page<ChatConversationMember> findByUserIdAndStatusOrderByJoinedAtDesc(Long userId, String status, Pageable pageable);
    List<ChatConversationMember> findByConversationIdAndStatus(Long conversationId, String status);
    List<ChatConversationMember> findByConversationIdInAndUserIdAndStatus(Collection<Long> conversationIds, Long userId, String status);
}
