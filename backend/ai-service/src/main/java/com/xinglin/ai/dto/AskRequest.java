package com.xinglin.ai.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class AskRequest {
    private Long conversationId;
    @NotBlank(message = "问题不能为空")
    @Size(max = 1000, message = "问题不能超过1000字")
    private String question;

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
}
