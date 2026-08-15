package com.xinglin.chat.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class CreateGroupConversationRequest {
    @NotBlank(message = "群名称不能为空")
    @Size(max = 64, message = "群名称不能超过64字")
    private String title;
    private List<Long> memberIds = new ArrayList<>();

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public List<Long> getMemberIds() { return memberIds; }
    public void setMemberIds(List<Long> memberIds) { this.memberIds = memberIds; }
}
