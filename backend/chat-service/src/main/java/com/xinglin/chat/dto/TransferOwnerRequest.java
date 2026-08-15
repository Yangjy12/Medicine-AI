package com.xinglin.chat.dto;

import javax.validation.constraints.NotNull;

public class TransferOwnerRequest {
    @NotNull(message = "请选择新群主")
    private Long targetUserId;

    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }
}
