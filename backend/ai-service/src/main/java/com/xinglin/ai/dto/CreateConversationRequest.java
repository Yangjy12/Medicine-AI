package com.xinglin.ai.dto;

import javax.validation.constraints.Size;

public class CreateConversationRequest {
    @Size(max = 64, message = "会话标题不能超过64字")
    private String title;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
