package com.xinglin.chat.vo;

import java.time.LocalDateTime;

public class MemberVO {
    private Long userId;
    private String nickname;
    private String avatar;
    private String memberRole;
    private Long lastReadSeq;
    private String status;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
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
