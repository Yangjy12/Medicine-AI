package com.xinglin.chat.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message", uniqueConstraints = {
        @UniqueConstraint(name = "uk_chat_msg_conv_seq", columnNames = {"conversation_id", "seq"}),
        @UniqueConstraint(name = "uk_chat_msg_conv_sender_client", columnNames = {"conversation_id", "sender_id", "client_msg_id"})
}, indexes = {
        @Index(name = "idx_chat_msg_conv_time", columnList = "conversation_id,sent_at"),
        @Index(name = "idx_chat_msg_sender_time", columnList = "sender_id,sent_at")
})
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long conversationId;
    private Long seq;
    private Long senderId;
    private String clientMsgId;
    private String contentType = "TEXT";
    @Column(length = 2000)
    private String content;
    private String mediaUrl;
    private String mediaObjectKey;
    private String status = "NORMAL";
    private LocalDateTime sentAt = LocalDateTime.now();
    private LocalDateTime recalledAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public Long getSeq() { return seq; }
    public void setSeq(Long seq) { this.seq = seq; }
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
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
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    public LocalDateTime getRecalledAt() { return recalledAt; }
    public void setRecalledAt(LocalDateTime recalledAt) { this.recalledAt = recalledAt; }
}
