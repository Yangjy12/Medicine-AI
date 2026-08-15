package com.xinglin.ai.repository;

import com.xinglin.ai.entity.AiConversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {
    Page<AiConversation> findByUserIdAndStatusOrderByUpdatedAtDesc(Long userId, String status, Pageable pageable);
    Optional<AiConversation> findByIdAndUserIdAndStatus(Long id, Long userId, String status);

    @Modifying
    @Query("update AiConversation c set c.lastQuestion = :lastQuestion, " +
            "c.messageCount = coalesce(c.messageCount, 0) + 1, c.updatedAt = :updatedAt " +
            "where c.id = :id and c.userId = :userId and c.status = :status")
    int recordUserMessage(@Param("id") Long id,
                          @Param("userId") Long userId,
                          @Param("status") String status,
                          @Param("lastQuestion") String lastQuestion,
                          @Param("updatedAt") LocalDateTime updatedAt);

    @Modifying
    @Query("update AiConversation c set c.lastAnswerPreview = :lastAnswerPreview, " +
            "c.messageCount = coalesce(c.messageCount, 0) + 1, c.updatedAt = :updatedAt " +
            "where c.id = :id and c.userId = :userId and c.status = :status")
    int recordAssistantMessage(@Param("id") Long id,
                               @Param("userId") Long userId,
                               @Param("status") String status,
                               @Param("lastAnswerPreview") String lastAnswerPreview,
                               @Param("updatedAt") LocalDateTime updatedAt);
}
