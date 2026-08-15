package com.xinglin.chat.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class SendMessageRequest {
    @NotNull(message = "会话ID不能为空")
    private Long conversationId;
    private String clientMsgId;
    private String contentType = "TEXT";
    @Size(max = 2000, message = "消息内容不能超过2000字")
    private String content;
    @Size(max = 512, message = "媒体地址不能超过512字")
    private String mediaUrl;
    @Size(max = 256, message = "媒体对象Key不能超过256字")
    private String mediaObjectKey;

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public String getClientMsgId() { return clientMsgId; }
    public void setClientMsgId(String clientMsgId) { this.clientMsgId = clientMsgId; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    public String getMediaObjectKey() { return mediaObjectKey; }
    public void setMediaObjectKey(String mediaObjectKey) { this.mediaObjectKey = mediaObjectKey; }
}
