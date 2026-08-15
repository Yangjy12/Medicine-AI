package com.xinglin.chat.dto;

import javax.validation.constraints.NotNull;

public class CreatePrivateConversationRequest {
    @NotNull(message = "请选择聊天对象")
    private Long targetUserId;

    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }
}
