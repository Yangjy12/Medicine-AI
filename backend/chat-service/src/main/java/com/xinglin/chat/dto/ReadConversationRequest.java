package com.xinglin.chat.dto;

import javax.validation.constraints.NotNull;

public class ReadConversationRequest {
    @NotNull(message = "已读序号不能为空")
    private Long lastReadSeq;

    public Long getLastReadSeq() { return lastReadSeq; }
    public void setLastReadSeq(Long lastReadSeq) { this.lastReadSeq = lastReadSeq; }
}
