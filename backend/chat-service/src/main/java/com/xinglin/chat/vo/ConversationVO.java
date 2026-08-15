package com.xinglin.chat.vo;

import java.time.LocalDateTime;

public class ConversationVO {
    private Long id;
    private String conversationType;
    private String title;
    private String avatar;
    private Long targetUserId;
    private String targetUserName;
    private String targetUserAvatar;
    private String lastMessagePreview;
    private LocalDateTime lastMessageTime;
    private Long lastReadSeq;
    private Long unreadCount;
    private String currentUserRole;
    private Long memberCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getConversationType() { return conversationType; }
    public void setConversationType(String conversationType) { this.conversationType = conversationType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }
    public String getTargetUserName() { return targetUserName; }
    public void setTargetUserName(String targetUserName) { this.targetUserName = targetUserName; }
    public String getTargetUserAvatar() { return targetUserAvatar; }
    public void setTargetUserAvatar(String targetUserAvatar) { this.targetUserAvatar = targetUserAvatar; }
    public String getLastMessagePreview() { return lastMessagePreview; }
    public void setLastMessagePreview(String lastMessagePreview) { this.lastMessagePreview = lastMessagePreview; }
    public LocalDateTime getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(LocalDateTime lastMessageTime) { this.lastMessageTime = lastMessageTime; }
    public Long getLastReadSeq() { return lastReadSeq; }
    public void setLastReadSeq(Long lastReadSeq) { this.lastReadSeq = lastReadSeq; }
    public Long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(Long unreadCount) { this.unreadCount = unreadCount; }
    public String getCurrentUserRole() { return currentUserRole; }
    public void setCurrentUserRole(String currentUserRole) { this.currentUserRole = currentUserRole; }
    public Long getMemberCount() { return memberCount; }
    public void setMemberCount(Long memberCount) { this.memberCount = memberCount; }
}
