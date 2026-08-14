package com.xinglin.forum.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "forum_like", uniqueConstraints = {
        @UniqueConstraint(name = "uk_forum_like_user_target", columnNames = {"user_id", "target_type", "target_id"})
}, indexes = {
        @Index(name = "idx_forum_like_target", columnList = "target_type,target_id")
})
public class ForumLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String targetType;
    private Long targetId;
    private LocalDateTime createdAt = LocalDateTime.now();

    public ForumLike() {
    }

    public ForumLike(Long userId, String targetType, Long targetId) {
        this.userId = userId;
        this.targetType = targetType;
        this.targetId = targetId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
