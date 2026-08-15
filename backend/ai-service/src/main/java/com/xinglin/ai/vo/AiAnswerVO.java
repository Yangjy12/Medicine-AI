package com.xinglin.ai.vo;

import java.util.ArrayList;
import java.util.List;

public class AiAnswerVO {
    private Long conversationId;
    private Long userMessageId;
    private Long assistantMessageId;
    private String answer;
    private List<CitationVO> citations = new ArrayList<>();

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public Long getUserMessageId() { return userMessageId; }
    public void setUserMessageId(Long userMessageId) { this.userMessageId = userMessageId; }
    public Long getAssistantMessageId() { return assistantMessageId; }
    public void setAssistantMessageId(Long assistantMessageId) { this.assistantMessageId = assistantMessageId; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public List<CitationVO> getCitations() { return citations; }
    public void setCitations(List<CitationVO> citations) { this.citations = citations; }
}
