package com.xinglin.chat.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_conversation_member", uniqueConstraints = {
        @UniqueConstraint(name = "uk_chat_member_conv_user", columnNames = {"conversation_id", "user_id"})
}, indexes = {
        @Index(name = "idx_chat_member_user_status", columnList = "user_id,status"),
        @Index(name = "idx_chat_member_conv_status", columnList = "conversation_id,status")
})
public class ChatConversationMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long conversationId;
    private Long userId;
    private String memberRole = "MEMBER";
    private Long lastReadSeq = 0L;
    private String status = "ACTIVE";
    private LocalDateTime joinedAt = LocalDateTime.now();
    private LocalDateTime leftAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getMemberRole() { return memberRole; }
    public void setMemberRole(String memberRole) { this.memberRole = memberRole; }
    public Long getLastReadSeq() { return lastReadSeq; }
    public void setLastReadSeq(Long lastReadSeq) { this.lastReadSeq = lastReadSeq; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
    public LocalDateTime getLeftAt() { return leftAt; }
    public void setLeftAt(LocalDateTime leftAt) { this.leftAt = leftAt; }
}
