package com.xinglin.ai.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_conversation", indexes = {
        @Index(name = "idx_ai_conv_user_status_time", columnList = "user_id,status,updated_at")
})
public class AiConversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String title;
    private String status = "ACTIVE";
    private String lastQuestion;
    @Column(length = 1000)
    private String lastAnswerPreview;
    private Integer messageCount = 0;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLastQuestion() { return lastQuestion; }
    public void setLastQuestion(String lastQuestion) { this.lastQuestion = lastQuestion; }
    public String getLastAnswerPreview() { return lastAnswerPreview; }
    public void setLastAnswerPreview(String lastAnswerPreview) { this.lastAnswerPreview = lastAnswerPreview; }
    public Integer getMessageCount() { return messageCount; }
    public void setMessageCount(Integer messageCount) { this.messageCount = messageCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
