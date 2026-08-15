package com.xinglin.ai.vo;

import java.time.LocalDateTime;

public class AiConversationVO {
    private Long id;
    private String title;
    private String lastQuestion;
    private String lastAnswerPreview;
    private Integer messageCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
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
