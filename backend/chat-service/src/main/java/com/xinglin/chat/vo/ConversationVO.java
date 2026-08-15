package com.xinglin.chat.vo;

import java.time.LocalDateTime;

public class ConversationVO {
    private Long id;
    private String conversationType;
    private String title;
    private String lastMessagePreview;
    private LocalDateTime lastMessageTime;
    private Long lastReadSeq;
    private Long unreadCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getConversationType() { return conversationType; }
    public void setConversationType(String conversationType) { this.conversationType = conversationType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getLastMessagePreview() { return lastMessagePreview; }
    public void setLastMessagePreview(String lastMessagePreview) { this.lastMessagePreview = lastMessagePreview; }
    public LocalDateTime getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(LocalDateTime lastMessageTime) { this.lastMessageTime = lastMessageTime; }
    public Long getLastReadSeq() { return lastReadSeq; }
    public void setLastReadSeq(Long lastReadSeq) { this.lastReadSeq = lastReadSeq; }
    public Long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(Long unreadCount) { this.unreadCount = unreadCount; }
}
