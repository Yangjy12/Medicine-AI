package com.xinglin.ai.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AiMessageVO {
    private Long id;
    private Long conversationId;
    private String role;
    private String content;
    private List<CitationVO> citations = new ArrayList<>();
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<CitationVO> getCitations() { return citations; }
    public void setCitations(List<CitationVO> citations) { this.citations = citations; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
