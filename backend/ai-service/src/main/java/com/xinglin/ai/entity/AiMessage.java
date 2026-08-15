package com.xinglin.ai.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_message", indexes = {
        @Index(name = "idx_ai_msg_conv_time", columnList = "conversation_id,created_at"),
        @Index(name = "idx_ai_msg_user_time", columnList = "user_id,created_at")
})
public class AiMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long conversationId;
    private Long userId;
    private String role;
    @Column(columnDefinition = "TEXT")
    private String content;
    @Column(columnDefinition = "TEXT")
    private String citationsJson;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCitationsJson() { return citationsJson; }
    public void setCitationsJson(String citationsJson) { this.citationsJson = citationsJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
